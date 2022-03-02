package com.salihutimothy.myaudiojournalapp.services

import android.Manifest
import android.app.Service
import android.content.Intent
import android.media.MediaRecorder
import android.media.audiofx.NoiseSuppressor
import android.os.IBinder
import android.widget.Toast
import com.salihutimothy.myaudiojournalapp.database.DBHelper
import com.salihutimothy.myaudiojournalapp.entities.RecordingItem
import java.io.File
import java.io.IOException
import java.util.*

class RecordingService : Service() {

    private var mediaRecorder : MediaRecorder = MediaRecorder()
    var mStartingTimeMillis:kotlin.Long = 0
    var mElapsedMillis: Long = 0
    private lateinit var file : File
    private lateinit var dbHelper: DBHelper
    var fileName : String? = null
    private lateinit var timer: Timer

    private val isRecording = false

    private val recordPermission: String = Manifest.permission.RECORD_AUDIO
    private val PERMISSION_CODE = 21

    companion object {
        var maxAmplitude = 0f
    }

    override fun onCreate() {
        super.onCreate()
        dbHelper = DBHelper(applicationContext)
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        startRecording()
        return START_STICKY
    }

    private fun startRecording(){
        val timeStampLong = System.currentTimeMillis() / 1000
        val timeStamp = timeStampLong.toString()
        timer = Timer()

        fileName = "Mental Note $timeStamp"

        file = File(
            applicationContext.getExternalFilesDir(null)
                .toString() + "/MySoundRec/" + fileName + ".mp3"
        )

        mediaRecorder = MediaRecorder()
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder.setOutputFile(file.absolutePath)
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder.setAudioChannels(1)



        try {
            mediaRecorder.prepare()
            mediaRecorder.start()
            mStartingTimeMillis = System.currentTimeMillis()
//            maxAmplitude = 1000f
            timer.schedule(object : TimerTask() {
                override fun run() {
                    maxAmplitude = mediaRecorder.maxAmplitude.toFloat()
                }
            }, 0, 100) //wait 0 ms before doing the action and do it evry 1000ms (1second)


        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun stopRecording() {
        timer.cancel()
        mediaRecorder.stop()
        mElapsedMillis = (System.currentTimeMillis() - mStartingTimeMillis)
        mediaRecorder.release()
        Toast.makeText(applicationContext, "Recording stopped ${file.absolutePath}", Toast.LENGTH_LONG).show()

        // add to database
        val recordingItem =
            RecordingItem(fileName, file.absolutePath, mElapsedMillis, System.currentTimeMillis())

        dbHelper.addRecording(recordingItem)
    }

    override fun onDestroy() {
        stopRecording()
        super.onDestroy()
    }
}