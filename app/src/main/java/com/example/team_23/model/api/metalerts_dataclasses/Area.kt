package com.example.team_23.model.api.metalerts_dataclasses

// Area which alert is valid for
data class Area (
        val areaDesc: String? = null, // Description of geographical area for alert
        val polygon: String? = null   // Polygon encircling the current area
)