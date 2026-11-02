package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Card
import com.example.model.GamePhase

@Composable
fun TableCenterView(
    stockPile: List<Card>,
    discardPile: List<Card>,
    tipluCard: Card?,
    isTipluRevealed: Boolean,
    isHumanTurn: Boolean,
    gamePhase: GamePhase,
    statusBanner: String,
    bannerHighlight: Boolean,
    onDrawStock: () -> Unit,
    onDrawDiscard: () -> Unit,
    onDiscardToPile: (() -> Unit)? = null,
    onStockPositioned: ((Offset) -> Unit)? = null,
    onDiscardPositioned: ((Offset) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        // Status Banner Pill
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (bannerHighlight) Color(0xFF0F172A).copy(alpha = 0.95f) else Color(0xFF1E293B).copy(alpha = 0.85f),
            border = BorderStroke(
                width = if (bannerHighlight) 1.5.dp else 1.dp,
                color = if (bannerHighlight) Color(0xFFFFD700) else Color(0xFF475569)
            ),
            modifier = Modifier
                .padding(bottom = 3.dp)
                .testTag("status_banner")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.5.dp)
            ) {
                if (bannerHighlight) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Turn Alert",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier
                            .size(12.dp)
                            .padding(end = 3.dp)
                    )
                }
                Text(
                    text = statusBanner,
                    color = if (bannerHighlight) Color(0xFFFFD700) else Color(0xFFE2E8F0),
                    fontSize = 10.5.sp,
                    fontWeight = if (bannerHighlight) FontWeight.Bold else FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }

        // Center Table Felt Core: Stock, Discard, Tiplu
        val isDiscardTurn = isHumanTurn && gamePhase == GamePhase.AWAITING_DISCARD
        val isDrawTurn = isHumanTurn && gamePhase == GamePhase.AWAITING_DRAW
        val topDiscard = discardPile.lastOrNull()
        val canClickDiscard = (isDrawTurn && topDiscard != null) || isDiscardTurn

        val infiniteTransition = rememberInfiniteTransition(label = "pulse_table")
        val drawBorderColor by infiniteTransition.animateColor(
            initialValue = Color(0xFFFFD700),
            targetValue = Color(0xFF34D399),
            animationSpec = infiniteRepeatable(
                animation = tween(650, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "draw_border"
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Stock Pile (Draw Deck) - Prominent & impossible to miss on mobile!
            val tableCardWidth = 50.dp
            val tableCardHeight = 72.dp

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.testTag("stock_pile")
            ) {
                Box(
                    modifier = Modifier
                        .onGloballyPositioned { coords ->
                            val pos = coords.positionInRoot()
                            onStockPositioned?.invoke(Offset(pos.x + coords.size.width / 2f, pos.y + coords.size.height / 2f))
                        }
                        .clickable(
                            enabled = isDrawTurn,
                            onClick = onDrawStock
                        )
                ) {
                    // Visual stack depth effect
                    if (stockPile.size > 2) {
                        CardBackView(
                            width = tableCardWidth,
                            height = tableCardHeight,
                            modifier = Modifier.offset(x = 2.dp, y = 2.dp)
                        )
                    }
                    if (stockPile.size > 1) {
                        CardBackView(
                            width = tableCardWidth,
                            height = tableCardHeight,
                            modifier = Modifier.offset(x = 1.dp, y = 1.dp)
                        )
                    }

                    CardBackView(
                        width = tableCardWidth,
                        height = tableCardHeight,
                        label = "${stockPile.size}",
                        modifier = if (isDrawTurn) {
                            Modifier.border(2.5.dp, drawBorderColor, RoundedCornerShape(7.dp))
                        } else Modifier
                    )

                    // "👉 TAP TO DRAW" prominent glow when it's user's turn to draw
                    if (isDrawTurn) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFFFD700),
                            shadowElevation = 5.dp,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .offset(y = 7.dp)
                        ) {
                            Text(
                                text = "👉 DRAW",
                                color = Color(0xFF0F172A),
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = if (isDrawTurn) "Stock (${stockPile.size}) - DRAW" else "Stock (${stockPile.size})",
                    color = if (isDrawTurn) Color(0xFFFFD700) else Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    fontWeight = if (isDrawTurn) FontWeight.Bold else FontWeight.Medium
                )
            }

            // 2. Discard Pile (Open)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.testTag("discard_pile")
            ) {
                Box(
                    modifier = Modifier
                        .onGloballyPositioned { coords ->
                            val pos = coords.positionInRoot()
                            onDiscardPositioned?.invoke(Offset(pos.x + coords.size.width / 2f, pos.y + coords.size.height / 2f))
                        }
                        .clickable(
                            enabled = canClickDiscard,
                            onClick = {
                                if (isDiscardTurn) {
                                    onDiscardToPile?.invoke()
                                } else if (isDrawTurn) {
                                    onDrawDiscard()
                                }
                            }
                        )
                ) {
                    if (discardPile.size > 1) {
                        val prevCard = discardPile[discardPile.size - 2]
                        PlayingCardView(
                            card = prevCard,
                            width = tableCardWidth,
                            height = tableCardHeight,
                            modifier = Modifier
                                .rotate(-5f)
                                .offset(x = (-2.5).dp, y = 1.dp)
                        )
                    }

                    if (topDiscard != null) {
                        PlayingCardView(
                            card = topDiscard,
                            tiplu = tipluCard,
                            isTipluRevealed = isTipluRevealed,
                            width = tableCardWidth,
                            height = tableCardHeight,
                            modifier = if (isDiscardTurn) {
                                Modifier.border(2.5.dp, Color(0xFFEF4444), RoundedCornerShape(6.dp))
                            } else if (isDrawTurn) {
                                Modifier.border(2.dp, Color(0xFF10B981), RoundedCornerShape(6.dp))
                            } else Modifier
                        )

                        // "PICK" badge when draw is available
                        if (isDrawTurn) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF10B981),
                                shadowElevation = 4.dp,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .offset(y = 6.dp)
                            ) {
                                Text(
                                    text = "📥 PICK",
                                    color = Color.White,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                                )
                            }
                        }

                        // "DISCARD HERE" badge when player needs to discard
                        if (isDiscardTurn) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFDC2626),
                                border = BorderStroke(1.dp, Color(0xFFFFD700)),
                                shadowElevation = 4.dp,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .offset(y = 6.dp)
                            ) {
                                Text(
                                    text = "DISCARD HERE",
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.5.dp)
                                )
                            }
                        }
                    } else {
                        // Empty discard slot
                        Box(
                            modifier = Modifier
                                .size(tableCardWidth, tableCardHeight)
                                .border(
                                    if (isDiscardTurn) 2.dp else 1.dp,
                                    if (isDiscardTurn) Color(0xFFFFD700) else Color(0x44FFFFFF),
                                    RoundedCornerShape(5.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isDiscardTurn) "DISCARD" else "Empty",
                                color = if (isDiscardTurn) Color(0xFFFFD700) else Color(0x66FFFFFF),
                                fontSize = 9.sp,
                                fontWeight = if (isDiscardTurn) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = if (isDiscardTurn) "Tap to Discard" else if (isDrawTurn && topDiscard != null) "Pick ${topDiscard.shortName}" else "Discard",
                    color = if (isDiscardTurn) Color(0xFFFFD700) else if (isDrawTurn && topDiscard != null) Color(0xFF34D399) else Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    fontWeight = if (isDiscardTurn || isDrawTurn) FontWeight.Bold else FontWeight.Medium
                )
            }

            // 3. Tiplu Joker Box
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.testTag("tiplu_indicator")
            ) {
                if (isTipluRevealed && tipluCard != null) {
                    // Revealed Tiplu Card
                    Box {
                        PlayingCardView(
                            card = tipluCard,
                            tiplu = tipluCard,
                            isTipluRevealed = true,
                            width = tableCardWidth,
                            height = tableCardHeight
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFFFD700),
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = (-4).dp)
                        ) {
                            Text(
                                text = "JOKER",
                                color = Color(0xFF0F172A),
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Tiplu (${tipluCard.shortName})",
                        color = Color(0xFFFFD700),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    // Hidden / Locked Tiplu Chest
                    Box(
                        modifier = Modifier
                            .width(tableCardWidth)
                            .height(tableCardHeight)
                            .clip(RoundedCornerShape(5.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                                )
                            )
                            .border(1.dp, Color(0xFFD97706), RoundedCornerShape(5.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked Tiplu",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "LOCKED",
                                color = Color(0xFFFFD700),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "3 Pure Sets",
                                color = Color(0xFF94A3B8),
                                fontSize = 7.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Tiplu (Secret)",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // 4. Joker Matrix Quick Cheat Strip (when Tiplu is unlocked)
        if (isTipluRevealed && tipluCard != null) {
            val popluRank = tipluCard.rank.nextRank()
            val jhipluRank = tipluCard.rank.prevRank()
            val otherSuits = com.example.model.Suit.values().filter { it != tipluCard.suit }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.95f),
                border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
                modifier = Modifier
                    .padding(top = 6.dp)
                    .testTag("joker_matrix_strip")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🃏 JOKERS:",
                        color = Color(0xFFFFD700),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(text = "Poplu:", color = Color(0xFF6EE7B7), fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                        MiniCardTag(rank = popluRank.display, suit = tipluCard.suit)
                        Text(text = "(2p)", color = Color(0xFF6EE7B7), fontSize = 7.sp)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(text = "Jhiplu:", color = Color(0xFF93C5FD), fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                        MiniCardTag(rank = jhipluRank.display, suit = tipluCard.suit)
                        Text(text = "(2p)", color = Color(0xFF93C5FD), fontSize = 7.sp)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(text = "Alters:", color = Color(0xFFFCA5A5), fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                        otherSuits.forEach { suit ->
                            MiniCardTag(rank = tipluCard.rank.display, suit = suit)
                        }
                        Text(text = "(5p)", color = Color(0xFFFCA5A5), fontSize = 7.sp)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(text = "★ Marriage:", color = Color(0xFFFFD700), fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                        MiniCardTag(rank = jhipluRank.display, suit = tipluCard.suit)
                        MiniCardTag(rank = tipluCard.rank.display, suit = tipluCard.suit, isJoker = true)
                        MiniCardTag(rank = popluRank.display, suit = tipluCard.suit)
                        Text(text = "(10p)", color = Color(0xFFFFD700), fontSize = 7.sp)
                    }
                }
            }
        }
    }
}
