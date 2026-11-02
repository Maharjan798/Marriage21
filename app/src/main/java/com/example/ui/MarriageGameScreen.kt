package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.GameMode
import com.example.model.GamePhase
import com.example.ui.components.DealingCardsOverlay
import com.example.ui.components.FlyingCardOverlay
import com.example.ui.components.OpponentView
import com.example.ui.components.PlayerHandView
import com.example.ui.components.TableCenterView
import com.example.ui.dialogs.FacebookConnectDialog
import com.example.ui.dialogs.FirstTimeUserGuideDialog
import com.example.ui.dialogs.GameModeLobbyDialog
import com.example.ui.dialogs.RulesTutorialDialog
import com.example.ui.dialogs.ScoreboardDialog
import com.example.ui.dialogs.SettingsDialog
import com.example.viewmodel.MarriageGameViewModel

@Composable
fun MarriageGameScreen(
    viewModel: MarriageGameViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val theme = uiState.activeTheme

    var stockCenter by remember { mutableStateOf(Offset.Zero) }
    var discardCenter by remember { mutableStateOf(Offset.Zero) }
    var handCenter by remember { mutableStateOf(Offset.Zero) }
    val opponentCenters = remember { mutableStateMapOf<Int, Offset>() }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("marriage_game_screen"),
        containerColor = Color(theme.primaryFelt)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(theme.primaryFelt),
                            Color(theme.secondaryFelt)
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. TOP BAR: Title, 5P Mode Selector, Facebook Connect, Scoreboard, Rules, Sound, Guide, Settings
                TopGameBar(
                    roundNumber = uiState.roundNumber,
                    gameMode = uiState.gameMode,
                    roomCode = uiState.onlineRoomCode,
                    soundEnabled = uiState.soundEnabled,
                    isFacebookConnected = uiState.facebookUser.isConnected,
                    facebookName = uiState.facebookUser.name,
                    onOpenFacebookConnect = { viewModel.setShowFacebookConnectDialog(true) },
                    onToggleSound = { viewModel.toggleSound() },
                    onOpenFirstTimeGuide = { viewModel.setShowFirstTimeGuide(true) },
                    onOpenModeSelector = { viewModel.setShowModeLobby(true) },
                    onOpenScoreboard = { viewModel.setShowScoreboard(true) },
                    onOpenRules = { viewModel.setShowRules(true) },
                    onOpenSettings = { viewModel.setShowSettings(true) },
                    onRestartRound = { viewModel.dealNewRound() }
                )

                // 2. 5-PLAYER OPPONENTS ROW (4 Opponents: Aarav, Priya, Rohan, Maya)
                val opponents = uiState.players.drop(1)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Top
                ) {
                    opponents.forEach { opponent ->
                        OpponentView(
                            player = opponent,
                            isCurrentTurn = uiState.currentPlayerIndex == opponent.id,
                            modifier = Modifier
                                .weight(1f)
                                .onGloballyPositioned { coords ->
                                    val pos = coords.positionInRoot()
                                    opponentCenters[opponent.id] = Offset(pos.x + coords.size.width / 2f, pos.y + coords.size.height / 2f)
                                }
                        )
                    }
                }

                // 3. CENTER TABLE FELT: Stock, Discard, Tiplu & Status Banner
                TableCenterView(
                    stockPile = uiState.stockPile,
                    discardPile = uiState.discardPile,
                    tipluCard = uiState.tipluCard,
                    isTipluRevealed = uiState.isTipluRevealedToHuman,
                    isHumanTurn = uiState.players.getOrNull(uiState.currentPlayerIndex)?.isHuman == true,
                    gamePhase = uiState.gamePhase,
                    statusBanner = uiState.statusBanner,
                    bannerHighlight = uiState.bannerHighlight,
                    onDrawStock = { viewModel.onHumanDrawStock() },
                    onDrawDiscard = { viewModel.onHumanDrawDiscard() },
                    onDiscardToPile = { viewModel.onHumanDiscardSelected() },
                    onStockPositioned = { stockCenter = it },
                    onDiscardPositioned = { discardCenter = it },
                    modifier = Modifier.weight(1f, fill = true)
                )

                // 4. BOTTOM PLAYER HAND (21 cards tray for active human player)
                val activePlayer = if (uiState.gameMode == GameMode.PASS_AND_PLAY) {
                    uiState.players.getOrNull(uiState.currentPlayerIndex) ?: uiState.players.firstOrNull()
                } else {
                    uiState.players.getOrNull(0)
                }

                if (activePlayer != null) {
                    PlayerHandView(
                        player = activePlayer,
                        tiplu = uiState.tipluCard,
                        isTipluRevealed = uiState.isTipluRevealedToHuman,
                        isHumanTurn = uiState.players.getOrNull(uiState.currentPlayerIndex)?.isHuman == true && uiState.gamePhase != GamePhase.PASS_PHONE_CURTAIN,
                        gamePhase = uiState.gamePhase,
                        selectedCardIds = uiState.selectedCardIds,
                        onToggleCardSelect = { viewModel.toggleCardSelection(it) },
                        onAutoGroup = { viewModel.onHumanAutoGroup() },
                        onSortBySuit = { viewModel.onHumanSortBySuit() },
                        onSortByRank = { viewModel.onHumanSortByRank() },
                        onCreateManualMeld = { viewModel.createManualMeldFromSelected() },
                        onDisbandMeld = { viewModel.disbandMeld(it) },
                        onUnlockSeen = { viewModel.onHumanUnlockSeen() },
                        onDiscardSelected = { viewModel.onHumanDiscardSelected() },
                        onDeclareWin = { viewModel.onHumanDeclareWin() },
                        isGroupMode = uiState.isGroupMode,
                        onToggleGroupMode = { viewModel.toggleGroupMode() },
                        onDiscardCard = { viewModel.onHumanDiscardCard(it) },
                        onDrawStock = { viewModel.onHumanDrawStock() },
                        onDrawDiscard = { viewModel.onHumanDrawDiscard() },
                        topDiscardCard = uiState.discardPile.lastOrNull(),
                        modifier = Modifier.onGloballyPositioned { coords ->
                            val pos = coords.positionInRoot()
                            handCenter = Offset(pos.x + coords.size.width / 2f, pos.y + coords.size.height * 0.4f)
                        }
                    )
                }
            }

            // 1. Moving Card Flight Animation Overlay (from Stock/Discard to Hand or Bot)
            FlyingCardOverlay(
                flyEvent = uiState.activeFlyEvent,
                stockCenter = stockCenter,
                discardCenter = discardCenter,
                handCenter = handCenter,
                opponentCenters = opponentCenters,
                tipluCard = uiState.tipluCard
            )

            // 2. Dealing Cards Transition Animation Overlay (cards dealing out to 5 players)
            DealingCardsOverlay(
                isVisible = uiState.isDealingAnimation,
                progress = uiState.dealingProgress,
                onSkip = { viewModel.skipDealingAnimation() },
                stockCenter = stockCenter,
                opponentCenters = opponentCenters,
                handCenter = handCenter
            )

            // Pass & Play Privacy Screen
            if (uiState.gamePhase == GamePhase.PASS_PHONE_CURTAIN) {
                val nextPlayer = uiState.players.getOrNull(uiState.currentPlayerIndex)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF090D16).copy(alpha = 0.96f))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD700)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "📱 PASS DEVICE",
                                color = Color(0xFFFFD700),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Pass the phone to:",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${nextPlayer?.avatarEmoji} ${nextPlayer?.name}",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                            Text(
                                text = "Hands are concealed for privacy. Tap below when you are ready to view your cards.",
                                color = Color(0xFFCBD5E1),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.dismissPassCurtain() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "I Am ${nextPlayer?.name} (Ready)",
                                    color = Color(0xFF0F172A),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Dialogs
            if (uiState.showModeLobbyDialog) {
                GameModeLobbyDialog(
                    currentMode = uiState.gameMode,
                    aiSpeed = uiState.aiSpeed,
                    aiDifficulty = uiState.aiDifficulty,
                    onlineRoomCode = uiState.onlineRoomCode,
                    lobbySeats = uiState.lobbySeats,
                    facebookUser = uiState.facebookUser,
                    facebookFriends = uiState.facebookFriends,
                    onSelectOfflineBots = { speed, diff ->
                        viewModel.setAiSpeed(speed)
                        viewModel.setAiDifficulty(diff)
                        viewModel.setupOffline5PlayerBots()
                    },
                    onStartOnlineGame = { viewModel.startOnlineGameFromLobby() },
                    onToggleLobbySeatBot = { viewModel.toggleLobbySeatBot(it) },
                    onJoinRoom = { viewModel.joinOnlineRoom(it) },
                    onStartPassAndPlay = { viewModel.setupPassAndPlay5Humans(it) },
                    onConnectFacebook = { viewModel.connectFacebook(it) },
                    onInviteFacebookFriend = { viewModel.inviteFacebookFriend(it) },
                    onInviteAllFacebookFriends = { viewModel.inviteAllFacebookFriends() },
                    onStartFacebookGame = { viewModel.startFacebook5PlayerGame() },
                    onOpenGuide = {
                        viewModel.setShowModeLobby(false)
                        viewModel.setShowFirstTimeGuide(true)
                    },
                    onDismiss = { viewModel.setShowModeLobby(false) }
                )
            }

            if (uiState.showFacebookConnectDialog) {
                FacebookConnectDialog(
                    facebookUser = uiState.facebookUser,
                    isConnecting = uiState.isFacebookConnecting,
                    onlineRoomCode = uiState.onlineRoomCode,
                    onConnect = { viewModel.connectFacebook(it) },
                    onDisconnect = { viewModel.disconnectFacebook() },
                    onCreateFacebookRoom = { viewModel.setupFacebook5PlayerLobby() },
                    onDismiss = { viewModel.setShowFacebookConnectDialog(false) }
                )
            }

            if (uiState.showScoreboardDialog) {
                ScoreboardDialog(
                    scoreBreakdowns = uiState.scoreBreakdowns,
                    winnerPlayer = uiState.winnerPlayer,
                    roundNumber = uiState.roundNumber,
                    onNextRound = { viewModel.dealNewRound() },
                    onDismiss = { viewModel.setShowScoreboard(false) }
                )
            }

            if (uiState.showFirstTimeGuideDialog) {
                FirstTimeUserGuideDialog(
                    initialDoNotShowAgain = uiState.hasSeenFirstTimeGuide,
                    onDismiss = { dontShowAgain ->
                        viewModel.completeFirstTimeGuide(dontShowAgain)
                    }
                )
            }

            if (uiState.showRulesDialog) {
                RulesTutorialDialog(
                    onOpenInteractiveGuide = {
                        viewModel.setShowRules(false)
                        viewModel.setShowFirstTimeGuide(true)
                    },
                    onDismiss = { viewModel.setShowRules(false) }
                )
            }

            if (uiState.showSettingsDialog) {
                SettingsDialog(
                    currentTheme = uiState.activeTheme,
                    soundEnabled = uiState.soundEnabled,
                    hapticsEnabled = uiState.hapticsEnabled,
                    onSelectTheme = { viewModel.setTableTheme(it) },
                    onToggleSound = { viewModel.toggleSound() },
                    onToggleHaptics = { viewModel.toggleHaptics() },
                    onRestartGame = { viewModel.dealNewRound() },
                    onOpenFirstTimeGuide = {
                        viewModel.setShowSettings(false)
                        viewModel.setShowFirstTimeGuide(true)
                    },
                    onDismiss = { viewModel.setShowSettings(false) }
                )
            }
        }
    }
}

@Composable
private fun TopGameBar(
    roundNumber: Int,
    gameMode: GameMode,
    roomCode: String,
    soundEnabled: Boolean,
    isFacebookConnected: Boolean,
    facebookName: String,
    onOpenFacebookConnect: () -> Unit,
    onToggleSound: () -> Unit,
    onOpenFirstTimeGuide: () -> Unit,
    onOpenModeSelector: () -> Unit,
    onOpenScoreboard: () -> Unit,
    onOpenRules: () -> Unit,
    onOpenSettings: () -> Unit,
    onRestartRound: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Title & Mode Selector Pill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "👑 MARRIAGE 21",
                color = Color(0xFFFFD700),
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )

            // Interactive Mode Selector Chip
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF1E293B),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.6f)),
                modifier = Modifier
                    .clickable { onOpenModeSelector() }
                    .testTag("top_btn_mode_selector")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.5.dp)
                ) {
                    Text(
                        text = when (gameMode) {
                            GameMode.OFFLINE_BOTS -> "🤖 5P Offline"
                            GameMode.FACEBOOK_5P_ROOM -> "👥 FB #$roomCode"
                            GameMode.ONLINE_MULTIPLAYER -> "🌐 #$roomCode"
                            GameMode.PASS_AND_PLAY -> "📱 Pass&Play"
                        },
                        color = Color(0xFFFFD700),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFF0F172A),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF475569))
            ) {
                Text(
                    text = "Rnd $roundNumber",
                    color = Color(0xFFE2E8F0),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }

        // Top Action Icons
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            // Facebook Connect / 5P Button
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF1877F2),
                modifier = Modifier
                    .clickable(onClick = onOpenFacebookConnect)
                    .testTag("top_btn_facebook")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        modifier = Modifier.size(13.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "f",
                                color = Color(0xFF1877F2),
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (isFacebookConnected) facebookName.take(6) else "FB 5P",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            // First Time User Guide
            IconButton(
                onClick = onOpenFirstTimeGuide,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("top_btn_how_to_play")
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "Beginner Tutorial",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(17.dp)
                )
            }

            // Sound Effects Mute / Unmute Toggle
            IconButton(
                onClick = onToggleSound,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("top_btn_sound_toggle")
            ) {
                Icon(
                    imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                    contentDescription = if (soundEnabled) "Sound On (Tap to Mute)" else "Sound Off (Tap to Unmute)",
                    tint = if (soundEnabled) Color(0xFF34D399) else Color(0xFF94A3B8),
                    modifier = Modifier.size(17.dp)
                )
            }

            // Mode / Lobby
            IconButton(
                onClick = onOpenModeSelector,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("top_btn_lobby")
            ) {
                Icon(
                    imageVector = Icons.Default.SportsEsports,
                    contentDescription = "Game Mode Lobby",
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(17.dp)
                )
            }

            // Scoreboard Button
            IconButton(
                onClick = onOpenScoreboard,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("top_btn_scoreboard")
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Scoreboard",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(17.dp)
                )
            }

            // Rules / Tutorial
            IconButton(
                onClick = onOpenRules,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("top_btn_rules")
            ) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = "Rules Guide",
                    tint = Color(0xFF60A5FA),
                    modifier = Modifier.size(17.dp)
                )
            }

            // Redeal Round
            IconButton(
                onClick = onRestartRound,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("top_btn_redeal")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Redeal Round",
                    tint = Color(0xFF34D399),
                    modifier = Modifier.size(17.dp)
                )
            }

            // Settings
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("top_btn_settings")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color(0xFFE2E8F0),
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}
