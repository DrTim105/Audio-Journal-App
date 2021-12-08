package com.salihutimothy.myaudiojournalapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.salihutimothy.myaudiojournalapp.R
import com.salihutimothy.myaudiojournalapp.adapters.FileAdapter
import com.salihutimothy.myaudiojournalapp.database.DBHelper
import com.salihutimothy.myaudiojournalapp.entities.RecordingItem
import java.security.AccessController
import java.util.ArrayList

class FileViewerFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var dbHelper: DBHelper
    var arrayListAudios: ArrayList<RecordingItem>? = null
    private lateinit var fileAdapter : FileAdapter


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
        return inflater.inflate(R.layout.fragment_file_viewer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DBHelper(view.context)
        recyclerView = view.findViewById(R.id.recyclerView)

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
            fileAdapter = FileAdapter(requireContext(), arrayListAudios!!, llm)
            recyclerView.adapter = fileAdapter
        }
    }

    companion object {

        fun newInstance() =
            FileViewerFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}