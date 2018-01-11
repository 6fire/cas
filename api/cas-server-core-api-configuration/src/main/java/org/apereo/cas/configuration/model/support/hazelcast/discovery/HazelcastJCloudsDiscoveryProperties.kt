package org.apereo.cas.configuration.model.support.hazelcast.discovery

import org.apereo.cas.configuration.support.RequiredProperty
import org.apereo.cas.configuration.support.RequiresModule

import java.io.Serializable

/**
 * This is [HazelcastJCloudsDiscoveryProperties].
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-support-hazelcast-core")
class HazelcastJCloudsDiscoveryProperties : Serializable {

    /**
     * String value that is used to identify ComputeService provider. For example, "google-compute-engine" is used for Google Cloud services.
     * See [here](https://jclouds.apache.org/reference/providers/#compute) for more info.
     */
    @RequiredProperty
    var provider: String? = null

    /**
     * Cloud Provider identity, can be thought of as a user name for cloud services.
     */
    @RequiredProperty
    var identity: String? = null

    /**
     * Cloud Provider credential, can be thought of as a password for cloud services.
     */
    @RequiredProperty
    var credential: String? = null

    /**
     * Defines the endpoint for a gneric API such as OpenStack or CloudStack (optional).
     */
    var endpoint: String? = null

    /**
     * Defines zone for a cloud service (optional). Can be used with comma separated values for multiple values.
     */
    var zones: String? = null

    /**
     * Defines region for a cloud service (optional). Can be used with comma separated values for multiple values.
     */
    var regions: String? = null

    /**
     * Filters cloud instances with tags (optional). Can be used with comma separated values for multiple values.
     */
    var tagKeys: String? = null

    /**
     * Filters cloud instances with tags (optional) Can be used with comma separated values for multiple values.
     */
    var tagValues: String? = null

    /**
     * Filters instance groups (optional). When used with AWS it maps to security group.
     */
    var group: String? = null

    /**
     * Port which the hazelcast instance service uses on the cluster member. Default value is 5701. (optional)
     */
    var port = -1

    /**
     * Used for IAM role support specific to AWS (optional, but if defined, no identity or credential should be defined in the configuration).
     */
    var roleName: String? = null

    /**
     * Used for cloud providers which require an extra JSON or P12 key file. This denotes the path of that file. Only tested with Google Compute Engine.
     * (Required if Google Compute Engine is used.)
     */
    var credentialPath: String? = null

    companion object {

        /**
         * JClouds provider property.
         */
        const val JCLOUDS_DISCOVERY_PROVIDER = "provider"
        /**
         * JClouds identity property.
         */
        const val JCLOUDS_DISCOVERY_IDENTITY = "identity"
        /**
         * JClouds credential property.
         */
        const val JCLOUDS_DISCOVERY_CREDENTIAL = "credential"
        /**
         * JClouds endpoint property.
         */
        const val JCLOUDS_DISCOVERY_ENDPOINT = "endpoint"
        /**
         * JClouds zones property.
         */
        const val JCLOUDS_DISCOVERY_ZONES = "zones"
        /**
         * JClouds regions property.
         */
        const val JCLOUDS_DISCOVERY_REGIONS = "regions"
        /**
         * JClouds tag-keys property.
         */
        const val JCLOUDS_DISCOVERY_TAG_KEYS = "tag-keys"
        /**
         * JClouds tag-values property.
         */
        const val JCLOUDS_DISCOVERY_TAG_VALUES = "tag-values"
        /**
         * JClouds group property.
         */
        const val JCLOUDS_DISCOVERY_GROUP = "group"
        /**
         * JClouds hz-port property.
         */
        const val JCLOUDS_DISCOVERY_HZ_PORT = "hz-port"
        /**
         * JClouds role-name property.
         */
        const val JCLOUDS_DISCOVERY_ROLE_NAME = "role-name"
        /**
         * JClouds credential path property.
         */
        const val JCLOUDS_DISCOVERY_CREDENTIAL_PATH = "credentialPath"

        private const val serialVersionUID = -8281247687171101766L
    }
}
