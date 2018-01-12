package org.apereo.cas.configuration.support

import org.springframework.core.io.Resource

import java.io.Serializable

/**
 * This is [SpringResourceProperties].
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
open class SpringResourceProperties : Serializable {
    /**
     * The location of service definitions. Resources can be URLS, or
     * files found either on the classpath or outside somewhere
     * in the file system.
     */
    @RequiredProperty
    var location: Resource? = null

    companion object {
        private const val serialVersionUID = 4142130961445546358L
    }
}
