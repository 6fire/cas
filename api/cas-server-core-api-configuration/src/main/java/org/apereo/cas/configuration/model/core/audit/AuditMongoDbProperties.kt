package org.apereo.cas.configuration.model.core.audit

import org.apereo.cas.configuration.model.support.mongo.SingleCollectionMongoDbProperties
import org.apereo.cas.configuration.support.RequiresModule

/**
 * This is [AuditMongoDbProperties].
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-audit-mongo")
class AuditMongoDbProperties : SingleCollectionMongoDbProperties() {

    /**
     * Execute the recording of audit records in async manner.
     * This setting must almost always be set to true.
     */
    var isAsynchronous: Boolean = true

    init {
        collection = "MongoDbCasAuditRepository"
    }

    companion object {
        private val serialVersionUID = 4940497540189318943L
    }
}
