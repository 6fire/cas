package org.apereo.cas.configuration.support

/**
 * This is [RestEndpointProperties].
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
open class RestEndpointProperties : BaseRestEndpointProperties() {

    /**
     * HTTP method to use when contacting the rest endpoint.
     * Examples include `GET, POST`, etc.
     */
    @RequiredProperty
    var method: String? = null

    companion object {
        private val serialVersionUID = 2687020856160473089L
    }
}
