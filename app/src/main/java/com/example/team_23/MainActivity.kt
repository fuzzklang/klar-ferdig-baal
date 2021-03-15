package com.example.team_23

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.example.team_23.api.CapParser
import com.example.team_23.api.MetAlertsRssParser
import com.example.team_23.api.API

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tag = "MainActivity"

        val textView = findViewById<TextView>(R.id.myTextView)
        textView.text = "RESET TEXT"

        val titleView = findViewById<TextView>(R.id.titleView)
        titleView.text = "Alert fra mai 2019"

        Log.d(tag, "Calling API")
        API().fetchAllAlerts()

        // Read from local XML test file
        // Show info (text) on screen
        /*val capAlert = assets.open("capAlert.xml")
        val alert = CapParser().parse(capAlert)
        val infoString = """
            Identifier: ${alert.identifier}
            MsgType: ${alert.msgType}
            Sent: ${alert.sent}
            Status: ${alert.status}
            Info/event: ${alert.info.event}
            Info/responseType: ${alert.info.responseType}
            Info/instruction: ${alert.info.instruction}
            Area/AreaDesc: ${alert.info.area.areaDesc}
            Area/Polygon: ${alert.info.area.polygon?.substring(0,25)}...
        """.trimIndent()
        textView.text = infoString*/
    }
}