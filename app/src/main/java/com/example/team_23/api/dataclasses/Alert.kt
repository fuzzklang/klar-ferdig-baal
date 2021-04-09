package com.example.team_23.api.dataclasses

// Source: https://api.met.no/weatherapi/metalerts/1.1/documentation#DESCRIPTION
// Corresponds to Alert described in MET report
// Contains only a selection of fields from CAP-alert
// Need the HTTP-requests to properly work to be able to test these
data class Alert (
        val identifier: String?, // Unik identifikator utstedt av MET eller NVE
        val sent: String?,       // Tidspunkt for utsendelse
        val status: String?,     // 'Actual' or 'Test' (test skal ignoreres)
        val msgType: String?,    // 'Alert': første melding. 'Update': erstatter tidligere meldinger. 'Cancel': kanseller tidligere feilaktig melding, eller fare er over.
        val infoItemsNo: List<Info>, // Liste med Info-elementer på norsk (vanligvis kun ett element?)
        val infoItemsEn: List<Info>  // Liste med Info-elementer på engelsk (vanligvis kun ett element?)
)