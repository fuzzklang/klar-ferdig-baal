package com.example.team_23.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.team_23.R
import com.example.team_23.model.MainRepository
import com.example.team_23.model.api.ApiServiceImpl
import com.example.team_23.viewmodel.KartViewModel

class KartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Flytte instansiering av disse til Factory eller tilsvarende?
        val apiService = ApiServiceImpl()
        val repo = MainRepository(apiService)
        val kartViewModel = KartViewModel(repo)

        //knapp som sender bruker til reglene
        val rulesActicityBtn = findViewById<ImageButton>(R.id.send_rules)

        rulesActicityBtn.setOnClickListener{
            val intent = Intent(this,RegelView::class.java)
            startActivity(intent)

        }

        val infoButton = findViewById<ImageButton>(R.id.info_button)
        val infoCloseButton = findViewById<ImageButton>(R.id.info_close_button)
        val info = findViewById<View>(R.id.infoBox)

        var infoSynlig = true //Variabel som holder styr paa synligheten til info view
        //Funksjon som endrer synligheten til info view
        fun closeInfo(){
            if (infoSynlig == true) {
                info.setVisibility(View.INVISIBLE)
            } else{
                info.setVisibility(View.VISIBLE)
            }
            infoSynlig = !infoSynlig
        }
        //Info knapp som endrer info sin synlighet
        infoButton.setOnClickListener {closeInfo()}
        //Info knapp som gjør info view usynelig
        infoCloseButton.setOnClickListener{closeInfo()}

    }
}