package com.example.audiotrim

import android.content.ContentValues
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.example.audiotrim.databinding.ActivityRecorderBinding
import java.io.IOException

class RecorderActivity : AppCompatActivity() {

    lateinit var binding: ActivityRecorderBinding

    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var outputFile: String? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRecorderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvRecording.text = "Start Recording"


        binding.btnRecord.setOnClickListener {

            if(isRecording) {
                stopRecording()
            }
            else{
                startRecording()
            }
        }






    }

    private fun startRecording() {


        try {

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

                Toast.makeText(applicationContext, "apply", Toast.LENGTH_SHORT).show()


//                outputFile = Environment.getExternalStorageDirectory().absolutePath +
//                        "/${System.currentTimeMillis()}.3gp"

                // Use MediaStore to store the audio recording
                val audioCollection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val values = ContentValues().apply {
                    put(MediaStore.Audio.Media.DISPLAY_NAME, "recording.3gp") // Replace with your desired filename and extension
                    put(MediaStore.Audio.Media.MIME_TYPE, "audio/3gpp") // Replace with the appropriate mime type
                    put(MediaStore.Audio.Media.IS_PENDING, 1)
                }
                val item = contentResolver.insert(audioCollection, values)
                outputFile = item?.toString() ?: ""


                Toast.makeText(applicationContext, "output file", Toast.LENGTH_SHORT).show()


                setOutputFile(outputFile)
                prepare()
                start()
                Toast.makeText(applicationContext, "start", Toast.LENGTH_SHORT).show()

            }
            binding.tvRecording.text = "Stop Recording"
            isRecording = true
            binding.btnRecord.setImageResource(R.drawable.baseline_stop_24)

        }
        catch (e:IOException) {
            Toast.makeText(this, "Exception: $e", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopRecording() {
        binding.tvRecording.text = "Start Recording"
        isRecording = false
        binding.btnRecord.setImageResource(R.drawable.baseline_mic_24)


    }


}