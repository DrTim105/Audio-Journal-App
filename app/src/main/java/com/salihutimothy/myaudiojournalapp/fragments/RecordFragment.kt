package com.salihutimothy.myaudiojournalapp.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.Chronometer
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.melnykov.fab.FloatingActionButton
import com.salihutimothy.myaudiojournalapp.R
import com.salihutimothy.myaudiojournalapp.WaveformView
import com.salihutimothy.myaudiojournalapp.services.RecordingService
import com.salihutimothy.myaudiojournalapp.services.RecordingService.Companion.maxAmplitude
import java.io.File
import java.util.*


class RecordFragment : Fragment() {

    private lateinit var chronometer: Chronometer
    private lateinit var recordingStatus: TextView
    private lateinit var recordButton: FloatingActionButton
    private lateinit var pauseButton: Button
    private lateinit var listButton: Button
    private lateinit var waveformView: WaveformView
    private lateinit var timer: Timer

    private lateinit var navController: NavController

    private var mStartRecording = true
    private var mPauseRecording = true
    private var timeWhenPaused = 0L

    private val recordPermission: String = Manifest.permission.RECORD_AUDIO

    @RequiresApi(Build.VERSION_CODES.P)
    private val foregroundPermission: String = Manifest.permission.FOREGROUND_SERVICE

    @RequiresApi(Build.VERSION_CODES.P)
    var permissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.FOREGROUND_SERVICE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
    )

    private val PERMISSION_CODE = 21

    companion object {

        fun newInstance() =
            RecordFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        checkPermissions()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_record, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pauseButton = view.findViewById(R.id.btnPause) as Button
        listButton = view.findViewById(R.id.btnList) as Button
        recordButton = view.findViewById(R.id.btnRecord) as FloatingActionButton
        pauseButton = view.findViewById(R.id.btnPause) as Button
        waveformView = view.findViewById(R.id.waveformView) as WaveformView
        navController = Navigation.findNavController(view)

        pauseButton.visibility = View.GONE
        recordButton.colorPressed = resources.getColor(R.color.background_tab_pressed)

        recordButton.setOnClickListener {
            onRecord(mStartRecording)
            mStartRecording = !mStartRecording

        }

        listButton.setOnClickListener {
            navController.navigate(R.id.action_recordFragment_to_audioListFragment);
        }

    }

    private fun onRecord(start: Boolean) {
        val intent = Intent(context, RecordingService::class.java)
        recordButton = requireView().findViewById(R.id.btnRecord) as FloatingActionButton
        recordingStatus = requireView().findViewById(R.id.recording_status_txt) as TextView
        chronometer = requireView().findViewById(R.id.chronometer) as Chronometer
        waveformView = requireView().findViewById(R.id.waveformView) as WaveformView

        if (start) {
            // check permission to record audio
            if (
                checkPermissions()
            ) {
                recordButton.setImageResource(R.drawable.ic_stop)
                Toast.makeText(context, "Recording started", Toast.LENGTH_LONG).show()

                val folder = File(context?.getExternalFilesDir(null).toString() + "/MySoundRec")

                if (!folder.exists()) {
                    folder.mkdir()
                }

                chronometer.base = SystemClock.elapsedRealtime()
//                    chronometer.format = "00:%s"
                chronometer.start()

                var amp = 1000f

                chronometer.setOnChronometerTickListener {

                    Log.d("TAG", "Chronometer $amp: recorder amplitude: $maxAmplitude ")
                    amp += 100


                }
                timer = Timer()
                timer.schedule(object : TimerTask() {
                    override fun run() {
                        waveformView.addAmplitude(maxAmplitude)
                    }
                }, 0, 100)

                activity?.startService(intent)

                activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

                recordingStatus.text = "Recording..."
            }


        } else {
            recordButton.setImageResource(R.drawable.ic_placeholder)
            chronometer.stop()
            timer.cancel()
            waveformView.clear()
            chronometer.base = SystemClock.elapsedRealtime()
            timeWhenPaused = 0
            recordingStatus.text = "Tap the Button to start recording"

            activity?.stopService(intent)
        }
    }

    private fun checkPermissions(): Boolean {
        //Check permission
        Log.d("permissions", "checking permission")

        var result: Int
        val listPermissionsNeeded: MutableList<String> = ArrayList()
        for (p in permissions) {
            result = ActivityCompat.checkSelfPermission(requireContext(), p)
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p)
            }
        }

        if (listPermissionsNeeded.isNotEmpty()) {
            Log.d("permissions", "some permission not granted: ${listPermissionsNeeded.toString()}")

            ActivityCompat.requestPermissions(
                requireActivity(),
//                listPermissionsNeeded.toArray(new String [listPermissionsNeeded.size()]),
                (listPermissionsNeeded as List<String>).toTypedArray(),
                PERMISSION_CODE
            )

            onRecord(true)


            return false
        }

        return true

//        return if (ActivityCompat.checkSelfPermission(
//                requireContext(),
//                recordPermission,
//            ) == PackageManager.PERMISSION_GRANTED
//
//            && ActivityCompat.checkSelfPermission(
//                requireContext(),
//                foregroundPermission,
//            ) == PackageManager.PERMISSION_GRANTED
//         )  {
//            //Permission Granted
//            true
//        } else {
//            //Permission not granted, ask for permission
//            ActivityCompat.requestPermissions(
//                requireActivity(),
//                arrayOf(recordPermission, foregroundPermission),
//                PERMISSION_CODE
//            )
//            false
//        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissionsList: Array<String>,
        grantResults: IntArray
    ) {
        Log.d("permissions", "requesting permission")
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty()) {
                    var permissionsDenied = ""
                    for (per in permissionsList) {
                        if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                            permissionsDenied += """
                            
                            $per
                            """.trimIndent()
                        }
                    }
                    // Show permissionsDenied

                    if (permissionsDenied == ""){
                        onRecord(true)
                    }
                }
                return
            }
        }
    }
}