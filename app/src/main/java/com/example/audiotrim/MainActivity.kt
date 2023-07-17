package com.example.audiotrim

import AudioFileContract
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.audiotrim.databinding.ActivityMainBinding
import kotlinx.coroutines.NonCancellable.start
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var mediaPlayer: MediaPlayer
    private lateinit var audioUri: Uri


//    private val audioFileLauncher = registerForActivityResult(AudioFileContract()) { audioUri: Uri? ->
//        if (audioUri != null) {
//            createMediaPlayer(audioUri)
//            Toast.makeText(this, "URI: $audioUri", Toast.LENGTH_SHORT).show()
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btn.setOnClickListener {
            audioFileLauncher.launch("audio/*")
        }

        binding.play.setOnClickListener {
            if (::mediaPlayer.isInitialized) {
                mediaPlayer.start()
            }
        }


    }

    private val audioFileLauncher = registerForActivityResult(AudioFileContract()) { uri: Uri? ->
        if (uri != null) {
            audioUri = uri
            createMediaPlayer(uri)
            Toast.makeText(this, "URI: $audioUri", Toast.LENGTH_SHORT).show()

            binding.textView.text = getFileNameFromUri(uri)
        }
    }

    private fun createMediaPlayer(uri: Uri) {

        if (::mediaPlayer.isInitialized) {
            // Release the previous MediaPlayer instance before creating a new one
            mediaPlayer.release()
        }
            mediaPlayer = MediaPlayer().apply {
            setDataSource(applicationContext, uri)
            prepareAsync()
        }
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayNameIndex = it.getColumnIndex(android.provider.MediaStore.MediaColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    return it.getString(displayNameIndex)
                }
            }
        }
        return null
    }
}
