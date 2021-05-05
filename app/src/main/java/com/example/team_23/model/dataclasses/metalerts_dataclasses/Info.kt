package com.example.team_23.model.dataclasses.metalerts_dataclasses

// Contains more detailed information on an Alert
data class Info (
        val lang: String?,          // Språk: norsk ('no') eller engelsk ('en')
        val event: String?,         // In our project only 'forestFire' relevant
        val responseType: String?,  // 'Monitor': act according to <instruction>. 'AllClear': danger ended.
        // val urgency: String,     // 'Future', 'Immediate'. Unsure if actually used by MetAlerts
        val severity: String?,     // 'Extreme', 'Severe', 'Moderate', 'Minor'.
        //val certainty: String,    // 'Likely', 'Possible', 'Observed'.
        //val effective: String,    // Message is valid from this time.
        //val onset: String,        // Starting time of event.
        //val expires: String,      // Message is valid until this time.
        val instruction: String?,    // Description of recommended action.
        // + much more
        val area: Area
)