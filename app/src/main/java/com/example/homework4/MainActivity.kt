package com.example.homework4

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.homework2.MainActivity.WeatherApiService
import com.example.homework4.ui.theme.Homework4Theme
import io.ktor.client.*
import io.ktor.client.request.*

enum class TemperatureUnit {
    Celsius,
    Fahrenheit
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Homework4Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val locationPermissionRequester = LocationPermissionRequester(this, viewModel())
                    val weatherApiService = WeatherApiService("your_api_key_here")
                    val temperatureUnit = remember { mutableStateOf(TemperatureUnit.Celsius) }

                    NavHost(navController = navController, startDestination = "welcome_screen") {
                        composable("welcome_screen") {
                            WelcomeScreen(
                                navController,
                                locationPermissionRequester,
                                weatherApiService,
                                temperatureUnit.value
                            ) { selectedUnit ->
                                temperatureUnit.value = selectedUnit
                            }
                        }
                        composable("second_screen/{cityName}") { backStackEntry ->
                            val cityName = backStackEntry.arguments?.getString("cityName") ?: ""
                            val cityInfo = citiesInfo.find { it.cityName == cityName }
                            cityInfo?.let {
                                SecondScreen(
                                    cityInfo,
                                    navController,
                                    weatherApiService,
                                    temperatureUnit.value
                                )
                            }
                        }
                    }
                }
            }
        }
    }


    @Composable
    fun WelcomeScreen(
        navController: NavHostController,
        locationPermissionRequester: LocationPermissionRequester,
        weatherApiService: WeatherApiService,
        temperatureUnit: TemperatureUnit,
        onTemperatureUnitChanged: (TemperatureUnit) -> Unit
    ) {

        DisposableEffect(Unit) {
            locationPermissionRequester.requestLocationPermission { weatherInfo ->
                Text(
                    text = "Current Location Weather: $weatherInfo",
                    style = TextStyle(fontSize = 16.sp),
                    modifier = Modifier.padding(8.dp)
                )
            }
            onDispose { }
        }

        // ...

        LaunchedEffect(locationPermissionRequester) {
            if (ContextCompat.checkSelfPermission(
                    activity,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                try {
                    val location =
                        getCurrentCity()
                    val currentLocationWeather = weatherApiService.getWeatherForCity(location)

                    currentLocationWeather?.let {
                        val temperatureValue = if (temperatureUnit == TemperatureUnit.Celsius) {
                            it.current.temp_c.toString() + "°C"
                        } else {
                            it.current.temp_c.toString() + "°F"
                        }

                        Text(
                            text = "Current Location Temperature: $temperatureValue",
                            style = TextStyle(fontSize = 16.sp),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                } catch (e: Exception) {
                }
            }
        }

    }


    @Composable
    fun SecondScreen(
        cityInfo: CityInfo,
        navController: NavHostController,
        weatherApiService: WeatherApiService,
        temperatureUnit: TemperatureUnit
    ) {
        // ...

        LaunchedEffect(cityInfo) {
            try {
                val weatherResponse = weatherApiService.getWeatherForCity(cityInfo.cityName)
                val temperature = weatherResponse.current.temp_c

                val temperatureValue = if (temperatureUnit == TemperatureUnit.Celsius) {
                    temperature.toString() + "°C"
                } else {
                    val temperatureFahrenheit = (temperature * 9 / 5) + 32
                    temperatureFahrenheit.toString() + "°F"
                }

                temperature = temperatureValue.toFloat()
            } catch (e: Exception) {
            }
        }

    }

    class LocationPermissionRequester(
        private val activity: ComponentActivity,
        private val viewModel: LocationPermissionViewModel
    ) {

        @Composable
        fun requestLocationPermission(onWeatherInfoReceived: (String) -> Unit) {

            citiesInfo.forEach { city ->
                Button(
                    onClick = { navController.navigate("second_screen/${city.cityName}") },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(text = "Städte erkunden in ${city.cityName}")
                }
            }
        }
    }

    @Composable
    fun SettingsPage(
        temperatureUnit: TemperatureUnit,
        onTemperatureUnitChanged: (TemperatureUnit) -> Unit
    ) {
    }


    class LocationPermissionViewModel : androidx.lifecycle.ViewModel() {
        var isPermissionRequested by mutableStateOf(false)
            private set

        fun onPermissionRequested() {
            isPermissionRequested = true
        }
    }
}
