package com.salihutimothy.myaudiojournalapp.fragments

import android.app.SearchManager
import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.salihutimothy.myaudiojournalapp.R
import com.salihutimothy.myaudiojournalapp.adapters.FileAdapter
import com.salihutimothy.myaudiojournalapp.database.DBHelper
import com.salihutimothy.myaudiojournalapp.entities.RecordingItem
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import androidx.navigation.NavController
import java.io.File


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
    private lateinit var optionsButton: ImageView
    private lateinit var playbackLayout: CoordinatorLayout
    private lateinit var searchView: SearchView
    private lateinit var navController: NavController

    private lateinit var item: RecordingItem
    private var mediaPlayer: MediaPlayer? = MediaPlayer()
    private var handler: Handler = Handler(Looper.myLooper()!!)

    private val MY_SORT_PREF = "sortOrder"

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
        val view = inflater.inflate(R.layout.fragment_file_viewer, container, false)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.inflateMenu(R.menu.menu_main)
        toolbar.setOnMenuItemClickListener {
            onOptionsItemSelected(it)
        }

//        searchView = toolbar.findViewById(R.id.search).actionView as SearchView
//        searchView = (R.menu.menu_main.
//        findItem(R.id.search).actionView as SearchView)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DBHelper(view.context)
        recyclerView = view.findViewById(R.id.recyclerView)

        playButton = view.findViewById(R.id.play_iv) as ImageView
        backwardButton = view.findViewById(R.id.iv_backward) as ImageView
        forwardButton = view.findViewById(R.id.iv_forward) as ImageView
        optionsButton = view.findViewById(R.id.iv_options) as ImageView
        fileName = view.findViewById(R.id.file_name_text_view) as TextView
        fileLength = view.findViewById(R.id.file_length_text_view) as TextView
        currentProgress = view.findViewById(R.id.current_progress_text_view) as TextView
        seekBar = view.findViewById(R.id.seekbar) as SeekBar
        playbackLayout = view.findViewById(R.id.playback) as CoordinatorLayout
        navController = Navigation.findNavController(requireView())


        setViewAndChildrenEnabled(playbackLayout, false)

        setSeekbarValues(view)

        recyclerView.setHasFixedSize(true)
        val llm = LinearLayoutManager(view.context)
        llm.orientation = LinearLayoutManager.VERTICAL
        llm.reverseLayout = true
        llm.stackFromEnd = true
        recyclerView.layoutManager = llm

        val pref: SharedPreferences =
            context!!.getSharedPreferences(MY_SORT_PREF, Context.MODE_PRIVATE)
//        val editor = pref.edit()
        val sort = pref.getString("sorting", "sortByDate")

        arrayListAudios = dbHelper.getAllAudios(sort!!)

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

        optionsButton.setOnClickListener {
            bottomSheetDialog = BottomSheetDialog(context!!, R.style.BottomSheetTheme)
            val bsView = LayoutInflater.from(context).inflate(R.layout.bottomsheet_layout,
                view.findViewById(R.id.bottom_sheet)
            )

            bsView.findViewById<LinearLayout>(R.id.bs_rename).setOnClickListener {
                val alertDialog: AlertDialog.Builder = AlertDialog.Builder(context!!)
                alertDialog.setTitle("Rename to")
                val editText: EditText = EditText(ContextThemeWrapper(context, R.style.CustomEditTextTheme))
                editText.setText(item.name)
                val file = File(item.path!!)
                alertDialog.setView(editText)
                editText.requestFocus()


                alertDialog.setPositiveButton("RENAME",
                    DialogInterface.OnClickListener { dialog, which ->
                        val onlyPath: String? = file.parentFile?.absolutePath
                        val newName = editText.text.toString()
                        val newPath = onlyPath + "/" + newName + ".mp3"
                        val newFile = File(newPath)
                        val rename = file.renameTo(newFile)
                        if (item.path != newPath){
                            if (rename) {
                                dbHelper.updateRecording(item, newName, newPath)

                                fileAdapter.notifyItemChanged(mPosition)
                                navController.run {
                                    popBackStack()
                                    navigate(R.id.audioListFragment)
                                }

                            } else {
                                Toast.makeText(context, "Process Failed", Toast.LENGTH_SHORT).show()
                            }
                        }

                    }
                )
                alertDialog.setNegativeButton("CANCEL", null)
                alertDialog.create().show()
                bottomSheetDialog.dismiss()

            }

            bsView.findViewById<LinearLayout>(R.id.bs_share).setOnClickListener {
                val uri = Uri.parse(item.path)
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "audio/*"
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                context!!.startActivity(Intent.createChooser(shareIntent, "Share Recording via"))
                bottomSheetDialog.dismiss()
            }

            bsView.findViewById<LinearLayout>(R.id.bs_delete).setOnClickListener {
                val alertDialog: AlertDialog.Builder = AlertDialog.Builder(context!!)
                alertDialog.setTitle("Delete")
                alertDialog.setMessage("Do you want to delete this recording?")
                alertDialog.setPositiveButton("DELETE",
                    DialogInterface.OnClickListener { dialog, which ->
                        val file = File (item.path!!)
                        val delete = file.delete()

                        if (delete){
                            dbHelper.deleteRecording(item)
                            fileAdapter.notifyItemRemoved(mPosition)
                            fileAdapter.notifyItemRangeChanged(mPosition, arrayListAudios!!.size)
                            navController.run {
                                popBackStack()
                                navigate(R.id.audioListFragment)
                            }
                        } else {
                            Toast.makeText(context, "Process Failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
                alertDialog.setNegativeButton("CANCEL", null)
                alertDialog.create().show()
                bottomSheetDialog.dismiss()
            }
            bottomSheetDialog.setContentView(bsView)
            bottomSheetDialog.show()
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

    private var mPosition = 0

    override fun onClickListener(recordingItem: RecordingItem, position: Int) {

        playbackLayout = requireView().findViewById(R.id.playback) as CoordinatorLayout

        setViewAndChildrenEnabled(playbackLayout, true)

        item = recordingItem

        mPosition = position


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
    private lateinit var bottomSheetDialog : BottomSheetDialog

    override fun onMoreClickListener(recordingItem: RecordingItem, position: Int) {
        bottomSheetDialog = BottomSheetDialog(context!!, R.style.BottomSheetTheme)
        val bsView = LayoutInflater.from(context).inflate(R.layout.bottomsheet_layout,
        view?.findViewById(R.id.bottom_sheet))

//        bsView.findViewById(R.id.bs_rename).set

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

        val pref: SharedPreferences =
            context!!.getSharedPreferences(MY_SORT_PREF, Context.MODE_PRIVATE)
        val editor = pref.edit()

        navController = Navigation.findNavController(requireView())


        return when (item.itemId) {
            R.id.search -> {
                val searchManager =
                    context?.getSystemService(Context.SEARCH_SERVICE) as SearchManager
                searchView = item.actionView as SearchView

                searchView.queryHint = "Search your journal entries..."
                searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))

                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {

                        val tempArr = ArrayList<RecordingItem>()

                        for (arr in arrayListAudios!!) {
                            if (arr.name!!.lowercase(Locale.getDefault())
                                    .contains(query.toString())
                            ) {
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
                            if (arr.name!!.lowercase(Locale.getDefault())
                                    .contains(newText.toString())
                            ) {
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

            R.id.sort_by_date -> {
                editor.putString("sorting", "sortByDate")
                editor.apply()
                navController.run {
                    popBackStack()
                    navigate(R.id.audioListFragment)
                }
                true
            }

            R.id.sort_by_name -> {
                editor.putString("sorting", "sortByName")
                editor.apply()
                navController.run {
                    popBackStack()
                    navigate(R.id.audioListFragment)
                }
                true
            }

            R.id.sort_by_length -> {
                editor.putString("sorting", "sortByLength")
                editor.apply()
                navController.run {
                    popBackStack()
                    navigate(R.id.audioListFragment)
                }
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