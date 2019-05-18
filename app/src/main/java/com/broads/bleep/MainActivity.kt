package com.broads.bleep

import android.os.Build
import android.os.Bundle
import android.support.v13.view.inputmethod.EditorInfoCompat
import android.support.v13.view.inputmethod.InputConnectionCompat
import android.support.v7.app.AppCompatActivity
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.EditText

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
/*
    private val editText = object : EditText(this) {
        override fun onCreateInputConnection(editorInfo: EditorInfo): InputConnection {

            val ic: InputConnection = super.onCreateInputConnection(editorInfo)
            EditorInfoCompat.setContentMimeTypes(editorInfo, arrayOf("audio/mp4"))

            val callback = InputConnectionCompat.OnCommitContentListener { inputContentInfo, flags, _ ->

                val lacksPermission = (flags and InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION) != 0
                // read and display inputContentInfo asynchronously
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 && lacksPermission) {
                    try {
                        inputContentInfo.requestPermission()
                    } catch (e: Exception) {
                        return@OnCommitContentListener false
                    }

                }

                // read and display inputContentInfo asynchronously.
                // call inputContentInfo.releasePermission() as needed.

                true
            }
            return InputConnectionCompat.createWrapper(ic, editorInfo, callback)
        }
    }*/
}
