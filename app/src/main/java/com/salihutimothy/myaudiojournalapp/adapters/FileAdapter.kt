package com.salihutimothy.myaudiojournalapp.adapters

import android.content.Context
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.salihutimothy.myaudiojournalapp.R
import com.salihutimothy.myaudiojournalapp.database.DBHelper
import com.salihutimothy.myaudiojournalapp.entities.RecordingItem
import com.salihutimothy.myaudiojournalapp.interfaces.OnDatabaseChangedListener
import java.util.*
import java.util.concurrent.TimeUnit


class FileAdapter(
    var context: Context,
    var arrayList: ArrayList<RecordingItem>,
    var llm: LinearLayoutManager,
    var onItemListClick: OnItemListClick
) :
    RecyclerView.Adapter<FileAdapter.FileViewerViewHolder?>(), OnDatabaseChangedListener {
    private val dbHelper: DBHelper = DBHelper(context)

    private var ten = 10
    private var selectedPos = RecyclerView.NO_POSITION
    private var selectedRecordingItem: RecordingItem? = null


    override fun onBindViewHolder(holder: FileAdapter.FileViewerViewHolder, position: Int) {

        val recordingItem: RecordingItem = arrayList[position]
        val minutes = TimeUnit.MILLISECONDS.toMinutes(recordingItem.length)
        val seconds =
            TimeUnit.MILLISECONDS.toSeconds(recordingItem.length) - TimeUnit.MINUTES.toSeconds(
                minutes
            )

        holder.ivRecord!!.isEnabled = false
        holder.tvRecordName!!.text = recordingItem.name
        holder.tvRecordLength!!.text = String.format("%02d:%02d", minutes, seconds)


        if (DateUtils.isToday(recordingItem.time_added)) {
            holder.tvRecordTime!!.text = String.format(
                context.resources.getString(R.string.date_today), DateUtils.formatDateTime(
                    context,
                    recordingItem.time_added,
                    DateUtils.FORMAT_SHOW_TIME
                )
            )
        } else {
            holder.tvRecordTime!!.text = DateUtils.formatDateTime(
                context, recordingItem.time_added,
                DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME
                        or DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_SHOW_WEEKDAY or DateUtils.FORMAT_ABBREV_ALL
            )
        }
        holder.itemView.isSelected = selectedPos == position

        if (selectedRecordingItem != null) {
            if (selectedRecordingItem == arrayList[position]) {

                Log.d("BUG", "clicked item name ${holder.tvRecordName!!.text}")


                holder.tvRecordName!!.setTextColor(
                    ContextCompat.getColor(
                        context,
                        (R.color.accentz)
                    )
                )
                holder.tvRecordName!!.isSelected = true
                holder.ivRecord!!.isEnabled = true
                holder.ivRecord!!.setImageResource(R.drawable.ic_music)
            }
            else {
                holder.tvRecordName!!.setTextColor(
                    ContextCompat.getColor(
                        context,
                        (R.color.textColorPrimary)
                    )
                )
                Log.d("BUG", "unclicked item name ${holder.tvRecordName!!.text}")

                holder.ivRecord!!.isEnabled = false
                holder.ivRecord!!.setImageResource(R.drawable.ic_play)
            }
        }
    }

    private fun dpToPx(dp: Int): Float {
        val density: Float = context.resources
            .displayMetrics.density
        return (dp.toFloat() * density)
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    fun setData(arrNotesList: List<RecordingItem>) {
        arrayList = arrNotesList as ArrayList<RecordingItem>
    }

    override fun onNewDatabaseEntryAdded(recordingItem: RecordingItem?) {
        if (recordingItem != null) {
            arrayList.add(recordingItem)
        }

        notifyItemInserted(arrayList.size - 1)

    }

    override fun onDatabaseEntryDeleted(recordingItem: RecordingItem?) {
        if (recordingItem != null) {
            arrayList.remove(recordingItem)
        }
        notifyItemRemoved(selectedPos)
        notifyItemRangeChanged(selectedPos, arrayList.size)
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
        return FileViewerViewHolder(itemView)
    }


    inner class FileViewerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        var tvRecordName: TextView? = itemView.findViewById(R.id.file_name_text)
        var tvRecordLength: TextView? = itemView.findViewById(R.id.file_length_text)
        var tvRecordTime: TextView? = itemView.findViewById(R.id.file_time_added)
        var ivRecord: ImageView? = itemView.findViewById(R.id.imageView)

        init {
            itemView.setOnClickListener(this)
        }


        override fun onClick(v: View?) {
            notifyItemChanged(selectedPos)
            selectedPos = layoutPosition
            selectedRecordingItem = arrayList[selectedPos]

            notifyItemChanged(selectedPos)


            Log.d("BUG", "selected position $selectedPos")
            Log.d("BUG", "onclick ${selectedRecordingItem!!.name}")


            onItemListClick.onClickListener(arrayList[adapterPosition], adapterPosition)
//            notifyDataSetChanged()
        }
    }


    interface OnItemListClick {
        fun onClickListener(recordingItem: RecordingItem, position: Int)
    }

}

