package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Player

@Composable
fun OpponentView(
    player: Player,
    isCurrentTurn: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_turn")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isCurrentTurn) Color(0xFF1E293B) else Color(0xFF0F172A).copy(alpha = 0.85f),
        border = BorderStroke(
            1.dp,
            if (isCurrentTurn) Color(0xFFFFD700) else Color(0xFF334155)
        ),
        modifier = modifier
            .testTag("opponent_${player.id}")
            .padding(horizontal = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Avatar with turn glow
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(26.dp)
            ) {
                if (isCurrentTurn) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .scale(pulseScale)
                            .background(
                                Brush.radialGradient(
                                    listOf(Color(0xFFFFD700), Color.Transparent)
                                ),
                                CircleShape
                            )
                    )
                }

                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF334155), Color(0xFF1E293B))
                            ),
                            CircleShape
                        )
                        .border(
                            width = if (isCurrentTurn) 1.5.dp else 1.dp,
                            color = if (isCurrentTurn) Color(0xFFFFD700) else Color(0xFF64748B),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = player.avatarEmoji, fontSize = 11.sp)
                }

                // Card count pill
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF0F172A),
                    border = BorderStroke(0.5.dp, Color(0xFF94A3B8)),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(10.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${player.hand.size}",
                            color = Color.White,
                            fontSize = 6.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Info Column
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = player.name,
                        color = if (isCurrentTurn) Color(0xFFFFD700) else Color(0xFFF1F5F9),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    if (player.isFacebookFriend) {
                        Surface(shape = RoundedCornerShape(2.dp), color = Color(0xFF1877F2)) {
                            Text(
                                text = "FB",
                                color = Color.White,
                                fontSize = 6.5.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (player.isSeen) {
                        Surface(shape = RoundedCornerShape(2.dp), color = Color(0xFF10B981)) {
                            Text(
                                text = "SEEN" + if (player.malPoints > 0) " +${player.malPoints}M" else "",
                                color = Color.White,
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 2.5.dp, vertical = 0.5.dp)
                            )
                        }
                    } else {
                        Text(
                            text = if (player.isThinking) "⏳ Thinking" else if (player.lastActionText.isNotEmpty()) player.lastActionText.take(12) else "Unseen",
                            color = if (isCurrentTurn) Color(0xFFFFD700) else Color(0xFF94A3B8),
                            fontSize = 7.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
