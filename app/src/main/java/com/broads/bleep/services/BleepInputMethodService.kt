package com.broads.bleep.services

import android.content.ClipDescription
import android.content.Context
import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.media.AudioManager
import android.os.VibrationEffect
import android.os.VibrationEffect.DEFAULT_AMPLITUDE
import android.os.Vibrator
import android.support.annotation.Nullable
import android.support.annotation.RawRes
import android.support.v13.view.inputmethod.EditorInfoCompat
import android.support.v13.view.inputmethod.InputConnectionCompat
import android.support.v13.view.inputmethod.InputContentInfoCompat
import android.support.v4.content.FileProvider
import android.text.TextUtils
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_ENTER
import android.view.View
import android.view.inputmethod.EditorInfo
import com.broads.bleep.R
import java.io.*


class BleepInputMethodService : InputMethodService(), KeyboardView.OnKeyboardActionListener {

    companion object {
        const val AUTHORITY = "com.broads.bleep.inputcontent"
        const val MIME_TYPE_MP4 = "audio/mp4"
        const val KEYCODE_SEND_BLEEP = -7
    }

    private var keyboardView: KeyboardView? = null
    private var keyboard: Keyboard? = null

    private var caps = false

    private var am: AudioManager? = null
    private var v: Vibrator? = null

    private var mMp4File: File? = null

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
                    if (isCommitContentSupported(currentInputEditorInfo, MIME_TYPE_MP4)) {
                        mMp4File?.let { doCommitContent("Russian anthem", MIME_TYPE_MP4, it) }
                    }
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

    private fun isCommitContentSupported(
        @Nullable editorInfo: EditorInfo?, mimeType: String
    ): Boolean {
        if (editorInfo == null) {
            return false
        }

        if (currentInputConnection == null) {
            return false
        }

        if (!validatePackageName(editorInfo)) {
            return false
        }

        val supportedMimeTypes = EditorInfoCompat.getContentMimeTypes(editorInfo)
        for (supportedMimeType in supportedMimeTypes) {
            if (ClipDescription.compareMimeTypes(mimeType, supportedMimeType)) {
                return true
            }
        }
        return false
    }

    private fun doCommitContent(
        description: String, mimeType: String,
        file: File
    ) {
        val editorInfo = currentInputEditorInfo

        // Validate packageName again just in case.
        if (!validatePackageName(editorInfo)) {
            return
        }

        val contentUri = FileProvider.getUriForFile(this, AUTHORITY, file)

        // As you as an IME author are most likely to have to implement your own content provider
        // to support CommitContent API, it is important to have a clear spec about what
        // applications are going to be allowed to access the content that your are going to share.
        val flag: Int = InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION

        val inputContentInfoCompat = InputContentInfoCompat(
            contentUri,
            ClipDescription(description, arrayOf(mimeType)), null/* linkUrl */
        )
        InputConnectionCompat.commitContent(
            currentInputConnection, currentInputEditorInfo, inputContentInfoCompat,
            flag, null
        )
    }

    private fun validatePackageName(@Nullable editorInfo: EditorInfo?): Boolean {
        if (editorInfo == null) {
            return false
        }
        if (editorInfo.packageName == null) {
            return false
        }
        return true
    }

    override fun onCreate() {
        super.onCreate()

        // TODO: Avoid file I/O in the main thread.
        val soundsDir = File(filesDir, "sounds")
        soundsDir.mkdirs()
        mMp4File = getFileForResource(this, R.raw.mp4_sample, soundsDir, "mp4_sample.mp4")
    }

    private fun getFileForResource(
        context: Context, @RawRes res: Int, outputDir: File,
        filename: String
    ): File? {
        val outputFile = File(outputDir, filename)
        val buffer = ByteArray(4096)
        var resourceReader: InputStream? = null
        try {
            try {
                resourceReader = context.resources.openRawResource(res)
                var dataWriter: OutputStream? = null
                try {
                    dataWriter = FileOutputStream(outputFile)
                    while (true) {
                        val numRead = resourceReader!!.read(buffer)
                        if (numRead <= 0) {
                            break
                        }
                        dataWriter.write(buffer, 0, numRead)
                    }
                    return outputFile
                } finally {
                    if (dataWriter != null) {
                        dataWriter.flush()
                        dataWriter.close()
                    }
                }
            } finally {
                resourceReader?.close()
            }
        } catch (e: IOException) {
            return null
        }

    }
}