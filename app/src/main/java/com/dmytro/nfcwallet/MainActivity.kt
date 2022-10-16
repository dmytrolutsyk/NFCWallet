package com.dmytro.nfcwallet

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    lateinit var readButton : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        readButton = findViewById(R.id.button)
        val nfcIntent : Intent =   Intent(this,NfcReaderActivity::class.java)
        readButton.setOnClickListener {
            startActivity(nfcIntent)
        }

    }
}