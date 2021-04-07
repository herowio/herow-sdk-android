package io.herow.sdk.common.logger

import android.content.Context
import io.herow.sdk.common.helpers.DeviceHelper
import java.util.*

class GlobalLogger {
    private var debug: Boolean = false
    private var debugInFile: Boolean = false

    var logger: ILogger? = null

    companion object {
        var shared = GlobalLogger()
    }

    private fun methodName(): String {
        return Thread.currentThread().stackTrace[4].let {
            "${it.methodName}"
        }
    }

    private fun fileName(): String {
        return Thread.currentThread().stackTrace[4].let {
            "${it.fileName}"
        }
    }

    private fun lineNumber(): String {
        return Thread.currentThread().stackTrace[4].let {
            "${it.lineNumber}"
        }
    }


    fun startDebug() {
        debug = true
        logger?.startDebug()
    }

    fun stopDebug() {
        debug = false
        logger?.stopDebug()
    }

    fun startLogInFile() {
        debugInFile = true
        logger?.startLogInFile()
    }

    fun stopLogInFile() {
        debugInFile = false
        logger?.stopLogInFile()
    }

    fun verbose(
        context: Context?,
        message: Any
    ) {
        val messageToDispatch =
            format(context, fileName(), methodName(), lineNumber(), message)
        dispatch(messageToDispatch, type = MessageType.VERBOSE)
    }

    fun debug(
        context: Context?,
        message: Any
    ) {
        val messageToDispatch =
            format(context, fileName(), methodName(), lineNumber(), message)
        dispatch(messageToDispatch, type = MessageType.DEBUG)
    }

    fun info(
        context: Context?,
        message: Any
    ) {
        val messageToDispatch =
            format(context, fileName(), methodName(), lineNumber(), message)
        dispatch(messageToDispatch, type = MessageType.INFO)
    }

    fun warning(
        context: Context?,
        message: Any
    ) {
        val messageToDispatch =
            format(context, fileName(), methodName(), lineNumber(), message)
        dispatch(messageToDispatch, type = MessageType.WARNING)
    }

    fun error(
        context: Context?,
        message: Any
    ) {
        val messageToDispatch =
            format(context, fileName(), methodName(), lineNumber(), message)
        dispatch(messageToDispatch, type = MessageType.ERROR)
    }

    fun registerHerowId(herowId: String) {
        logger?.registerHerowId(herowId)
    }

    fun registerLogger(logger: ILogger) {
        this.logger = logger
    }

    private fun format(
        context: Context?,
        fileName: String,
        functionName: String,
        lineNumber: String,
        message: Any
    ): String =
        "($fileName) - ($functionName) at line ($lineNumber): $message - battery level: ${
            if (context != null) {
                DeviceHelper.getBatteryLevel(
                    context
                )
            } else {
                "Can't reach battery level"
            }
        }%"

    private fun log(message: Any) {
        if (debug) {
            println(message as String)
        }
    }

    private fun dispatch(message: String, type: MessageType) {
        val display = "[${type.messageType.toUpperCase(Locale.ROOT)}] $message"

        logger?.let {
            when (type) {
                MessageType.DEBUG -> logger!!.debug(display)
                MessageType.VERBOSE -> logger!!.verbose(display)
                MessageType.INFO -> logger!!.info(display)
                MessageType.WARNING -> logger!!.warning(display)
                MessageType.ERROR -> logger!!.error(display)
            }
        } ?: log(display)
    }
}