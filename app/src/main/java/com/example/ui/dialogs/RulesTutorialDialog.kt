package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun RulesTutorialDialog(
    onOpenInteractiveGuide: () -> Unit = {},
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 330.dp)
                .padding(4.dp)
                .testTag("rules_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = BorderStroke(1.dp, Color(0xFF3B82F6))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "Rules",
                        tint = Color(0xFF60A5FA),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "5-Player Marriage Rules",
                        color = Color(0xFF60A5FA),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Interactive Guide Banner
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1E293B),
                    border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenInteractiveGuide() }
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🎓 First-Time User? Read Visual Guide",
                                color = Color(0xFFFFD700),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "5-step illustrated guide with card examples",
                                color = Color(0xFFCBD5E1),
                                fontSize = 9.sp
                            )
                        }
                        Text(
                            text = "OPEN →",
                            color = Color(0xFFFFD700),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        RuleSection(
                            title = "1. Deck & Hand Setup (5 Players)",
                            content = "• 3 standard 52-card decks are shuffled (156 cards total).\n• 5 players are dealt 21 cards each (105 cards dealt total).\n• 1 card is cut as the secret Tiplu (under stock deck).\n• Remaining 50 cards form the Stock draw pile."
                        )
                    }

                    item {
                        RuleSection(
                            title = "2. Phase 1: Locked Stage (3 Pure Sets)",
                            content = "• All players start LOCKED (cannot see Tiplu or use Jokers).\n• To UNLOCK (Seen), you must build 3 Pure Sets:\n   - Tunnella: 3 IDENTICAL cards (same suit & same rank, e.g., 7♥ 7♥ 7♥).\n   - Pure Sequence: 3+ consecutive cards of same suit (e.g., 4♠ 5♠ 6♠ or A♣ 2♣ 3♣).\n• Once 3 Pure Sets are ready, tap 'UNLOCK SEEN'!"
                        )
                    }

                    item {
                        RuleSection(
                            title = "3. The Joker Matrix (Tiplu, Poplu, Jhiplu, Alters)",
                            content = "• Tiplu (Main Joker): The cut card (e.g. 8♠). Worth 3 Mal points each.\n• Poplu: Same suit, 1 rank higher (9♠). Worth 2 Mal points.\n• Jhiplu: Same suit, 1 rank lower (7♠). Worth 2 Mal points.\n• Alter Jokers: Same rank in other suits (8♥, 8♦, 8♣). Worth 5 Mal points & act as wildcards!\n• ★ MARRIAGE: Holding Tiplu + Poplu + Jhiplu together (7♠ 8♠ 9♠) pays 10 BONUS Mal points from every player!"
                        )
                    }

                    item {
                        RuleSection(
                            title = "4. Phase 2: Unlocked Stage & Declaring Win",
                            content = "• After Unlocking, Jokers can substitute any card in sequences & sets.\n• Form all 21 cards into 7 valid 3-card melds.\n• Discard the 22nd card and tap 'DECLARE WIN' to win the round!"
                        )
                    }

                    item {
                        RuleSection(
                            title = "5. Mal & Scoring Settlement",
                            content = "• Winner receives 10 base points from each of the 4 opponents.\n• Unseen players pay -20 penalty + Winner's full Mal points!\n• Seen players score their Mal points against the winner's points."
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenInteractiveGuide,
                        border = BorderStroke(1.dp, Color(0xFFFFD700)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFD700)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Visual Guide", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Got It! Let's Play", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun RuleSection(title: String, content: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF1E293B),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                color = Color(0xFFFFD700),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = content,
                color = Color(0xFFE2E8F0),
                fontSize = 10.sp,
                lineHeight = 14.sp
            )
        }
    }
}
