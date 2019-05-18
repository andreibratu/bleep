package com.broads.bleep.services

import com.broads.bleep.entities.Bleep
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import java.io.File

@Suppress("UNCHECKED_CAST")
class StorageService {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    fun query(keywords: Array<String>): List<Bleep> {
        // Return best 5 bleeps for given keywords
        var bleeps = mutableListOf<Bleep>()

        if (keywords.isEmpty()) {
            // No keywords, filter by popularity
            this.db.collection("bleeps").orderBy("popularity", Query.Direction.DESCENDING)
            .limit(5).get().addOnSuccessListener { bleepRefs ->
                bleepRefs.forEachIndexed { idx, data ->
                    val url = data["storageRef"] as String
                    val file = File.createTempFile("bleep$idx", "mp4")
                    storage.getReferenceFromUrl(url).getFile(file).addOnSuccessListener {
                        bleeps.add(Bleep(
                            data["title"] as String,
                            data["publisher"] as String,
                            data["popularity"] as Int,
                            data["storageRef"] as String,
                            file
                        ))
                    }
                }
            }
        }

        else {
            val keywordsMatched = hashMapOf<String, Int>()
            // Get references of bleeps that match the keywords
            keywords.forEach { keyword ->
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
            val bleepRefs = keywordsMatched.toList().sortedByDescending { it.second }.map { it.first }.take(10)


            // Fetch entities
            bleepRefs.forEachIndexed{ idx, bleepRef ->
                this.db.document(bleepRef).get().addOnSuccessListener { data ->
                    val url = data["storageRef"] as String
                    val file = File.createTempFile("bleep$idx", "mp4")
                    storage.getReferenceFromUrl(url).getFile(file).addOnSuccessListener {
                        bleeps.add(Bleep(
                            data["title"] as String,
                            data["publisher"] as String,
                            data["popularity"] as Int,
                            data["storageRef"] as String,
                            file
                        ))
                    }
                }
            }
            // Filter by popularity
            bleeps = bleeps.sortedByDescending { it.popularity }.take(5).toMutableList()
        }

        return bleeps
    }
}