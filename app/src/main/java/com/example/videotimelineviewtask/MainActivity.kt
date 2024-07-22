package com.example.videotimelineviewtask

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.videotimelineviewtask.ui.VideoEditFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, VideoEditFragment())
                .commit()
        }

    }
}