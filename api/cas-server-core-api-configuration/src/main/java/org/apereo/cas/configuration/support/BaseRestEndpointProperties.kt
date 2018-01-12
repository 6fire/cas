package org.apereo.cas.configuration.support

import java.io.Serializable

/**
 * This is [BaseRestEndpointProperties].
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
open class BaseRestEndpointProperties : Serializable {

    /**
     * The endpoint URL to contact and retrieve attributes.
     */
    @RequiredProperty
    var url: String? = null

    /**
     * If REST endpoint is protected via basic authentication,
     * specify the username for authentication.
     */
    var basicAuthUsername: String? = null
    /**
     * If REST endpoint is protected via basic authentication,
     * specify the password for authentication.
     */
    var basicAuthPassword: String? = null

    companion object {
        private const val serialVersionUID = 2687020856160473089L
    }
}
