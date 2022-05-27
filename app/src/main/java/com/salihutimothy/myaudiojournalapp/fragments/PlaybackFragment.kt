package com.salihutimothy.myaudiojournalapp.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.salihutimothy.myaudiojournalapp.R
import com.salihutimothy.myaudiojournalapp.adapters.FileAdapter
import com.salihutimothy.myaudiojournalapp.entities.RecordingItem
import java.io.IOException
import java.util.concurrent.TimeUnit
import android.media.AudioRecord

import android.media.MediaRecorder
import androidx.core.app.ActivityCompat


class PlaybackFragment : Fragment(), FileAdapter.OnItemListClick {

    private lateinit var fileName: TextView
    private lateinit var fileLength: TextView
    private lateinit var currentProgress: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var playButton: ImageView
    private lateinit var forwardButton: ImageView
    private lateinit var backwardButton: ImageView

    private lateinit var item: RecordingItem
    private var mediaPlayer: MediaPlayer? = MediaPlayer()
    private var handler: Handler = Handler(Looper.myLooper()!!)

    private lateinit var fileAdapter: FileAdapter

    private var isPlaying = false
    var minutes: Long = 0
    var seconds: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_playback, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playButton = view.findViewById(R.id.play_iv) as ImageView
        backwardButton = view.findViewById(R.id.iv_backward) as ImageView
        forwardButton = view.findViewById(R.id.iv_forward) as ImageView
        fileName = view.findViewById(R.id.file_name_text_view) as TextView
        fileLength = view.findViewById(R.id.file_length_text_view) as TextView
        currentProgress = view.findViewById(R.id.current_progress_text_view) as TextView
        seekBar = view.findViewById(R.id.seekbar) as SeekBar

        minutes = TimeUnit.MILLISECONDS.toMinutes(item.length)
        seconds =
            TimeUnit.MILLISECONDS.toSeconds(item.length) - TimeUnit.MINUTES.toSeconds(minutes)

        setSeekbarValues(view)



        playButton.setOnClickListener {
//            try {
//                onPlay(isPlaying)
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//            isPlaying = !isPlaying

            if (isPlaying){
                pausePlaying()
            } else {
                if (item != null) {
                    resumePlaying()
                }
            }
        }


        fileName.text = item.name
        fileLength.text = String.format("%02d:%02d", minutes, seconds)


    }


//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        Log.d("PlayBackFragment", "onCreateDialog opened")
//
//        val dialog = super.onCreateDialog(savedInstanceState)
//        val builder = AlertDialog.Builder(activity)
//        val view: View = requireActivity().layoutInflater.inflate(R.layout.fragment_playback, null)
//        builder.setView(view)
//
//        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
//
//        dialog.setContentView(R.layout.fragment_playback)
//
//        faButton = dialog.findViewById(R.id.iv_forward)
//        fileName = dialog.findViewById(R.id.file_name_text_view)
//        fileLength = dialog.findViewById(R.id.file_length_text_view)
//
//        setSeekbarValues(view)
//
//        faButton.setOnClickListener {
//            Log.d("PlayBackFragment", "FAB clicked")
//            try {
//                Log.d("PlayBackFragment", "try playing")
//
//                onPlay(isPlaying)
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//            isPlaying = !isPlaying
//        }
//
//
//        fileName.text = item.name
//        fileLength.text = String.format("%02d:%02d", minutes, seconds)
////        return builder.create()
//        return dialog
//
//    }

    private fun setSeekbarValues(view: View) {
        Log.d("PlayBackFragment", "setSeekbarValues opened")

        currentProgress = view.findViewById(R.id.current_progress_text_view)
        seekBar = view.findViewById(R.id.seekbar)

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
        Log.d("PlayBackFragment", "onPlay called")

        if (!isPlaying) {
            startPlaying()
        } else {
            pausePlaying()
        }
    }

    private fun pausePlaying() {
        playButton = requireActivity().findViewById(R.id.play_iv)

        playButton.setImageResource(R.drawable.ic_placeholder)
        handler.removeCallbacks(mRunnable)
        isPlaying = false
        mediaPlayer!!.pause()
    }

    private fun resumePlaying(){
        mediaPlayer!!.start()
        isPlaying = true
        updateSeekbar()
    }

    @Throws(IOException::class)
    private fun startPlaying() {
        Log.d("PlayBackFragment", "startPlaying called")

        playButton = requireActivity().findViewById(R.id.play_iv)
        seekBar = requireActivity().findViewById(R.id.seekbar)

        playButton.setImageResource(R.drawable.ic_placeholder)
        mediaPlayer = MediaPlayer()
        mediaPlayer!!.setDataSource(item.path)
        mediaPlayer!!.prepare()
        mediaPlayer!!.start()
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
        playButton = requireActivity().findViewById(R.id.play_iv)
        currentProgress = requireActivity().findViewById(R.id.current_progress_text_view)
        fileLength = requireActivity().findViewById(R.id.file_length_text_view)
        seekBar = requireActivity().findViewById(R.id.seekbar)


        playButton.setImageResource(R.drawable.ic_placeholder)
        handler.removeCallbacks(mRunnable)
        mediaPlayer?.stop()
        mediaPlayer?.reset()
        mediaPlayer?.release()
        mediaPlayer = null
        seekBar.progress = seekBar.max
        isPlaying = !isPlaying
        currentProgress.text = fileLength.text
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

    companion object {

        fun newInstance() =
            PlaybackFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    override fun onClickListener(recordingItem: RecordingItem, position: Int) {
        item = recordingItem
        if (isPlaying){
            stopPlaying()
            startPlaying()
        } else {
            startPlaying()
        }
    }

    override fun onStop() {
        super.onStop()
        if (isPlaying){
            stopPlaying()
        }
    }


    var RECORDER_SAMPLERATE = 8000
    var RECORDER_CHANNELS: Int = AudioFormat.CHANNEL_CONFIGURATION_MONO
    var RECORDER_AUDIO_ENCODING: Int = AudioFormat.ENCODING_PCM_16BIT

//    private fun record() {
//        val audioRecorder: AudioRecord
//        val bufferSizeInBytes: Int
//        val bufferSizeInShorts: Int
//        var shortsRead: Int
//        val audioBuffer: ShortArray
//        try {
//            // Get the minimum buffer size required for the successful creation of an AudioRecord object.
//            bufferSizeInBytes = AudioRecord.getMinBufferSize(
//                RECORDER_SAMPLERATE,
//                RECORDER_CHANNELS,
//                RECORDER_AUDIO_ENCODING
//            )
//            bufferSizeInShorts = bufferSizeInBytes / 2
//
//            // Initialize Audio Recorder.
//            if (ActivityCompat.checkSelfPermission(
//                    requireContext(),
//                    Manifest.permission.RECORD_AUDIO
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                return
//            }
//
//            audioRecorder = AudioRecord(
//                MediaRecorder.AudioSource.VOICE_RECOGNITION,
//                RECORDER_SAMPLERATE,
//                RECORDER_CHANNELS,
//                RECORDER_AUDIO_ENCODING,
//                bufferSizeInBytes
//            )
//
//            // Start Recording.
//            audioBuffer = ShortArray(bufferSizeInShorts)
//            audioRecorder.startRecording()
//            isRecording = true
//            while (isRecording) {
//                shortsRead = audioRecorder.read(audioBuffer, 0, bufferSizeInShorts)
//                if (shortsRead == AudioRecord.ERROR_BAD_VALUE || shortsRead == AudioRecord.ERROR_INVALID_OPERATION) {
//                    Log.e("record()", "Error reading from microphone.")
//                    isRecording = false
//                    break
//                }
//
//            audi
//                // Whatever your code needs to do with the audio here...
//            }
//        } finally {
//            if (audioRecorder != null) {
//                audioRecorder.stop()
//                audioRecorder.release()
//            }
//        }
//
//        if (audioRecorder != null) {
//            audioRecorder.stop()
//            audioRecorder.release()
//        }
//    }
}