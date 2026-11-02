package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Card
import com.example.model.CardFlyEvent
import com.example.model.DrawSource
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * FlyingCardOverlay:
 * Renders a smooth animated card moving from either the Stock Pile or Discard Pile
 * directly into the target player's hand (or opponent's HUD) in landscape orientation.
 */
@Composable
fun FlyingCardOverlay(
    flyEvent: CardFlyEvent?,
    stockCenter: Offset,
    discardCenter: Offset,
    handCenter: Offset,
    opponentCenters: Map<Int, Offset>,
    tipluCard: Card?,
    modifier: Modifier = Modifier
) {
    if (flyEvent == null) return

    val progress = remember(flyEvent.id) { Animatable(0f) }

    LaunchedEffect(flyEvent.id) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
        )
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val screenWidthPx = with(density) { maxWidth.toPx() }
        val screenHeightPx = with(density) { maxHeight.toPx() }

        // Start Coordinate (Stock or Discard pile)
        val startPos = when (flyEvent.source) {
            DrawSource.STOCK -> {
                if (stockCenter != Offset.Zero) stockCenter
                else Offset(screenWidthPx * 0.42f, screenHeightPx * 0.45f)
            }
            DrawSource.DISCARD -> {
                if (discardCenter != Offset.Zero) discardCenter
                else Offset(screenWidthPx * 0.58f, screenHeightPx * 0.45f)
            }
        }

        // Target Coordinate (Human hand or Bot seat)
        val targetPos = if (flyEvent.targetPlayerIndex == 0) {
            if (handCenter != Offset.Zero) handCenter
            else Offset(screenWidthPx * 0.50f, screenHeightPx * 0.85f)
        } else {
            val oppPos = opponentCenters[flyEvent.targetPlayerIndex]
            if (oppPos != null && oppPos != Offset.Zero) {
                oppPos
            } else {
                val idx = (flyEvent.targetPlayerIndex - 1).coerceIn(0, 3)
                Offset(screenWidthPx * (0.15f + idx * 0.23f), screenHeightPx * 0.16f)
            }
        }

        val p = progress.value
        // Parabolic arc for natural card flight
        val isMovingDown = targetPos.y > startPos.y
        val arcFactor = if (isMovingDown) 40f else -30f
        val arcY = sin(p * PI.toFloat()) * arcFactor

        val currentX = startPos.x + (targetPos.x - startPos.x) * p
        val currentY = startPos.y + (targetPos.y - startPos.y) * p - arcY

        // Mid-air scale pop and subtle rotation
        val scale = if (p < 0.5f) 1f + p * 0.3f else 1.15f - (p - 0.5f) * 0.3f
        val tiltAngle = (1f - p) * if (flyEvent.source == DrawSource.STOCK) -12f else 12f

        // Card size: slightly larger for human hand, compact for bot targets
        val cardWidthDp = if (flyEvent.targetPlayerIndex == 0) 48.dp else 36.dp
        val cardHeightDp = if (flyEvent.targetPlayerIndex == 0) 70.dp else 52.dp
        val cardWidthPx = with(density) { cardWidthDp.toPx() }
        val cardHeightPx = with(density) { cardHeightDp.toPx() }

        val renderX = (currentX - cardWidthPx / 2f).roundToInt()
        val renderY = (currentY - cardHeightPx / 2f).roundToInt()

        Box(
            modifier = Modifier
                .offset { IntOffset(renderX, renderY) }
                .scale(scale)
                .rotate(tiltAngle)
                .shadow(elevation = 12.dp, shape = RoundedCornerShape(6.dp))
        ) {
            if (flyEvent.targetPlayerIndex == 0 && flyEvent.source == DrawSource.STOCK && flyEvent.card != null) {
                // Card 3D Flip from Back to Face-Up
                val flipProgress = (p * 2f).coerceIn(0f, 2f)
                val isShowingFace = flipProgress >= 1f
                val rotationY = if (flipProgress < 1f) flipProgress * 90f else (2f - flipProgress) * -90f

                Box(
                    modifier = Modifier.graphicsLayer {
                        this.rotationY = rotationY
                        cameraDistance = 12f * density.density
                    }
                ) {
                    if (isShowingFace) {
                        PlayingCardView(
                            card = flyEvent.card,
                            tiplu = tipluCard,
                            isTipluRevealed = false,
                            width = cardWidthDp,
                            height = cardHeightDp
                        )
                    } else {
                        CardBackView(
                            width = cardWidthDp,
                            height = cardHeightDp
                        )
                    }
                }
            } else if (flyEvent.card != null && (flyEvent.isFaceUp || flyEvent.source == DrawSource.DISCARD)) {
                // Face-up card from Discard
                PlayingCardView(
                    card = flyEvent.card,
                    tiplu = tipluCard,
                    isTipluRevealed = false,
                    width = cardWidthDp,
                    height = cardHeightDp
                )
            } else {
                // Standard card back for draw
                CardBackView(
                    width = cardWidthDp,
                    height = cardHeightDp
                )
            }
        }
    }
}

/**
 * DealingCardsOverlay:
 * Smooth full-table animated dealing transition.
 * Cards fly outward from the 3-deck stack in the table center to all 5 player seats.
 */
@Composable
fun DealingCardsOverlay(
    isVisible: Boolean,
    progress: Float,
    onSkip: () -> Unit,
    stockCenter: Offset,
    opponentCenters: Map<Int, Offset>,
    handCenter: Offset,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(180)),
        exit = fadeOut(tween(220)),
        modifier = modifier
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF070B14).copy(alpha = 0.82f))
                .testTag("dealing_cards_overlay")
        ) {
            val density = LocalDensity.current
            val screenWidthPx = with(density) { maxWidth.toPx() }
            val screenHeightPx = with(density) { maxHeight.toPx() }

            val centerDeckPos = if (stockCenter != Offset.Zero) stockCenter
            else Offset(screenWidthPx * 0.50f, screenHeightPx * 0.44f)

            // Infinite transition to continuously burst dealing cards during deal duration
            val infiniteTransition = rememberInfiniteTransition(label = "dealing_burst")
            val burstProgress by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 600, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "burst_anim"
            )

            // Targets for the 5 players: 0=Human (bottom), 1=Aarav, 2=Priya, 3=Rohan, 4=Maya
            val playerTargets = remember(screenWidthPx, screenHeightPx, opponentCenters, handCenter) {
                listOf(
                    // Seat 0: You (Bottom Hand)
                    if (handCenter != Offset.Zero) handCenter else Offset(screenWidthPx * 0.50f, screenHeightPx * 0.86f),
                    // Seat 1: Aarav (Top Left)
                    opponentCenters[1] ?: Offset(screenWidthPx * 0.14f, screenHeightPx * 0.15f),
                    // Seat 2: Priya (Top Mid-Left)
                    opponentCenters[2] ?: Offset(screenWidthPx * 0.38f, screenHeightPx * 0.15f),
                    // Seat 3: Rohan (Top Mid-Right)
                    opponentCenters[3] ?: Offset(screenWidthPx * 0.62f, screenHeightPx * 0.15f),
                    // Seat 4: Maya (Top Right)
                    opponentCenters[4] ?: Offset(screenWidthPx * 0.86f, screenHeightPx * 0.15f)
                )
            }

            // Render 10 animated dealing cards in flight in staggered intervals
            val activeDealtCardsCount = 10
            for (i in 0 until activeDealtCardsCount) {
                val targetPlayerIndex = i % 5
                val targetPos = playerTargets[targetPlayerIndex]

                val offsetPhase = (i.toFloat() / activeDealtCardsCount.toFloat())
                val cardProgress = (burstProgress + offsetPhase) % 1f

                // Ease out trajectory
                val travelEase = FastOutSlowInEasing.transform(cardProgress)
                val curX = centerDeckPos.x + (targetPos.x - centerDeckPos.x) * travelEase
                val curY = centerDeckPos.y + (targetPos.y - centerDeckPos.y) * travelEase

                // Arc deflection
                val arcHeight = if (targetPlayerIndex == 0) 25f else -25f
                val arcOffset = sin(travelEase * PI.toFloat()) * arcHeight

                // Card rotation matching flight angle
                val angleDeg = when (targetPlayerIndex) {
                    0 -> 0f
                    1 -> -45f
                    2 -> -20f
                    3 -> 20f
                    4 -> 45f
                    else -> 0f
                } + (cardProgress * 20f - 10f)

                val cardScale = 1.0f - travelEase * 0.35f
                val cardAlpha = if (cardProgress < 0.1f) cardProgress * 10f else if (cardProgress > 0.85f) (1f - cardProgress) * 6.6f else 1f

                val cardWidthPx = with(density) { 36.dp.toPx() }
                val cardHeightPx = with(density) { 52.dp.toPx() }

                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (curX - cardWidthPx / 2f).roundToInt(),
                                (curY - arcOffset - cardHeightPx / 2f).roundToInt()
                            )
                        }
                        .rotate(angleDeg)
                        .scale(cardScale)
                        .alpha(cardAlpha.coerceIn(0f, 1f))
                        .shadow(elevation = 6.dp, shape = RoundedCornerShape(4.dp))
                ) {
                    CardBackView(
                        width = 36.dp,
                        height = 52.dp
                    )
                }
            }

            // Central 3-Deck Stack
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-14).dp),
                contentAlignment = Alignment.Center
            ) {
                // Layered cards showing deep shoe deck (156 cards)
                CardBackView(
                    width = 54.dp,
                    height = 76.dp,
                    modifier = Modifier.offset(x = 4.dp, y = 4.dp).alpha(0.6f)
                )
                CardBackView(
                    width = 54.dp,
                    height = 76.dp,
                    modifier = Modifier.offset(x = 2.dp, y = 2.dp).alpha(0.8f)
                )
                CardBackView(
                    width = 54.dp,
                    height = 76.dp,
                    label = "156"
                )

                // Gold pulsing glow around the dealing deck
                Box(
                    modifier = Modifier
                        .size(82.dp)
                        .border(
                            width = 2.dp,
                            brush = Brush.radialGradient(listOf(Color(0xFFFFD700), Color.Transparent)),
                            shape = CircleShape
                        )
                )
            }

            // Bottom Center Dealing HUD Pill
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.94f),
                border = BorderStroke(1.5.dp, Color(0xFFFFD700)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 36.dp)
                    .width(360.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🎴", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "Dealing 21 Cards to 5 Players...",
                                    color = Color(0xFFFFD700),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "3 Decks (156 Cards) • Aarav, Priya, Rohan, Maya, You",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 9.sp
                                )
                            }
                        }

                        // Skip button with 48dp minimum touch target
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF334155),
                            border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.6f)),
                            modifier = Modifier
                                .size(width = 64.dp, height = 36.dp)
                                .clickable(onClick = onSkip)
                                .testTag("skip_dealing_btn")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "SKIP",
                                    color = Color(0xFFFFD700),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.Default.FastForward,
                                    contentDescription = "Skip Dealing",
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Gold gradient progress bar
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp),
                        color = Color(0xFFFFD700),
                        trackColor = Color(0xFF1E293B)
                    )
                }
            }
        }
    }
}
