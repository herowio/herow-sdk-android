package io.herow.sdk.connection.entities.response

data class ConfigResult(private val cacheInterval: Long,
                        private val configInterval: Long,
                        private val enabled: Boolean)