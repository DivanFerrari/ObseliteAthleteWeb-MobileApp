package com.example.athletesync

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class ChatBotActivity : AppCompatActivity() {
    var recyclerView: RecyclerView? = null
    var welcomeTextView: TextView? = null
    var messageEditText: EditText? = null
    var sendButton: ImageButton? = null
    var messageList: MutableList<Message?>? = null
    var messageAdapter: MessageAdapter? = null
    var client: OkHttpClient = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_bot)
        messageList = ArrayList()

        recyclerView = findViewById(R.id.recycler_view)
        welcomeTextView = findViewById(R.id.welcome_text)
        messageEditText = findViewById(R.id.message_edit_text)
        sendButton = findViewById(R.id.send_btn)

        messageAdapter = MessageAdapter(messageList)
        recyclerView!!.adapter = messageAdapter
        val llm = LinearLayoutManager(this)
        llm.stackFromEnd = true
        recyclerView!!.layoutManager = llm

        sendButton!!.setOnClickListener { v: View? ->
            val question = messageEditText!!.text.toString().trim()
            if (question.isNotEmpty()) {
                addToChat(question, Message.SENT_BY_ME)
                messageEditText!!.setText("")
                callAPI(question)
                welcomeTextView!!.visibility = View.GONE
            }
        }
    }

    fun addToChat(message: String?, sentBy: String?) {
        runOnUiThread {
            messageList!!.add(Message(message, sentBy))
            messageAdapter!!.notifyDataSetChanged()
            recyclerView!!.smoothScrollToPosition(messageAdapter!!.itemCount)
        }
    }

    fun addResponse(response: String?) {
        messageList!!.removeAt(messageList!!.size - 1)
        addToChat(response, Message.SENT_BY_BOT)
    }

    fun callAPI(question: String?) {
        messageList!!.add(Message("Typing...", Message.SENT_BY_BOT))

        val jsonBody = JSONObject()
        try {
            jsonBody.put("model", "meta-llama/llama-3.3-70b-instruct:free")

            val messages = JSONArray()
            messages.put(JSONObject().put("role", "user").put("content", question))

            jsonBody.put("messages", messages)
            jsonBody.put("max_tokens", 400)
            jsonBody.put("temperature", 0.7)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val body: RequestBody = jsonBody.toString().toRequestBody(JSON)
        val request = Request.Builder()
            .url("https://openrouter.ai/api/v1/chat/completions")
            .header(
                "Authorization",
                "Bearer $API_KEY"
            )
            .header("HTTP-Referer", "http://localhost")
            .header("X-Title", "My Android Chatbot")
            .header("Content-Type", "application/json")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                addResponse("Failed to load response due to: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody: ResponseBody? = response.body
                val bodyString = responseBody?.string() ?: ""

                if (response.isSuccessful) {
                    try {
                        val jsonObject = JSONObject(bodyString)
                        val choices = jsonObject.getJSONArray("choices")
                        val result = choices.getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content")
                        addResponse(result.trim())
                    } catch (e: JSONException) {
                        addResponse("Failed to parse response: ${e.message}")
                    }
                } else {
                    addResponse("API Error: $bodyString")
                }

                responseBody?.close()
            }
        })
    }

    companion object {
        val JSON: MediaType = "application/json; charset=utf-8".toMediaType()

        private const val API_KEY =
            "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"
    }
}