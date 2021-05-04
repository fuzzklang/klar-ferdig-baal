package com.example.team_23.model.api.metalerts_dataclasses

import com.google.android.gms.maps.model.LatLng

// Source: https://api.met.no/weatherapi/metalerts/1.1/documentation#DESCRIPTION
// Corresponds to Alert described in MET report
// Contains only a selection of fields from CAP-alert
// Need the HTTP-requests to properly work to be able to test these
class Alert (
        val identifier: String?, // Unik identifikator utstedt av MET eller NVE
        val sent: String?,       // Tidspunkt for utsendelse
        val status: String?,     // 'Actual' or 'Test' (test skal ignoreres)
        val msgType: String?,    // 'Alert': første melding. 'Update': erstatter tidligere meldinger. 'Cancel': kanseller tidligere feilaktig melding, eller fare er over.
        val infoItemsNo: List<Info>, // Liste med Info-elementer på norsk (vanligvis kun ett element?)
        val infoItemsEn: List<Info>  // Liste med Info-elementer på engelsk (vanligvis kun ett element?)
) {
    fun getPolygon(): List<LatLng> {
        val latLngList = mutableListOf<LatLng>()
        val polygonString = infoItemsNo[0].area.polygon  // TODO: dobbeltsjekk at vi kan anta kun ett info-item per varsel!
        if (polygonString != null)
        // Splitt strengen ved whitespace (resulterer i en liste av "<Desimaltall>,<Desimaltall>")
            polygonString.split(" ").forEach {latLngString ->
                // For hvert streng med slik tallpar, splitt ved komma og konverter hver desimaltall til Double (vha. map).
                val latLngValues = latLngString.split(",").map { it.toDouble() }
                latLngList.add(LatLng(latLngValues[0], latLngValues[1]))
            }
        return latLngList
    }
}