package dev.reeve.koikatsu

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.apache.log4j.BasicConfigurator
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.jetbrains.kotlinx.dl.api.core.Sequential
import org.jetbrains.kotlinx.dl.api.core.activation.Activations
import org.jetbrains.kotlinx.dl.api.core.initializer.Constant
import org.jetbrains.kotlinx.dl.api.core.initializer.GlorotNormal
import org.jetbrains.kotlinx.dl.api.core.layer.core.Dense
import org.jetbrains.kotlinx.dl.api.core.layer.core.Input
import org.jetbrains.kotlinx.dl.api.core.layer.pooling.MaxPool2D
import org.jetbrains.kotlinx.dl.api.core.layer.reshaping.Flatten
import org.jetbrains.kotlinx.dl.api.core.loss.Losses
import org.jetbrains.kotlinx.dl.api.core.metric.Metrics
import org.jetbrains.kotlinx.dl.api.core.optimizer.*
import org.jetbrains.kotlinx.dl.dataset.OnHeapDataset
import java.io.File
import javax.imageio.ImageIO

val imageWidth = 96L
val imageHeight = 54L
val seed = 20L

fun main() = runBlocking {
	var tasks = 0
	
	BasicConfigurator.configure()
	Logger.getRootLogger().level = Level.OFF
	
	val dataSet = OnHeapDataset.createTrainAndTestDatasets(
		"./games.json",
		"./games.json",
		"./testGames.json",
		"./testGames.json",
		Games.GameType.values().size,
		::extractImages,
		::extractLabels
	)
	
	val (train, test) = dataSet
	
	fun runModel(activation: Activations, outerSize: Int, innerSize: Int, lastSize: Int, epochs: Int, optimizer: Optimizer) {
		tasks++
		val model = Sequential.of(
			Input(imageWidth, imageHeight, 1),
			Flatten(),
			Dense(
				outputSize = outerSize,
				activation = activation,
				kernelInitializer = GlorotNormal(seed),
				biasInitializer = Constant(0.1f)
			),
			Dense(
				outputSize = innerSize,
				activation = activation,
				kernelInitializer = GlorotNormal(seed),
				biasInitializer = Constant(0.1f)
			),
			Dense(
				outputSize = lastSize,
				activation = activation,
				kernelInitializer = GlorotNormal(seed),
				biasInitializer = Constant(0.1f)
			),
			Dense(
				outputSize = 3,
				activation = Activations.Linear,
				kernelInitializer = GlorotNormal(seed),
				biasInitializer = Constant(0.1f)
			)
		)
		
		model.use {
			it.compile(
				optimizer = optimizer,
				loss = Losses.HUBER,
				metric = Metrics.ACCURACY
			)
			
			it.fit(
				dataset = train,
				epochs = epochs,
				batchSize = 50
			)
			
			val accuracy = it.evaluate(dataset = test, batchSize = 25).metrics[Metrics.ACCURACY]
			
			println("$accuracy $outerSize, $innerSize, $lastSize, $epochs")
			//it.save(File("model/my_model"), writingMode = WritingMode.OVERRIDE)
			tasks--
		}
	}
	
	runModel(Activations.Tanh, 200, 300, 80, 40, Adam())
	
	while (tasks != 0) {
		delay(500)
	}
}

fun extractImages(path: String): Array<FloatArray> {
	val games = gson.fromJson(File(path).reader(), Games::class.java)
	
	val floatArrays = mutableListOf<FloatArray>()
	games.forEach {
		val dir = File(gamesDir, it.title)
		for (image in dir.listFiles()!!) {
			println("image:$image, dir:$dir, title: ${it.title}")
			val bufferedImage = ImageIO.read(image)
			val imageData = bufferedImage.data
			val pixels = imageData.getPixels(0, 0, imageData.width, imageData.height, null as FloatArray?)
			floatArrays.add(pixels)
		}
	}
	return floatArrays.toTypedArray()
}

fun extractLabels(path: String, numClasses: Int): FloatArray {
	val games = gson.fromJson(File(path).reader(), Games::class.java)
	
	val list = mutableListOf<Float>()
	games.forEach {
		val dir = File(gamesDir, it.title)
		for (image in dir.listFiles()!!) {
			list.add(it.type.ordinal.toFloat())
		}
	}
	return list.toFloatArray()
}