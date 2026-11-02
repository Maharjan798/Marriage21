package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * Procedural Sound Engine for Marriage 21.
 * Synthesizes crisp 16-bit PCM card game sound effects directly in memory.
 * Requires zero external audio files, has zero latency, works completely offline.
 */
object SoundManager {

    var isSoundEnabled: Boolean = true

    private val scope = CoroutineScope(Dispatchers.Default)
    private const val SAMPLE_RATE = 44100

    // Cached sound buffers
    private val dealPcm by lazy { generateDealSound() }
    private val drawPcm by lazy { generateDrawSound() }
    private val discardPcm by lazy { generateDiscardSound() }
    private val selectPcm by lazy { generateSelectSound() }
    private val meldFormedPcm by lazy { generateMeldFormedSound() }
    private val unlockSeenPcm by lazy { generateUnlockSeenSound() }
    private val victoryPcm by lazy { generateVictorySound() }
    private val yourTurnPcm by lazy { generateYourTurnSound() }
    private val chatPopPcm by lazy { generateChatPopSound() }
    private val buttonTapPcm by lazy { generateButtonTapSound() }

    fun playDeal() = play(dealPcm)
    fun playDraw() = play(drawPcm)
    fun playDiscard() = play(discardPcm)
    fun playSelect() = play(selectPcm)
    fun playMeldFormed() = play(meldFormedPcm)
    fun playUnlockSeen() = play(unlockSeenPcm)
    fun playVictory() = play(victoryPcm)
    fun playYourTurn() = play(yourTurnPcm)
    fun playChatPop() = play(chatPopPcm)
    fun playButtonTap() = play(buttonTapPcm)

    private fun play(pcmData: ShortArray) {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val byteBuffer = ByteArray(pcmData.size * 2)
                for (i in pcmData.indices) {
                    val sample = pcmData[i].toInt()
                    byteBuffer[i * 2] = (sample and 0xFF).toByte()
                    byteBuffer[i * 2 + 1] = ((sample shr 8) and 0xFF).toByte()
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(byteBuffer.size)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(byteBuffer, 0, byteBuffer.size)
                audioTrack.play()

                val durationMs = (pcmData.size * 1000L) / SAMPLE_RATE + 60L
                delay(durationMs)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (_: Throwable) {}
            } catch (_: Throwable) {
                // Ignore any device audio exception
            }
        }
    }

    // --- Sound Generators ---

    /**
     * Card Dealing / Riffle Flutter (rapid sequence of subtle card slide clicks).
     */
    private fun generateDealSound(): ShortArray {
        val durationMs = 380
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        val clickInterval = (SAMPLE_RATE * 0.055).toInt() // Click every 55ms
        val random = Random(42)

        for (i in 0 until numSamples) {
            val clickPos = i % clickInterval
            if (clickPos < 400) {
                val decay = exp(-clickPos / 70.0)
                val noise = (random.nextDouble() * 2.0 - 1.0)
                val tone = sin(2.0 * PI * 650.0 * clickPos / SAMPLE_RATE)
                val sample = ((noise * 0.7 + tone * 0.3) * decay * 14000).toInt()
                buffer[i] = sample.coerceIn(-32767, 32767).toShort()
            }
        }
        return buffer
    }

    /**
     * Card Draw: Smooth swoosh / frequency glide as card is drawn from deck.
     */
    private fun generateDrawSound(): ShortArray {
        val durationMs = 140
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        val random = Random(12)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = i.toDouble() / numSamples
            // Rising frequency 300Hz -> 750Hz
            val freq = 300.0 + 450.0 * progress
            val envelope = sin(progress * PI) // smooth attack & release
            val noise = (random.nextDouble() * 2.0 - 1.0) * 0.25
            val tone = sin(2.0 * PI * freq * t)
            val sample = ((tone * 0.75 + noise) * envelope * 18000).toInt()
            buffer[i] = sample.coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    /**
     * Card Discard: Crisp table snap / thump on the felt cloth.
     */
    private fun generateDiscardSound(): ShortArray {
        val durationMs = 130
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        val random = Random(99)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            // Short initial slap transient + resonant table thump (140Hz decaying)
            val slapDecay = exp(-i / 80.0)
            val slapNoise = (random.nextDouble() * 2.0 - 1.0) * slapDecay * 18000.0
            val thumpDecay = exp(-i / 600.0)
            val thump = sin(2.0 * PI * 140.0 * t) * thumpDecay * 15000.0
            val sample = (slapNoise + thump).toInt()
            buffer[i] = sample.coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    /**
     * Card Select: Delicate click when tapping a card to select/deselect.
     */
    private fun generateSelectSound(): ShortArray {
        val durationMs = 35
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val decay = exp(-i / 90.0)
            val tone = sin(2.0 * PI * 1350.0 * t) * decay * 14000.0
            buffer[i] = tone.toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    /**
     * Meld Formed: Pleasant sparkling harmonic chime (C5 - E5 - G5).
     */
    private fun generateMeldFormedSound(): ShortArray {
        val durationMs = 450
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        val notes = listOf(523.25, 659.25, 783.99) // C5, E5, G5

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            var sum = 0.0
            notes.forEachIndexed { index, freq ->
                val noteStartSample = index * (SAMPLE_RATE * 0.07).toInt()
                if (i >= noteStartSample) {
                    val noteI = i - noteStartSample
                    val decay = exp(-noteI / (SAMPLE_RATE * 0.16))
                    val tone = sin(2.0 * PI * freq * (noteI.toDouble() / SAMPLE_RATE))
                    // Subtle harmonic overtone
                    val overtone = sin(4.0 * PI * freq * (noteI.toDouble() / SAMPLE_RATE)) * 0.3
                    sum += (tone + overtone) * decay
                }
            }
            val sample = (sum * 9000).toInt()
            buffer[i] = sample.coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    /**
     * Unlock Seen: Triumphant ascending arpeggio chime (C5 -> E5 -> G5 -> C6).
     */
    private fun generateUnlockSeenSound(): ShortArray {
        val durationMs = 600
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        val notes = listOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6

        for (i in 0 until numSamples) {
            var sum = 0.0
            notes.forEachIndexed { index, freq ->
                val noteStartSample = index * (SAMPLE_RATE * 0.09).toInt()
                if (i >= noteStartSample) {
                    val noteI = i - noteStartSample
                    val decay = exp(-noteI / (SAMPLE_RATE * 0.22))
                    val tone = sin(2.0 * PI * freq * (noteI.toDouble() / SAMPLE_RATE))
                    sum += tone * decay
                }
            }
            val sample = (sum * 8500).toInt()
            buffer[i] = sample.coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    /**
     * Victory Fanfare: Grand victory celebration chord progression.
     */
    private fun generateVictorySound(): ShortArray {
        val durationMs = 900
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        val notes = listOf(392.00, 523.25, 659.25, 783.99, 1046.50) // G4, C5, E5, G5, C6

        for (i in 0 until numSamples) {
            var sum = 0.0
            notes.forEachIndexed { index, freq ->
                val noteStartSample = index * (SAMPLE_RATE * 0.11).toInt()
                if (i >= noteStartSample) {
                    val noteI = i - noteStartSample
                    val decay = exp(-noteI / (SAMPLE_RATE * 0.32))
                    val tone = sin(2.0 * PI * freq * (noteI.toDouble() / SAMPLE_RATE))
                    val overtone = sin(4.0 * PI * freq * (noteI.toDouble() / SAMPLE_RATE)) * 0.25
                    sum += (tone + overtone) * decay
                }
            }
            val sample = (sum * 7500).toInt()
            buffer[i] = sample.coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    /**
     * Your Turn Bell: Soft dual bell chime to alert user that it is their turn.
     */
    private fun generateYourTurnSound(): ShortArray {
        val durationMs = 320
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        val notes = listOf(659.25, 880.00) // E5 -> A5

        for (i in 0 until numSamples) {
            var sum = 0.0
            notes.forEachIndexed { index, freq ->
                val noteStartSample = index * (SAMPLE_RATE * 0.12).toInt()
                if (i >= noteStartSample) {
                    val noteI = i - noteStartSample
                    val decay = exp(-noteI / (SAMPLE_RATE * 0.18))
                    val tone = sin(2.0 * PI * freq * (noteI.toDouble() / SAMPLE_RATE))
                    sum += tone * decay
                }
            }
            val sample = (sum * 11000).toInt()
            buffer[i] = sample.coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    /**
     * Chat Reaction Pop: Bubbly frequency glide pop.
     */
    private fun generateChatPopSound(): ShortArray {
        val durationMs = 80
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = i.toDouble() / numSamples
            val freq = 380.0 + 520.0 * progress
            val decay = exp(-i / 350.0)
            val tone = sin(2.0 * PI * freq * t) * decay * 18000.0
            buffer[i] = tone.toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    /**
     * Subtle button tap click.
     */
    private fun generateButtonTapSound(): ShortArray {
        val durationMs = 30
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val decay = exp(-i / 80.0)
            val tone = sin(2.0 * PI * 900.0 * t) * decay * 14000.0
            buffer[i] = tone.toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }
}
