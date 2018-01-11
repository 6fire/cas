package org.apereo.cas.configuration.model.support.hazelcast.discovery

import org.apereo.cas.configuration.support.RequiredProperty
import org.apereo.cas.configuration.support.RequiresModule

import java.io.Serializable

/**
 * This is [HazelcastAzureDiscoveryProperties].
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-support-hazelcast-core")
class HazelcastAzureDiscoveryProperties : Serializable {

    /**
     * The Azure subscription ID.
     */
    @RequiredProperty
    var subscriptionId: String? = null

    /**
     * The Azure Active Directory Service Principal client ID.
     */
    @RequiredProperty
    var clientId: String? = null

    /**
     * The Azure Active Directory Service Principal client secret.
     */
    @RequiredProperty
    var clientSecret: String? = null

    /**
     * The Azure Active Directory tenant ID.
     */
    @RequiredProperty
    var tenantId: String? = null

    /**
     * The name of the tag on the hazelcast vm resources.
     * With every Hazelcast Virtual Machine you deploy in your resource group, you need to ensure that each VM is tagged with the value of cluster-id defined in
     * your Hazelcast configuration. The only requirement is that every VM can access each other either by private or public IP address.
     */
    @RequiredProperty
    var clusterId: String? = null

    /**
     * The Azure resource group name of the cluster. You can find this in the Azure portal or CLI.
     */
    @RequiredProperty
    var groupName: String? = null

    companion object {

        /**
         * Microsoft Azure's client id property for discovery.
         */
        const val AZURE_DISCOVERY_CLIENT_ID = "client-id"

        /**
         * Microsoft Azure's client secret property for discovery.
         */
        const val AZURE_DISCOVERY_CLIENT_SECRET = "client-secret"

        /**
         * Microsoft Azure's tenant id property for discovery.
         */
        const val AZURE_DISCOVERY_TENANT_ID = "tenant-id"

        /**
         * Microsoft Azure's subscription id property for discovery.
         */
        const val AZURE_DISCOVERY_SUBSCRIPTION_ID = "subscription-id"

        /**
         * Microsoft Azure's subscription id property for discovery.
         */
        const val AZURE_DISCOVERY_CLUSTER_ID = "cluster-id"

        /**
         * Microsoft Azure's group name property for discovery.
         */
        const val AZURE_DISCOVERY_GROUP_NAME = "group-name"

        private const val serialVersionUID = 3861923784551442190L
    }
}
