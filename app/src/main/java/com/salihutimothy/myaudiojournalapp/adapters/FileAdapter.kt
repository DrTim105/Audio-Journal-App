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


    override fun onBindViewHolder(holder: FileAdapter.FileViewerViewHolder, position: Int) {
        Log.d("TAG", "Binding item at position $position")

        val recordingItem: RecordingItem = arrayList[position]
        val minutes = TimeUnit.MILLISECONDS.toMinutes(recordingItem.length)
        val seconds =
            TimeUnit.MILLISECONDS.toSeconds(recordingItem.length) - TimeUnit.MINUTES.toSeconds(
                minutes
            )

        holder.ivRecord!!.isEnabled = false
        holder.tvRecordName!!.text = recordingItem.name
        holder.tvRecordLength!!.text = String.format("%02d:%02d", minutes, seconds)
        holder.ivMore!!.setOnClickListener {
        }

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

        if (selectedPos == position) {
            holder.tvRecordName!!.setTextColor(
                ContextCompat.getColor(
                    context,
                    (R.color.accentz)
                )
            )
            holder.ivRecord!!.isEnabled = true


            holder.cardView!!.strokeColor = ContextCompat.getColor(context, (R.color.cardBorder))
            holder.cardView!!.elevation = 0f
        } else {
            holder.tvRecordName!!.setTextColor(
                ContextCompat.getColor(
                    context,
                    (R.color.textColorPrimary)
                )
            )
            holder.ivRecord!!.isEnabled = false

            holder.cardView!!.strokeColor = ContextCompat.getColor(context, (R.color.accent2))
            holder.cardView!!.elevation = dpToPx(8)

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
        var ivMore: ImageView? = itemView.findViewById(R.id.iv_more)
        var cardView: MaterialCardView? = itemView.findViewById(R.id.card_view)

        init {
            itemView.setOnClickListener(this)
        }

        // Game-specific bonuses
        //
        //

        override fun onClick(v: View?) {
            notifyItemChanged(selectedPos)
            selectedPos = layoutPosition
            notifyItemChanged(selectedPos)

            onItemListClick.onClickListener(arrayList[adapterPosition], adapterPosition)
        }

    }


    interface OnItemListClick {
        fun onClickListener(recordingItem: RecordingItem, position: Int)
        fun onMoreClickListener(recordingItem: RecordingItem, position: Int)
    }

}

