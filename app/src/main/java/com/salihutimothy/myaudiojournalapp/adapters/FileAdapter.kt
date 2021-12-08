package com.salihutimothy.myaudiojournalapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.salihutimothy.myaudiojournalapp.R
import com.salihutimothy.myaudiojournalapp.database.DBHelper
import com.salihutimothy.myaudiojournalapp.entities.RecordingItem
import com.salihutimothy.myaudiojournalapp.interfaces.OnDatabaseChangedListener
import java.util.ArrayList

class FileAdapter(
    var context: Context,
    var arrayList: ArrayList<RecordingItem>,
    var llm: LinearLayoutManager
) :
    RecyclerView.Adapter<FileAdapter.FileViewerViewHolder?>(), OnDatabaseChangedListener {
    private val dbHelper: DBHelper = DBHelper(context)

    override fun onBindViewHolder(holder: FileAdapter.FileViewerViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    override fun onNewDatabaseEntryAdded(recordingItem: RecordingItem?) {
        TODO("Not yet implemented")
    }

    init {
        dbHelper.setOnDatabaseChangedListener(this)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FileAdapter.FileViewerViewHolder {
        val itemView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card_view, parent, false)
        return FileViewerViewHolder(itemView)    }

    class FileViewerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var tvRecordName: TextView? = itemView.findViewById(R.id.file_name_text)
        private var tvRecordLength: TextView? = itemView.findViewById(R.id.file_length_text)
        private var tvRecordTime: TextView? = itemView.findViewById(R.id.file_time_added)
        private var ivRecordImage: ImageView? = itemView.findViewById(R.id.imageView)
        private var cardView: CardView? = itemView.findViewById(R.id.card_view)

    }

}

