package com.salihutimothy.myaudiojournalapp.services

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.salihutimothy.myaudiojournalapp.MainActivity
import com.salihutimothy.myaudiojournalapp.R
import com.salihutimothy.myaudiojournalapp.database.Constants.mentalNote
import com.salihutimothy.myaudiojournalapp.database.DBHelper
import com.salihutimothy.myaudiojournalapp.entities.RecordingItem
import java.io.File
import java.io.IOException
import java.util.*


class RecordingService : Service() {

    private var mediaRecorder: MediaRecorder = MediaRecorder()
    var mStartingTimeMillis: kotlin.Long = 0
    var mElapsedMillis: Long = 0
    private lateinit var file: File
    private lateinit var dbHelper: DBHelper
    var fileName: String? = null
    private lateinit var timer: Timer

    private val isRecording = false
    private var promptName: String? = null

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

        var notification = createNotification()
        startForeground(1, notification)

        if (intent != null) {
            val bundle = intent.extras
            if (bundle != null) {
                promptName = bundle.getString("prompt")
            }
        }

        Log.d("TAG", "promptname 3 $promptName")
        startRecording()
        return START_STICKY
    }

    private fun createNotification(): Notification {
        val notificationChannelId = "ENDLESS SERVICE CHANNEL"

        // depending on the Android API that we're dealing with we will have
        // to use a specific method to create the notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager;
            val channel = NotificationChannel(
                notificationChannelId,
                "Audio Journal notifications channel",
                NotificationManager.IMPORTANCE_NONE
            ).let {
                it.description = "Audio Journal channel"
                it.enableLights(true)
                it.lightColor = Color.RED
                it.enableVibration(false)
                it
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationIntent = newLauncherIntent(this)

        val intent = PendingIntent.getActivity(
            this, 0,
            notificationIntent, 0
        )
//        val pendingIntent: PendingIntent = Intent(this, MainActivity::class.java).let { notificationIntent ->
//            PendingIntent.getActivity(this, 0, notificationIntent, 0)
//        }

        val builder: Notification.Builder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(
                this,
                notificationChannelId
            ) else Notification.Builder(this)

        return builder
            .setContentTitle("Audio Journal")
            .setContentText("Recording...")
            .setContentIntent(intent)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_mic)
            .build()
    }

    private fun newLauncherIntent(context: Context?): Intent? {
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.action = Intent.ACTION_MAIN
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        return intent
    }

    private fun startRecording() {
        val timeStampLong = System.currentTimeMillis() / 1000
        val timeStamp = timeStampLong.toString()
        timer = Timer()

        fileName = if (promptName == null) {
            "mental note note note note note note note note noteeeeeeeeeee"
//            + mentalNote.toString().padStart(3, '0')
        } else {
            promptName
        }

        val outputFile = File(
            applicationContext.getExternalFilesDir(null)
                .toString()
                    + "/MySoundRec/"
        )

        if (!outputFile.exists()) {
            outputFile.mkdirs()
        }

//        val file: String =
//            applicationContext.getExternalFilesDir("/")!!.absolutePath +
//                    "/MySoundRec/" + fileName + ".mp3"

//        val root = Environment.getExternalStorageDirectory().toString()
//        val outputFile = File("$root/Audio Journal")
//        if (!outputFile.exists()) {
//            outputFile.mkdirs()
//        }
//
        file = File("$outputFile/$fileName.mp3")

        mediaRecorder = MediaRecorder()
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder.setOutputFile(file.absolutePath)
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder.setAudioEncodingBitRate(384000)
        mediaRecorder.setAudioSamplingRate(44100)
        mediaRecorder.setAudioChannels(1)

        Log.d("Storage - start service", "File path : ${file} ")

        try {
            mediaRecorder.prepare()
            mediaRecorder.start()
            mStartingTimeMillis = System.currentTimeMillis()
//            maxAmplitude = 1000f

            timer.schedule(object : TimerTask() {
                override fun run() {
                    maxAmplitude = mediaRecorder.maxAmplitude.toFloat()
                    Log.d("RecordingService", "timer still running $maxAmplitude")

                }
            }, 0, 80) //wait 0 ms before doing the action and do it evry 1000ms (1second)


        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun stopRecording() {

        try {
            mediaRecorder.stop()
            mElapsedMillis = (System.currentTimeMillis() - mStartingTimeMillis)

            // add to database
            val recordingItem =
                RecordingItem(
                    fileName,
                    file.absolutePath,
                    mElapsedMillis,
                    System.currentTimeMillis()
                )
            Log.d("Storage - stop service", "File path : ${file.absolutePath} ")


            dbHelper.addRecording(recordingItem)
            mentalNote++

            mediaRecorder.reset()
            mediaRecorder.release()

        } catch (e: RuntimeException) {
            // handle cleanup here
            Toast.makeText(
                applicationContext,
                "Unable to save recording",
                Toast.LENGTH_SHORT
            ).show()
            e.printStackTrace()


        }
    }

    override fun onDestroy() {
        timer.cancel()
        stopRecording()
        super.onDestroy()
    }
}