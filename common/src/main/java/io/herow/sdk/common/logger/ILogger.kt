package io.herow.sdk.common.logger

interface ILogger {

    fun startDebug()
    fun stopDebug()
    fun startLogInFile()
    fun stopLogInFile()
    fun verbose(message: Any)
    fun debug(message: Any)
    fun info(message: Any)
    fun warning(message: Any)
    fun error(message: Any)
    fun registerHerowId(herowId: String)
}