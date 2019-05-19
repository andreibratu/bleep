package com.broads.bleep

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.broads.bleep.entities.Bleep
import com.broads.bleep.tasks.BleepTask
import org.json.JSONObject


//// Async request
//val request = object: JsonObjectRequest(
//    Method.POST, apiUrl, postParams,
//    Response.Listener { response ->
//        bleeps.add(
//            Bleep(
//                data["title"] as String,
//                data["publisher"] as String,
//                data["popularity"] as Int,
//                response["shortUrl"] as String
//            )
//        )
//    },
//    Response.ErrorListener{ e ->
//        Toast.makeText(contextReference.get(), e.message, Toast.LENGTH_SHORT).show()
//    })
//{
//    // Set request custom headers
//    override fun getHeaders(): MutableMap<String, String> {
//        return mutableMapOf(
//            "Content-type" to "application/json",
//            "apiKey" to apiKey,
//            "workspace" to apiWorkspace
//        )
//    }
//}

class MainActivity : AppCompatActivity() {

    private val queue: RequestQueue = Volley.newRequestQueue(this)
    private val apiKey: String = "369db66db7504d78aaa55135e6b8d5c7"
    private val apiUrl: String = "https://api.rebrandly.com/v1/links"
    private val apiWorkspace: String = "3e2dcccf869b40c093298caed2239f57"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val bleeps = mutableListOf<Bleep>()

        for(bleep in BleepTask().execute().get()) {
            val postParams = JSONObject(
                mutableMapOf(
                    "destination" to bleep.url,
                    "title" to "Bleep it!",
                    "domain" to JSONObject(mutableMapOf("fullName" to "rebrand.ly"))
                )
            )
            val request = object : JsonObjectRequest(Method.POST, apiUrl, postParams,
                Response.Listener { response -> bleeps.add(Bleep(bleep.title, response["shortUrl"] as String)) },
                Response.ErrorListener { e -> Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show() }
            )
            {
                // Set request custom headers
                override fun getHeaders(): MutableMap<String, String> {
                    return mutableMapOf(
                        "Content-type" to "application/json",
                        "apiKey" to apiKey,
                        "workspace" to apiWorkspace
                    )
                }
            }

            queue.add(request)
        }
    }
}
