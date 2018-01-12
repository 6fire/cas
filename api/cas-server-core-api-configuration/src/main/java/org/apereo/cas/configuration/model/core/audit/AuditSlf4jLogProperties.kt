package org.apereo.cas.configuration.model.core.audit

import org.apereo.cas.configuration.support.RequiresModule
import org.apereo.inspektr.audit.support.AbstractStringAuditTrailManager

import java.io.Serializable

/**
 * This is [AuditSlf4jLogProperties].
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-core-audit")
class AuditSlf4jLogProperties : Serializable {

    /**
     * Indicates whether audit logs should be recorded as a single-line.
     *
     *
     * By default, audit logs are split into multiple lines where each action and activity
     * takes up a full line. This is a more compact version.
     */
    var isUseSingleLine: Boolean = false

    /**
     * Character to separate audit fields if single-line audits are used.
     */
    var singlelineSeparator = "|"

    /**
     * The audit format to use in the logs.
     */
    var auditFormat: AbstractStringAuditTrailManager.AuditFormats = AbstractStringAuditTrailManager.AuditFormats.DEFAULT

    companion object {
        private const val serialVersionUID = 4227475246873515918L
    }
}

