package com.example.team_23.view

import androidx.appcompat.app.AppCompatActivity
import com.example.team_23.R


import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Button
import android.widget.ImageButton
import android.widget.ScrollView
import androidx.constraintlayout.widget.ConstraintLayout


class RegelView : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rules)

        val scrollView = ScrollView(this)
        val layoutParams = ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
        scrollView.layoutParams = layoutParams

        val returnButton = findViewById(R.id.returnButton) as ImageButton

        returnButton.setOnClickListener {
            onBackPressed()
        }




    }
}