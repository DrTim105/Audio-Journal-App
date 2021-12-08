package com.salihutimothy.myaudiojournalapp.interfaces

import com.salihutimothy.myaudiojournalapp.entities.RecordingItem

interface OnDatabaseChangedListener {
    fun onNewDatabaseEntryAdded(recordingItem: RecordingItem?)
}
