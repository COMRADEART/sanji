package com.baratie.sanji

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.baratie.sanji.ui.*
import com.baratie.sanji.ui.theme.SanjiCookbookTheme

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        checkCameraPermission()

        setContent {
            SanjiCookbookTheme {
                val viewModel: ChefViewModel = viewModel()
                var currentScreen by remember { mutableStateOf("welcome") }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (currentScreen) {
                        "welcome" -> ChefWelcomeScreen(
                            viewModel = viewModel,
                            onEnterKitchen = { currentScreen = "chat" }
                        )
                        "chat" -> ChefCompanionScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = "welcome" },
                            onOpenVision = { currentScreen = "vision" },
                            onStartCooking = { 
                                viewModel.startCooking(viewModel.risottoRecipe)
                                currentScreen = "cook" 
                            },
                            onOpenGallery = { currentScreen = "gallery" },
                            onOpenProfile = { currentScreen = "profile" }
                        )
                        "vision" -> KitchenVisionScreen(
                            onBack = { currentScreen = "chat" }
                        )
                        "cook" -> ProCookScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = "chat" }
                        )
                        "gallery" -> MasterpieceGalleryScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = "chat" }
                        )
                        "profile" -> ProfileScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = "chat" },
                            onOpenArchive = { currentScreen = "archive" }
                        )
                        "archive" -> ArchiveScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = "profile" }
                        )
                    }
                }
            }
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
}
