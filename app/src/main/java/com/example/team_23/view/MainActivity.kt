package com.example.team_23.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.Observer
import com.example.team_23.R
import com.example.team_23.model.MainRepository
import com.example.team_23.model.api.ApiServiceImpl
import com.example.team_23.model.api.dataclasses.Alert
import com.example.team_23.viewmodel.KartViewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val apiService = ApiServiceImpl()
        val repo = MainRepository(apiService)  // Flytt instansiering til Factory eller tilsvarende?
        val viewModel = KartViewModel(repo)

        val textView = findViewById<TextView>(R.id.myTextView)

        viewModel.varsler.observe(this, Observer<List<Alert>> { varselListe ->
            var str = ""
            varselListe.forEach {
                textView.text = it.infoItemsNo[0].instruction
                str +=  "${it.identifier}\n\n"
                        /*"${it.msgType}\n" +
                        "${it.infoItemsNo[0].event}\n" +
                        "${it.infoItemsNo[0].instruction}\n\n"*/
            }
            textView.text = str
        })

        val btn = findViewById<Button>(R.id.btn)
        btn.setOnClickListener {
            textView.text = "Henter varsler"
            viewModel.hentAlleVarsler()
        }
    }
}