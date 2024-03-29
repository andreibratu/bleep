package com.broads.bleep.services

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.media.AudioManager
import android.os.VibrationEffect
import android.os.VibrationEffect.DEFAULT_AMPLITUDE
import android.os.Vibrator
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_ENTER
import android.view.View
import android.view.inputmethod.ExtractedTextRequest
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import com.broads.bleep.R
import com.broads.bleep.adapters.BleepsRecyclerAdapter
import com.broads.bleep.entities.Bleep
import com.google.firebase.firestore.FirebaseFirestore

@Suppress("UNCHECKED_CAST")
class BleepInputMethodService : InputMethodService(), KeyboardView.OnKeyboardActionListener {

    private var containerLayout: ConstraintLayout? = null

    private var keyboardView: KeyboardView? = null
    private var bleepsView: RecyclerView? = null

    private var keyboard: Keyboard? = null

    private var caps = true

    private var am: AudioManager? = null
    private var v: Vibrator? = null

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private var bleeps: ArrayList<Bleep> = ArrayList()

    private lateinit var linearLayoutManager: LinearLayoutManager

    private lateinit var bleepsAdapter: BleepsRecyclerAdapter

    private var toggleBleepsViewButton: ImageButton? = null

    private var appNameTextView: TextView? = null
    private var searchEditText: EditText? = null

    override fun onCreateInputView(): ConstraintLayout? {
        containerLayout = ConstraintLayout.inflate(this, R.layout.container_layout, null) as ConstraintLayout

        keyboardView = layoutInflater.inflate(R.layout.keyboard_view, containerLayout, false) as KeyboardView
        keyboard = Keyboard(this, R.xml.keys_layout)
        keyboard?.isShifted = caps
        keyboardView?.keyboard = keyboard
        keyboardView?.setOnKeyboardActionListener(this)

        bleepsView = layoutInflater.inflate(R.layout.bleeps_view, containerLayout, false) as RecyclerView

        containerLayout?.addView(keyboardView, 0)

        toggleBleepsViewButton = containerLayout?.findViewById(R.id.toggleBleepsViewButton) as ImageButton

        appNameTextView = containerLayout?.findViewById(R.id.appNameTextView) as TextView
        searchEditText = containerLayout?.findViewById(R.id.searchEditText) as EditText

        val toggleBleepsViewButton = containerLayout?.findViewById<ImageButton>(R.id.toggleBleepsViewButton)
        toggleBleepsViewButton?.setOnClickListener { toggleBleepsView() }

        // TODO: Configure the toggleBleepsSearch method properly.
        // searchEditText?.setOnFocusChangeListener{ _, b -> toggleBleepsSearch(b) }

        linearLayoutManager = LinearLayoutManager(this)
        bleepsView?.layoutManager = linearLayoutManager

        bleepsAdapter = BleepsRecyclerAdapter(this, bleeps)
        bleepsView?.adapter = bleepsAdapter

        return containerLayout
    }

    override fun onPress(primaryCode: Int) {

    }

    override fun onRelease(primaryCode: Int) {
        val inputConnection = currentInputConnection
        if (inputConnection != null && primaryCode != Keyboard.KEYCODE_SHIFT) {
            val currentText = inputConnection.getExtractedText(ExtractedTextRequest(), 0).text
            setCaps(TextUtils.isEmpty(currentText))
        }
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
                    setCaps(!caps)
                }
                Keyboard.KEYCODE_DONE -> {
                    inputConnection.commitText("\n", 1)
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

    private fun retrieveBleeps(vararg params: String?) {
        bleeps.clear()
        if (params.isEmpty()) {
            // No keywords, filter by popularity
            this.db.collection("bleeps").limit(20).get().addOnSuccessListener { bleepRefs ->
                bleepRefs.forEach { data ->
                    val title = data["title"] as? String
                    val url = data["url"] as? String
                    bleeps.add(Bleep(title, url))
                    bleepsAdapter.notifyItemInserted(bleeps.size)
                }
            }
        } else {
            val keywordsMatched = hashMapOf<String, Int>()
            // Get references of bleeps that match the keywords
            params.forEach { keyword ->
                this.db.document("/bleepsRef/$keyword").get().addOnSuccessListener { keywordRef ->
                    // If keyword exists, refs array cannot be null
                    if (keywordRef.exists()) {
                        (keywordRef["refs"] as Array<String>).forEach { bleepRef ->
                            keywordsMatched[bleepRef] = keywordsMatched.getValue(bleepRef) + 1
                        }
                    }
                }
            }
            // Filter by how many keywords were matched
            val bleepRefs = keywordsMatched.toList().sortedByDescending { it.second }.map { it.first }.take(5)

            // Fetch entities
            bleepRefs.forEach { bleepRef ->
                this.db.document(bleepRef).get().addOnSuccessListener { data ->
                    val title = data["title"] as String
                    val url = data["url"] as String
                    bleeps.add(Bleep(title, url))
                    bleepsAdapter.notifyItemInserted(bleeps.size)
                }
            }
        }
    }

    private fun toggleBleepsView() {
        when (containerLayout?.getChildAt(0)) {
            is KeyboardView -> {
                containerLayout?.removeView(keyboardView)

                bleepsView?.layoutParams =
                    keyboardView?.height?.let { RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, it) }

                toggleBleepsViewButton?.setImageResource(R.drawable.ic_keyboard_black_24dp)

                appNameTextView?.visibility = View.GONE
                searchEditText?.visibility = View.VISIBLE

                containerLayout?.addView(bleepsView, 0)

                retrieveBleeps()
            }
            is RecyclerView -> {
                containerLayout?.removeView(bleepsView)

                toggleBleepsViewButton?.setImageResource(R.drawable.ic_music_note_black_24dp)

                searchEditText?.visibility = View.GONE
                appNameTextView?.visibility = View.VISIBLE

                containerLayout?.addView(keyboardView, 0)
            }
        }
    }

    private fun toggleBleepsSearch(searching: Boolean) {
        when(searching) {
            true -> {
                val keyboardHeight = keyboardView?.height
                val bleepsHeight = bleepsView?.height
                val bleepsPaddingTop = bleepsView?.paddingTop

                containerLayout?.removeView(bleepsView)

                bleepsView?.layoutParams =
                    keyboardHeight?.let { RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, it * 3 / 2) }
                bleepsPaddingTop?.let { bleepsHeight?.let { it1 -> bleepsView?.setPadding(0, it, 0, it1) } }

                val constraintSet = ConstraintSet()
                constraintSet.clone(containerLayout)

                containerLayout?.addView(keyboardView, 0)
                containerLayout?.addView(bleepsView, 1)

                bleepsView?.id?.let { keyboardView?.id?.let { it1 ->
                    constraintSet.connect(it, ConstraintSet.BOTTOM,
                        it1, ConstraintSet.TOP, 0)
                } }
                constraintSet.applyTo(containerLayout)
            }
            false -> {
                containerLayout?.removeView(keyboardView)
                containerLayout?.removeView(bleepsView)

                bleepsView?.layoutParams =
                    keyboardView?.height?.let { RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, it) }

                containerLayout?.addView(bleepsView, 0)

                retrieveBleeps()
            }
        }
    }

    fun sendBleep(bleep: Bleep) {
        val inputConnection = currentInputConnection
        if (inputConnection != null) {
            val currentText = inputConnection.getExtractedText(ExtractedTextRequest(), 0).text
            val beforeCursorText = inputConnection.getTextBeforeCursor(currentText.length, 0)
            val afterCursorText = inputConnection.getTextAfterCursor(currentText.length, 0)

            inputConnection.deleteSurroundingText(beforeCursorText.length, afterCursorText.length)

            inputConnection.commitText("♪ " + bleep.title + " ♪\n" + bleep.url, 1)

            inputConnection.sendKeyEvent(
                KeyEvent(
                    KeyEvent.ACTION_DOWN,
                    KEYCODE_ENTER
                )
            )

            toggleBleepsViewButton?.setImageResource(R.drawable.ic_music_note_black_24dp)

            searchEditText?.visibility = View.GONE
            appNameTextView?.visibility = View.VISIBLE

            containerLayout?.removeView(bleepsView)
            containerLayout?.addView(keyboardView, 0)
        }
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

    private fun setCaps(active: Boolean) {
        caps = active
        keyboard?.isShifted = caps
        keyboardView?.invalidateAllKeys()
    }
}