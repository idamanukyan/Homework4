package com.example.homework4

import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(RobolectricTestRunner::class)
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    private val mockWeatherApiService = mockk<WeatherApiService>()
    private val mockLocationPermissionRequester = mockk<MainActivity.LocationPermissionRequester>()
    private val mockNavController = mockk<NavHostController>()

    @Test
    fun testWelcomeScreen() {
        val mockWeatherInfo = "Mock Weather Info"
        coEvery { mockLocationPermissionRequester.requestLocationPermission(any()) } coAnswers {
            secondArg<(String) -> Unit>().invoke(mockWeatherInfo)
        }

        val locationPermissionViewModel = MainActivity.LocationPermissionViewModel()

        WelcomeScreen(
            navController = mockNavController,
            locationPermissionRequester = mockLocationPermissionRequester,
            weatherApiService = mockWeatherApiService,
            temperatureUnit = TemperatureUnit.Celsius,
            onTemperatureUnitChanged = { }
        )


        coEvery { mockWeatherApiService.getWeatherForCity(any()) } coAnswers {
            mockk<WeatherResponse>()
        }

        @Test
        fun testLocationPermissionRequester() {
            val mockWeatherInfo = "Mock Weather Info"
            coEvery { mockLocationPermissionRequester.requestLocationPermission(any()) } coAnswers {
                secondArg<(String) -> Unit>().invoke(mockWeatherInfo)
            }

            val locationPermissionViewModel = MainActivity.LocationPermissionViewModel()


            val locationPermissionRequester =
                MainActivity.LocationPermissionRequester(
                    mockNavController,
                    locationPermissionViewModel
                )
        }

        @Test
        fun testLocationPermissionViewModel() {
            val locationPermissionViewModel = MainActivity.LocationPermissionViewModel()
            assert(!locationPermissionViewModel.isPermissionRequested)
            locationPermissionViewModel.onPermissionRequested()
            assert(locationPermissionViewModel.isPermissionRequested)
        }

        @Test
        fun testWeatherApiService() {
            val cityName = "Berlin"
            val mockWeatherResponse = mockk<WeatherResponse>()
            coEvery { mockWeatherApiService.getWeatherForCity(cityName) } returns mockWeatherResponse
        }

        @Test
        fun testNavigation() {
            val cityName = "Berlin"
            val navController = rememberNavController()
            navController.navigate("second_screen/$cityName")
        }

        @Test
        fun testWelcomeScreenUI() {
            val mockWeatherInfo = "Mock Weather Info"
            val locationPermissionViewModel = MainActivity.LocationPermissionViewModel()

            WelcomeScreen(
                navController = mockNavController,
                locationPermissionRequester = mockLocationPermissionRequester,
                weatherApiService = mockWeatherApiService,
                temperatureUnit = TemperatureUnit.Celsius,
                onTemperatureUnitChanged = {}
            )
        }


    }

}