package io.herow.sdk.connection.config

interface ConfigListener {
    fun onConfigResult(configResult: ConfigResult)
}