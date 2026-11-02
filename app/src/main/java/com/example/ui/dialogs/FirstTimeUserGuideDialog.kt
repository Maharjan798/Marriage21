package com.example.ui.dialogs

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.audio.SoundManager

@Composable
fun FirstTimeUserGuideDialog(
    initialDoNotShowAgain: Boolean = false,
    onDismiss: (dontShowAgain: Boolean) -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) }
    var doNotShowAgain by remember { mutableStateOf(initialDoNotShowAgain) }
    val totalSteps = 5

    Dialog(onDismissRequest = { onDismiss(doNotShowAgain) }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 330.dp)
                .padding(vertical = 4.dp)
                .testTag("first_time_user_guide_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = BorderStroke(1.5.dp, Color(0xFFFFD700))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Header: Title & Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFD700).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFFFFD700))
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = "How to play",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(16.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "How to Play 21-Card Marriage",
                                color = Color(0xFFFFD700),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Beginner's Step-by-Step Guide (${currentStep + 1} of $totalSteps)",
                                color = Color(0xFF94A3B8),
                                fontSize = 9.5.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            SoundManager.playButtonTap()
                            onDismiss(doNotShowAgain)
                        },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Guide",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Progress Step Dots
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(totalSteps) { index ->
                        val isCurrent = index == currentStep
                        val isCompleted = index < currentStep
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .height(5.dp)
                                .width(if (isCurrent) 22.dp else 8.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    when {
                                        isCurrent -> Color(0xFFFFD700)
                                        isCompleted -> Color(0xFF10B981)
                                        else -> Color(0xFF334155)
                                    }
                                )
                                .clickable {
                                    SoundManager.playButtonTap()
                                    currentStep = index
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Dynamic Step Content
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "guide_step"
                    ) { step ->
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            when (step) {
                                0 -> item { Step1WelcomeAndSetup() }
                                1 -> item { Step2LockedStageAndPureSets() }
                                2 -> item { Step3TipluAndJokerMatrix() }
                                3 -> item { Step4TurnFlowAndControls() }
                                4 -> item { Step5DeclaringWinAndTips() }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // "Don't show again on startup" Checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { doNotShowAgain = !doNotShowAgain },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = doNotShowAgain,
                        onCheckedChange = { doNotShowAgain = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFFFFD700),
                            checkmarkColor = Color(0xFF0F172A),
                            uncheckedColor = Color(0xFF64748B)
                        ),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Don't show this guide on startup",
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Navigation Controls (Back, Next, Finish)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 0) {
                        OutlinedButton(
                            onClick = {
                                SoundManager.playButtonTap()
                                currentStep--
                            },
                            border = BorderStroke(1.dp, Color(0xFF475569)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFCBD5E1)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous Step",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Back", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                SoundManager.playButtonTap()
                                onDismiss(doNotShowAgain)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Text("Skip", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                    }

                    if (currentStep < totalSteps - 1) {
                        Button(
                            onClick = {
                                SoundManager.playButtonTap()
                                currentStep++
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Text("Next Step", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next",
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                SoundManager.playUnlockSeen()
                                onDismiss(doNotShowAgain)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Ready",
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Start Playing!",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- STEP 1: Overview & 5-Player Setup ---
@Composable
private fun Step1WelcomeAndSetup() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        GuideCard(
            title = "1. Overview & Table Setup",
            badge = "5 Players",
            badgeColor = Color(0xFF38BDF8)
        ) {
            Text(
                text = "Marriage is a classic 21-card rummy game played widely across South Asia. In this version, you play at a dynamic 5-Player table!",
                color = Color(0xFFE2E8F0),
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                InfoPill(emoji = "🃏", title = "3 Decks", desc = "156 cards total", modifier = Modifier.weight(1f))
                InfoPill(emoji = "🖐️", title = "21 Cards", desc = "Dealt to each player", modifier = Modifier.weight(1f))
                InfoPill(emoji = "👑", title = "1 Secret Tiplu", desc = "Cut under stock", modifier = Modifier.weight(1f))
            }
        }

        GuideCard(
            title = "The Ultimate Goal",
            badge = "How to Win",
            badgeColor = Color(0xFFFFD700)
        ) {
            Text(
                text = "Be the first player to organize all 21 cards into 7 valid 3-card melds (Sequences or Sets), discard the 22nd card, and tap DECLARE WIN!",
                color = Color(0xFFCBD5E1),
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }

        GuideCard(
            title = "Sound & Atmosphere",
            badge = "Audio",
            badgeColor = Color(0xFF34D399)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enjoy realistic card shuffles, draws, table snaps, and victory chimes!",
                    color = Color(0xFFCBD5E1),
                    fontSize = 10.sp,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = { SoundManager.playDeal() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "Test Audio", modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Test Sound", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- STEP 2: Phase 1 & 3 Pure Melds ---
@Composable
private fun Step2LockedStageAndPureSets() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        GuideCard(
            title = "Phase 1: The 'LOCKED' Stage",
            badge = "Must Unlock First",
            badgeColor = Color(0xFFEF4444)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "All players start LOCKED (Unseen). You cannot see the cut Tiplu card and CANNOT use Jokers as wildcards yet!",
                    color = Color(0xFFFCA5A5),
                    fontSize = 10.5.sp,
                    lineHeight = 14.sp
                )
            }
        }

        GuideCard(
            title = "How to UNLOCK (Become 'SEEN')",
            badge = "3 Pure Melds",
            badgeColor = Color(0xFF10B981)
        ) {
            Text(
                text = "To unlock the Joker matrix, you MUST form at least 3 Pure Melds using natural cards only (no jokers):",
                color = Color(0xFFCBD5E1),
                fontSize = 10.5.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Example 1: Tunnella
            SampleMeldRow(
                meldName = "1. TUNNELLA (3 Identical Cards)",
                explanation = "Three exactly identical cards of the SAME rank AND SAME suit:",
                cards = listOf("7♥", "7♥", "7♥"),
                isRed = true,
                badge = "Pays Bonus Mal!"
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Example 2: Pure Sequence
            SampleMeldRow(
                meldName = "2. PURE SEQUENCE (Straight Flush)",
                explanation = "3+ consecutive cards in the same suit without jokers:",
                cards = listOf("4♠", "5♠", "6♠"),
                isRed = false,
                badge = "Natural"
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LockOpen,
                    contentDescription = "Unlock",
                    tint = Color(0xFF34D399),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Once you have 3 Pure Melds, tap 'UNLOCK SEEN' to reveal the Tiplu!",
                    color = Color(0xFF34D399),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// --- STEP 3: Joker Matrix & Marriage ---
@Composable
private fun Step3TipluAndJokerMatrix() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        GuideCard(
            title = "Phase 2: The Joker Matrix Revealed",
            badge = "High Mal Points!",
            badgeColor = Color(0xFFFFD700)
        ) {
            Text(
                text = "Once you are SEEN, the cut card is revealed and creates 4 types of Jokers:",
                color = Color(0xFFCBD5E1),
                fontSize = 10.5.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            JokerItem(name = "TIPLU (Main Cut Joker)", desc = "The exact cut card (e.g. 8♠).", points = "+3 Mal pts each", color = Color(0xFFFFD700))
            JokerItem(name = "POPLU (+1 Rank Joker)", desc = "Same suit, 1 rank higher (9♠).", points = "+2 Mal pts each", color = Color(0xFF60A5FA))
            JokerItem(name = "JHIPLU (-1 Rank Joker)", desc = "Same suit, 1 rank lower (7♠).", points = "+2 Mal pts each", color = Color(0xFF4ADE80))
            JokerItem(name = "ALTER JOKERS", desc = "Same rank, other 3 suits (8♥, 8♦, 8♣). Act as wildcards!", points = "+5 Mal pts each", color = Color(0xFFFB923C))
        }

        GuideCard(
            title = "👑 The Legendary 'MARRIAGE' Combo",
            badge = "+10 Bonus Mal!",
            badgeColor = Color(0xFFFFD700)
        ) {
            Text(
                text = "Holding Tiplu + Poplu + Jhiplu in hand together (e.g., 7♠ + 8♠ + 9♠) forms the famous MARRIAGE! Every single player pays you 10 BONUS Mal points!",
                color = Color(0xFFFFE082),
                fontSize = 10.5.sp,
                lineHeight = 14.sp
            )
        }
    }
}

// --- STEP 4: Turn-by-Turn Guide ---
@Composable
private fun Step4TurnFlowAndControls() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        GuideCard(
            title = "How Your Turn Works (3 Simple Steps)",
            badge = "Turn Flow",
            badgeColor = Color(0xFF38BDF8)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                TurnStepItem(
                    stepNum = "1",
                    title = "DRAW (Stock or Discard)",
                    desc = "Tap the face-down Stock Deck OR the top Discard card. Your hand temporarily has 22 cards."
                )
                TurnStepItem(
                    stepNum = "2",
                    title = "ORGANIZE YOUR HAND",
                    desc = "Tap cards to select them. Tap 'Auto Group' to let AI sort your best melds, or form sets manually."
                )
                TurnStepItem(
                    stepNum = "3",
                    title = "DISCARD 1 CARD",
                    desc = "Select 1 unwanted card and tap 'DISCARD SELECTED' to end your turn. Turn passes clockwise!"
                )
            }
        }

        GuideCard(
            title = "Handy Controls",
            badge = "Features",
            badgeColor = Color(0xFFA78BFA)
        ) {
            Text(
                text = "• ✨ Auto Group: Instantly detects your best Tunnellas, sequences, and sets.\n• ↕ Sort by Suit / Rank: Quickly rearranges hand for easy reading.\n• 🔊 Sound Effects: Realistic card flips and game audio with 1-tap top bar toggle.",
                color = Color(0xFFCBD5E1),
                fontSize = 10.sp,
                lineHeight = 14.sp
            )
        }
    }
}

// --- STEP 5: Declaring Win & Scoring Tips ---
@Composable
private fun Step5DeclaringWinAndTips() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        GuideCard(
            title = "Declaring Win & Mal Settlement",
            badge = "Victory!",
            badgeColor = Color(0xFFFFD700)
        ) {
            Text(
                text = "When all 21 cards form 7 valid 3-card melds:\n1. Select your 22nd card to discard.\n2. Tap 'DECLARE WIN'!\n\nWinner receives 10 base points from each player. Unseen players pay a -20 penalty + winner's Mal!",
                color = Color(0xFFCBD5E1),
                fontSize = 10.5.sp,
                lineHeight = 14.sp
            )
        }

        GuideCard(
            title = "Golden Tips for Beginners",
            badge = "Pro Tips",
            badgeColor = Color(0xFF34D399)
        ) {
            Text(
                text = "1. Focus on Phase 1 first: Aim for 3 Pure Sequences or Tunnellas right away.\n2. Tap 'Auto Group' regularly to see if you have unlocked 3 Pure Melds.\n3. Hold onto Jokers: Even if someone else declares, your Jokers pay you Mal points!\n4. Practice in '5P Offline' mode against the 4 Computer AI Bots to master the rules risk-free.",
                color = Color(0xFFCBD5E1),
                fontSize = 10.sp,
                lineHeight = 14.sp
            )
        }
    }
}

// --- Reusable UI Helpers for the Guide ---

@Composable
private fun GuideCard(
    title: String,
    badge: String,
    badgeColor: Color,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF1E293B),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = badgeColor.copy(alpha = 0.2f),
                    border = BorderStroke(0.5.dp, badgeColor)
                ) {
                    Text(
                        text = badge,
                        color = badgeColor,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            content()
        }
    }
}

@Composable
private fun InfoPill(
    emoji: String,
    title: String,
    desc: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 16.sp)
            Text(text = title, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(text = desc, color = Color(0xFF94A3B8), fontSize = 8.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun SampleMeldRow(
    meldName: String,
    explanation: String,
    cards: List<String>,
    isRed: Boolean,
    badge: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF0F172A))
            .padding(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = meldName, color = Color(0xFFFFD700), fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
            Text(text = badge, color = Color(0xFF34D399), fontSize = 8.sp, fontWeight = FontWeight.Bold)
        }
        Text(text = explanation, color = Color(0xFF94A3B8), fontSize = 8.5.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            cards.forEach { cardStr ->
                val suitChar = cardStr.takeLast(1)
                val rankStr = cardStr.dropLast(1)
                val isRedSuit = suitChar == "♥" || suitChar == "♦"
                val suitColor = if (isRedSuit) Color(0xFFDC2626) else Color(0xFF18181B)
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFFCFBF9),
                    border = BorderStroke(0.5.dp, Color(0xFFCBD5E1)),
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(1.5.dp)
                    ) {
                        Text(
                            text = rankStr,
                            color = suitColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                        Text(
                            text = suitChar,
                            color = suitColor,
                            fontWeight = FontWeight.Normal,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun JokerItem(
    name: String,
    desc: String,
    points: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, color = color, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
            Text(text = desc, color = Color(0xFF94A3B8), fontSize = 8.5.sp)
        }
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = color.copy(alpha = 0.15f),
            border = BorderStroke(0.5.dp, color)
        ) {
            Text(
                text = points,
                color = color,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun TurnStepItem(
    stepNum: String,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFF2563EB),
            modifier = Modifier.size(18.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = stepNum, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(text = desc, color = Color(0xFF94A3B8), fontSize = 8.5.sp, lineHeight = 12.sp)
        }
    }
}
