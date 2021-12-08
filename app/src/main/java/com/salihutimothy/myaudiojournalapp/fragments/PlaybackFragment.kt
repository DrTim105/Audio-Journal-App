package com.salihutimothy.myaudiojournalapp.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.ColorFilter
import android.graphics.LightingColorFilter
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnPreparedListener
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.melnykov.fab.FloatingActionButton
import com.salihutimothy.myaudiojournalapp.R
import com.salihutimothy.myaudiojournalapp.entities.RecordingItem
import java.io.IOException
import java.util.concurrent.TimeUnit

class PlaybackFragment : DialogFragment() {

    private lateinit var fileName: TextView
    private lateinit var fileLength: TextView
    private lateinit var currentProgress: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var faButton: FloatingActionButton

    private lateinit var item: RecordingItem
    private var mediaPlayer: MediaPlayer? = MediaPlayer()
    private var handler: Handler = Handler(Looper.myLooper()!!)


    private var isPlaying = false
    var minutes: Long = 0
    var seconds: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        item = (requireArguments().getSerializable("item") as RecordingItem)

        minutes = TimeUnit.MILLISECONDS.toMinutes(item.length)
        seconds =
            TimeUnit.MILLISECONDS.toSeconds(item.length) - TimeUnit.MINUTES.toSeconds(minutes)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_playback, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        faButton = view.findViewById(R.id.fab_play) as FloatingActionButton
        fileName = view.findViewById(R.id.file_name_text) as TextView
        fileLength = view.findViewById(R.id.file_length_text_view) as TextView
        currentProgress = view.findViewById(R.id.current_progress_text_view) as TextView
        seekBar = view.findViewById(R.id.seekbar) as SeekBar

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val builder = AlertDialog.Builder(activity)
        val view: View = requireActivity().layoutInflater.inflate(R.layout.fragment_playback, null)
        faButton = requireActivity().findViewById(R.id.fab_play) as FloatingActionButton
        fileName = requireActivity().findViewById(R.id.file_name_text) as TextView
        fileLength = requireActivity().findViewById(R.id.file_length_text_view) as TextView

        setSeekbarValues()

        faButton.setOnClickListener(View.OnClickListener {
            try {
                onPlay(isPlaying)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            isPlaying = !isPlaying
        })


        fileName.text = item.name
        fileLength.text = String.format("%02d:%02d", minutes, seconds)



        builder.setView(view)

        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        return builder.create()

    }

    private fun setSeekbarValues() {
        currentProgress = requireActivity().findViewById(R.id.current_progress_text_view)
        seekBar = requireActivity().findViewById(R.id.seekbar)

//        val colorFilter: ColorFilter = LightingColorFilter(
//            resources.getColor(R.color.colorPrimary),
//            resources.getColor(R.color.colorPrimary)
//        )
//        seekBar.progressDrawable.colorFilter = colorFilter
//        seekBar.thumb.colorFilter = colorFilter
        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer!!.seekTo(progress)
                    handler.removeCallbacks(mRunnable)
                    val minutes =
                        TimeUnit.MILLISECONDS.toMinutes(mediaPlayer!!.currentPosition.toLong())
                    val seconds =
                        (TimeUnit.MILLISECONDS.toSeconds(mediaPlayer!!.currentPosition.toLong())
                                - TimeUnit.MINUTES.toSeconds(minutes))
                    currentProgress.text = String.format("%02d:%02d", minutes, seconds)
                    updateSeekbar()

                } else if (mediaPlayer == null && fromUser) {
                    try {
                        prepareMediaPlayerFromPoint(progress)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    updateSeekbar()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    @Throws(IOException::class)
    private fun onPlay(isPlaying: Boolean) {
        if (!isPlaying) {
            if (mediaPlayer == null) {
                startPlaying()
            }
        } else {
            pausePlaying()
        }
    }

    private fun pausePlaying() {
        faButton = requireActivity().findViewById(R.id.fab_play)

        faButton.setImageResource(R.drawable.ic_placeholder)
        handler.removeCallbacks(mRunnable)
        mediaPlayer?.pause()
    }

    @Throws(IOException::class)
    private fun startPlaying() {
        faButton = requireActivity().findViewById(R.id.fab_play)
        seekBar = requireActivity().findViewById(R.id.seekbar)

        faButton.setImageResource(R.drawable.ic_placeholder)
        mediaPlayer = MediaPlayer()
        mediaPlayer!!.setDataSource(item.path)
        mediaPlayer!!.prepare()
        seekBar.max = mediaPlayer!!.duration
        mediaPlayer!!.setOnPreparedListener { mediaPlayer!!.start() }
        mediaPlayer!!.setOnCompletionListener { stopPlaying() }
        updateSeekbar()
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    @Throws(IOException::class)
    private fun prepareMediaPlayerFromPoint(progress: Int) {
        mediaPlayer = MediaPlayer()
        mediaPlayer!!.setDataSource(item.path)
        mediaPlayer!!.prepare()
        seekBar.max = mediaPlayer!!.duration
        mediaPlayer!!.seekTo(progress)
        mediaPlayer!!.setOnCompletionListener { stopPlaying() }
    }

    private fun stopPlaying() {
        faButton = requireActivity().findViewById(R.id.fab_play)
        currentProgress = requireActivity().findViewById(R.id.current_progress_text_view)
        fileLength = requireActivity().findViewById(R.id.file_length_text_view)
        seekBar = requireActivity().findViewById(R.id.seekbar)


        faButton.setImageResource(R.drawable.ic_placeholder)
        handler.removeCallbacks(mRunnable)
        mediaPlayer?.stop()
        mediaPlayer?.reset()
        mediaPlayer?.release()
        mediaPlayer = null
        seekBar.progress = seekBar.max
        isPlaying = !isPlaying
        currentProgress.text = fileLength.getText()
        seekBar.progress = seekBar.max
    }

    private val mRunnable = Runnable {
        currentProgress = requireActivity().findViewById(R.id.current_progress_text_view)
        seekBar = requireActivity().findViewById(R.id.seekbar)

        if (mediaPlayer != null) {
            val mCurrentPosition = mediaPlayer!!.currentPosition
            seekBar.progress = mCurrentPosition
            val minutes = TimeUnit.MILLISECONDS.toMinutes(mCurrentPosition.toLong())
            val seconds = (TimeUnit.MILLISECONDS.toSeconds(mCurrentPosition.toLong())
                    - TimeUnit.MINUTES.toSeconds(minutes))
            currentProgress.text = String.format("%02d:%02d", minutes, seconds)
            updateSeekbar()
        }
    }

    private fun updateSeekbar() {
        handler.postDelayed(mRunnable, 1000)
    }
}