package io.herow.sdk.common.states

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

object DataHolder {
    lateinit var preferences: SharedPreferences

    fun init(context: Context): DataHolder {
        preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return this
    }

    private inline fun edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor: SharedPreferences.Editor = preferences.edit()
        operation(editor)
        editor.apply()
    }

    operator fun set(key: String, value: Any?) {
        when (value) {
            is String -> edit {
                it.putString(key, value)
            }
            is Int -> edit {
                it.putInt(key, value)
            }
            is Boolean -> edit {
                it.putBoolean(key, value)
            }
            is Float -> edit {
                it.putFloat(key, value)
            }
            is Long -> edit {
                it.putLong(key, value)
            }
            is Double -> edit {
                it.putLong(key, value.toBits())
            }
            else -> throw UnsupportedOperationException("Not yet implemented")
        }
    }

    inline operator fun <reified T : Any> get(key: String, defaultValue: T? = null): T {
        return when (T::class) {
            String::class -> preferences.getString(key, defaultValue as? String ?: "") as T
            Int::class -> preferences.getInt(key, defaultValue as? Int ?: -1) as T
            Boolean::class -> preferences.getBoolean(key, defaultValue as? Boolean ?: false) as T
            Float::class -> preferences.getFloat(key, defaultValue as? Float ?: -1f) as T
            Long::class -> preferences.getLong(key, defaultValue as? Long ?: -1) as T
            Double::class -> Double.fromBits(preferences.getLong(key, defaultValue as? Long ?: -1)) as T
            else -> throw UnsupportedOperationException("Not yet implemented")
        }
    }
}