package com.salihutimothy.myaudiojournalapp.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.salihutimothy.myaudiojournalapp.entities.RecordingItem
import com.salihutimothy.myaudiojournalapp.interfaces.OnDatabaseChangedListener
import java.util.*


class DBHelper(private val context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQLITE_CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
    }

    fun addRecording(recordingItem: RecordingItem): Boolean {
        return try {
            val db = writableDatabase
            val contentValues = ContentValues()
            contentValues.put(COLUMN_NAME, recordingItem.name)
            contentValues.put(COLUMN_PATH, recordingItem.path)
            contentValues.put(COLUMN_LENGTH, recordingItem.length)
            contentValues.put(COLUMN_TIME_ADDED, recordingItem.time_added)
            db.insert(TABLE_NAME, null, contentValues)
            if (mOnDatabaseChangedListener != null) {
                mOnDatabaseChangedListener?.onNewDatabaseEntryAdded(recordingItem)
            }
            db.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getAllAudios(): ArrayList<RecordingItem>? {
        val arrayList: ArrayList<RecordingItem> = ArrayList<RecordingItem>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("select * from $TABLE_NAME", null)

        return if (cursor != null) {
            while (cursor.moveToNext()) {
                val name = cursor.getString(1)
                val path = cursor.getString(2)
                val length = cursor.getLong(3)
                val timeAdded = cursor.getLong(4)
                val recordingItem = RecordingItem(name, path, length, timeAdded)

                arrayList.add(recordingItem)
            }
            cursor.close()
            db.close()
            arrayList
        } else {
            null
        }
    }

    fun setOnDatabaseChangedListener(listener: OnDatabaseChangedListener?) {
        mOnDatabaseChangedListener = listener
    }

    companion object {
        const val DATABASE_NAME = "saved_recordings.db"
        private const val DATABASE_VERSION = 1
        const val TABLE_NAME = "saved_recording_table"
        const val COLUMN_NAME = "name"
        const val COLUMN_PATH = "path"
        const val COLUMN_LENGTH = "length"
        const val COLUMN_TIME_ADDED = "time_added"
        private var mOnDatabaseChangedListener: OnDatabaseChangedListener? = null
        private const val COMA_SEP = ","
        private const val SQLITE_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" + "id INTEGER PRIMARY KEY" +
                    " AUTOINCREMENT" + COMA_SEP +
                    COLUMN_NAME + " TEXT" + COMA_SEP +
                    COLUMN_PATH + " TEXT" + COMA_SEP +
                    COLUMN_LENGTH + " INTEGER" + COMA_SEP +
                    COLUMN_TIME_ADDED + " INTEGER " + ")"


    }
}
