package dev.reeve.koikatsu

class Games: ArrayList<Games.Game>() {
	
	data class Game(val type: GameType, val link: String, var title: String? = null)
	
	enum class GameType {
		Koikatsu,
		Realism,
		Drawn
	}
}