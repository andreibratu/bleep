package com.broads.bleep.services

import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView

import android.view.View
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_ENTER

import android.text.TextUtils

import android.media.AudioManager
import android.os.Vibrator

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.VibrationEffect.DEFAULT_AMPLITUDE
import android.support.v13.view.inputmethod.EditorInfoCompat
import android.support.v13.view.inputmethod.InputConnectionCompat
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.EditText

import android.content.ClipDescription
import android.net.Uri

import android.support.v13.view.inputmethod.InputContentInfoCompat
import android.view.inputmethod.InputConnection.INPUT_CONTENT_GRANT_READ_URI_PERMISSION
import android.view.inputmethod.InputContentInfo
import com.broads.bleep.R
import java.io.File


class BleepInputMethodService : InputMethodService(), KeyboardView.OnKeyboardActionListener {
    

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
                true
            }
            return InputConnectionCompat.createWrapper(ic, editorInfo, callback)
        }
    }*/
/*
    fun commitM4PFile(contentUri: Uri, imageDescription: String) {
        val inputContentInfo = InputContentInfoCompat(contentUri,
            ClipDescription(imageDescription, arrayOf("audio/mp4")), null)
        val inputConnection = currentInputConnection
        val editorInfo = currentInputEditorInfo
        var flags = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            flags = flags or InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION
        }
        InputConnectionCompat.commitContent(inputConnection, editorInfo, inputContentInfo, flags, null)
    }*/


    private var keyboardView: KeyboardView? = null
    private var keyboard: Keyboard? = null

    private var caps = false

    private var am: AudioManager? = null
    private var v: Vibrator? = null

    companion object {
        const val KEYCODE_SEND_BLEEP = -7
    }

    override fun onCreateInputView(): KeyboardView? {
        keyboardView = View.inflate(this, R.layout.keyboard_view, null) as KeyboardView
        keyboard = Keyboard(this, R.xml.keys_layout)
        keyboardView?.keyboard = keyboard
        keyboardView?.setOnKeyboardActionListener(this)
        return keyboardView
    }

    override fun onPress(i: Int) {

    }

    override fun onRelease(i: Int) {

    }

    override fun onKey(primaryCode: Int, keyCodes: IntArray) {
        playSound(primaryCode)
        val inputConnection = currentInputConnection
        if (inputConnection != null) {
            when (primaryCode) {
                Keyboard.KEYCODE_DELETE -> {
                    val selectedText = inputConnection.getSelectedText(0)

                    if (TextUtils.isEmpty(selectedText)) {
                        inputConnection.deleteSurroundingText(1, 0)
                    } else {
                        inputConnection.commitText("", 1)
                    }
                }
                Keyboard.KEYCODE_SHIFT -> {
                    caps = !caps
                    keyboard?.isShifted = caps
                    keyboardView?.invalidateAllKeys()
                }
                Keyboard.KEYCODE_DONE -> inputConnection.sendKeyEvent(
                    KeyEvent(
                        KeyEvent.ACTION_DOWN,
                        KEYCODE_ENTER
                    )
                )
                KEYCODE_SEND_BLEEP -> {

                    val myUri = Uri.fromFile(File("/app/src/main/res/test.mp4"))
                    val clipDescription = ClipDescription("anthem", arrayOf("audio/mp4"))
                    val contentInfo = InputContentInfo(myUri, clipDescription)
                    inputConnection.commitContent(contentInfo, INPUT_CONTENT_GRANT_READ_URI_PERMISSION, null)
                    ///commitM4PFile(myUri, "Rusian anthem")
                }
                else -> {
                    var code = primaryCode.toChar()
                    if (Character.isLetter(code) && caps) {
                        code = Character.toUpperCase(code)
                    }
                    inputConnection.commitText(code.toString(), 1)
                }
            }
        }
    }

    override fun onText(charSequence: CharSequence) {

    }

    override fun swipeLeft() {

    }

    override fun swipeRight() {

    }

    override fun swipeDown() {

    }

    override fun swipeUp() {

    }

    private fun playSound(keyCode: Int) {
        v?.vibrate(VibrationEffect.createOneShot(50, DEFAULT_AMPLITUDE))
        am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        when (keyCode) {
            32 -> am?.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR)
            Keyboard.KEYCODE_DONE, 10 -> am?.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN)
            Keyboard.KEYCODE_DELETE -> am?.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE)
            else -> am?.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD)
        }
    }
}