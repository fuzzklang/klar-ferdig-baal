package com.example.team_23.api

// Data class for items in RSS-feed fetched from MetAlerts
// Each item directs to an alert (via link)
data class RssItem (
        val title: String?,
        val description: String?,
        val link: String?
)