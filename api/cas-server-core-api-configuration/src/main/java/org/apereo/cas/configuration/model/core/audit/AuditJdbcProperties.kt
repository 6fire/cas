package org.apereo.cas.configuration.model.core.audit

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties
import org.apereo.cas.configuration.support.RequiresModule

/**
 * This is [AuditJdbcProperties].
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-audit-jdbc")
class AuditJdbcProperties : AbstractJpaProperties() {

    /**
     * Execute the recording of audit records in async manner.
     * This setting must almost always be set to true.
     */
    var isAsynchronous: Boolean = true

    /**
     * Indicates how long audit records should be kept in the database.
     * This is used by the clean-up criteria to clean up after stale audit records.
     */
    var maxAgeDays = 180

    /**
     * Defines the isolation level for transactions.
     *
     * @see org.springframework.transaction.TransactionDefinition
     */
    var isolationLevelName = "ISOLATION_READ_COMMITTED"

    /**
     * Defines the propagation behavior for transactions.
     *
     * @see org.springframework.transaction.TransactionDefinition
     */
    var propagationBehaviorName = "PROPAGATION_REQUIRED"

    companion object {
        private val serialVersionUID = 4227475246873515918L
    }
}

