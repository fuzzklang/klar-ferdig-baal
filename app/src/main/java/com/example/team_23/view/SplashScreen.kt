package com.example.team_23.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.team_23.R
/*
*Kode hentet fra GeeksForGeeks: https://www.geeksforgeeks.org/how-to-create-a-splash-screen-in-android-using-kotlin/
*/
//Er avhengig av @Suppress for at koden skal fungere
@Suppress("DEPRECATION")
class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        // This is used to hide the status bar and make
        // the splash screen as a full screen activity.
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // we used the postDelayed(Runnable, time) method
        // to send a message with a delayed time.
        Handler().postDelayed({
            val intent = Intent(this, KartActivity::class.java)
            startActivity(intent)
            finish()
        }, 1500) // 3000 is the delayed time in milliseconds.
    }
}