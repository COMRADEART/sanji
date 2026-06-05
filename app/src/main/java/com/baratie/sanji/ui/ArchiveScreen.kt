package com.baratie.sanji.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baratie.sanji.model.MasterpieceRecord
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(viewModel: ChefViewModel, onBack: () -> Unit) {
    val logs by viewModel.masterpieceLogs.collectAsState()
    var selectedRecord by remember { mutableStateOf<MasterpieceRecord?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BARATIE LOGS", style = MaterialTheme.typography.titleMedium.copy(letterSpacing = 2.sp)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                )
        ) {
            if (logs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Your logbook is empty. \nGo create a masterpiece.", 
                        textAlign = TextAlign.Center, 
                        style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(logs) { record ->
                        MasterpieceThumbnail(record) { selectedRecord = record }
                    }
                }
            }
        }

        // Critique Detail Modal
        selectedRecord?.let { record ->
            AlertDialog(
                onDismissRequest = { selectedRecord = null },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(24.dp).verticalScroll(androidx.compose.foundation.rememberScrollState())) {
                        Image(
                            bitmap = record.photo.asImageBitmap(),
                            contentDescription = record.title,
                            modifier = Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = record.title.uppercase(),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        )
                        
                        val date = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(record.timestamp))
                        Text(
                            text = "Served on $date",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = record.critique,
                            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 28.sp)
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Button(
                            onClick = { selectedRecord = null },
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        ) {
                            Text("CLOSE LOG", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasterpieceThumbnail(record: MasterpieceRecord, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().height(200.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            Image(
                bitmap = record.photo.asImageBitmap(),
                contentDescription = record.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                    )
                )
            )
            Text(
                text = record.title,
                modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
                color = Color.White,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}
