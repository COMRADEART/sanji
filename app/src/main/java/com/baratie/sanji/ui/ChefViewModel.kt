package com.baratie.sanji.ui

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baratie.sanji.BuildConfig
import com.baratie.sanji.model.CookStep
import com.baratie.sanji.model.Recipe
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.Calendar

data class ChatMessage(
    val role: String,
    val text: String
)

sealed class ChefState {
    object Idle : ChefState()
    object Thinking : ChefState()
    data class Success(val response: String) : ChefState()
    data class Error(val message: String) : ChefState()
}

enum class SanjiAction {
    IDLE, CHOPPING, STIRRING, PLATING, PASSIONATE, THINKING
}

enum class KitchenChaosState {
    IDLE, LUFFY_STEALING, NAMI_STOPS_HIM, LUFFY_EATING
}

class ChefViewModel : ViewModel() {
    private val _greeting = MutableStateFlow("")
    val greeting: StateFlow<String> = _greeting

    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatHistory: StateFlow<List<ChatMessage>> = _chatHistory

    private val _chefState = MutableStateFlow<ChefState>(ChefState.Idle)
    val chefState: StateFlow<ChefState> = _chefState

    private val _currentAction = MutableStateFlow(SanjiAction.IDLE)
    val currentAction: StateFlow<SanjiAction> = _currentAction

    private val _chaosState = MutableStateFlow(KitchenChaosState.IDLE)
    val chaosState: StateFlow<KitchenChaosState> = _chaosState

    private val _activeRecipe = MutableStateFlow<Recipe?>(null)
    val activeRecipe: StateFlow<Recipe?> = _activeRecipe

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        systemInstruction = content {
            text("You are Vinsmoke Sanji, the world-class sous chef from the Baratie. " +
                 "You are a professional culinary mentor. Your tone is sophisticated, disciplined, and passionate. " +
                 "You are visually present on screen helping the user cook. " +
                 "If you are explaining a cutting technique, start your response with [ACTION:CHOPPING]. " +
                 "If you are explaining cooking at the stove, start with [ACTION:STIRRING]. " +
                 "If you are praising the user or excited, start with [ACTION:PASSIONATE]. " +
                 "If you are showing the final dish, start with [ACTION:PLATING]. " +
                 "Otherwise, assume [ACTION:IDLE]. " +
                 "Keep instructions concise for audio-to-audio conversation.")
        }
    )

    private val chat = generativeModel.startChat()

    // Mock recipe
    val risottoRecipe = Recipe(
        id = "1",
        title = "Prawn Risotto with Saffron",
        description = "A delicate masterpiece inspired by the East Blue.",
        toolRecommendation = "Grab a heavy-bottomed sauté pan and your finest Santoku knife.",
        steps = listOf(
            CookStep(1, "Mince", "Finely mince the shallots. Precision is key, don't rush it.", isCritical = true),
            CookStep(2, "Sauté", "Sauté the shallots in butter until translucent.", durationSeconds = 120),
            CookStep(3, "Toast", "Add the rice and toast it until the edges are clear.", durationSeconds = 180),
            CookStep(4, "Simmer", "Gradually add the saffron-infused broth. Respect the rhythm of the boil.", durationSeconds = 900),
            CookStep(5, "Plate", "Plate with the seared prawns. Make it beautiful.")
        )
    )

    init {
        updateGreeting()
    }

    private fun updateGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        _greeting.value = when (hour) {
            in 5..11 -> "Good morning! The kitchen is pristine and ready for service."
            in 12..17 -> "Good afternoon. Shall we prepare a masterpiece for lunch?"
            in 18..22 -> "Good evening! A chef's passion never sleeps. What's on the menu?"
            else -> "Late night service? I like your dedication. Let's cook."
        }
    }

    fun startCooking(recipe: Recipe) {
        _activeRecipe.value = recipe
        viewModelScope.launch {
            while (_activeRecipe.value != null) {
                delay(15000) // Every 15 seconds, Luffy might try something
                triggerChaos()
            }
        }
    }

    fun triggerChaos() {
        viewModelScope.launch {
            if (_activeRecipe.value != null && _chaosState.value == KitchenChaosState.IDLE) {
                _chaosState.value = KitchenChaosState.LUFFY_STEALING
                delay(4000)
                _chaosState.value = KitchenChaosState.NAMI_STOPS_HIM
                delay(3000)
                _chaosState.value = KitchenChaosState.IDLE
            }
        }
    }

    fun finishCooking() {
        viewModelScope.launch {
            _chaosState.value = KitchenChaosState.LUFFY_EATING
            delay(6000)
            _activeRecipe.value = null
            _chaosState.value = KitchenChaosState.IDLE
        }
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return
        
        _chatHistory.value += ChatMessage("user", userText)
        _chefState.value = ChefState.Thinking
        _currentAction.value = SanjiAction.THINKING

        viewModelScope.launch {
            try {
                val response = chat.sendMessage(userText)
                var responseText = response.text ?: "I have no words for this... let's try again."
                
                val action = when {
                    responseText.contains("[ACTION:CHOPPING]") -> SanjiAction.CHOPPING
                    responseText.contains("[ACTION:STIRRING]") -> SanjiAction.STIRRING
                    responseText.contains("[ACTION:PASSIONATE]") -> SanjiAction.PASSIONATE
                    responseText.contains("[ACTION:PLATING]") -> SanjiAction.PLATING
                    else -> SanjiAction.IDLE
                }
                
                responseText = responseText
                    .replace("[ACTION:CHOPPING]", "")
                    .replace("[ACTION:STIRRING]", "")
                    .replace("[ACTION:PASSIONATE]", "")
                    .replace("[ACTION:PLATING]", "")
                    .replace("[ACTION:IDLE]", "").trim()

                _currentAction.value = action
                _chatHistory.value += ChatMessage("model", responseText)
                _chefState.value = ChefState.Success(responseText)
            } catch (e: Exception) {
                _currentAction.value = SanjiAction.IDLE
                _chefState.value = ChefState.Error(e.localizedMessage ?: "The flame went out.")
            }
        }
    }

    fun analyzeDishMasterpiece(bitmap: Bitmap) {
        _chefState.value = ChefState.Thinking
        _currentAction.value = SanjiAction.PLATING

        viewModelScope.launch {
            try {
                val inputContent = content {
                    image(bitmap)
                    text("Critique this dish as Vinsmoke Sanji. " +
                         "Focus on the plating, the colors, and the technique. " +
                         "Be sophisticated but strict. If it looks delicious, show your passion. " +
                         "If it looks like a mess, tell me how to fix it next time. " +
                         "Don't waste words - keep it to a chef's summary.")
                }

                val response = generativeModel.generateContent(inputContent)
                val responseText = response.text ?: "I'm speechless... and not in a good way. Try again."
                
                _chefState.value = ChefState.Success(responseText)
                _chatHistory.value += ChatMessage("model", "[Masterpiece Critique]: $responseText")
            } catch (e: Exception) {
                _chefState.value = ChefState.Error(e.localizedMessage ?: "The lens is blurred. I can't see the dish.")
            }
        }
    }
}
