package io.herow.sdk.detection

import java.util.*
import kotlin.random.Random

object RandomGenerator {
    private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    fun id(): Long {
        return UUID.randomUUID().mostSignificantBits
    }

    fun idString(): String {
        return UUID.randomUUID().toString()
    }

    fun lat(): Double {
        return Math.random() * 180.0 - 90.0
    }

    fun lng(): Double {
        return Math.random() * 360.0 - 180.0
    }

    fun alphanumericalString(length: Int = 12): String {
        return (1..length)
            .map { Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
    }

    fun randomInt(min: Int = 100,
                  max: Int = 10_000): Int {
        return (min..max).random()
    }
}