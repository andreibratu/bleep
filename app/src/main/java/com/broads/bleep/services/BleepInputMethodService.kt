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
import android.os.VibrationEffect
import android.os.VibrationEffect.DEFAULT_AMPLITUDE
import android.widget.Toast

import com.broads.bleep.R

class BleepInputMethodService : InputMethodService(), KeyboardView.OnKeyboardActionListener {

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
                    Toast.makeText(this, "Bleep sent", Toast.LENGTH_SHORT).show()
                    // TODO: Add service call for sending a bleep.
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