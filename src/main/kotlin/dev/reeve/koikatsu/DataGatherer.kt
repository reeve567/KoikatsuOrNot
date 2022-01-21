package dev.reeve.koikatsu

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.closeQuietly
import org.jsoup.Jsoup
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import javax.imageio.ImageIO

val gson: Gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
val client = OkHttpClient()
val gamesFile = File("./games.json")
val testGamesFile = File("./testGames.json")
val games: Games = if (gamesFile.exists()) {
	gson.fromJson(gamesFile.reader(), Games::class.java)
} else {
	Games()
}
val testGames: Games = if (testGamesFile.exists()) {
	gson.fromJson(testGamesFile.reader(), Games::class.java)
} else {
	Games()
}
val gamesDir = File("./games")

var tasks = 0

@OptIn(DelicateCoroutinesApi::class)
fun main() = runBlocking {
	if (!gamesDir.exists()) gamesDir.mkdirs()
	
	fun runGame(game: Games.Game) {
		GlobalScope.launch {
			tasks++
			val document = Jsoup.parse(URL(game.link), 5000)
			
			val title = document.getElementsByClass("p-title-value").firstOrNull()?.text()
				?.replace("[VN]", "")?.replace("[Completed]", "")?.replace("[Onhold]", "")?.replace("[Abandoned]", "")
				?.substringAfter(']')
				?.substringBefore('[')?.trim()?.convertToFile() ?: error("Could not find title")
			
			println("Started $title")
			game.title = title
			
			val firstPost = document.getElementsByClass("bbWrapper").firstOrNull() ?: error("Could not find the first post")
			
			val images = firstPost.getElementsByClass("bbImage").map {
				var value = it.attr("src")
				if (value.contains("/thumb/")) value = value.replace("/thumb", "")
				value
			}
			
			val folder = File(gamesDir, title)
			if (!folder.exists()) folder.mkdirs()
			
			image@ for (image in images) {
				val file = File(folder, image.substringAfterLast('/'))
				
				if (file.exists() || file.extension == "gif") continue@image
				
				val request = Request.Builder().url(image).build()
				val response = client.newCall(request).execute()
				val body = response.body ?: error("Missing image")
				val inputStream = body.byteStream()
				try {
					val image = ImageIO.read(inputStream)
					
					if (image.width / image.height.toDouble() != 16.0 / 9.0) {
						response.closeQuietly()
						body.closeQuietly()
						inputStream.closeQuietly()
						continue@image
					}
					try {
						val imageWidth = imageWidth.toInt()
						val imageHeight = imageHeight.toInt()
						
						val resizedImage = BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_USHORT_GRAY)
						val graphics = resizedImage.createGraphics()
						graphics.drawImage(image, 0, 0, imageWidth, imageHeight, null)
						graphics.dispose()
						
						ImageIO.write(resizedImage, file.extension, file)
					} catch (e: IllegalArgumentException) {
						println("Unknown image type 0 $file for title $title")
					}
					
				} catch (e: IndexOutOfBoundsException) {
					println("Could not download file $file for title $title")
				}
				
				response.closeQuietly()
				body.closeQuietly()
				inputStream.closeQuietly()
			}
			println("Finished $title, ${tasks--} left")
		}
	}
	
	for (game in games) {
		runGame(game)
	}
	
	for (game in testGames) {
		runGame(game)
	}
	
	while (tasks != 0) {
		delay(500)
	}
	
	gamesFile.writeText(gson.toJson(games))
	testGamesFile.writeText(gson.toJson(testGames))
}