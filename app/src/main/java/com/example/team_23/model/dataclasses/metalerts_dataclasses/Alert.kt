package com.example.team_23.model.dataclasses.metalerts_dataclasses

import android.util.Log
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
        polygonString?.split(" ")?.forEach { latLngString ->
            // For hvert streng med slik tallpar, splitt ved komma og konverter hver desimaltall til Double (vha. map).
            val latLngValues = latLngString.split(",").map { it.toDouble() }
            latLngList.add(LatLng(latLngValues[0], latLngValues[1]))
        }
        return latLngList
    }

    // Hent grad av fare (gul, oransje eller rød)
    // Kilde: https://www.met.no/vaer-og-klima/ekstremvaervarsler-og-andre-farevarsler/faregradering-i-farger/_/attachment/download/a3f74362-7899-41d1-bd5c-a10c88a62af8:eb0b2127d9e584a4feaefb3681d8207a5d5a48de/MET-report%2020-2017.pdf
    // TODO: implementer certainty slik at denne kan brukes sammen med severity for avgjøre farenivå (og farge)
    // TODO: Bruk enum class for returverdier
    fun getAlertColor(): String {
        val info = infoItemsNo[0]
        val severity = info.severity?.toLowerCase()
        Log.d("Alert", "Henter farge for varsel. Severity: $severity. "/*Certainty: $certainty*/)
        /*
        val certainty = info.certainty?.toLowerCase()
        val alertColor: String = when (certainty) {
            "observed" -> when (severity) {"moderate" -> "yellow"; "severe" -> "orange"; "extreme" -> "red";    else -> "unknown"}
            "likely"   -> when (severity) {"moderate" -> "yellow"; "severe" -> "orange"; "extreme" -> "red";    else -> "unknown"}
            "possible" -> when (severity) {                        "severe" -> "yellow"; "extreme" -> "orange"; else -> "unknown"}
            "unlikely" -> when (severity) {"extreme" -> "yellow"; else -> "unkown"}
        }
         */
        val alertColor = when (severity) {
            "moderate" -> "yellow"
            "severe" -> "orange"
            "extreme" -> "red"
            else -> "unkown"
        }
        Log.d("Alert", "Farge for varsel: $alertColor")
        return alertColor
    }
}