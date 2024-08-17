package com.example.matrng

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import java.io.IOException
import java.time.DayOfWeek
import java.time.LocalDate

class MainActivity : AppCompatActivity() {
    private val healthyMealsUrl = "https://kims.github.io/mat.json"
    private val unhealthyMealsUrl = "https://kims.github.io/tjockis.json"
    private val client = OkHttpClient()
    private var meals: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mealTextView: TextView = findViewById(R.id.mealTextView)
        val rollButton: Button = findViewById(R.id.rollButton)

        // Fetch meals from the appropriate URL
        fetchMeals()

        rollButton.setOnClickListener {
            if (meals.isNotEmpty()) {
                val randomMeal = meals.random()
                mealTextView.text = randomMeal
            } else {
                mealTextView.text = "Loading..."
            }
        }
    }

    private fun fetchMeals() {
        // Determine the URL to use based on the current day
        val urlToFetch = if (LocalDate.now().dayOfWeek == DayOfWeek.SATURDAY) {
            unhealthyMealsUrl
        } else {
            healthyMealsUrl
        }

        val request = Request.Builder()
            .url(urlToFetch)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    // Handle failure (e.g., show an error message)
                    findViewById<TextView>(R.id.mealTextView).text = "Failed to load meals"
                }
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                if (!response.isSuccessful) {
                    runOnUiThread {
                        findViewById<TextView>(R.id.mealTextView).text = "Failed to load meals"
                    }
                    return
                }

                response.body?.string()?.let { jsonString ->
                    try {
                        val jsonArray = JSONArray(jsonString)
                        meals = List(jsonArray.length()) { jsonArray.getString(it) }
                        runOnUiThread {
                            // Update the UI to reflect that meals have been loaded
                            findViewById<TextView>(R.id.mealTextView).text = "Maaaaaaaat?"
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        runOnUiThread {
                            findViewById<TextView>(R.id.mealTextView).text = "Error parsing meals"
                        }
                    }
                }
            }
        })
    }
}