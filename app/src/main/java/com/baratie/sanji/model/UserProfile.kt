package com.baratie.sanji.model

enum class ChefRank(val label: String, val threshold: Int) {
    CHORE_BOY("Chore Boy", 0),
    LINE_COOK("Line Cook", 5),
    SOUS_CHEF("Sous Chef", 15),
    HEAD_CHEF("Head Chef", 30),
    ALL_BLUE_MASTER("All Blue Master", 50)
}

data class UserProfile(
    val name: String,
    val dishesCooked: Int = 0,
    val sanjiRapport: Int = 0
) {
    val rank: ChefRank
        get() = ChefRank.entries.last { dishesCooked >= it.threshold }
}
