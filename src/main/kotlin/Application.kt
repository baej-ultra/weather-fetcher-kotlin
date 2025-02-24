import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.apache.commons.cli.*
import java.util.*

val apiKey = System.getenv("WEATHER_API_KEY")

fun main(args: Array<String>) = runBlocking {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
    val queryParams = getArgs(args)
    val geoCode = getGeocode(queryParams, client)
    val weatherData = getWeather(geoCode, client)

    println(
        """
        Weather in ${geoCode.name}
        ${weatherData.weather[0].main} - ${weatherData.weather[0].description}
        Temperature: ${weatherData.main.temp} C
        Feels like: ${weatherData.main.feels_like}
        Humidity: ${weatherData.main.humidity}
        Pressure: ${weatherData.main.pressure}        
    """.trimIndent()
    )
}

fun getArgs(args: Array<String>): WeatherQuery {
    val options = Options()
    val parser: CommandLineParser = DefaultParser()

    options.addOption("zip", true, "Specify the zip code")
    options.addOption("country", true, "Specify the country")

    val parsed: CommandLine
    try {
        //parse commands
        parsed = parser.parse(options, args)
    } catch (e: ParseException) {
        error("Invalid command (${e.message})")
    }

    val zip: String = parsed?.let {
        val value = it.getOptionValue("zip")
        requireNotNull(value) { "Zip code required" }
    } ?: error("Invalid commands")

    val countryCode: String = parsed.let {
        val value = it.getOptionValue("country")
        if (!isValidISOCountryCode(value)) {
            error("Invalid country code")
        }
        requireNotNull(value) { "Country code required" }
    }

    return WeatherQuery(zip, countryCode)
}

fun isValidISOCountryCode(code: String): Boolean {
    if (code.length != 2) {
        return false
    }
    val isoCountries = Locale.getISOCountries()
    return isoCountries.contains(code)
}

suspend fun getGeocode(weatherQuery: WeatherQuery, client: HttpClient): Geocode {
    val zip = weatherQuery.zip
    val countryCode = weatherQuery.countryCode
    val response =
        client.get("http://api.openweathermap.org/geo/1.0/zip?zip=$zip,$countryCode&appid=$apiKey")
    if (response.status != HttpStatusCode.OK) {
        error("Geocode API error")
    }

    return response.body()
}

suspend fun getWeather(geocode: Geocode, client: HttpClient): WeatherData {
    val lat = geocode.lat
    val lon = geocode.lon
    val response =
        client.get("https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&units=metric&appid=$apiKey")
    if (response.status != HttpStatusCode.OK) {
        error("Weather API error")
    }
    return response.body()
}

data class WeatherQuery(val zip: String, val countryCode: String)

@Serializable
data class Geocode(
    val lon: Double,
    val lat: Double,
    val name: String
)

@Serializable
data class WeatherData(
    val main: WeatherDetails,
    val weather: Array<WeatherGeneral>
)

@Serializable
data class WeatherGeneral(
    val description: String,
    val main: String
)

@Serializable
data class WeatherDetails(
    val feels_like: Double,
    val temp: Double,
    val humidity: Int,
    val pressure: Int
)
