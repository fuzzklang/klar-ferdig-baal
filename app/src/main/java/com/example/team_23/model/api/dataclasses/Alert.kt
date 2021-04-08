package com.example.team_23.model.api.dataclasses

// Source: https://api.met.no/weatherapi/metalerts/1.1/documentation#DESCRIPTION
// Corresponds to Alert described in MET report
// Contains only a selection of fields from CAP-alert
// Need the HTTP-requests to properly work to be able to test these
data class Alert (
        val identifier: String?, // Unique identifier issued by MET or NVE
        val sent: String?,       // Time of sending
        val status: String?,     // 'Actual' or 'Test' (should be ignored)
        val msgType: String?,    // 'Alert': first message. 'Update': replaces previous messages. 'Cancel': cancel previous erroneous message, or danger ended.
        val info: Info
)