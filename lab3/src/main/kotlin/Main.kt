import com.google.gson.JsonElement
import com.google.gson.JsonParser
import org.slf4j.LoggerFactory
import java.io.*
import java.lang.Thread.sleep
import java.net.URL
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.CompletableFuture


fun main() {
    val rv = PlaceInfoFinder().work()
    for (i in rv) {
        println(i)
    }
}

@Suppress("NAME_SHADOWING")
class PlaceInfoFinder {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val graphhopperAPIKey = "9ee5a8c8-ddf2-4ecb-ae05-83dad708f805"
    private val openweathermapAPIKey = "b0165c7939ff6bb00fa02f5e86efce9c"
    private val opentripmapAPIKey = "5ae2e3f221c38a28845f05b6f6f5297bd21dde18fd5ed7b1f68fbd98"

    @Throws(IOException::class)
    private fun readAll(
        rd: Reader
    ): String {
        val sb = StringBuilder()
        var cp: Int
        while (rd.read().also { cp = it } != -1) {
            sb.append(cp.toChar())
        }
        return sb.toString()
    }

    private fun getWeather(
        lat: String,
        lon: String
    ): CompletableFuture<String> {
        var description: String
        var windSpeed: String
        return CompletableFuture.supplyAsync {
            val urlString =
                "https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&format=json&appid=$openweathermapAPIKey"
            logger.info("weather url: $urlString")
            val je = getJsonElementByUrl(urlString)
            description = je.asJsonObject.get("weather")
                .asJsonArray.get(0)
                .asJsonObject.get("description").toString()
            windSpeed = je.asJsonObject.get("wind")
                .asJsonObject.get("speed").toString()
            return@supplyAsync "Weather info: $description, $windSpeed"
        }
    }

    private fun getInfoByxid(
        xid: String
    ): CompletableFuture<String> {

        return CompletableFuture.supplyAsync {
            val urlString = "https://api.opentripmap.com/0.1/ru/places/xid/${
                xid.substring(1..xid.length - 2)
            }?apikey=$opentripmapAPIKey"
            val stream: InputStream = URL(urlString).openConnection().getInputStream()
            var jsonText: String
            stream.use { stream ->
                val rd = BufferedReader(InputStreamReader(stream, Charset.forName("UTF-8")))
                jsonText = readAll(rd)
            }
            val je: JsonElement = JsonParser().parse(jsonText)
            stream.close()
            return@supplyAsync (
                    "city : ${je.asJsonObject.get("address").asJsonObject.get("city")}\n" +
                            "state : ${je.asJsonObject.get("address").asJsonObject.get("state")}\n" +
                            "county : ${je.asJsonObject.get("address").asJsonObject.get("county")}\n" +
                            "region : ${je.asJsonObject.get("address").asJsonObject.get("region")}\n" +
                            "country : ${je.asJsonObject.get("address").asJsonObject.get("country")}\n" +
                            "kinds : ${je.asJsonObject.get("kinds")}")
        }
    }

    private fun getInterestPlacesNear(
        lat: String,
        lon: String,
        list: MutableList<CompletableFuture<String>>
    ) {
        val urlString =
            "https://api.opentripmap.com/0.1/en/places/radius?radius=1000&lon=$lon&lat=$lat&limit=10&apikey=$opentripmapAPIKey"
        logger.info("places list url: $urlString")
        val je = getJsonElementByUrl(urlString)
        for (i in 0 until je.asJsonObject.get("features").asJsonArray.size()) {
            val xid =
                je.asJsonObject.get("features")
                    .asJsonArray.get(i)
                    .asJsonObject.get("properties")
                    .asJsonObject.get("xid")
                    .toString()
            list.add(getInfoByxid(xid))
            sleep(1000) //429 error
        }
    }

    fun work(): MutableList<String> {
        val futuresList = mutableListOf<CompletableFuture<String>>()
        val textList = mutableListOf<String>()
        val coords = CompletableFuture.supplyAsync {
            val urlString = "https://graphhopper.com/api/1/geocode?q=%22${getPlace()}%22&key=$graphhopperAPIKey"
            logger.info("graphhopper url: $urlString")
            val je = getJsonElementByUrl(urlString)
            logger.info("Get data success")
            val jo = je.asJsonObject.get("hits").asJsonArray
            for (i in 0 until jo.size()) {
                val objec = jo.get(i)
                println("$i ${objec.asJsonObject.get("country")} ${objec.asJsonObject.get("name")}")
            }
            logger.info("Get place success")
            var number: Int
            do {
                println("Enter place number. (less then ${jo.size()})")
                number = Scanner(System.`in`).nextInt()
            } while (number > jo.size())
            val cords = jo.get(number).asJsonObject.get("point").toString()
            val lat_lon = cords.split(",")
            val lat = lat_lon[0].substring(lat_lon[0].indexOf(":") + 1 until lat_lon[0].length)
            val lon = lat_lon[1].substring(lat_lon[1].indexOf(":") + 1 until lat_lon[1].length - 1)
            logger.info("get coords : $lat ; $lon")
            Pair(lat, lon)
            futuresList.add(getWeather(lat, lon))
            getInterestPlacesNear(lat, lon, futuresList)
            for (i in 0 until futuresList.size) {
                futuresList[i].join()
                textList.add(futuresList[i].get())
            }
        }
        coords.join()
        return textList
    }

    private fun getJsonElementByUrl(
        url: String
    ): JsonElement {
        val `in` = BufferedReader(InputStreamReader(URL(url).openStream()))
        var inputLine: String?
        val sb = StringBuilder()
        while (`in`.readLine()
                .also { inputLine = it } != null
        ) {
            sb.append(inputLine)
        }
        `in`.close()
        return JsonParser().parse(sb.toString())
    }

    private fun getPlace(
    ): String {
        println("Enter place")
        return Scanner(System.`in`).nextLine()
    }
}