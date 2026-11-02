package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Card
import com.example.model.JokerRole

@Composable
fun PlayingCardView(
    card: Card,
    tiplu: Card? = null,
    isTipluRevealed: Boolean = false,
    isSelected: Boolean = false,
    showDiscardBadge: Boolean = false,
    width: Dp = 56.dp,
    height: Dp = 82.dp,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val jokerRole = if (isTipluRevealed) card.getJokerRole(tiplu) else JokerRole.NONE
    val isJoker = jokerRole != JokerRole.NONE

    val yOffset by animateDpAsState(
        targetValue = if (isSelected) (-12).dp else 0.dp,
        label = "card_y_offset"
    )

    val elevation by animateDpAsState(
        targetValue = if (isSelected) 10.dp else 3.dp,
        label = "card_elevation"
    )

    Card(
        modifier = modifier
            .width(width)
            .height(height)
            .offset(y = yOffset)
            .testTag("card_${card.id}")
            .let { if (onClick != null) it.clickable(onClick = onClick) else it },
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        border = BorderStroke(
            width = if (isSelected) 2.5.dp else if (isJoker) 1.5.dp else 1.dp,
            color = if (isSelected) Color(0xFFFFD700) else if (isJoker) Color(0xFFF59E0B) else Color(0xFFCBD5E1)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 3.dp, vertical = 2.dp)
        ) {
            // Corner Top-Left (Rank + Suit)
            val rankSize = when {
                width >= 54.dp -> 16.5.sp
                width >= 46.dp -> 15.sp
                width >= 38.dp -> 13.sp
                else -> 11.5.sp
            }
            val cornerSuitSize = when {
                width >= 54.dp -> 14.sp
                width >= 46.dp -> 12.5.sp
                width >= 38.dp -> 11.sp
                else -> 9.5.sp
            }
            val lineSpace = when {
                width >= 54.dp -> 15.sp
                width >= 46.dp -> 13.5.sp
                width >= 38.dp -> 12.sp
                else -> 10.5.sp
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Text(
                    text = card.rank.display,
                    fontSize = rankSize,
                    fontWeight = FontWeight.Black,
                    color = card.suit.color,
                    lineHeight = lineSpace
                )
                Text(
                    text = card.suit.symbol,
                    fontSize = cornerSuitSize,
                    fontWeight = FontWeight.Bold,
                    color = card.suit.color,
                    lineHeight = cornerSuitSize
                )
            }

            // Center clean suit emblem
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val emblemDiameter = when {
                    width >= 54.dp -> 26.dp
                    width >= 46.dp -> 22.dp
                    else -> 18.dp
                }
                val centerSuitSize = when {
                    width >= 54.dp -> 17.sp
                    width >= 46.dp -> 14.5.sp
                    else -> 12.sp
                }
                Surface(
                    shape = CircleShape,
                    color = card.suit.color.copy(alpha = 0.06f),
                    border = BorderStroke(0.5.dp, card.suit.color.copy(alpha = 0.15f)),
                    modifier = Modifier.size(emblemDiameter)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = card.suit.symbol,
                            fontSize = centerSuitSize,
                            fontWeight = FontWeight.Bold,
                            color = card.suit.color.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Bottom-Right Inverted Corner
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .rotate(180f)
            ) {
                Text(
                    text = card.rank.display,
                    fontSize = rankSize,
                    fontWeight = FontWeight.Black,
                    color = card.suit.color,
                    lineHeight = lineSpace
                )
                Text(
                    text = card.suit.symbol,
                    fontSize = cornerSuitSize,
                    fontWeight = FontWeight.Bold,
                    color = card.suit.color,
                    lineHeight = cornerSuitSize
                )
            }

            // Joker Badge overlay
            if (isJoker) {
                val badgeText = when (jokerRole) {
                    JokerRole.TIPLU -> "TIPLU"
                    JokerRole.POPLU -> "POPLU"
                    JokerRole.JHIPLU -> "JHIPLU"
                    JokerRole.ALTER -> "ALTER"
                    JokerRole.NONE -> ""
                }

                Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = Color(jokerRole.badgeColor),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 1.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = Color(0xFF0F172A),
                        fontSize = 7.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                    )
                }
            } else if (isSelected && showDiscardBadge) {
                Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = Color(0xFFDC2626),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 1.dp)
                ) {
                    Text(
                        text = "DISCARD",
                        color = Color.White,
                        fontSize = 6.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 2.5.dp, vertical = 0.5.dp)
                    )
                }
            }

            // Deck Index dot indicator (Deck 1, 2, or 3)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(1.dp),
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                repeat(card.deckIndex + 1) {
                    Box(
                        modifier = Modifier
                            .size(2.5.dp)
                            .background(Color(0xFF94A3B8), CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
fun CardBackView(
    width: Dp = 56.dp,
    height: Dp = 82.dp,
    label: String? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(width)
            .height(height)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it },
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E293B)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.5.dp, Color(0xFFB8860B))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF8B1E2B),
                            Color(0xFF5B0E1B),
                            Color(0xFF38060F)
                        )
                    )
                )
                .padding(3.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, Color(0x66FFD700), RoundedCornerShape(4.dp))
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🎴",
                        fontSize = if (width < 50.dp) 14.sp else 18.sp
                    )
                    if (label != null) {
                        Text(
                            text = label,
                            color = Color(0xFFFFD700),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

/**
 * Small, clean pill badge representing a card (rank + colored suit symbol),
 * ideal for inline descriptions, summaries, and cheat strips.
 */
@Composable
fun MiniCardTag(
    rank: String,
    suit: com.example.model.Suit,
    isJoker: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = Color(0xFFFCFBF9),
        border = BorderStroke(
            0.5.dp,
            if (isJoker) Color(0xFFFFD700) else Color(0xFFCBD5E1)
        ),
        shadowElevation = 1.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(1.5.dp)
        ) {
            Text(
                text = rank,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = suit.color
            )
            Text(
                text = suit.symbol,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Normal,
                color = suit.color
            )
        }
    }
}

/**
 * Compact clean suit badge for displaying ♠, ♥, ♦, ♣ with clean styling.
 */
@Composable
fun SuitBadge(
    suit: com.example.model.Suit,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.TextUnit = 9.sp
) {
    Text(
        text = suit.symbol,
        fontSize = size,
        color = suit.color,
        modifier = modifier
    )
}
