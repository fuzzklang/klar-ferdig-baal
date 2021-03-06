package com.example.team_23.model.dataclasses.metalerts_dataclasses

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import java.util.*

// Source: https://api.met.no/weatherapi/metalerts/1.1/documentation#DESCRIPTION
// Corresponds to Alert described in MET report
// Contains only a selection of fields from CAP-alert
// Need the HTTP-requests to properly work to be able to test these
class Alert (
    val identifier: String?, // Unik identifikator utstedt av MET eller NVE
    val sent: String?,       // Tidspunkt for utsendelse
    val status: String?,     // 'Actual' or 'Test' (test skal ignoreres)
    val msgType: String?,    // 'Alert': første melding. 'Update': erstatter tidligere meldinger. 'Cancel': kanseller tidligere feilaktig melding, eller fare er over.
    val infoNo: Info,        // Info-element med norsk info
    val infoEn: Info         // Info-element med engelsk info
) {
    fun getPolygon(): List<LatLng> {
        val latLngList = mutableListOf<LatLng>()
        val polygonString = infoNo.area.polygon
        if (polygonString != null && polygonString.isNotBlank()) {
                polygonString.split(" ").forEach { latLngString ->
                    // For hvert streng med slik tallpar, splitt ved komma og konverter hver desimaltall til Double (vha. map).
                    val latLngValues = latLngString.split(",").map { it.toDouble() }
                    latLngList.add(LatLng(latLngValues[0], latLngValues[1]))
                }
            }
        return latLngList
    }

    // Hent grad av fare (gul, oransje eller rød)
    // Se her for beregning av farge: https://www.met.no/vaer-og-klima/ekstremvaervarsler-og-andre-farevarsler/faregradering-i-farger
    // Kilde: https://www.met.no/vaer-og-klima/ekstremvaervarsler-og-andre-farevarsler/faregradering-i-farger/_/attachment/download/a3f74362-7899-41d1-bd5c-a10c88a62af8:eb0b2127d9e584a4feaefb3681d8207a5d5a48de/MET-report%2020-2017.pdf
    // Sitat ang. Certainty: «MET vil bruke “Observed”, “Likely”, “Possible” og “Unlikely”»
    // Sitat ang. Severity: Det brukes Minor, Moderate, Severe og Extreme.
    fun getAlertColor(): AlertColors {
        val certainty = infoNo.certainty?.lowercase(Locale.US)
        val severity = infoNo.severity?.lowercase(Locale.US)
        Log.d("Alert", "Henter farge for varsel. Severity: $severity. Certainty: $certainty")
        val alertColor: AlertColors = when (certainty) {
            "observed" -> when (severity) {
                "moderate" -> AlertColors.YELLOW
                "severe" -> AlertColors.ORANGE
                "extreme" -> AlertColors.RED
                else -> AlertColors.UNKNOWN
            }
            "likely" -> when (severity) {
                "moderate" -> AlertColors.YELLOW
                "severe" -> AlertColors.ORANGE
                "extreme" -> AlertColors.RED
                else -> AlertColors.UNKNOWN
            }
            "possible" -> when (severity) {
                "severe" -> AlertColors.YELLOW
                "extreme" -> AlertColors.ORANGE
                else -> AlertColors.UNKNOWN
            }
            "unlikely" -> when (severity) {
                "extreme" -> AlertColors.YELLOW
                else -> AlertColors.UNKNOWN
            }
            else -> AlertColors.UNKNOWN
        }
        Log.d("Alert", "Farge for varsel: $alertColor")
        return alertColor
    }
}