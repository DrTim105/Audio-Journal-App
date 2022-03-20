package com.salihutimothy.myaudiojournalapp.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.Chronometer
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
        checkrequestPermissions()
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
                Log.d("RecordFragment", "onREcord - start record")
                mStartRecording = !mStartRecording

                recordButton.setImageResource(R.drawable.ic_stop)
                Toast.makeText(context, "Recording started", Toast.LENGTH_SHORT).show()

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
            } else {
                Log.d("RecordFragment", "onRecord - request permissions")
                requestPermissions()
            }


        } else {
            Log.d("RecordFragment", "onRecord - stop record")
            mStartRecording = !mStartRecording

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

    var listPermissionsNeeded: MutableList<String> = ArrayList()

    private fun checkrequestPermissions() {
        var result: Int
        for (p in permissions) {
            result = ActivityCompat.checkSelfPermission(requireContext(), p)
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p)
            }
        }

        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                (listPermissionsNeeded as List<String>).toTypedArray(),
                PERMISSION_CODE
            )
        }

        // reset the permission array
        listPermissionsNeeded = ArrayList()
    }

    private fun checkPermissions(): Boolean {
        //Check permission
        Log.d("RecordFragment", "permissions - checking permission")

        var result: Int
        for (p in permissions) {
            result = ActivityCompat.checkSelfPermission(requireContext(), p)
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p)
            }
        }

        if (listPermissionsNeeded.isNotEmpty()) {
            Log.d(
                "RecordFragment",
                "permissions - some permission not granted: ${listPermissionsNeeded.toString()}"
            )
            return false
        }

        return true
    }

    private fun requestPermissions() {
        Log.d(
            "RecordFragment",
            "permissions - requesting permissions: ${listPermissionsNeeded.toString()}"
        )

        checkrequestPermissions()

        checkPermissions()

        for (p in listPermissionsNeeded) {
            mPermissionResult.launch(p)
        }
    }

    private val mPermissionResult = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { result ->

        when {
            result -> {
                Log.e(ContentValues.TAG, "onActivityResult: PERMISSION GRANTED")
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) -> {

            }
            else -> {
                Log.e(ContentValues.TAG, "onActivityResult: PERMISSION DENIED")
                AlertDialog.Builder(requireContext())
                    .setTitle("Permissions Required")
                    .setMessage("This app may not work correctly without the requested permission. Open the app settings screen to modify app permissions.")
                    .setPositiveButton("Settings") { _, _ ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", requireContext().packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }
                    .setNegativeButton("Cancel") { dialog, which -> }
                    .create()
                    .show()
            }
        }

        listPermissionsNeeded = ArrayList()

    }
}