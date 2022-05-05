package com.salihutimothy.myaudiojournalapp.fragments

import android.app.SearchManager
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.salihutimothy.myaudiojournalapp.R
import com.salihutimothy.myaudiojournalapp.adapters.FileAdapter
import com.salihutimothy.myaudiojournalapp.database.DBHelper
import com.salihutimothy.myaudiojournalapp.entities.RecordingItem
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import android.view.MenuInflater
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat.getSystemService


class FileViewerFragment : FileAdapter.OnItemListClick, Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var dbHelper: DBHelper
    var arrayListAudios: ArrayList<RecordingItem>? = null
    private lateinit var fileAdapter: FileAdapter

    private lateinit var fileName: TextView
    private lateinit var fileLength: TextView
    private lateinit var currentProgress: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var forwardButton: ImageView
    private lateinit var playButton: ImageView
    private lateinit var backwardButton: ImageView
    private lateinit var playbackLayout: CoordinatorLayout
    private lateinit var searchView: SearchView

    private lateinit var item: RecordingItem
    private var mediaPlayer: MediaPlayer? = MediaPlayer()
    private var handler: Handler = Handler(Looper.myLooper()!!)

    private var isPlaying = false
    var minutes: Long = 0
    var seconds: Long = 0
    val seekTime = 5000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val view =inflater.inflate(R.layout.fragment_file_viewer, container, false)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.inflateMenu(R.menu.menu_main)
        toolbar.setOnMenuItemClickListener {
            onOptionsItemSelected(it)        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DBHelper(view.context)
        recyclerView = view.findViewById(R.id.recyclerView)

        playButton = view.findViewById(R.id.play_iv) as ImageView
        backwardButton = view.findViewById(R.id.iv_backward) as ImageView
        forwardButton = view.findViewById(R.id.iv_forward) as ImageView
        fileName = view.findViewById(R.id.file_name_text_view) as TextView
        fileLength = view.findViewById(R.id.file_length_text_view) as TextView
        currentProgress = view.findViewById(R.id.current_progress_text_view) as TextView
        seekBar = view.findViewById(R.id.seekbar) as SeekBar
        playbackLayout = view.findViewById(R.id.playback) as CoordinatorLayout

        setViewAndChildrenEnabled(playbackLayout, false)

        setSeekbarValues(view)

        recyclerView.setHasFixedSize(true)
        val llm = LinearLayoutManager(view.context)
        llm.orientation = LinearLayoutManager.VERTICAL
        llm.reverseLayout = true
        llm.stackFromEnd = true
        recyclerView.layoutManager = llm

        arrayListAudios = dbHelper.getAllAudios()

        if (arrayListAudios == null) {
            Toast.makeText(context, "No audio files", Toast.LENGTH_LONG).show()
        } else {
            fileAdapter =
                FileAdapter(requireContext(), arrayListAudios!!, llm, this@FileViewerFragment)
            recyclerView.adapter = fileAdapter
        }



        playButton.setOnClickListener {
            if (isPlaying) {
                pausePlaying()
            } else {
                if (item != null) {
                    resumePlaying()
                }
            }
        }

        forwardButton.setOnClickListener {
            val currentPosition = mediaPlayer!!.currentPosition
            if (currentPosition + seekTime <= mediaPlayer!!.duration) {
                mediaPlayer!!.seekTo(currentPosition + seekTime)
            } else {
                mediaPlayer!!.seekTo(mediaPlayer!!.duration)

            }

            updateSeekbar()
        }

        backwardButton.setOnClickListener {
            val currentPosition = mediaPlayer!!.currentPosition
            if (currentPosition - seekTime >= 0) {
                mediaPlayer!!.seekTo(currentPosition - seekTime)
            } else {
                mediaPlayer!!.seekTo(0)

            }

            updateSeekbar()
        }
    }

    private fun setSeekbarValues(view: View) {
        Log.d("PlayBackFragment", "setSeekbarValues opened")

        currentProgress = view.findViewById(R.id.current_progress_text_view)
        seekBar = view.findViewById(R.id.seekbar)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

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

        playButton.setImageResource(R.drawable.ic_play)
        handler.removeCallbacks(mRunnable)
        isPlaying = false
        mediaPlayer?.pause()
    }

    private fun resumePlaying() {
        playButton = requireActivity().findViewById(R.id.play_iv)
        playButton.setImageResource(R.drawable.ic_pause)

        if (mediaPlayer != null) {
            mediaPlayer!!.start()
        } else {
            startPlaying()
        }
        isPlaying = true
        updateSeekbar()
    }

    @Throws(IOException::class)
    private fun startPlaying() {
        Log.d("PlayBackFragment", "startPlaying called")

        isPlaying = true

        playButton = requireActivity().findViewById(R.id.play_iv)
        seekBar = requireActivity().findViewById(R.id.seekbar)
        fileName = requireActivity().findViewById(R.id.file_name_text_view) as TextView
        fileLength = requireActivity().findViewById(R.id.file_length_text_view) as TextView
        backwardButton = requireActivity().findViewById(R.id.iv_backward) as ImageView
        forwardButton = requireActivity().findViewById(R.id.iv_forward) as ImageView

        forwardButton.isEnabled = true
        backwardButton.isEnabled = true

        fileName.text = item.name
        fileLength.text = String.format("%02d:%02d", minutes, seconds)
        fileName.isSelected = true

        playButton.setImageResource(R.drawable.ic_pause)
        mediaPlayer = MediaPlayer()
        try {
            mediaPlayer!!.reset()
            mediaPlayer!!.setDataSource(item.path)
            mediaPlayer!!.prepare()
            mediaPlayer!!.start()
            seekBar.max = mediaPlayer!!.duration

        } catch (e: IOException) {
            Toast.makeText(context, "Recording not found", Toast.LENGTH_SHORT).show()
        }

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
        backwardButton = requireActivity().findViewById(R.id.iv_backward) as ImageView
        forwardButton = requireActivity().findViewById(R.id.iv_forward) as ImageView

        forwardButton.isEnabled = false
        backwardButton.isEnabled = false
        playButton.setImageResource(R.drawable.ic_play)
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


    override fun onClickListener(recordingItem: RecordingItem, position: Int) {

        playbackLayout = requireView().findViewById(R.id.playback) as CoordinatorLayout

        setViewAndChildrenEnabled(playbackLayout, true)

        item = recordingItem



        minutes = TimeUnit.MILLISECONDS.toMinutes(item.length)
        seconds =
            TimeUnit.MILLISECONDS.toSeconds(item.length) - TimeUnit.MINUTES.toSeconds(minutes)

        if (isPlaying) {
            stopPlaying()
            startPlaying()
        } else {
            startPlaying()
        }
    }

    private fun setViewAndChildrenEnabled(view: View, enabled: Boolean) {
        view.isEnabled = enabled
        if (view is ViewGroup) {
            val viewGroup = view
            for (i in 0 until viewGroup.childCount) {
                val child = viewGroup.getChildAt(i)
                setViewAndChildrenEnabled(child, enabled)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (isPlaying) {
            stopPlaying()
        } else {
            mediaPlayer?.release()
        }
    }

//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.menu_main, menu)
//        super.onCreateOptionsMenu(menu, inflater)
//
//        val searchManager = context?.getSystemService(Context.SEARCH_SERVICE) as SearchManager
//        searchView = (menu.findItem(R.id.search).actionView as SearchView)
//
//
//        searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
//
//
//        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String?): Boolean {
//
//                val tempArr = ArrayList<RecordingItem>()
//
//                for (arr in arrayListAudios!!) {
//                    if (arr.name!!.lowercase(Locale.getDefault()).contains(query.toString())) {
//                        tempArr.add(arr)
//                    }
//                }
//
//                fileAdapter.setData(tempArr)
//                fileAdapter.notifyDataSetChanged()
//
//                return false
//            }
//
//            override fun onQueryTextChange(newText: String?): Boolean {
//                val tempArr = ArrayList<RecordingItem>()
//
//                for (arr in arrayListAudios!!) {
//                    if (arr.name!!.lowercase(Locale.getDefault()).contains(newText.toString())) {
//                        tempArr.add(arr)
//                    }
//                }
//
//                fileAdapter.setData(tempArr)
//                fileAdapter.notifyDataSetChanged()
//
//                return true
//            }
//
//        })
////        return true
//    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.search -> {
                val searchManager = context?.getSystemService(Context.SEARCH_SERVICE) as SearchManager
                searchView = item.actionView as SearchView

                searchView.queryHint = "Search your journal entries..."
                searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))

//                val closeButtonId =
//                    resources.getIdentifier("android:id/search_close_btn", null, null)
//                val closeButtonImage = searchView.findViewById<ImageView>(closeButtonId) as ImageView?
//                closeButtonImage?.setImageResource(R.drawable.ic_close)

                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {

                        val tempArr = ArrayList<RecordingItem>()

                        for (arr in arrayListAudios!!) {
                            if (arr.name!!.lowercase(Locale.getDefault()).contains(query.toString())) {
                                tempArr.add(arr)
                            }
                        }

                        fileAdapter.setData(tempArr)
                        fileAdapter.notifyDataSetChanged()

                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        val tempArr = ArrayList<RecordingItem>()

                        for (arr in arrayListAudios!!) {
                            if (arr.name!!.lowercase(Locale.getDefault()).contains(newText.toString())) {
                                tempArr.add(arr)
                            }
                        }

                        fileAdapter.setData(tempArr)
                        fileAdapter.notifyDataSetChanged()

                        return true
                    }

                })
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        // TODO Auto-generated method stub
//        super.onActivityCreated(savedInstanceState)
//        setHasOptionsMenu(true)
//    }


    companion object {
        fun newInstance() =
            FileViewerFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}