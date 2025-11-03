package com.example.athletesync

import android.content.Context
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

class WeatherManager(private val context: Context) {

    companion object {
        private const val TAG = "WeatherManager"
        // Port Elizabeth coordinates
        private const val PORT_ELIZABETH_LAT = -33.9608
        private const val PORT_ELIZABETH_LON = 25.6022
    }

    private val weatherApiService: WeatherApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(WeatherApiService::class.java)
    }

    // Use secure API key from resources
    private val apiKey = context.getString(R.string.openweather_api_key)

    interface WeatherCallback {
        fun onWeatherReceived(weatherData: WeatherData)
        fun onError(message: String)
    }

    fun getCurrentWeather(callback: WeatherCallback) {
        Log.d(TAG, "Fetching weather for Port Elizabeth...")

        // Always use Port Elizabeth coordinates directly
        fetchWeatherByLocation(PORT_ELIZABETH_LAT, PORT_ELIZABETH_LON, callback)
    }

    private fun fetchWeatherByLocation(lat: Double, lon: Double, callback: WeatherCallback) {
        Log.d(TAG, "Fetching weather for coordinates: $lat, $lon")

        val call = weatherApiService.getCurrentWeather(lat, lon, "metric", apiKey)
        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                Log.d(TAG, "Weather API response code: ${response.code()}")

                if (response.isSuccessful) {
                    response.body()?.let { weatherResponse ->
                        Log.d(TAG, "Raw temperature: ${weatherResponse.main.temperature}°C")
                        Log.d(TAG, "Weather condition: ${weatherResponse.weather.firstOrNull()?.main}")
                        Log.d(TAG, "City name from API: ${weatherResponse.cityName}")
                        Log.d(TAG, "Humidity: ${weatherResponse.main.humidity}%")
                        Log.d(TAG, "Wind speed: ${weatherResponse.wind.speed} km/h")

                        // Convert temperature to Int properly
                        val temperature = weatherResponse.main.temperature.toInt()

                        // Use the city name from API, but if it's wrong, force "Port Elizabeth"
                        val cityName = if (weatherResponse.cityName.isNullOrEmpty() ||
                            weatherResponse.cityName.contains("Mountain View", true)) {
                            "Port Elizabeth"
                        } else {
                            weatherResponse.cityName
                        }

                        val weatherData = WeatherData(
                            temperature = temperature,
                            condition = weatherResponse.weather.firstOrNull()?.main ?: "Unknown",
                            description = weatherResponse.weather.firstOrNull()?.description ?: "",
                            city = cityName,
                            humidity = weatherResponse.main.humidity,
                            windSpeed = weatherResponse.wind.speed.toInt(),
                            iconCode = weatherResponse.weather.firstOrNull()?.icon ?: "01d"
                        )

                        Log.d(TAG, "Final WeatherData: $temperature°C, ${weatherData.condition}, ${weatherData.city}")
                        callback.onWeatherReceived(weatherData)
                    } ?: run {
                        Log.e(TAG, "Response body is null")
                        callback.onError("No weather data received")
                    }
                } else {
                    Log.e(TAG, "API Error: ${response.code()} - ${response.message()}")
                    when (response.code()) {
                        401 -> callback.onError("Invalid API key")
                        404 -> callback.onError("Location not found")
                        429 -> callback.onError("API limit exceeded")
                        else -> callback.onError("Server error: ${response.code()}")
                    }
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Log.e(TAG, "Network error: ${t.message}")
                callback.onError("Network error: ${t.message}")
            }
        })
    }
}