package org.apereo.cas.configuration.model.support.hazelcast

import org.apereo.cas.configuration.support.RequiresModule
import org.springframework.boot.context.properties.NestedConfigurationProperty

import java.io.Serializable

/**
 * This is [BaseHazelcastProperties].
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-hazelcast-core")
open class BaseHazelcastProperties : Serializable {

    /**
     * Hazelcast cluster settings if CAS is able to auto-create caches.
     */
    @NestedConfigurationProperty
    var cluster = HazelcastClusterProperties()

    companion object {
        /**
         * Logging type property name.
         */
        const val LOGGING_TYPE_PROP = "hazelcast.logging.type"
        /**
         * Enable discovery.
         */
        const val HAZELCAST_DISCOVERY_ENABLED = "hazelcast.discovery.enabled"

        /**
         * Max num of seconds for heartbeat property name.
         */
        const val MAX_HEARTBEAT_SECONDS_PROP = "hazelcast.max.no.heartbeat.seconds"
        /**
         * Ipv4 protocol stack.
         */
        const val IPV4_STACK_PROP = "hazelcast.prefer.ipv4.stack"

        private const val serialVersionUID = 4204884717547468480L
    }
}
