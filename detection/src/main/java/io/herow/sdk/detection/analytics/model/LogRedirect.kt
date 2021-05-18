package io.herow.sdk.detection.analytics.model

class LogRedirect(
    zoneHash: String,
    idCampaign: String
): HerowLogData() {

    companion object {
        const val CAMPAIGN_ID = "campaign_id"
        const val ZONE_HASH = "techno_hash"
    }

    init {
        this[SUBTYPE] = LogSubtype.REDIRECT
        this[APP_STATE] = "bg"
        this[CAMPAIGN_ID] = idCampaign
        this[ZONE_HASH] = zoneHash
    }
}