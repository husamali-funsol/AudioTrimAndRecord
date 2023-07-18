package com.example.audiotrim

import AudioFileContract
import android.content.ContentResolver
import android.content.ContentValues
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.arthenica.mobileffmpeg.FFmpeg
import com.arthenica.mobileffmpeg.FFmpegExecution
import com.example.audiotrim.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var mediaPlayer: MediaPlayer
    private lateinit var audioUri: Uri

    private var startPosition = 0
    private var endPosition = 0

    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private var isReadPermissionGranted = false
    private var isLocationPermissionGranted = false
    private var isRecordPermissionGranted = false

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

        binding.button.setOnClickListener {
            Toast.makeText(this, "${binding.rangeSlider.values.get(0).toString()} , ${binding.rangeSlider.valueFrom} , ${binding.rangeSlider.valueTo}", Toast.LENGTH_SHORT).show()
        }

        binding.rangeSlider.addOnChangeListener { slider, value, fromUser ->
            if (fromUser) {
                val start = binding.rangeSlider.values.get(0)
                val end = binding.rangeSlider.values.get(1)

                // Calculate the playback position in milliseconds
                val audioDuration = mediaPlayer.duration
                startPosition = (audioDuration * start / 100).toInt()
                endPosition = (audioDuration * end / 100).toInt()

                if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
                    // Seek to the new playback position
                    mediaPlayer.seekTo(startPosition)
                }
            }
        }

        binding.trimButton.setOnClickListener {
            if (::mediaPlayer.isInitialized) {
                // Stop the MediaPlayer if it's playing
                mediaPlayer.stop()
            }

            trimAudio()
        }
    }

    private fun requestPermissions() {
        isReadPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        isLocationPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        isRecordPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

        val permissions : MutableList<String> = ArrayList()
    }

    private fun trimAudio() {
        val audioFilePath = audioUri.path // Get the path of the selected audio file
        val outputUri = getOutputFilePath() // Get the Uri of the trimmed output audio file

        // FFmpeg command to trim the audio
        val cmd = arrayOf(
            "-i", audioFilePath,
            "-ss", (startPosition/1000).toString(), // Replace with the start time in seconds (e.g., "10")
            "-to", (endPosition/1000).toString(), // Replace with the end time in seconds (e.g., "30")
            "-c", "copy",
            outputUri.path // Use the path from the output Uri instead of file path
        )



        FFmpeg.executeAsync(cmd){
            executionId, returnCode -> Log.d("hello", "return  $returnCode")
            Log.d("hello", "executionID  $executionId")
            Log.d("hello", "FFMPEG  " + FFmpegExecution(executionId, cmd))
        }

    }




    private fun getOutputFilePath(): Uri {
        val resolver: ContentResolver = contentResolver
        val audioCollection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, "trimmed_audio.mp3") // Replace with your desired filename and extension
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg") // Replace with the appropriate mime type
            put(MediaStore.Audio.Media.IS_PENDING, 1)
        }

        val item = resolver.insert(audioCollection, values)

        // After writing the file, set IS_PENDING to 0 so it can be scanned
        values.clear()
        values.put(MediaStore.Audio.Media.IS_PENDING, 0)
        resolver.update(item!!, values, null, null)

        return item
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

                setOnPreparedListener { mp ->
                    val durationMillis = mp.duration
                    val startTime = 0.0f // Start time in milliseconds
                    val endTime = durationMillis.toFloat() // End time in milliseconds

                    // You can convert the millisecond values to a more readable format if needed
                    val startTimeInSeconds = ((startTime / 1000.0f)/60.0f).toFloat()
                    val endTimeInSeconds = ((endTime / 1000.0f)/60.0f).toFloat()


                    Toast.makeText(this@MainActivity, "Start Time: ${binding.rangeSlider.valueFrom} minutes", Toast.LENGTH_SHORT).show()

                    binding.start.text = startTimeInSeconds.toString()
                    binding.end.text = endTimeInSeconds.toString()

                    binding.rangeSlider.values = listOf(startTimeInSeconds, endTimeInSeconds)

                    binding.rangeSlider.valueFrom = startTimeInSeconds
                    binding.rangeSlider.valueTo = endTimeInSeconds

                    mediaPlayer.setOnCompletionListener {

                        mediaPlayer.pause()
                    }
                }
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

    fun clickOnRecorder(view: View) {
        val intent  = Intent(this, RecorderActivity::class.java)
        startActivity(intent)


    }
}
