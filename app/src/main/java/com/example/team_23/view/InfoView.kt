package com.example.team_23.view


import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.team_23.R


class InfoView : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        val scrollView = ScrollView(this)
        val layoutParams = ConstraintLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT)
        scrollView.layoutParams = layoutParams

        val returnButton = findViewById<ImageButton>(R.id.returnButton)

        returnButton.setOnClickListener {
            onBackPressed()
        }




    }
}