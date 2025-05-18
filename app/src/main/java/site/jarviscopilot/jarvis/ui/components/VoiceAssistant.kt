package site.jarviscopilot.jarvis.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme

@Composable
fun VoiceAssistant(
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isListening by remember { mutableStateOf(false) }
    var lastCommand by remember { mutableStateOf<String?>(null) }
    var processingCommand by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (isListening) "Listening..." else "Hey Jarvis",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )
                
                MicButton(
                    isListening = isListening,
                    onMicClick = {
                        if (isListening) {
                            isListening = false
                            onStopListening()
                        } else {
                            isListening = true
                            onStartListening()
                            // Simulate a voice command after 3 seconds
                            processingCommand = true
                            // In a real app, this would be handled by the ViewModel
                        }
                    }
                )
            }
            
            AnimatedVisibility(
                visible = lastCommand != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Text(
                    text = "Last command: $lastCommand",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            if (isListening) {
                Spacer(modifier = Modifier.height(16.dp))
                VoiceWaveform()
            }
        }
    }
}

@Composable
fun MicButton(
    isListening: Boolean,
    onMicClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1000
                1f at 0 with FastOutSlowInEasing
                1.2f at 500 with FastOutSlowInEasing
                1f at 1000 with FastOutSlowInEasing
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "scale"
    )
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Background circle that pulses when listening
        if (isListening) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .scale(scale)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        shape = CircleShape
                    )
            )
        }
        
        IconButton(
            onClick = onMicClick,
            modifier = Modifier
                .background(
                    color = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
                .size(48.dp)
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = if (isListening) "Stop Listening" else "Start Listening",
                tint = Color.White
            )
        }
    }
}

@Composable
fun VoiceWaveform(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    
    // Define different animation phases for each bar
    val bar1Height by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 800
                10f at 0
                30f at 200
                10f at 400
                20f at 600
                10f at 800
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "bar1"
    )
    
    val bar2Height by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 800
                10f at 0
                20f at 200
                40f at 400
                20f at 600
                10f at 800
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "bar2"
    )
    
    val bar3Height by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 35f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 800
                10f at 0
                15f at 200
                10f at 400
                35f at 600
                10f at 800
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "bar3"
    )
    
    val bar4Height by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 25f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 800
                10f at 0
                25f at 200
                15f at 400
                10f at 600
                20f at 800
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "bar4"
    )
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxWidth()
    ) {
        VoiceBar(height = bar1Height)
        Spacer(modifier = Modifier.width(4.dp))
        VoiceBar(height = bar2Height)
        Spacer(modifier = Modifier.width(4.dp))
        VoiceBar(height = bar3Height)
        Spacer(modifier = Modifier.width(4.dp))
        VoiceBar(height = bar4Height)
    }
}

@Composable
fun VoiceBar(height: Float) {
    Box(
        modifier = Modifier
            .width(8.dp)
            .height(height.dp)
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.small
            )
    )
}

@Preview(showBackground = true)
@Composable
fun VoiceAssistantPreview() {
    JarvisTheme {
        VoiceAssistant(
            onStartListening = {},
            onStopListening = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MicButtonPreview() {
    JarvisTheme {
        MicButton(
            isListening = false,
            onMicClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MicButtonListeningPreview() {
    JarvisTheme {
        MicButton(
            isListening = true,
            onMicClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun VoiceWaveformPreview() {
    JarvisTheme {
        VoiceWaveform()
    }
}