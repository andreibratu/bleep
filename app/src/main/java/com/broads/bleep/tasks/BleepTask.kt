package com.broads.bleep.tasks

import android.content.Context
import android.os.AsyncTask
import android.widget.Toast
import com.broads.bleep.entities.Bleep
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.*
import org.json.JSONObject
import java.lang.ref.WeakReference


@Suppress("UNCHECKED_CAST")
class BleepTask : AsyncTask<String, Nothing, List<Bleep>>() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    override fun doInBackground(vararg params: String?): List<Bleep> {
        /**
         * Return 5 best bleeps for given keywords
        */
        val bleeps = mutableListOf<Bleep>()

        if (params.isEmpty()) {
            // No keywords, filter by popularity
            this.db.collection("bleeps").limit(5).get().addOnSuccessListener { bleepRefs ->
                bleepRefs.forEach { data ->
                    val storageRef = data["storageRef"] as String
                    // POST to get short download URL
                    storage.getReferenceFromUrl(storageRef).downloadUrl.addOnSuccessListener { downloadUrl ->
                        bleeps.add(Bleep(
                            data["title"] as String,
                            downloadUrl.path
                        ))
                    }
                }
            }
        }

        else {
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
                    val storageRef = data["storageRef"] as String
                    storage.getReferenceFromUrl(storageRef).downloadUrl.addOnSuccessListener { downloadUrl ->
                        bleeps.add(Bleep(
                            data["title"] as String,
                            downloadUrl.path
                        ))
                    }
                }
            }
        }
        return bleeps
    }
}