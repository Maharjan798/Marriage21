package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.MarriageEngine
import com.example.model.Card
import com.example.model.GamePhase
import com.example.model.JokerRole
import com.example.model.Meld
import com.example.model.Player

@Composable
fun PlayerHandView(
    player: Player,
    tiplu: Card?,
    isTipluRevealed: Boolean,
    isHumanTurn: Boolean,
    gamePhase: GamePhase,
    selectedCardIds: Set<String>,
    onToggleCardSelect: (String) -> Unit,
    onAutoGroup: () -> Unit,
    onSortBySuit: () -> Unit,
    onSortByRank: () -> Unit,
    onCreateManualMeld: () -> Unit,
    onDisbandMeld: (String) -> Unit,
    onUnlockSeen: () -> Unit,
    onDiscardSelected: () -> Unit,
    onDeclareWin: () -> Unit,
    isGroupMode: Boolean = false,
    onToggleGroupMode: () -> Unit = {},
    onDiscardCard: ((String) -> Unit)? = null,
    onDrawStock: (() -> Unit)? = null,
    onDrawDiscard: (() -> Unit)? = null,
    topDiscardCard: Card? = null,
    modifier: Modifier = Modifier
) {
    var viewMode by remember { mutableStateOf(0) } // 0 = Melds View, 1 = Card Strip View
    var isZoomed by remember { mutableStateOf(false) } // Card zoom scaling
    val pureSetsCount = MarriageEngine.countPureSets(player.melds)
    val canUnlock = pureSetsCount >= 3 && !player.isSeen
    val canDeclare = MarriageEngine.canDeclareWin(player.melds, tiplu, player.isSeen)
    val isDiscardPhase = isHumanTurn && gamePhase == GamePhase.AWAITING_DISCARD

    val cardWidth = if (isZoomed) 52.dp else 44.dp
    val cardHeight = if (isZoomed) 76.dp else 64.dp
    val overlapOffset = if (isZoomed) (-19).dp else (-16).dp
    val trayHeight = if (isZoomed) 92.dp else 78.dp

    Surface(
        color = Color(0xFF0F172A).copy(alpha = 0.95f),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("player_hand_view")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            // Hand Header & Controls Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Pills
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    // Pure Sets indicator
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (pureSetsCount >= 3) Color(0xFF059669) else Color(0xFF334155),
                        border = BorderStroke(1.dp, if (pureSetsCount >= 3) Color(0xFF34D399) else Color(0xFF475569))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                        ) {
                            Text(
                                text = "Pure Sets: $pureSetsCount/3",
                                color = Color.White,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Seen / Mal badge
                    if (player.isSeen) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFD97706)
                        ) {
                            Text(
                                text = "★ SEEN (+${player.malPoints})",
                                color = Color(0xFF0F172A),
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF475569)
                        ) {
                            Text(
                                text = "LOCKED",
                                color = Color(0xFFE2E8F0),
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                            )
                        }
                    }

                    Text(
                        text = "(${player.hand.size})",
                        color = Color(0xFF94A3B8),
                        fontSize = 8.5.sp
                    )
                }

                // View Mode & Sort Quick Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    // View Switcher
                    Surface(
                        shape = RoundedCornerShape(5.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.dp, Color(0xFF475569)),
                        modifier = Modifier.clickable { viewMode = if (viewMode == 0) 1 else 0 }
                    ) {
                        Text(
                            text = if (viewMode == 0) "📋 Melds" else "🃏 Strip",
                            color = Color(0xFFFFD700),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }

                    // Card Zoom Toggle (Enlarges all cards for maximum legibility)
                    Surface(
                        shape = RoundedCornerShape(5.dp),
                        color = if (isZoomed) Color(0xFF0D9488) else Color(0xFF1E293B),
                        border = BorderStroke(1.dp, if (isZoomed) Color(0xFF2DD4BF) else Color(0xFF475569)),
                        modifier = Modifier
                            .clickable { isZoomed = !isZoomed }
                            .testTag("btn_zoom_toggle")
                    ) {
                        Text(
                            text = if (isZoomed) "🔍 Large" else "🔍 Zoom",
                            color = if (isZoomed) Color.White else Color(0xFFFFD700),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }

                    // Auto Group
                    Surface(
                        shape = RoundedCornerShape(5.dp),
                        color = Color(0xFF2563EB),
                        modifier = Modifier
                            .clickable(onClick = onAutoGroup)
                            .testTag("btn_auto_group")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Auto Group",
                                tint = Color.White,
                                modifier = Modifier.size(9.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Auto Group",
                                color = Color.White,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Group Mode Toggle (for manual melds)
                    Surface(
                        shape = RoundedCornerShape(5.dp),
                        color = if (isGroupMode) Color(0xFF7C3AED) else Color(0xFF1E293B),
                        border = BorderStroke(1.dp, if (isGroupMode) Color(0xFFA78BFA) else Color(0xFF475569)),
                        modifier = Modifier
                            .clickable(onClick = onToggleGroupMode)
                            .testTag("btn_toggle_group_mode")
                    ) {
                        Text(
                            text = if (isGroupMode) "Group: ON" else "Group",
                            color = if (isGroupMode) Color.White else Color(0xFFCBD5E1),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }

                    // Sort Suit
                    Surface(
                        shape = RoundedCornerShape(5.dp),
                        color = Color(0xFF334155),
                        modifier = Modifier.clickable(onClick = onSortBySuit)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(text = "♠", color = Color(0xFF94A3B8), fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                            Text(text = "♥", color = Color(0xFFEF4444), fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                            Text(text = "♦", color = Color(0xFFEF4444), fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                            Text(text = "♣", color = Color(0xFF94A3B8), fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Suit",
                                color = Color(0xFFE2E8F0),
                                fontSize = 8.5.sp
                            )
                        }
                    }

                    // Sort Rank
                    Surface(
                        shape = RoundedCornerShape(5.dp),
                        color = Color(0xFF334155),
                        modifier = Modifier.clickable(onClick = onSortByRank)
                    ) {
                        Text(
                            text = "123 Rank",
                            color = Color(0xFFE2E8F0),
                            fontSize = 8.5.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Main Hand Content Area (Enlarged for maximum clarity & legibility)
            if (viewMode == 0) {
                // --- MODE 0: MELDA TRATING GROUPS ---
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(trayHeight),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    // Formed Melds
                    items(player.melds, key = { it.id }) { meld ->
                        MeldSlotView(
                            meld = meld,
                            tiplu = tiplu,
                            isTipluRevealed = isTipluRevealed,
                            selectedCardIds = selectedCardIds,
                            isDiscardPhase = isDiscardPhase && !isGroupMode,
                            cardWidth = cardWidth,
                            cardHeight = cardHeight,
                            overlapOffset = overlapOffset,
                            onToggleSelect = onToggleCardSelect,
                            onDisband = { onDisbandMeld(meld.id) }
                        )
                    }

                    // Unmelded Cards Tray
                    if (player.unmeldedCards.isNotEmpty()) {
                        item {
                            UnmeldedTrayView(
                                cards = player.unmeldedCards,
                                tiplu = tiplu,
                                isTipluRevealed = isTipluRevealed,
                                selectedCardIds = selectedCardIds,
                                isDiscardPhase = isDiscardPhase && !isGroupMode,
                                cardWidth = cardWidth,
                                cardHeight = cardHeight,
                                overlapOffset = overlapOffset,
                                onToggleSelect = onToggleCardSelect
                            )
                        }
                    }
                }
            } else {
                // --- MODE 1: ALL CARDS STRIP ---
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(trayHeight)
                        .horizontalScroll(scrollState),
                    horizontalArrangement = Arrangement.spacedBy(if (isZoomed) (-24).dp else (-20).dp)
                ) {
                    player.hand.forEach { card ->
                        val isCardSelected = selectedCardIds.contains(card.id)
                        PlayingCardView(
                            card = card,
                            tiplu = tiplu,
                            isTipluRevealed = isTipluRevealed,
                            isSelected = isCardSelected,
                            showDiscardBadge = isDiscardPhase && !isGroupMode && isCardSelected,
                            width = cardWidth,
                            height = cardHeight,
                            onClick = { onToggleCardSelect(card.id) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Action Buttons Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Form Meld Button (when 3+ selected)
                if (selectedCardIds.size >= 3) {
                    Button(
                        onClick = onCreateManualMeld,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 7.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("btn_form_meld")
                    ) {
                        Text(
                            text = "Group (${selectedCardIds.size})",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Unlock (Seen) Button
                if (!player.isSeen) {
                    Button(
                        onClick = onUnlockSeen,
                        enabled = canUnlock,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canUnlock) Color(0xFF059669) else Color(0xFF334155),
                            disabledContainerColor = Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 7.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("btn_unlock_seen")
                    ) {
                        Icon(
                            imageVector = Icons.Default.LockOpen,
                            contentDescription = "Unlock Tiplu",
                            tint = if (canUnlock) Color(0xFFFFD700) else Color(0xFF64748B),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (canUnlock) "★ UNLOCK SEEN!" else "Unlock (3 Pure)",
                            color = if (canUnlock) Color.White else Color(0xFF94A3B8),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 1. Draw action buttons (if it's the human player's turn to draw)
                if (isHumanTurn && gamePhase == GamePhase.AWAITING_DRAW) {
                    Button(
                        onClick = { onDrawStock?.invoke() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 9.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("btn_action_draw_stock")
                    ) {
                        Text(
                            text = "🎴 DRAW (Stock)",
                            color = Color.White,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    if (topDiscardCard != null) {
                        OutlinedButton(
                            onClick = { onDrawDiscard?.invoke() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFD700)),
                            border = BorderStroke(1.dp, Color(0xFFFFD700)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 7.dp, vertical = 2.dp),
                            modifier = Modifier
                                .height(30.dp)
                                .testTag("btn_action_pick_discard")
                        ) {
                            Text(
                                text = "📥 Pick ${topDiscardCard.shortName}",
                                color = Color(0xFFFFD700),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 2. Selected card indicator badge (inline, saving vertical height)
                val singleSelectedCard = if (selectedCardIds.size == 1) {
                    player.hand.find { it.id == selectedCardIds.first() }
                } else null

                if (singleSelectedCard != null) {
                    val role = if (isTipluRevealed) singleSelectedCard.getJokerRole(tiplu) else JokerRole.NONE
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.dp, if (isDiscardPhase) Color(0xFFEF4444) else Color(0xFF475569)),
                        modifier = Modifier.padding(horizontal = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${singleSelectedCard.rank.display}${singleSelectedCard.suit.symbol}",
                                color = singleSelectedCard.suit.color,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                            if (role != JokerRole.NONE) {
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = role.displayName,
                                    color = Color(role.badgeColor),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Discard Button - ALWAYS responsive and provides clear feedback
                val discardBtnBg = when {
                    isDiscardPhase && singleSelectedCard != null -> Color(0xFFDC2626)
                    isDiscardPhase -> Color(0xFFB91C1C)
                    gamePhase == GamePhase.AWAITING_DRAW && isHumanTurn -> Color(0xFF334155)
                    else -> Color(0xFF1E293B)
                }

                val discardLabel = when {
                    isDiscardPhase && singleSelectedCard != null -> "DISCARD ${singleSelectedCard.shortName}"
                    isDiscardPhase && selectedCardIds.size > 1 -> "DISCARD (${selectedCardIds.size})"
                    isDiscardPhase -> "Select 1 to Discard"
                    gamePhase == GamePhase.AWAITING_DRAW && isHumanTurn -> "Draw First"
                    else -> "Discard"
                }

                Button(
                    onClick = onDiscardSelected,
                    enabled = isHumanTurn,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = discardBtnBg,
                        disabledContainerColor = Color(0xFF1E293B)
                    ),
                    border = if (isDiscardPhase && singleSelectedCard != null) BorderStroke(1.5.dp, Color(0xFFFFD700)) else null,
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 9.dp, vertical = 2.dp),
                    modifier = Modifier
                        .height(30.dp)
                        .testTag("btn_discard")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Discard",
                        tint = if (isDiscardPhase) Color.White else Color(0xFF94A3B8),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(2.5.dp))
                    Text(
                        text = discardLabel,
                        color = Color.White,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Declare Win Button
                if (player.isSeen) {
                    Button(
                        onClick = onDeclareWin,
                        enabled = isHumanTurn && (canDeclare || player.unmeldedCards.size <= 1),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD97706),
                            disabledContainerColor = Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("btn_declare_win")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Declare Win",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "DECLARE WIN",
                            color = Color.White,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MeldSlotView(
    meld: Meld,
    tiplu: Card?,
    isTipluRevealed: Boolean,
    selectedCardIds: Set<String>,
    isDiscardPhase: Boolean = false,
    cardWidth: Dp = 46.dp,
    cardHeight: Dp = 68.dp,
    overlapOffset: Dp = (-17).dp,
    onToggleSelect: (String) -> Unit,
    onDisband: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFF1E293B),
        border = BorderStroke(
            1.dp,
            if (meld.isValid) Color(meld.type.colorHex) else Color(0xFFEF4444)
        ),
        modifier = Modifier.padding(vertical = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(3.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Meld Header Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.widthIn(min = 72.dp)
            ) {
                Text(
                    text = meld.type.title.take(13),
                    color = Color(meld.type.colorHex),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = "✕",
                    color = Color(0xFF94A3B8),
                    fontSize = 9.sp,
                    modifier = Modifier.clickable(onClick = onDisband)
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Cards in Meld
            Row(
                horizontalArrangement = Arrangement.spacedBy(overlapOffset)
            ) {
                meld.cards.forEach { card ->
                    val isCardSelected = selectedCardIds.contains(card.id)
                    PlayingCardView(
                        card = card,
                        tiplu = tiplu,
                        isTipluRevealed = isTipluRevealed,
                        isSelected = isCardSelected,
                        showDiscardBadge = isDiscardPhase && isCardSelected,
                        width = cardWidth,
                        height = cardHeight,
                        onClick = { onToggleSelect(card.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun UnmeldedTrayView(
    cards: List<Card>,
    tiplu: Card?,
    isTipluRevealed: Boolean,
    selectedCardIds: Set<String>,
    isDiscardPhase: Boolean = false,
    cardWidth: Dp = 46.dp,
    cardHeight: Dp = 68.dp,
    overlapOffset: Dp = (-17).dp,
    onToggleSelect: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFF1E293B).copy(alpha = 0.6f),
        border = BorderStroke(1.dp, Color(0xFF475569)),
        modifier = Modifier.padding(vertical = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(3.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Unmelded (${cards.size})",
                color = Color(0xFF94A3B8),
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(overlapOffset)
            ) {
                cards.forEach { card ->
                    val isCardSelected = selectedCardIds.contains(card.id)
                    PlayingCardView(
                        card = card,
                        tiplu = tiplu,
                        isTipluRevealed = isTipluRevealed,
                        isSelected = isCardSelected,
                        showDiscardBadge = isDiscardPhase && isCardSelected,
                        width = cardWidth,
                        height = cardHeight,
                        onClick = { onToggleSelect(card.id) }
                    )
                }
            }
        }
    }
}
