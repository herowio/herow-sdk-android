package io.herow.sdk.detection.helpers

import android.content.Context
import android.location.Location
import io.herow.sdk.common.json.GsonProvider
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.model.Access
import io.herow.sdk.connection.cache.model.Poi
import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.connection.cache.model.mapper.PoiMapper
import io.herow.sdk.connection.cache.model.mapper.ZoneMapper
import io.herow.sdk.connection.logs.Log
import io.herow.sdk.connection.logs.Logs
import io.herow.sdk.detection.analytics.ApplicationData
import io.herow.sdk.detection.analytics.model.HerowLogContext
import io.herow.sdk.detection.koin.ICustomKoinComponent
import org.koin.core.component.inject

class LogsHelper: ICustomKoinComponent {
    private val sessionHolder: SessionHolder by inject()

    fun createTestLogs(context: Context): String {
        val location = Location("tus")
        location.latitude = 48.875516
        location.longitude = 2.349096

        val zone = Zone(
            hash = "ivbxbhxm8rnk",
            lat = 48.875741,
            lng = 2.349255,
            radius = 300.0,
            campaigns = null,
            access = Access(
                address = "54 Rue de Paradis, 75010 Paris, France",
                id = "6004957256eb6779115b6d8a",
                name = "HEROW"
            )
        )

        val zone2 = Zone(
            hash = "ivbxbhxm8rnm",
            lat = 48.875629,
            lng = 2.348838,
            radius = 300.0,
            campaigns = null,
            access = Access(
                address = "55 Rue de Paradis, 75010 Paris, France",
                id = "6004957256eb6779115b6d8b",
                name = "BISTRO PARADIS"
            )
        )

        val poi = Poi(
            id = "7515771363",
            lat = 48.84748,
            lng = 2.35231
        )

        val herowLogContext =
            HerowLogContext(
                "fg",
                location,
                arrayListOf(
                    PoiMapper(
                        poi.id,
                        poi.distance,
                        poi.tags
                    )
                ),
                arrayListOf(
                    ZoneMapper(zone.lng, zone.lat, zone.hash, zone.distance, zone.radius),
                    ZoneMapper(zone2.lng, zone2.lat, zone2.hash, zone2.distance, zone2.radius)
                )
            )
        herowLogContext.enrich(
            ApplicationData(context), sessionHolder
        )

        val listOfLogs = listOf(Log(herowLogContext))
        val logs = Logs(listOfLogs)

        return GsonProvider.toJson(logs)
    }
}