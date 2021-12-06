package com.example

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import java.io.*
import java.net.URL
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier


fun main() {
    PlaceInfoFinder().start();
}

class PlaceInfoFinder {
    fun step1(): Supplier<MutableList<String>> = Supplier<MutableList<String>> {
        val pointsList = mutableListOf<String>();
        Thread.sleep(5000)
        val urlString =
            "https://graphhopper.com/api/1/geocode?q=%22${getGeocode()}%22&key=62624b7b-852c-4f5c-acda-bcefee5bea9e"
        val `is`: InputStream = URL(urlString).openStream()
        var jsonText: String
        `is`.use { `is` ->
            val rd = BufferedReader(InputStreamReader(`is`, Charset.forName("UTF-8")))
            jsonText = readAll(rd)
        }
        val je: JsonElement = JsonParser().parse(jsonText)
        val jo = je.asJsonObject.get("hits").asJsonArray
        for (i in 0 until jo.size()) {
            val objec = jo.get(i)
            println("$(i + 1) ${objec.asJsonObject.get("country")} ${objec.asJsonObject.get("name")}")
            pointsList.add(i, objec.asJsonObject.get("point").toString());
        }
        pointsList;
    }

    @Throws(IOException::class)
    private fun readAll(rd: Reader): String {
        val sb = StringBuilder()
        var cp: Int
        while (rd.read().also { cp = it } != -1) {
            sb.append(cp.toChar())
        }
        return sb.toString()
    }

    fun start() {
        val result_1 = CompletableFuture.supplyAsync(step1())
        result_1.join()
        val index = getPlace();
        val cord = result_1.get()[index-1];
        println("hui again")
    }

    fun getGeocode(): String {
        println("Enter place")
        return Scanner(System.`in`).nextLine();
    }

    fun getPlace(): Int {
        println("Enter place number")
        return Scanner(System.`in`).nextInt();
    }
}