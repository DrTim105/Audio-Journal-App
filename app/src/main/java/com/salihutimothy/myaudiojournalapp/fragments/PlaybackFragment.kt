package com.salihutimothy.myaudiojournalapp.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.ColorFilter
import android.graphics.LightingColorFilter
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.Window
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.melnykov.fab.FloatingActionButton
import com.salihutimothy.myaudiojournalapp.R
import com.salihutimothy.myaudiojournalapp.entities.RecordingItem
import java.io.IOException
import java.util.concurrent.TimeUnit

class PlaybackFragment : DialogFragment() {

    private lateinit var fileName : TextView
    private lateinit var fileLength : TextView
    private lateinit var currentProgress : TextView
    private lateinit var seekBar: SeekBar
    private lateinit var faButton : FloatingActionButton

    private lateinit var item : RecordingItem
    private var mediaPlayer: MediaPlayer = MediaPlayer()
    private lateinit var handler : Handler

    private val isPlaying = false
    var minutes: Long = 0
    var seconds: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        item = (requireArguments().getSerializable("item") as RecordingItem)

        minutes = TimeUnit.MILLISECONDS.toMinutes(item.length)
        seconds =
            TimeUnit.MILLISECONDS.toSeconds(item.length) - TimeUnit.MINUTES.toSeconds(minutes)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val builder = AlertDialog.Builder(activity)
        val view: View = requireActivity().layoutInflater.inflate(R.layout.fragment_playback, null)


        builder.setView(view)

        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        return builder.create()

    }

}