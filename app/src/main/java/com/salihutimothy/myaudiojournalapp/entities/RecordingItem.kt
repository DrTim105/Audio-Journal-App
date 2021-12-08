package com.salihutimothy.myaudiojournalapp.entities

import java.io.Serializable


class RecordingItem(var name: String?, var path: String?, var length: Long, var time_added: Long) : Serializable {

}