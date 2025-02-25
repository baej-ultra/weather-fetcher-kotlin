# First Kotlin app

Simple weather app
<br>
## How to build and run
**Requires openweathermap.org API key**. Can be passed either as an env variable or slapped directly into code
<br><br>
Build with Gradle
<br>
`gradlew shadowJar` or `gradlew.bat shadowJar`. Builds to `./build/libs/`
<br><br>
Run:
```bash
WEATHER_API_KEY={api key} java -jar first-kotlin-1.0-all.jar -zip {zip code} -country {ISO country code}
```

```cmd
set WEATHER_API_KEY={api key} && java -jar first-kotlin-1.0-all.jar -zip {zip code} -country {ISO country code}
```

```powershell
$env:WEATHER_API_KEY="{api key}"; java -jar first-kotlin-1.0-all.jar -zip {zip code} -country {ISO country code}
```

<br>
Example for my home town

```
WEATHER_API_KEY={api key} java -jar first-kotlin-1.0-all.jar -zip 65-001 -country PL
```
Output:
```
Weather in Zielona Góra
Clouds - overcast clouds
Temperature: 8.87 C
Feels like: 6.98 C
Humidity: 41 %
Pressure: 1017 hPa
```
<br>
UX wasn't the priority. I wanted to test out the Apache Commons CLI library for parsing command line arguments, hence the non-interactive nature of this app.