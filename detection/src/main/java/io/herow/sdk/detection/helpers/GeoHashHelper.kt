package io.herow.sdk.detection.helpers

import android.location.Location

/*
 * @see https://www.wikiwand.com/en/Geohash
 */
object GeoHashHelper {
    private val BASE32 = charArrayOf(
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'b', 'c', 'd', 'e', 'f', 'g',
        'h', 'j', 'k', 'm', 'n', 'p', 'q', 'r',
        's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
    )
    private val BASE32_INV = ByteArray('z'.code + 1)

    init {
        for (i in BASE32.indices) {
            BASE32_INV[BASE32[i].code] = i.toByte()
        }
    }

    /**
     * Takes a lat/lng and a precision, and returns a 64-bit long containing that
     * many low-order bits (big-endian). You can convert this long to a base-32
     * string using [.toBase32].
     *
     * This function doesn't validate preconditions, which are the following:
     * 1. lat ∈ [-90, 90)
     * 2. lng ∈ [-180, 180)
     * 3. bits ∈ [0, 61]
     *
     * Results are undefined if these preconditions are not met.
     */
    private fun encode(
        lat: Double,
        lng: Double,
        bits: Int
    ): Long {
        val lats = widen(((lat + 90) * 0x80000000L / 180.0).toLong() and 0x7fffffffL)
        val lngs = widen(((lng + 180) * 0x80000000L / 360.0).toLong() and 0x7fffffffL)
        return lats shr 1 or lngs shr 61 - bits or precisionTag(bits)
    }

    private fun precisionTag(bits: Int): Long {
        return 0x4000000000000000L or 1L shl bits
    }

    /**
     * Takes an encoded geohash (as a long) and its precision, and returns a
     * base-32 string representing it. The precision must be a multiple of 5 for
     * this to be accurate.
     */
    private fun toBase32(newGh: Long, bits: Int): String {
        var gh = newGh
        val chars = CharArray(bits / 5)
        for (i in chars.indices.reversed()) {
            chars[i] = BASE32[(gh and 0x1fL).toInt()]
            gh = gh shr 5
        }
        return String(chars)
    }

    fun encodeBase32(location: Location): String {
        return encodeBase32(location.latitude, location.longitude)
    }

    /**
     * Takes a latitude, longitude, and precision, and returns a base-32 string
     * representing the encoded geohash. See [.encode] and [ ][.toBase32] for preconditions (but they're pretty obvious).
     */
    private fun encodeBase32(lat: Double, lng: Double, bits: Int = 5 * 12): String {
        return toBase32(encode(lat, lng, bits), bits)
    }

    /**
     * Takes a base-32 string and returns an object representing its decoding.
     */
    fun decodeBase32(base32: String): Location {
        return decode(fromBase32(base32), base32.length * 5)
    }

    private fun decode(gh: Long, bits: Int): Location {
        val shifted = gh shl 61 - bits
        val lat = (unwiden(shifted shr 1) and 0x3fffffffL).toDouble() / 0x40000000L * 180 - 90
        val lng = (unwiden(shifted) and 0x7fffffffL).toDouble() / 0x80000000L * 360 - 180
        val location = Location("")
        location.latitude = lat
        location.longitude = lng
        return location
    }

    /**
     * Takes a base-32 string and returns a long containing its bits.
     */
    private fun fromBase32(base32: String): Long {
        var result: Long = 0
        for (element in base32) {
            result = result shl 5
            result = result or BASE32_INV[element.code].toLong()
        }
        result = result or precisionTag(base32.length * 5)
        return result
    }

    /**
     * "Widens" each bit by creating a zero to its left. This is the first step
     * in interleaving values. @see: https://graphics.stanford.edu/~seander/bithacks.html#InterleaveBMN
     */
    private fun widen(newLow32: Long): Long {
        var low32 = newLow32
        low32 = low32 or (low32 shl 16)
        low32 = low32 and 0x0000ffff0000ffffL
        low32 = low32 or (low32 shl 8)
        low32 = low32 and 0x00ff00ff00ff00ffL
        low32 = low32 or (low32 shl 4)
        low32 = low32 and 0x0f0f0f0f0f0f0f0fL
        low32 = low32 or (low32 shl 2)
        low32 = low32 and 0x3333333333333333L
        low32 = low32 or (low32 shl 1)
        low32 = low32 and 0x5555555555555555L
        return low32
    }

    /**
     * "Unwidens" each bit by removing the zero from its left. This is the
     * inverse of "widen". @see: http://fgiesen.wordpress.com/2009/12/13/decoding-morton-codes/
     */
    private fun unwiden(newWide: Long): Long {
        var wide = newWide
        wide = wide and 0x5555555555555555L
        wide = wide xor (wide shr 1)
        wide = wide and 0x3333333333333333L
        wide = wide xor (wide shr 2)
        wide = wide and 0x0f0f0f0f0f0f0f0fL
        wide = wide xor (wide shr 4)
        wide = wide and 0x00ff00ff00ff00ffL
        wide = wide xor (wide shr 8)
        wide = wide and 0x0000ffff0000ffffL
        wide = wide xor (wide shr 16)
        wide = wide and 0x00000000ffffffffL
        return wide
    }
}