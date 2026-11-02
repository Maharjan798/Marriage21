package com.example.ui.dialogs

import android.content.Intent
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.AiDifficulty
import com.example.model.FacebookFriend
import com.example.model.FacebookUser
import com.example.model.GameMode
import com.example.model.GameSpeed
import com.example.model.LobbySeat

@Composable
fun GameModeLobbyDialog(
    currentMode: GameMode,
    aiSpeed: GameSpeed,
    aiDifficulty: AiDifficulty,
    onlineRoomCode: String,
    lobbySeats: List<LobbySeat>,
    facebookUser: FacebookUser = FacebookUser(),
    facebookFriends: List<FacebookFriend> = emptyList(),
    onSelectOfflineBots: (GameSpeed, AiDifficulty) -> Unit,
    onStartOnlineGame: () -> Unit,
    onToggleLobbySeatBot: (Int) -> Unit,
    onJoinRoom: (String) -> Unit,
    onStartPassAndPlay: (List<String>) -> Unit,
    onConnectFacebook: (String) -> Unit = {},
    onInviteFacebookFriend: (String) -> Unit = {},
    onInviteAllFacebookFriends: () -> Unit = {},
    onStartFacebookGame: () -> Unit = {},
    onOpenGuide: () -> Unit = {},
    onDismiss: () -> Unit
) {
    var selectedTab by remember {
        mutableStateOf(
            when (currentMode) {
                GameMode.OFFLINE_BOTS -> 0
                GameMode.FACEBOOK_5P_ROOM -> 1
                GameMode.ONLINE_MULTIPLAYER -> 2
                GameMode.PASS_AND_PLAY -> 3
            }
        )
    }

    var selectedSpeed by remember { mutableStateOf(aiSpeed) }
    var selectedDifficulty by remember { mutableStateOf(aiDifficulty) }
    var roomInputCode by remember { mutableStateOf("") }

    val passPlayerNames = remember {
        mutableStateListOf("Player 1 (You)", "Player 2", "Player 3", "Player 4", "Player 5")
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 340.dp)
                .padding(4.dp)
                .testTag("mode_lobby_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = BorderStroke(1.5.dp, Color(0xFFFFD700))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = "Game Modes",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "5-PLAYER GAME MODES",
                        color = Color(0xFFFFD700),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1E293B),
                    border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDismiss()
                            onOpenGuide()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "🎓 New player? Learn rules & hand melds",
                            color = Color(0xFFFFD700),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Guide →",
                            color = Color(0xFF60A5FA),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tabs: Offline vs AI, Facebook 5-Player Room, Online Room Code, Pass & Play
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF1E293B),
                    contentColor = Color(0xFFFFD700),
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Color(0xFFFFD700),
                            height = 3.dp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = "🤖 AI",
                                fontSize = 10.5.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) Color(0xFFFFD700) else Color(0xFF94A3B8)
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = "👥 FB 5P",
                                fontSize = 10.5.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) Color(0xFF60A5FA) else Color(0xFF94A3B8)
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Text(
                                text = "🌐 Code",
                                fontSize = 10.5.sp,
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 2) Color(0xFFFFD700) else Color(0xFF94A3B8)
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = {
                            Text(
                                text = "📱 Pass",
                                fontSize = 10.5.sp,
                                fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 3) Color(0xFFFFD700) else Color(0xFF94A3B8)
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                when (selectedTab) {
                    0 -> OfflineModeContent(
                        selectedSpeed = selectedSpeed,
                        selectedDifficulty = selectedDifficulty,
                        onSpeedSelected = { selectedSpeed = it },
                        onDifficultySelected = { selectedDifficulty = it },
                        onStartGame = {
                            onSelectOfflineBots(selectedSpeed, selectedDifficulty)
                        }
                    )
                    1 -> Facebook5PlayerContent(
                        roomCode = onlineRoomCode,
                        facebookUser = facebookUser,
                        facebookFriends = facebookFriends,
                        lobbySeats = lobbySeats,
                        roomInputCode = roomInputCode,
                        onRoomInputChange = { roomInputCode = it },
                        onJoinRoom = { onJoinRoom(roomInputCode) },
                        onConnectFacebook = onConnectFacebook,
                        onInviteFriend = onInviteFacebookFriend,
                        onInviteAllFriends = onInviteAllFacebookFriends,
                        onStartGame = onStartFacebookGame
                    )
                    2 -> OnlineRoomLobbyContent(
                        roomCode = onlineRoomCode,
                        lobbySeats = lobbySeats,
                        roomInputCode = roomInputCode,
                        onRoomInputChange = { roomInputCode = it },
                        onJoinRoom = { onJoinRoom(roomInputCode) },
                        onToggleSeatBot = onToggleLobbySeatBot,
                        onStartGame = onStartOnlineGame
                    )
                    3 -> PassAndPlayContent(
                        playerNames = passPlayerNames,
                        onNameChange = { index, newName -> passPlayerNames[index] = newName },
                        onStartGame = { onStartPassAndPlay(passPlayerNames.toList()) }
                    )
                }
            }
        }
    }
}

@Composable
private fun OfflineModeContent(
    selectedSpeed: GameSpeed,
    selectedDifficulty: AiDifficulty,
    onSpeedSelected: (GameSpeed) -> Unit,
    onDifficultySelected: (AiDifficulty) -> Unit,
    onStartGame: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF1E293B),
            border = BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "5-Player Offline Table vs 4 Computer Bots",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Play instant offline Marriage 21 against Aarav (🦁), Priya (🦊), Rohan (🐯), and Maya (🦚). No internet required!",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }
        }

        // Bot Speed Selection
        Text(
            text = "AI Turn Speed",
            color = Color(0xFFFFD700),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            GameSpeed.values().forEach { speed ->
                val isSelected = selectedSpeed == speed
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) Color(0xFFFFD700) else Color(0xFF1E293B),
                    border = BorderStroke(1.dp, if (isSelected) Color(0xFFFFD700) else Color(0xFF475569)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSpeedSelected(speed) }
                ) {
                    Text(
                        text = speed.label,
                        color = if (isSelected) Color(0xFF0F172A) else Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }

        // Bot Difficulty Selection
        Text(
            text = "AI Bot Intelligence",
            color = Color(0xFFFFD700),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AiDifficulty.values().forEach { diff ->
                val isSelected = selectedDifficulty == diff
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) Color(0xFFFFD700) else Color(0xFF1E293B),
                    border = BorderStroke(1.dp, if (isSelected) Color(0xFFFFD700) else Color(0xFF475569)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onDifficultySelected(diff) }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp)
                    ) {
                        Text(
                            text = diff.label,
                            color = if (isSelected) Color(0xFF0F172A) else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = diff.desc,
                            color = if (isSelected) Color(0xFF334155) else Color(0xFF94A3B8),
                            fontSize = 8.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = onStartGame,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_start_offline_game")
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Start Game",
                tint = Color(0xFF0F172A),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Play 5-Player Offline Game",
                color = Color(0xFF0F172A),
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun OnlineRoomLobbyContent(
    roomCode: String,
    lobbySeats: List<LobbySeat>,
    roomInputCode: String,
    onRoomInputChange: (String) -> Unit,
    onJoinRoom: () -> Unit,
    onToggleSeatBot: (Int) -> Unit,
    onStartGame: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Room Code Banner
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF1E293B),
            border = BorderStroke(1.dp, Color(0xFF3B82F6)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ROOM CODE (5-PLAYER TABLE)",
                        color = Color(0xFF94A3B8),
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "#$roomCode",
                        color = Color(0xFFFFD700),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF059669)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = "Online",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ONLINE LOBBY",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 5 Seats Grid
        Text(
            text = "Room Seats (5/5)",
            color = Color(0xFFCBD5E1),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        lobbySeats.forEach { seat ->
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF1E293B),
                border = BorderStroke(1.dp, if (seat.isHost) Color(0xFFFFD700) else Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = seat.avatarEmoji,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = seat.name,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (seat.isHost) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Surface(
                                        shape = RoundedCornerShape(3.dp),
                                        color = Color(0xFFFFD700)
                                    ) {
                                        Text(
                                            text = "HOST",
                                            color = Color(0xFF0F172A),
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 3.dp, vertical = 0.5.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = if (seat.isBot) "Computer AI Player" else "Connected (${seat.pingMs}ms)",
                                color = if (seat.isBot) Color(0xFF60A5FA) else Color(0xFF10B981),
                                fontSize = 8.5.sp
                            )
                        }
                    }

                    if (!seat.isHost) {
                        Button(
                            onClick = { onToggleSeatBot(seat.seatIndex) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (seat.isBot) Color(0xFF334155) else Color(0xFF0284C7)
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text(
                                text = if (seat.isBot) "🤖 AI Bot" else "🌐 Human",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF10B981)
                        ) {
                            Text(
                                text = "READY",
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Quick Join by Code
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedTextField(
                value = roomInputCode,
                onValueChange = onRoomInputChange,
                placeholder = { Text("Enter 6-digit Code", fontSize = 10.sp, color = Color(0xFF64748B)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFFD700),
                    unfocusedBorderColor = Color(0xFF475569),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
            )
            Button(
                onClick = onJoinRoom,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(44.dp)
            ) {
                Text("Join", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Button(
            onClick = onStartGame,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_start_online_match")
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Start",
                tint = Color(0xFF0F172A),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Start 5-Player Online Match",
                color = Color(0xFF0F172A),
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun PassAndPlayContent(
    playerNames: List<String>,
    onNameChange: (Int, String) -> Unit,
    onStartGame: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF1E293B),
            border = BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "📱 Pass 1 phone among 5 friends! Before each turn, a privacy curtain is shown so hands remain hidden.",
                color = Color(0xFFE2E8F0),
                fontSize = 10.5.sp,
                lineHeight = 14.sp,
                modifier = Modifier.padding(8.dp)
            )
        }

        Text(
            text = "Enter 5 Player Names:",
            color = Color(0xFFFFD700),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        playerNames.forEachIndexed { index, name ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "P${index + 1}:",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(28.dp)
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { onNameChange(index, it) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFFD700),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = onStartGame,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_start_pass_and_play")
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Start",
                tint = Color(0xFF0F172A),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Start 5-Player Pass & Play",
                color = Color(0xFF0F172A),
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun Facebook5PlayerContent(
    roomCode: String,
    facebookUser: FacebookUser,
    facebookFriends: List<FacebookFriend>,
    lobbySeats: List<LobbySeat>,
    roomInputCode: String,
    onRoomInputChange: (String) -> Unit,
    onJoinRoom: () -> Unit,
    onConnectFacebook: (String) -> Unit,
    onInviteFriend: (String) -> Unit,
    onInviteAllFriends: () -> Unit,
    onStartGame: () -> Unit
) {
    val context = LocalContext.current
    val readyCount = lobbySeats.count { it.isReady }
    val isAllReady = readyCount == 5

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Facebook Connection Status Banner
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF1877F2).copy(alpha = 0.15f),
            border = BorderStroke(1.dp, Color(0xFF1877F2)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF1877F2),
                        modifier = Modifier.size(20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("f", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = if (facebookUser.isConnected) facebookUser.name else "Connect Facebook Account",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (facebookUser.isConnected) "● Online (184 Friends Available)" else "Sync friends & play together",
                            color = if (facebookUser.isConnected) Color(0xFF34D399) else Color(0xFF94A3B8),
                            fontSize = 8.5.sp
                        )
                    }
                }

                if (!facebookUser.isConnected) {
                    Button(
                        onClick = { onConnectFacebook("Bibek Maharjan") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Connect", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Room Code & Share Invite Row
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF1E293B),
            border = BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "5-PLAYER ROOM CODE:",
                        color = Color(0xFF94A3B8),
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = roomCode,
                        color = Color(0xFFFFD700),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "🃏 Play 21-Card Marriage with me on Facebook! Room Code: $roomCode. Join here: marriage21://room/$roomCode"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Invite Facebook Friends"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share Link", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onInviteAllFriends,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("⚡ Invite 4 Friends", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 5 Table Seats
        Text(
            text = "5-PLAYER TABLE SEATS ($readyCount/5 READY):",
            color = Color(0xFFFFD700),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            lobbySeats.forEachIndexed { index, seat ->
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (seat.isReady) Color(0xFF0F2027) else Color(0xFF1E293B),
                    border = BorderStroke(
                        1.dp,
                        if (seat.isReady) Color(0xFF10B981) else Color(0xFF475569)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (index == 0) "HOST" else "P${index + 1}",
                            color = if (index == 0) Color(0xFFFFD700) else Color(0xFF94A3B8),
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = seat.avatarEmoji,
                            fontSize = 16.sp
                        )
                        Text(
                            text = seat.name.take(7),
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                        Surface(
                            shape = RoundedCornerShape(3.dp),
                            color = if (seat.isReady) Color(0xFF10B981) else Color(0xFFE53E3E),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = if (seat.isReady) "READY" else "WAITING",
                                color = Color.White,
                                fontSize = 6.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }
        }

        // Start Match Button
        Button(
            onClick = onStartGame,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isAllReady) Color(0xFFFFD700) else Color(0xFF334155),
                contentColor = if (isAllReady) Color(0xFF0F172A) else Color(0xFF94A3B8)
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .testTag("btn_start_facebook_game")
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Start Game",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isAllReady) "START 5-PLAYER FACEBOOK MATCH" else "START MATCH ($readyCount/5 READY)",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        // Online Facebook Friends Quick Invite List
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF0F1E36),
            border = BorderStroke(1.dp, Color(0xFF1877F2).copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = "ONLINE FACEBOOK FRIENDS (184 ONLINE)",
                    color = Color(0xFF93C5FD),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                facebookFriends.forEach { friend ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = friend.avatarEmoji, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = friend.name,
                                color = Color.White,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                shape = CircleShape,
                                color = if (friend.isOnline) Color(0xFF10B981) else Color(0xFF64748B),
                                modifier = Modifier.size(5.dp)
                            ) {}
                        }

                        Button(
                            onClick = { onInviteFriend(friend.id) },
                            enabled = !friend.isInvited,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (friend.isInvited) Color(0xFF334155) else Color(0xFF1877F2)
                            ),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 1.dp),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Text(
                                text = if (friend.isInvited) "✓ Joined" else "+ Invite",
                                color = if (friend.isInvited) Color(0xFF94A3B8) else Color.White,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Join Friend's Facebook Room
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = roomInputCode,
                onValueChange = onRoomInputChange,
                placeholder = { Text("Enter FB Room Code (e.g. FB-1029)", fontSize = 9.5.sp) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1877F2),
                    unfocusedBorderColor = Color(0xFF475569),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Button(
                onClick = onJoinRoom,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.height(38.dp)
            ) {
                Text("Join", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
