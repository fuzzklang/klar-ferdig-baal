package com.example.team_23.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
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
        val rulesActicityBtn = findViewById<Button>(R.id.send_rules)

        rulesActicityBtn.setOnClickListener{
            val intent = Intent(this,RegelView::class.java)
            startActivity(intent)

        }
    }
}