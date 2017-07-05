package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.DefaultAuthenticationResult;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.support.oauth.OAuthProperties;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.authenticator.OAuth20CasAuthenticationBuilder;
import org.apereo.cas.support.oauth.profile.OAuthUserProfile;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.profile.ProfileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * This is {@link AccessTokenPasswordGrantRequestExtractor}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class AccessTokenPasswordGrantRequestExtractor extends BaseAccessTokenGrantRequestExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccessTokenPasswordGrantRequestExtractor.class);

    private final OAuth20CasAuthenticationBuilder authenticationBuilder;

    public AccessTokenPasswordGrantRequestExtractor(final ServicesManager servicesManager,
                                                    final TicketRegistry ticketRegistry,
                                                    final HttpServletRequest request,
                                                    final HttpServletResponse response,
                                                    final OAuth20CasAuthenticationBuilder authenticationBuilder,
                                                    final CentralAuthenticationService centralAuthenticationService,
                                                    final OAuthProperties oAuthProperties) {
        super(servicesManager, ticketRegistry, request, response, centralAuthenticationService, oAuthProperties);
        this.authenticationBuilder = authenticationBuilder;
    }

    @Override
    public AccessTokenRequestDataHolder extract() {
        final String clientId = request.getParameter(OAuth20Constants.CLIENT_ID);
        LOGGER.debug("Locating OAuth registered service by client id [{}]", clientId);

        final OAuthRegisteredService registeredService = OAuth20Utils.getRegisteredOAuthService(this.servicesManager, clientId);
        LOGGER.debug("Located OAuth registered service [{}]", registeredService);

        final J2EContext context = WebUtils.getPac4jJ2EContext(request, response);
        final ProfileManager manager = WebUtils.getPac4jProfileManager(request, response);
        final Optional<OAuthUserProfile> profile = manager.get(true);
        if (!profile.isPresent()) {
            throw new UnauthorizedServiceException("OAuth user profile cannot be determined");
        }
        LOGGER.debug("Creating matching service request based on [{}]", registeredService);
        final boolean requireServiceHeader = oAuthProperties.getGrants().getResourceOwner().isRequireServiceHeader();
        if (requireServiceHeader) {
            LOGGER.debug("Using request headers to identify and build the target service url");
        }
        final Service service = this.authenticationBuilder.buildService(registeredService, context, requireServiceHeader);

        LOGGER.debug("Authenticating the OAuth request indicated by [{}]", service);
        final Authentication authentication = this.authenticationBuilder.build(profile.get(), registeredService, context, service);
        RegisteredServiceAccessStrategyUtils.ensurePrincipalAccessIsAllowedForService(service, registeredService, authentication);
        final boolean generateRefreshToken = isAllowedToGenerateRefreshToken(registeredService);

        final AuthenticationResult result = new DefaultAuthenticationResult(authentication, requireServiceHeader ? service : null);
        final TicketGrantingTicket ticketGrantingTicket = this.centralAuthenticationService.createTicketGrantingTicket(result);
        return new AccessTokenRequestDataHolder(service, authentication, registeredService, ticketGrantingTicket, generateRefreshToken);
    }
    
    /**
     * Is allowed to generate refresh token ?
     *
     * @param registeredService the registered service
     * @return the boolean
     */
    protected boolean isAllowedToGenerateRefreshToken(final OAuthRegisteredService registeredService) {
        return registeredService != null && registeredService.isGenerateRefreshToken();
    }

    @Override
    public boolean supports(final HttpServletRequest context) {
        final String grantType = context.getParameter(OAuth20Constants.GRANT_TYPE);
        return OAuth20Utils.isGrantType(grantType, OAuth20GrantTypes.PASSWORD);
    }
}
