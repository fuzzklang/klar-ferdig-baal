package com.example.team_23

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val textView = findViewById<TextView>(R.id.myTextView)
        textView.text = "RESET TEXT"

        val titleView = findViewById<TextView>(R.id.titleView)
        titleView.text = "Alert fra mai 2019"

        // Read from local XML test file
        // Show info (text) on screen
        val rssFeed = assets.open("rssFeedWithAlerts.xml")
        val info = MetAlertsRssParser().parse(rssFeed)
        val infoString = info.joinToString (separator = "\n\n") { it.toString() }
        textView.text = infoString
    }
}