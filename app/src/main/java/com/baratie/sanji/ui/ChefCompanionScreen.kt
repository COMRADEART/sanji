package com.baratie.sanji.ui

import android.speech.tts.TextToSpeech
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChefCompanionScreen(
    viewModel: ChefViewModel, 
    onBack: () -> Unit,
    onOpenVision: () -> Unit,
    onStartCooking: () -> Unit,
    onOpenGallery: () -> Unit,
    onOpenProfile: () -> Unit
) {
    val chatHistory by viewModel.chatHistory.collectAsState()
    val chefState by viewModel.chefState.collectAsState()
    val currentAction by viewModel.currentAction.collectAsState()
    val context = LocalContext.current
    
    // TTS Initialization
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.UK 
            }
        }
    }

    // Speak when Sanji responds
    LaunchedEffect(chefState) {
        if (chefState is ChefState.Success) {
            tts?.speak(chefState.response, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BARATIE MENTOR", style = MaterialTheme.typography.titleMedium.copy(letterSpacing = 2.sp)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onOpenProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Chef ID", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onOpenGallery) {
                        Icon(Icons.Default.Star, contentDescription = "Masterpiece Gallery", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onOpenVision) {
                        Icon(Icons.Default.Kitchen, contentDescription = "Scan", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                    )
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Visual Sanji Avatar
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                SanjiVisualPersona(currentAction)
            }

            // Dialogue Feedback Box
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                tonalElevation = 8.dp,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val lastMessage = chatHistory.lastOrNull { it.role == "model" }?.text
                    
                    Text(
                        text = when {
                            chefState is ChefState.Thinking -> "Sanji is preparing his advice..."
                            lastMessage != null -> lastMessage
                            else -> "I'm listening. What's on the cutting board today?"
                        },
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontStyle = FontStyle.Italic,
                            lineHeight = 28.sp
                        ),
                        textAlign = TextAlign.Center,
                        maxLines = 4
                    )
                }
            }

            // Multimodal Interaction Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mic Button
                FloatingActionButton(
                    onClick = { /* TODO: Trigger Speech Recognition */ },
                    containerColor = if (chefState is ChefState.Thinking) Color.Gray else MaterialTheme.colorScheme.primary,
                    shape = CircleShape,
                    modifier = Modifier.size(80.dp)
                ) {
                    Icon(
                        imageVector = if (chefState is ChefState.Thinking) Icons.Default.HourglassEmpty else Icons.Default.Mic,
                        contentDescription = "Talk to Sanji",
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                }
                
                if (chatHistory.any { it.role == "model" }) {
                    Spacer(modifier = Modifier.width(24.dp))
                    IconButton(onClick = onStartCooking) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Start Cook", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(40.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SanjiVisualPersona(action: SanjiAction) {
    val infiniteTransition = rememberInfiniteTransition(label = "Cooking")
    
    val glowColor = when(action) {
        SanjiAction.PASSIONATE -> MaterialTheme.colorScheme.secondary
        SanjiAction.THINKING -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.tertiary
    }
    
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
        label = "Glow"
    )

    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(300.dp)
                .background(glowColor.copy(alpha = glowAlpha), CircleShape)
        )

        AnimatedContent(
            targetState = action,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "SanjiAction"
        ) { targetAction ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = when(targetAction) {
                        SanjiAction.CHOPPING -> Icons.Default.ContentCut
                        SanjiAction.STIRRING -> Icons.Default.Refresh
                        SanjiAction.PLATING -> Icons.Default.AutoAwesome
                        SanjiAction.PASSIONATE -> Icons.Default.LocalFireDepartment
                        SanjiAction.THINKING -> Icons.Default.TipsAndUpdates
                        else -> Icons.Default.Person
                    },
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = targetAction.name,
                    style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 4.sp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            }
        }
    }
}
