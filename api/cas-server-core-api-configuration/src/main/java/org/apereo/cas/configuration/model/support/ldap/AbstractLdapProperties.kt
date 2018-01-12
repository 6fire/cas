package org.apereo.cas.configuration.model.support.ldap

import org.apache.commons.lang3.NotImplementedException
import org.springframework.boot.context.properties.DeprecatedConfigurationProperty
import org.springframework.boot.context.properties.NestedConfigurationProperty

import java.io.Serializable

/**
 * This is [AbstractLdapProperties].
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
abstract class AbstractLdapProperties : Serializable {

    /**
     * Path of the trust certificates to use for the SSL connection.
     * Ignores keystore-related settings when activated and used.
     */
    var trustCertificates: String? = null

    /**
     * Path to the keystore used for SSL connections.
     * Typically contains SSL certificates for the LDAP server.
     */
    var keystore: String? = null
    /**
     * Keystore password.
     */
    var keystorePassword: String? = null
    /**
     * The type of keystore. `PKCS12` or `JKS`.
     * If left blank, defaults to the default keystore type indicated
     * by the underlying Java platform.
     */
    var keystoreType: String? = null

    /**
     * Minimum LDAP connection pool size.
     * Size the pool should be initialized to and pruned to
     */
    var minPoolSize = 3

    /**
     * Maximum LDAP connection pool size which the pool can use to grow.
     */
    var maxPoolSize = 10

    /**
     * You may receive unexpected LDAP failures, when CAS is configured to authenticate
     * using DIRECT or AUTHENTICATED types and LDAP is locked down to not allow anonymous binds/searches.
     * Every second attempt with a given LDAP connection from the pool would fail if it was on
     * the same connection as a failed login attempt, and the regular connection validator would
     * similarly fail. When a connection is returned back to a pool,
     * it still may contain the principal and credentials from the previous attempt.
     * Before the next bind attempt using that connection, the validator tries to
     * validate the connection again but fails because it’s no longer trying with the
     * configured bind credentials but with whatever user DN was used in the previous step.
     * Given the validation failure, the connection is closed and CAS would deny access by default. Passivators attempt to reconnect
     * to LDAP with the configured bind credentials, effectively resetting the connection
     * to what it should be after each bind request.
     */
    var poolPassivator = "BIND"

    /**
     * Whether connections should be validated when loaned out from the pool.
     */
    var isValidateOnCheckout = true
    /**
     * Whether connections should be validated periodically when the pool is idle.
     */
    var isValidatePeriodically = true

    /**
     * Period at which validation operations may time out.
     */
    var validateTimeout = "PT5S"
    /**
     * Period at which pool should be validated.
     */
    var validatePeriod = "PT5M"

    /**
     * Attempt to populate the connection pool early on startup
     * and fail quickly if something goes wrong.
     */
    var isFailFast = true

    /**
     * Removes connections from the pool based on how long they have been idle in the available queue.
     * Prunes connections that have been idle for more than the indicated amount.
     */
    var idleTime = "PT10M"
    /**
     * Removes connections from the pool based on how long they have been idle in the available queue.
     * Run the pruning process at the indicated interval.
     */
    var prunePeriod = "PT2H"
    /**
     * The length of time the pool will block.
     * By default the pool will block indefinitely and there is no guarantee that
     * waiting threads will be serviced in the order in which they made their request.
     * This option should be used with a blocking connection pool when you need to control the exact
     * number of connections that can be created
     */
    var blockWaitTime = "PT3S"

    /**
     * If multiple URLs are provided as the ldapURL this describes how each URL will be processed.
     *
     *  * `DEFAULT` The default JNDI provider behavior will be used.
     *  * `ACTIVE_PASSIVE` First LDAP will be used for every request unless it fails and then the next shall be used.
     *  * `ROUND_ROBIN` For each new connection the next url in the list will be used.
     *  * `RANDOM` For each new connection a random LDAP url will be selected.
     *  * `DNS_SRV` LDAP urls based on DNS SRV records of the configured/given LDAP url will be used.
     *
     */
    var connectionStrategy: String? = null

    /**
     * The LDAP url to the server. More than one may be specified, separated by space and/or comma.
     */
    var ldapUrl = "ldap://localhost:389"
    /**
     * If the LDAP connection should be used with SSL enabled.
     */
    var isUseSsl = true
    /**
     * Whether TLS should be used and enabled when establishing the connection.
     */
    var isUseStartTls: Boolean = false
    /**
     * Sets the maximum amount of time that connects will block.
     */
    var connectTimeout = "PT5S"
    /**
     * Duration of time to wait for responses.
     */
    var responseTimeout = "PT5S"

    /**
     * LDAP operations are delegated to what we call a provider. This allows developers and deployers to change the underlying library
     * that provides the LDAP implementation without modifying any code. By default the JNDI provider is used, though
     * it may be swapped out for `org.ldaptive.provider.unboundid.UnboundIDProvider`.
     */
    var providerClass: String? = null
    /**
     * Whether search/query results are allowed to match on multiple DNs,
     * or whether a single unique DN is expected for the result.
     */
    var isAllowMultipleDns: Boolean = false

    /**
     * The bind DN to use when connecting to LDAP.
     * LDAP connection configuration injected into the LDAP connection pool can be initialized with the following parameters:
     *
     *  * `bindDn/bindCredential` provided - Use the provided credentials to bind when initializing connections.
     *  * `bindDn/bindCredential`  set to `*` - Use a fast-bind strategy to initialize the pool.
     *  * `bindDn/bindCredential`  set to blank - Skip connection initializing; perform operations anonymously.
     *  * SASL mechanism provided - Use the given SASL mechanism to bind when initializing connections.
     *
     */
    var bindDn: String? = null
    /**
     * The bind credential to use when connecting to LDAP.
     */
    var bindCredential: String? = null

    /**
     * The SASL realm.
     */
    var saslRealm: String? = null
    /**
     * The SASL mechanism.
     */
    var saslMechanism: String? = null
    /**
     * SASL authorization id.
     */
    var saslAuthorizationId: String? = null
    /**
     * SASL security strength.
     */
    var saslSecurityStrength: String? = null
    /**
     * SASL mutual auth is enabled?
     */
    var saslMutualAuth: Boolean? = null
    /**
     * SASL quality of protected.
     */
    var saslQualityOfProtection: String? = null

    /**
     * LDAP connection validator settings.
     */
    @NestedConfigurationProperty
    var validator = LdapValidatorProperties()

    /**
     * Name of the authentication handler.
     */
    var name: String? = null
    
    /**
     * The ldap type used to handle specific ops.
     */
    enum class LdapType {
        /**
         * Generic ldap type (OpenLDAP, 389ds, etc).
         */
        GENERIC,
        /**
         * Active directory.
         */
        AD,
        /**
         * FreeIPA directory.
         */
        FreeIPA,
        /**
         * EDirectory.
         */
        EDirectory
    }

    /**
     * The ldap connection pool passivator.
     */
    enum class LdapConnectionPoolPassivator {
        /**
         * No passivator.
         */
        NONE,
        /**
         * Close passivator.
         */
        CLOSE,
        /**
         * Bind passivator.
         */
        BIND
    }

    /**
     * Describe ldap connection strategies.
     */
    enum class LdapConnectionStrategy {
        /**
         * Default JNDI.
         */
        DEFAULT,
        /**
         * First ldap used until it fails.
         */
        ACTIVE_PASSIVE,
        /**
         * Navigate the ldap url list for new connections and circle back.
         */
        ROUND_ROBIN,
        /**
         * Randomly pick a url.
         */
        RANDOM,
        /**
         * ldap urls based on DNS SRV records.
         */
        DNS_SRV
    }

    companion object {
        private const val serialVersionUID = 2682743362616979324L
    }
}
