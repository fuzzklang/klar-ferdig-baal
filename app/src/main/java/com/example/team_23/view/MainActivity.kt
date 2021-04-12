package com.example.team_23.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.team_23.R
import com.example.team_23.viewmodel.KartViewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val viewModel = KartViewModel()
        viewModel.hentVarsler()
    }
}