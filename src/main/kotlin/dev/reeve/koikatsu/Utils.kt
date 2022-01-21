package dev.reeve.koikatsu

import java.util.regex.Pattern

fun String.convertToFile(): String {
	val pattern = Pattern.compile("([^A-Za-z0-9])+")
	return pattern.split(this).joinToString("")
}