package com.fonda.cobauas

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import kotlinx.coroutines.delay

class SplashScreen : AppCompatActivity() {

    lateinit var handler : Handler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        handler = Handler()
        handler.postDelayed(
            {
                val intent = Intent(this,Login::class.java)
                startActivity(intent)
                finish()
            },3000 //untuk memberi jeda 3 detik sebelom pindah page
        )
    }
}