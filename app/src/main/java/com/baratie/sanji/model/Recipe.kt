package com.baratie.sanji.model

data class Recipe(
    val id: String,
    val title: String,
    val description: String,
    val toolRecommendation: String,
    val steps: List<CookStep>
)

data class CookStep(
    val id: Int,
    val action: String, // e.g., "Mince", "Sauté", "Plate"
    val instruction: String,
    val durationSeconds: Int? = null,
    val isCritical: Boolean = false
)
