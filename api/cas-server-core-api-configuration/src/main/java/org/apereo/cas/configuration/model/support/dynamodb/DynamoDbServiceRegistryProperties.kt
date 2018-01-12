package org.apereo.cas.configuration.model.support.dynamodb

import org.apereo.cas.configuration.support.RequiresModule

/**
 * This is [DynamoDbServiceRegistryProperties].
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiresModule(name = "cas-server-support-dynamodb-service-registry")
class DynamoDbServiceRegistryProperties : AbstractDynamoDbProperties() {
    /**
     * The table name used and created by CAS to hold service definitions in DynamoDb.
     */
    var tableName = "DynamoDbCasServices"
    
    companion object {
        private val serialVersionUID = 809653348774854955L
    }
}
