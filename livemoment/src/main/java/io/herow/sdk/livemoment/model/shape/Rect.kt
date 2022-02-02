package io.herow.sdk.livemoment.model.shape

import android.location.Location
import io.herow.sdk.livemoment.model.enum.LeafType
import io.herow.sdk.livemoment.quadtree.HerowQuadTreeNode
import io.herow.sdk.livemoment.quadtree.IQuadTreeLocation

data class Rect(
    var originLat: Double = 0.0,
    var endLat: Double = 0.0,
    var originLng: Double = 0.0,
    var endLng: Double = 0.0
) {

    private val herowQuadTreeNode = HerowQuadTreeNode()

    val world = Rect(
        originLat = herowQuadTreeNode.minLat,
        endLat = herowQuadTreeNode.maxLat,
        originLng = herowQuadTreeNode.minLng,
        endLng = herowQuadTreeNode.maxLng
    )

    fun contains(location: IQuadTreeLocation): Boolean =
        location.lat in originLat..endLat && location.lng in originLng..endLng

    fun circle(): Circle {
        val radius = Location("").apply {
            latitude = originLat
            longitude = originLng
        }.distanceTo(Location("").apply {
            latitude = endLat
            longitude = endLng
        }).div(2.0)

        return Circle(radius = radius, center = Location("").apply {
            latitude = middleLat()
            longitude = middleLng()
        })
    }

    private fun middleLat(): Double = (endLat + originLat).div(2)
    private fun middleLng(): Double = (endLng + originLng).div(2)

    fun points(): ArrayList<Location> = arrayListOf(
        Location("").apply {
            latitude = originLat
            longitude = originLng
        },
        Location("").apply {
            latitude = originLat
            longitude = endLng
        },
        Location("").apply {
            latitude = endLat
            longitude = endLng
        },
        Location("").apply {
            latitude = endLat
            longitude = originLng
        },
        Location("").apply {
            latitude = originLat
            longitude = originLng
        }
    )

    fun area(): Double {
        val d1 = Location("").apply {
            latitude = originLat
            longitude = originLng
        }.distanceTo(Location("").apply {
            latitude = endLat
            longitude = endLng
        })

        val d2 = Location("").apply {
            latitude = endLat
            longitude = originLng
        }.distanceTo(Location("").apply {
            latitude = endLat
            longitude = endLng
        })

        return d1.times(d2).toDouble()
    }

    fun rectForType(type: LeafType): Rect? = when (type) {
        LeafType.RIGHTUP -> rightUpRect()
        LeafType.RIGHTBOTTOM -> rightBottomRect()
        LeafType.LEFTUP -> leftUpRect()
        LeafType.LEFTBOTTOM -> leftBottomRect()
        else -> null
    }

    fun leftUpRect(): Rect =
        Rect(originLat = middleLat(), endLat = endLat, originLng = originLng, endLng = middleLng())

    fun leftBottomRect(): Rect =
        Rect(originLat = originLat, endLat = middleLat(), originLng = originLng, endLng = middleLng())

    fun rightUpRect(): Rect =
        Rect(originLat = middleLat(), endLat = endLat, originLng = middleLng(), endLng = endLng)

    private fun rightBottomRect(): Rect =
        Rect(originLat = originLat, endLat = middleLat(), originLng = middleLng(), endLng = endLng)

    fun isMin(): Boolean {
        return Location("").apply {
            latitude = originLat
            longitude = originLng
        }.distanceTo(Location("").apply {
            latitude = endLat
            longitude = endLng
        }) <= herowQuadTreeNode.nodeSize
    }

    private fun isEqual(rect: Rect): Boolean =
        originLat == rect.originLat && endLat == rect.endLat && originLng == rect.originLng && endLng == rect.endLng

    fun contains(rect: Rect): Boolean =
        originLat <= rect.originLat && endLat >= rect.endLat && originLng <= rect.originLng && endLng >= rect.endLng && !isEqual(
            rect
        )
}