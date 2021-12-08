package com.salihutimothy.myaudiojournalapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment

class FileViewerFragment : Fragment() {

    companion object {

        fun newInstance() =
            FileViewerFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}