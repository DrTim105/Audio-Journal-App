package com.salihutimothy.myaudiojournalapp.fragments

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.Chronometer
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.melnykov.fab.FloatingActionButton
import com.salihutimothy.myaudiojournalapp.R
import com.salihutimothy.myaudiojournalapp.services.RecordingService
import java.io.File
import androidx.core.app.ActivityCompat

import android.content.pm.PackageManager




class RecordFragment : Fragment() {

    private lateinit var chronometer: Chronometer
    private lateinit var recordingStatus : TextView
    private lateinit var recordButton : FloatingActionButton
    private lateinit var pauseButton : Button

    private var mStartRecording = true
    private var mPauseRecording = true
    private var timeWhenPaused = 0L

    private val recordPermission: String = Manifest.permission.RECORD_AUDIO
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
        recordButton = view.findViewById(R.id.btnRecord) as FloatingActionButton
        pauseButton = view.findViewById(R.id.btnPause) as Button

        pauseButton.visibility = View.GONE
        recordButton.colorPressed = resources.getColor(R.color.background_tab_pressed)

        recordButton.setOnClickListener{
            onRecord(mStartRecording)
            mStartRecording = !mStartRecording

        }
    }

    private fun onRecord(start: Boolean) {
        val intent = Intent(context, RecordingService::class.java)
        recordButton = requireView().findViewById(R.id.btnRecord) as FloatingActionButton
        recordingStatus = requireView().findViewById(R.id.recording_status_txt) as TextView
        chronometer = requireView().findViewById(R.id.chronometer) as Chronometer

        if (start) {
            // check permission to record audio
                if (checkPermissions()) {
                    recordButton.setImageResource(R.drawable.ic_stop)
                    Toast.makeText(context, "Recording started", Toast.LENGTH_LONG).show()

                    val folder = File(context?.getExternalFilesDir(null).toString() + "/MySoundRec")

                    if (!folder.exists()) {
                        folder.mkdir()
                    }

                    chronometer.base = SystemClock.elapsedRealtime()
                    chronometer.start()

                    activity?.startService(intent)

                    activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

                    recordingStatus.text = "Recording..."
                }


        } else {
            recordButton.setImageResource(R.drawable.ic_placeholder)
            chronometer.stop()
            chronometer.base = SystemClock.elapsedRealtime()
            timeWhenPaused = 0
            recordingStatus.text = "Tap the Button to start recording"

            activity?.stopService(intent)
        }
    }

    private fun checkPermissions(): Boolean {
        //Check permission
        return if (ActivityCompat.checkSelfPermission(
                requireContext(),
                recordPermission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            //Permission Granted
            true
        } else {
            //Permission not granted, ask for permission
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(recordPermission),
                PERMISSION_CODE
            )
            false
        }
    }
}