package com.baratie.sanji.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baratie.sanji.model.CookStep
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProCookScreen(viewModel: ChefViewModel, onBack: () -> Unit) {
    val recipe by viewModel.activeRecipe.collectAsState()
    val chaosState by viewModel.chaosState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(recipe?.title?.uppercase() ?: "ACTIVE SERVICE", style = MaterialTheme.typography.titleMedium.copy(letterSpacing = 2.sp)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            
            // --- KITCHEN CHAOS BACKGROUND LAYER ---
            KitchenChaosOverlay(chaosState)

            // --- MAIN CONTENT LAYER ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "CHEF'S ORDERS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = recipe?.toolRecommendation ?: "Prepare your tools.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    recipe?.steps?.let { steps ->
                        itemsIndexed(steps) { index, step ->
                            StaggeredStepItem(index, step)
                        }
                    }
                }

                Button(
                    onClick = { viewModel.finishCooking() },
                    enabled = chaosState == KitchenChaosState.IDLE,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("PLATE THE MASTERPIECE", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun KitchenChaosOverlay(state: KitchenChaosState) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Luffy
        AnimatedVisibility(
            visible = state == KitchenChaosState.LUFFY_STEALING || state == KitchenChaosState.LUFFY_EATING,
            enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(),
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = CircleShape,
                    color = Color(0xFFFFD700), // Luffy Straw Hat Yellow
                    border = androidx.compose.foundation.BorderStroke(4.dp, Color.Red)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "🍖", fontSize = 40.sp)
                    }
                }
                Text(
                    text = if (state == KitchenChaosState.LUFFY_EATING) "YUMMY!!" else "GIMME FOOD!",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = Color.Red),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // Nami
        AnimatedVisibility(
            visible = state == KitchenChaosState.NAMI_STOPS_HIM,
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = CircleShape,
                    color = Color(0xFFFFA500), // Nami Orange
                    border = androidx.compose.foundation.BorderStroke(4.dp, Color.Blue)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "💢", fontSize = 40.sp)
                    }
                }
                Text(
                    text = "LUFFY! WAIT!",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = Color.Blue),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        
        // Final Eating Scene (Large Overlay)
        if (state == KitchenChaosState.LUFFY_EATING) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🍖 LUFFY IS HAPPY! 🍖",
                    style = MaterialTheme.typography.displayMedium.copy(color = Color.White, fontWeight = FontWeight.Black)
                )
            }
        }
    }
}

@Composable
fun StaggeredStepItem(index: Int, step: CookStep) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(index * 150L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(600)) + slideInVertically(initialOffsetY = { 20 })
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (step.isCritical) MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            ),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp, 
                if (step.isCritical) MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (step.isCritical) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = (index + 1).toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = step.action.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (step.isCritical) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        letterSpacing = 2.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = step.instruction,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 28.sp
                )

                if (step.durationSeconds != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    StepTimer(step.durationSeconds)
                }
            }
        }
    }
}

@Composable
fun StepTimer(seconds: Int) {
    var timeLeft by remember { mutableIntStateOf(seconds) }
    var isRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (timeLeft > 0) {
                delay(1000L)
                timeLeft--
            }
            isRunning = false
        }
    }

    val minutes = timeLeft / 60
    val remainingSeconds = timeLeft % 60
    val timeString = "%02d:%02d".format(minutes, remainingSeconds)

    // Pulse when running (using tween for infiniteRepeatable compatibility)
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRunning) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
            .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.LocalFireDepartment, 
            contentDescription = null, 
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = timeString,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Light),
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = { isRunning = !isRunning },
            modifier = Modifier
                .clip(CircleShape)
                .background(if (isRunning) Color.Transparent else MaterialTheme.colorScheme.secondary)
        ) {
            Icon(
                if (isRunning) Icons.Default.Check else Icons.Default.Timer, 
                contentDescription = null,
                tint = if (isRunning) MaterialTheme.colorScheme.secondary else Color.White
            )
        }
    }
}
