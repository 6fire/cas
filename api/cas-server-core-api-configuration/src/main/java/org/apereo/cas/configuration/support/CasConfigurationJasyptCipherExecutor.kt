package org.apereo.cas.configuration.support

import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.math.NumberUtils
import org.apereo.cas.CipherExecutor
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment

import java.security.Security

/**
 * This is [CasConfigurationJasyptCipherExecutor].
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
class CasConfigurationJasyptCipherExecutor
/**
 * Instantiates a new Cas configuration jasypt cipher executor.
 *
 * @param environment the environment
 */
(environment: Environment) : CipherExecutor<String, String> {

    /**
     * The Jasypt instance.
     */
    private val jasyptInstance: StandardPBEStringEncryptor

    /**
     * The Jasypt encryption parameters.
     */
    enum class JasyptEncryptionParameters
    /**
     * Instantiates a new Jasypt encryption parameters.
     *
     * @param name         the name
     * @param defaultValue the default value
     */
    private constructor(
            /**
             * Gets name.
             *
             * @return the name
             */
            var parameterName: String,
            /**
             * Gets default value.
             *
             * @return the default value
             */
            val defaultValue: String?) {

        /**
         * Jasypt algorithm name to use.
         */
        ALGORITHM("cas.standalone.config.security.alg", "PBEWithMD5AndTripleDES"),
        /**
         * Jasypt provider name to use.
         */
        PROVIDER("cas.standalone.config.security.provider", null),
        /**
         * Jasypt number of iterations to use.
         */
        ITERATIONS("cas.standalone.config.security.iteration", null),
        /**
         * Jasypt password to use.
         */
        PASSWORD("cas.standalone.config.security.psw", null)
    }

    init {
        Security.addProvider(BouncyCastleProvider())
        this.jasyptInstance = StandardPBEStringEncryptor()

        val alg = getJasyptParamFromEnv(environment, JasyptEncryptionParameters.ALGORITHM)
        setAlgorithm(alg)

        val psw = getJasyptParamFromEnv(environment, JasyptEncryptionParameters.PASSWORD)
        setPassword(psw)

        val pName = getJasyptParamFromEnv(environment, JasyptEncryptionParameters.PROVIDER)
        setProviderName(pName)

        val iter = getJasyptParamFromEnv(environment, JasyptEncryptionParameters.ITERATIONS)
        setKeyObtentionIterations(iter)
    }

    /**
     * Sets algorithm.
     *
     * @param alg the alg
     */
    fun setAlgorithm(alg: String) {
        if (StringUtils.isNotBlank(alg)) {
            LOGGER.debug("Configured Jasypt algorithm [{}]", alg)
            jasyptInstance.setAlgorithm(alg)
        }
    }

    /**
     * Sets password.
     *
     * @param psw the psw
     */
    fun setPassword(psw: String) {
        if (StringUtils.isNotBlank(psw)) {
            LOGGER.debug("Configured Jasypt password")
            jasyptInstance.setPassword(psw)
        }
    }

    /**
     * Sets key obtention iterations.
     *
     * @param iter the iter
     */
    fun setKeyObtentionIterations(iter: String) {
        if (StringUtils.isNotBlank(iter) && NumberUtils.isCreatable(iter)) {
            LOGGER.debug("Configured Jasypt iterations")
            jasyptInstance.setKeyObtentionIterations(Integer.parseInt(iter))
        }
    }

    /**
     * Sets provider name.
     *
     * @param pName the p name
     */
    fun setProviderName(pName: String) {
        if (StringUtils.isNotBlank(pName)) {
            LOGGER.debug("Configured Jasypt provider")
            this.jasyptInstance.setProviderName(pName)
        }
    }

    override fun encode(value: String): String? {
        return encryptValue(value)
    }

    override fun decode(value: String): String? {
        return decryptValue(value)
    }

    override fun getName(): String {
        return "CAS Configuration Jasypt Encryption"
    }

    /**
     * Encrypt value string.
     *
     * @param value the value
     * @return the string
     */
    fun encryptValue(value: String): String? {
        try {
            initializeJasyptInstanceIfNecessary()
            return ENCRYPTED_VALUE_PREFIX + this.jasyptInstance.encrypt(value)
        } catch (e: Exception) {
            LOGGER.error("Could not encrypt value [{}]", e)
        }

        return null
    }


    /**
     * Decrypt value string.
     *
     * @param value the value
     * @return the string
     */
    fun decryptValue(value: String): String? {
        try {
            if (StringUtils.isNotBlank(value) && value.startsWith(ENCRYPTED_VALUE_PREFIX)) {
                initializeJasyptInstanceIfNecessary()

                val encValue = value.substring(ENCRYPTED_VALUE_PREFIX.length)
                LOGGER.trace("Decrypting value [{}]...", encValue)
                val result = this.jasyptInstance.decrypt(encValue)

                if (StringUtils.isNotBlank(result)) {
                    LOGGER.debug("Decrypted value [{}] successfully.", encValue)
                    return result
                }
                LOGGER.warn("Encrypted value [{}] has no values.", encValue)
            }
            return value
        } catch (e: Exception) {
            LOGGER.error("Could not decrypt value [{}]", e)
        }

        return null
    }

    /**
     * Initialize jasypt instance if necessary.
     */
    private fun initializeJasyptInstanceIfNecessary() {
        if (!this.jasyptInstance.isInitialized) {
            LOGGER.debug("Initializing Jasypt...")
            this.jasyptInstance.initialize()
        }
    }

    companion object {
        /**
         * Prefix inserted at the beginning of a value to indicate it's encrypted.
         */
        val ENCRYPTED_VALUE_PREFIX = "{cipher}"

        private val LOGGER = LoggerFactory.getLogger(CasConfigurationJasyptCipherExecutor::class.java)

        /**
         * Retrieves the [String] of an [Object].
         *
         * @param propertyValue The property value to cast
         * @return A [String] representing the property value or `null` if it is not a [String]
         */
        private fun getStringPropertyValue(propertyValue: Any): String? {
            return (propertyValue as? String)?.toString()
        }

        /**
         * Gets jasypt param from env.
         *
         * @param environment the environment
         * @param param       the param
         * @return the jasypt param from env
         */
        private fun getJasyptParamFromEnv(environment: Environment, param: JasyptEncryptionParameters): String {
            return environment.getProperty(param.parameterName, param.defaultValue)
        }
    }
}
