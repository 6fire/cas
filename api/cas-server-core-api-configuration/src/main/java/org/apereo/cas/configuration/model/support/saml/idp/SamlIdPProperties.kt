package org.apereo.cas.configuration.model.support.saml.idp

import org.apereo.cas.configuration.model.support.saml.idp.metadata.SamlIdPMetadataProperties
import org.apereo.cas.configuration.support.RequiredProperty
import org.apereo.cas.configuration.support.RequiresModule
import org.springframework.boot.context.properties.NestedConfigurationProperty

import java.io.Serializable
import java.util.ArrayList
import java.util.Arrays
import java.util.HashMap

/**
 * This is [SamlIdPProperties].
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-saml-idp")
class SamlIdPProperties : Serializable {

    /**
     * Indicates whether attribute query profile is enabled.
     * Enabling this setting would allow CAS to record SAML
     * responses and have them be made available later for attribute lookups.
     */
    var isAttributeQueryProfileEnabled: Boolean = false

    /**
     * The SAML entity id for the deployment.
     */
    @RequiredProperty
    var entityId = "https://cas.example.org/idp"

    /**
     * The scope used in generation of metadata.
     */
    @RequiredProperty
    var scope = "example.org"

    /**
     * A mapping of authentication context class refs.
     * This is where specific authentication context classes
     * are references and mapped one ones that CAS may support
     * mainly for MFA purposes.
     *
     *
     * Example might be `urn:oasis:names:tc:SAML:2.0:ac:classes:SomeClassName->mfa-duo`.
     */
    var authenticationContextClassMappings: List<String>? = null

    /**
     * Settings related to SAML2 responses.
     */
    var response = Response()

    /**
     * SAML2 metadata related settings.
     */
    @NestedConfigurationProperty
    var metadata = SamlIdPMetadataProperties()

    /**
     * SAML2 logout related settings.
     */
    var logout = Logout()

    /**
     * Settings related to algorithms used for signing, etc.
     */
    var algs = Algorithms()

    class Response : Serializable {

        /**
         * Indicate the encoding type of the credential used when rendering the saml response.
         */
        var credentialType = SignatureCredentialTypes.X509

        /**
         * Time unit in seconds used to skew authentication dates such
         * as valid-from and valid-until elements.
         */
        var skewAllowance = 5
        /**
         * Whether error responses should be signed.
         */
        var isSignError: Boolean = false
        /**
         * The default authentication context class to include in the response
         * if none is specified via the service.
         */
        var defaultAuthenticationContextClass = "urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport"
        /**
         * Indicates the default name-format for all attributes
         * in case the individual attribute is not individually mapped.
         */
        var defaultAttributeNameFormat = "uri"
        /**
         * When creating attribute definitions, will ensure
         * the attribute's friendly name is set to the actual attribute name
         * in the event that the service provider wishes to use the friendly name.
         */
        var isUseAttributeFriendlyName = true
        /**
         * Each individual attribute can be mapped to a particular name-format.
         * Example: `attributeName->basic|uri|unspecified|custom-format-etc,...`.
         */
        var attributeNameFormats: List<String> = ArrayList()

        /**
         * Indicate the type of encoding used when rendering the
         * saml response and its signature blog.
         */
        enum class SignatureCredentialTypes {
            /**
             * DER-Encoded format.
             */
            BASIC,
            /**
             * PEM-encoded X509 format.
             */
            X509
        }

        /**
         * Configure attribute name formats and build a map.
         *
         * @return the map
         */
        fun configureAttributeNameFormats(): Map<String, String> {
            if (this.attributeNameFormats.isEmpty()) {
                return HashMap(0)
            }
            val nameFormats = HashMap<String, String>()
            this.attributeNameFormats.forEach { value ->
                Arrays.stream(value.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()).forEach { format ->
                    val values = format.split("->".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (values.size == 2) {
                        nameFormats.put(values[0], values[1])
                    }
                }
            }
            return nameFormats
        }

        companion object {
            private const val serialVersionUID = 7200477683583467619L
        }
    }

    class Logout : Serializable {

        /**
         * Whether SLO logout requests are required to be signed.
         */
        var isForceSignedLogoutRequests = true
        /**
         * Whether SAML SLO is enabled and processed.
         */
        var isSingleLogoutCallbacksDisabled: Boolean = false

        companion object {
            private const val serialVersionUID = -4608824149569614549L
        }
    }

    class Algorithms : Serializable {
        /**
         * The Override data encryption algorithms.
         */
        var overrideDataEncryptionAlgorithms: List<*>? = null
        /**
         * The Override key encryption algorithms.
         */
        var overrideKeyEncryptionAlgorithms: List<*>? = null
        /**
         * The Override black listed encryption algorithms.
         */
        var overrideBlackListedEncryptionAlgorithms: List<*>? = null
        /**
         * The Override white listed algorithms.
         */
        var overrideWhiteListedAlgorithms: List<*>? = null
        /**
         * The Override signature reference digest methods.
         */
        var overrideSignatureReferenceDigestMethods: List<*>? = null
        /**
         * The Override signature algorithms.
         */
        var overrideSignatureAlgorithms: List<*>? = null
        /**
         * The Override black listed signature signing algorithms.
         */
        var overrideBlackListedSignatureSigningAlgorithms: List<*>? = null
        /**
         * The Override white listed signature signing algorithms.
         */
        var overrideWhiteListedSignatureSigningAlgorithms: List<*>? = null
        /**
         * The Override signature canonicalization algorithm.
         */
        var overrideSignatureCanonicalizationAlgorithm: String? = null

        companion object {
            private const val serialVersionUID = 6547093517788229284L
        }
    }

    companion object {
        private const val serialVersionUID = -5848075783676789852L
    }
}
