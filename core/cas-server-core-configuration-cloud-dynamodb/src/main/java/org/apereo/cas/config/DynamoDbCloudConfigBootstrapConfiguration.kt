package org.apereo.cas.config

import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.model.*
import com.amazonaws.services.dynamodbv2.util.TableUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.tuple.Pair
import org.slf4j.LoggerFactory
import org.springframework.cloud.bootstrap.config.PropertySourceLocator
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.core.env.PropertiesPropertySource
import org.springframework.core.env.PropertySource
import java.net.InetAddress
import java.util.*

/**
 * This is [DynamoDbCloudConfigBootstrapConfiguration].
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("dynamoDbCloudConfigBootstrapConfiguration")
class DynamoDbCloudConfigBootstrapConfiguration : PropertySourceLocator {

    private enum class ColumnNames private constructor(val columnName: String) {
        ID("id"),
        NAME("name"),
        VALUE("value")
    }

    override fun locate(environment: Environment): PropertySource<*> {
        val amazonDynamoDBClient = getAmazonDynamoDbClient(environment)
        createSettingsTable(amazonDynamoDBClient, false)

        val scan = ScanRequest(TABLE_NAME)
        LOGGER.debug("Scanning table with request [{}]", scan)
        val result = amazonDynamoDBClient.scan(scan)
        LOGGER.debug("Scanned table with result [{}]", scan)

        val props = Properties()
        result.items
                .stream()
                .map<Pair<String, Any>>({ retrieveSetting(it) })
                .forEach { p -> props.put(p.key, p.value) }
        return PropertiesPropertySource(javaClass.simpleName, props)
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(DynamoDbCloudConfigBootstrapConfiguration::class.java)
        private val TABLE_NAME = "DynamoDbCasProperties"
        private val PROVISIONED_THROUGHPUT: Long = 10

        private fun retrieveSetting(entry: Map<String, AttributeValue>): Pair<String, Any> {
            val name = entry[ColumnNames.NAME.columnName]!!.s
            val value = entry[ColumnNames.VALUE.columnName]!!.s
            return Pair.of(name, value)
        }

        private fun getSetting(environment: Environment, key: String): String {
            return environment.getProperty("cas.spring.cloud.dynamodb." + key)
        }

        private fun getAmazonDynamoDbClient(environment: Environment): AmazonDynamoDB {
            val cfg = ClientConfiguration()

            try {
                val localAddress = getSetting(environment, "localAddress")
                if (StringUtils.isNotBlank(localAddress)) {
                    cfg.localAddress = InetAddress.getByName(localAddress)
                }
            } catch (e: Exception) {
                LOGGER.error(e.message, e)
            }

            val key = getSetting(environment, "credentialAccessKey")
            val secret = getSetting(environment, "credentialSecretKey")
            val credentials = BasicAWSCredentials(key, secret)

            var region = getSetting(environment, "region")
            if (StringUtils.isBlank(region)) {
                region = Regions.getCurrentRegion().name
            }

            var regionOverride = getSetting(environment, "regionOverride")
            if (StringUtils.isNotBlank(regionOverride)) {
                regionOverride = Regions.getCurrentRegion().name
            }
            val endpoint = getSetting(environment, "endpoint")
            return AmazonDynamoDBClient.builder()
                    .withCredentials(AWSStaticCredentialsProvider(credentials))
                    .withClientConfiguration(cfg)
                    .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(endpoint, regionOverride))
                    .withRegion(region)
                    .build()
        }

        private fun createSettingsTable(amazonDynamoDBClient: AmazonDynamoDB, deleteTables: Boolean) {
            try {
                val name = ColumnNames.ID.name
                val request = CreateTableRequest()
                        .withAttributeDefinitions(AttributeDefinition(name, ScalarAttributeType.S))
                        .withKeySchema(KeySchemaElement(name, KeyType.HASH))
                        .withProvisionedThroughput(ProvisionedThroughput(PROVISIONED_THROUGHPUT, PROVISIONED_THROUGHPUT))
                        .withTableName(TABLE_NAME)

                if (deleteTables) {
                    val delete = DeleteTableRequest(request.tableName)
                    LOGGER.debug("Sending delete request [{}] to remove table if necessary", delete)
                    TableUtils.deleteTableIfExists(amazonDynamoDBClient, delete)
                }
                LOGGER.debug("Sending delete request [{}] to create table", request)
                TableUtils.createTableIfNotExists(amazonDynamoDBClient, request)

                LOGGER.debug("Waiting until table [{}] becomes active...", request.tableName)
                TableUtils.waitUntilActive(amazonDynamoDBClient, request.tableName)

                val describeTableRequest = DescribeTableRequest().withTableName(request.tableName)
                LOGGER.debug("Sending request [{}] to obtain table description...", describeTableRequest)

                val tableDescription = amazonDynamoDBClient.describeTable(describeTableRequest).table
                LOGGER.debug("Located newly created table with description: [{}]", tableDescription)
            } catch (e: Exception) {
                throw RuntimeException(e.message, e)
            }

        }
    }
}
