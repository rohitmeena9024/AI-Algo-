package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.LanguageTone
import com.example.data.model.CryptoPair
import com.example.data.model.Timeframe
import com.example.data.model.TradeSignal
import com.example.ui.components.SignalCard
import com.example.ui.theme.CryptoBearishRed
import com.example.ui.theme.CryptoBorder
import com.example.ui.theme.CryptoBullishGreen
import com.example.ui.theme.CryptoDarkBackground
import com.example.ui.theme.CryptoDarkSurface
import com.example.ui.theme.CryptoDarkSurfaceVariant
import com.example.ui.theme.CryptoNeonCyan
import com.example.ui.theme.CryptoTextPrimary
import com.example.ui.theme.CryptoTextSecondary
import com.example.ui.theme.CryptoWarningGold
import com.example.ui.viewmodel.ChatMessage
import com.example.ui.viewmodel.MessageSender

val QUICK_ACTIONS = listOf(
    "Analyze BTC",
    "Analyze ETH",
    "Find LONG Setup",
    "Find SHORT Setup",
    "Scan Market",
    "Explain Current Signal",
    "Calculate Stop Loss",
    "Calculate Position Size"
)

@Composable
fun AiChatFriendScreen(
    messages: List<ChatMessage>,
    isGenerating: Boolean,
    isVoiceSpeaking: Boolean,
    isVoiceListening: Boolean,
    selectedTone: LanguageTone,
    activePair: CryptoPair,
    activeTimeframe: Timeframe,
    onSendMessage: (String) -> Unit,
    onSpeakMessage: (ChatMessage) -> Unit,
    onStartVoice: () -> Unit,
    onStopVoice: () -> Unit,
    onSelectTone: (LanguageTone) -> Unit,
    onViewChart: (String) -> Unit,
    onCalculateRisk: (TradeSignal) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to latest message
    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Pulsing animation for active speech or mic
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CryptoDarkBackground)
            .testTag("ai_chat_friend_screen")
    ) {
        // Top Companion Bar & Language Selector
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CryptoDarkSurface)
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CryptoNeonCyan.copy(alpha = 0.2f))
                            .scale(if (isVoiceSpeaking) pulseScale else 1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🤖", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Crypto AI Friend",
                                color = CryptoTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(CryptoBullishGreen)
                            )
                        }
                        Text(
                            text = if (isVoiceSpeaking) "Speaking response..." else if (isVoiceListening) "Listening..." else "Grounded to live ${activePair.displayPair} (${activeTimeframe.label})",
                            color = if (isVoiceSpeaking || isVoiceListening) CryptoNeonCyan else CryptoTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tone Switcher Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LanguageTone.entries.forEach { tone ->
                    val isSelected = tone == selectedTone
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) CryptoNeonCyan else CryptoDarkSurfaceVariant)
                            .border(1.dp, if (isSelected) CryptoNeonCyan else CryptoBorder, RoundedCornerShape(6.dp))
                            .clickable { onSelectTone(tone) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = tone.label,
                            color = if (isSelected) Color.Black else CryptoTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Chat Message Stream
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { msg ->
                val isAi = msg.sender == MessageSender.AI
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isAi) Alignment.Start else Alignment.End
                ) {
                    Box(
                        modifier = Modifier
                            .clip(
                                RoundedCornerShape(
                                    topStart = 14.dp,
                                    topEnd = 14.dp,
                                    bottomStart = if (isAi) 2.dp else 14.dp,
                                    bottomEnd = if (isAi) 14.dp else 2.dp
                                )
                            )
                            .background(if (isAi) CryptoDarkSurface else CryptoNeonCyan)
                            .border(
                                1.dp,
                                if (isAi) CryptoBorder else Color.Transparent,
                                RoundedCornerShape(14.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = msg.text,
                                color = if (isAi) CryptoTextPrimary else Color.Black,
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            )

                            if (isAi) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { onSpeakMessage(msg) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = "Read aloud",
                                            tint = CryptoNeonCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Embedded Signal Card if returned
                    msg.attachedSignal?.let { sig ->
                        Spacer(modifier = Modifier.height(8.dp))
                        SignalCard(
                            signal = sig,
                            onViewChart = onViewChart,
                            onCalculateRisk = onCalculateRisk,
                            onAskAi = { /* already here */ }
                        )
                    }
                }
            }

            if (isGenerating) {
                item {
                    Row(
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = CryptoNeonCyan,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Analyzing live orderbook & calculating indicators...",
                            color = CryptoTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Quick Action Chips Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(CryptoDarkSurfaceVariant)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(QUICK_ACTIONS) { action ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(CryptoDarkSurface)
                        .border(1.dp, CryptoBorder, RoundedCornerShape(14.dp))
                        .clickable { onSendMessage(action) }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = action,
                        color = CryptoNeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Input Box with Voice Mic Button & Send
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CryptoDarkSurface)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Voice Mic Toggle Button
            IconButton(
                onClick = {
                    if (isVoiceListening) onStopVoice() else onStartVoice()
                },
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isVoiceListening) CryptoBearishRed else CryptoDarkSurfaceVariant)
                    .scale(if (isVoiceListening) pulseScale else 1f)
                    .testTag("ai_btn_voice_mic")
            ) {
                Icon(
                    imageVector = if (isVoiceListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Voice Input",
                    tint = if (isVoiceListening) Color.White else CryptoNeonCyan
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text(
                        text = if (isVoiceListening) "Sun raha hoon bhai, bolo..." else "Ask about BTC, ETH, setups, RSI...",
                        color = CryptoTextSecondary,
                        fontSize = 12.sp
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_chat_input_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CryptoNeonCyan,
                    unfocusedBorderColor = CryptoBorder,
                    focusedTextColor = CryptoTextPrimary,
                    unfocusedTextColor = CryptoTextPrimary,
                    cursorColor = CryptoNeonCyan
                ),
                shape = RoundedCornerShape(20.dp),
                maxLines = 3
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        onSendMessage(inputText)
                        inputText = ""
                    }
                },
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(CryptoNeonCyan)
                    .testTag("ai_btn_send_message")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.Black
                )
            }
        }
    }
}
