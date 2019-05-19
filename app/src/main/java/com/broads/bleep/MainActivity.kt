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


class MainActivity : AppCompatActivity(), Observer {

    private val queue: RequestQueue = Volley.newRequestQueue(this)
    private val bleepTask: BleepTask = BleepTask()
    var bleeps: MutableList<Bleep> = mutableListOf()

    override fun processFinish(bleeps: List<Bleep>) {
        var counter = 0
        val result: MutableList<Bleep> = mutableListOf()

        for(bleep in bleeps) {
            val postParams = JSONObject(
                mutableMapOf(
                    "destination" to bleep.url,
                    "title" to "Bleep it!",
                    "domain" to JSONObject(mutableMapOf("fullName" to "rebrand.ly"))
                )
            )
            val request = object : JsonObjectRequest(Method.POST, "https://api.rebrandly.com/v1/links", postParams,
                Response.Listener { response ->
                    result.add(Bleep(bleep.title, response["shortUrl"] as String))
                    counter -= 1
                    if(counter == 0) {
                        this.bleeps = result
                    }
                },
                Response.ErrorListener { e -> Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show() }
            )
            {
                // Set request custom headers
                override fun getHeaders(): MutableMap<String, String> {
                    return mutableMapOf(
                        "Content-type" to "application/json",
                        "apiKey" to "369db66db7504d78aaa55135e6b8d5c7",
                        "workspace" to "3e2dcccf869b40c093298caed2239f57"
                    )
                }
            }

            queue.add(request)
            counter += 1
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bleepTask.observer = this
        bleepTask.execute()
    }
}
