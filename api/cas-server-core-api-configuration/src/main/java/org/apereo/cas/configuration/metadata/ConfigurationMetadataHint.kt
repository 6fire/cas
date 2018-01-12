package org.apereo.cas.configuration.metadata

import org.springframework.boot.configurationmetadata.ValueHint
import org.springframework.boot.configurationmetadata.ValueProvider

import java.util.ArrayList

/**
 * This is [ConfigurationMetadataHint].
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
class ConfigurationMetadataHint {
    var name: String? = null
    val values: MutableList<ValueHint> = mutableListOf()
    val providers: MutableList<ValueProvider> = mutableListOf()
}
