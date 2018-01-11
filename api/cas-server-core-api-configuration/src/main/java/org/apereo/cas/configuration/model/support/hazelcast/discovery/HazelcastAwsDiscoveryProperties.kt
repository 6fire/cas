package org.apereo.cas.configuration.model.support.hazelcast.discovery

import org.apereo.cas.configuration.support.RequiredProperty
import org.apereo.cas.configuration.support.RequiresModule

import java.io.Serializable

/**
 * This is [HazelcastAwsDiscoveryProperties].
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-support-hazelcast-core")
class HazelcastAwsDiscoveryProperties : Serializable {

    /**
     * AWS access key.
     */
    @RequiredProperty
    var accessKey: String? = null

    /**
     * AWS secret key.
     */
    @RequiredProperty
    var secretKey: String? = null
    /**
     * If you do not want to use access key and secret key, you can specify iam-role.
     * Hazelcast fetches your credentials by using your IAM role.
     */
    var iamRole: String? = null
    /**
     * AWS region. i.e. `us-east-1`.
     * The region where your members are running.
     */
    var region = "us-east-1"
    /**
     * Host header. i.e. `ec2.amazonaws.com`.
     * The URL that is the entry point for a web service.
     */
    var hostHeader: String? = null
    /**
     * If a security group is configured, only instances within that security group are selected.
     */
    var securityGroupName: String? = null
    /**
     * If a tag key/value is set, only instances with that tag key/value will be selected.
     */
    var tagKey: String? = null
    /**
     * If a tag key/value is set, only instances with that tag key/value will be selected.
     */
    var tagValue: String? = null
    /**
     * Hazelcast port. Typically may be set to `5701`.
     * You can set searching for other ports rather than 5701 if you've members on different ports.
     */
    var port = -1

    /**
     * The maximum amount of time Hazelcast will try to connect to a well known
     * member before giving up. Setting this value too low could mean that a
     * member is not able to connect to a cluster. Setting the value too high means
     * that member startup could slow down because of longer timeouts (for example,
     * when a well known member is not up). Increasing this value is recommended if
     * you have many IPs listed and the members cannot properly build up the cluster.
     * Its default value is 5.
     */
    private val connectionTimeoutSeconds = 5

    companion object {
        /**
         * AWS discovery access key property.
         */
        const val AWS_DISCOVERY_ACCESS_KEY = "access-key"
        /**
         * AWS discovery secret key property.
         */
        const val AWS_DISCOVERY_SECRET_KEY = "secret-key"
        /**
         * AWS discovery IAM role property.
         */
        const val AWS_DISCOVERY_IAM_ROLE = "iam-role"
        /**
         * AWS discovery  region property.
         */
        const val AWS_DISCOVERY_REGION = "region"
        /**
         * AWS discovery host header property.
         */
        const val AWS_DISCOVERY_HOST_HEADER = "host-header"
        /**
         * AWS discovery security group name property.
         */
        const val AWS_DISCOVERY_SECURITY_GROUP_NAME = "security-group-name"
        /**
         * AWS discovery  tag key property.
         */
        const val AWS_DISCOVERY_TAG_KEY = "tag-key"
        /**
         * AWS discovery tag value property.
         */
        const val AWS_DISCOVERY_TAG_VALUE = "tag-value"
        /**
         * AWS discovery HZ port property.
         */
        const val AWS_DISCOVERY_PORT = "hz-port"

        private const val serialVersionUID = -8281247687171101766L
    }
}
