package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiFriendEngine
import com.example.ai.LanguageTone
import com.example.ai.VoiceAssistantHelper
import com.example.data.model.Candle
import com.example.data.model.CryptoPair
import com.example.data.model.DEFAULT_CRYPTO_PAIRS
import com.example.data.model.TechnicalIndicators
import com.example.data.model.Ticker24h
import com.example.data.model.Timeframe
import com.example.data.model.TradeSignal
import com.example.data.repository.CryptoMarketRepository
import com.example.domain.indicators.TechnicalAnalysisEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

enum class MessageSender {
    USER,
    AI
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val attachedSignal: TradeSignal? = null,
    val isSpeaking: Boolean = false
)

class AiChatViewModel(
    application: Application,
    private val repository: CryptoMarketRepository
) : AndroidViewModel(application) {

    private val aiEngine = AiFriendEngine()
    val voiceHelper = VoiceAssistantHelper(application)

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _selectedTone = MutableStateFlow(LanguageTone.HINGLISH)
    val selectedTone: StateFlow<LanguageTone> = _selectedTone.asStateFlow()

    val isVoiceSpeaking = voiceHelper.isSpeaking
    val isVoiceListening = voiceHelper.isListening

    init {
        // Welcome message with personality
        val welcomeText = "Bhai! Main tera Crypto AI Friend hoon. Market ka live data check karke tujhe real-time analysis aur disciplined LONG/SHORT setups dunga. Mujhse voice mein baat kar ya text type kar. Bata, kaunse coin ka chart check karein?"
        _messages.value = listOf(
            ChatMessage(
                sender = MessageSender.AI,
                text = welcomeText
            )
        )
    }

    fun setTone(tone: LanguageTone) {
        _selectedTone.value = tone
        val toneAck = when (tone) {
            LanguageTone.HINGLISH -> "Done bhai! Ab main natural Hinglish trading friend mode mein baat karunga."
            LanguageTone.ENGLISH -> "Switched to Conversational English companion mode! Let's analyze the charts together."
            LanguageTone.HINDI -> "हिंदी मोड सक्रिय है। अब हम शुद्ध हिंदी में क्रिप्टो विश्लेषण करेंगे।"
            LanguageTone.PROFESSIONAL -> "Institutional Quantitative Analyst mode activated. Formal market reporting enabled."
        }
        _messages.value = _messages.value + ChatMessage(sender = MessageSender.AI, text = toneAck)
    }

    fun sendMessage(
        query: String,
        activePair: CryptoPair,
        activeTf: Timeframe
    ) {
        if (query.isBlank()) return

        val userMsg = ChatMessage(sender = MessageSender.USER, text = query)
        _messages.value = _messages.value + userMsg

        viewModelScope.launch {
            _isGenerating.value = true

            // Determine if a specific pair is mentioned (e.g. ETH, SOL, BTC)
            val pairToAnalyze = findMentionedPair(query) ?: activePair

            // Fetch latest real candles and ticker for ground truth
            val candleRes = repository.fetchCandles(pairToAnalyze.symbol, activeTf, limit = 100)
            val candles = candleRes.getOrNull() ?: emptyList()
            val ticker = repository.fetchSingleTicker(pairToAnalyze.symbol).getOrNull()
            val indicators = if (candles.isNotEmpty()) TechnicalAnalysisEngine.analyze(candles) else null

            val aiResponse = aiEngine.chat(
                userMessage = query,
                pair = pairToAnalyze.displayPair,
                timeframe = activeTf,
                ticker = ticker,
                indicators = indicators,
                candles = candles,
                tone = _selectedTone.value
            )

            val aiMsg = ChatMessage(
                sender = MessageSender.AI,
                text = aiResponse.replyText,
                attachedSignal = aiResponse.attachedSignal
            )
            _messages.value = _messages.value + aiMsg
            _isGenerating.value = false

            // Auto-speak reply if voice mode was active
            if (voiceHelper.isListening.value) {
                voiceHelper.speak(aiResponse.replyText, _selectedTone.value)
            }
        }
    }

    fun speakMessage(message: ChatMessage) {
        if (voiceHelper.isSpeaking.value) {
            voiceHelper.stopSpeaking()
        } else {
            voiceHelper.speak(message.text, _selectedTone.value)
        }
    }

    fun startVoiceInput(activePair: CryptoPair, activeTf: Timeframe) {
        voiceHelper.startListening { spoken ->
            if (spoken.isNotBlank()) {
                sendMessage(spoken, activePair, activeTf)
            }
        }
    }

    fun stopVoiceInput() {
        voiceHelper.stopListening()
    }

    fun executeQuickAction(
        actionPrompt: String,
        activePair: CryptoPair,
        activeTf: Timeframe
    ) {
        sendMessage(actionPrompt, activePair, activeTf)
    }

    private fun findMentionedPair(text: String): CryptoPair? {
        val lower = text.lowercase()
        return DEFAULT_CRYPTO_PAIRS.find {
            lower.contains(it.baseAsset.lowercase()) || lower.contains(it.displayName.lowercase())
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceHelper.shutdown()
    }
}
