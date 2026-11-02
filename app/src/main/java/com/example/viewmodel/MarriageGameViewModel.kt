package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundManager
import com.example.engine.HandOrganizer
import com.example.engine.MarriageAI
import com.example.engine.MarriageEngine
import com.example.model.AiDifficulty
import com.example.model.Card
import com.example.model.CardFlyEvent
import com.example.model.DrawSource
import com.example.model.FacebookFriend
import com.example.model.FacebookUser
import com.example.model.GameMode
import com.example.model.GamePhase
import com.example.model.GameSpeed
import com.example.model.LobbySeat
import com.example.model.Meld
import com.example.model.MeldType
import com.example.model.Player
import com.example.model.ScoreBreakdown
import com.example.model.TableTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class MarriageUiState(
    val players: List<Player> = emptyList(),
    val currentPlayerIndex: Int = 0,
    val tipluCard: Card? = null,
    val isTipluRevealedToHuman: Boolean = false,
    val stockPile: List<Card> = emptyList(),
    val discardPile: List<Card> = emptyList(),
    val gamePhase: GamePhase = GamePhase.IDLE,
    val roundNumber: Int = 1,
    val selectedCardIds: Set<String> = emptySet(),
    val activeTheme: TableTheme = TableTheme.ROYAL_EMERALD,
    val gameMode: GameMode = GameMode.OFFLINE_BOTS,
    val aiSpeed: GameSpeed = GameSpeed.NORMAL,
    val aiDifficulty: AiDifficulty = AiDifficulty.BALANCED,
    val onlineRoomCode: String = "FB-501",
    val isOnlineLobbyActive: Boolean = false,
    val lobbySeats: List<LobbySeat> = emptyList(),
    val facebookUser: FacebookUser = FacebookUser(),
    val facebookFriends: List<FacebookFriend> = emptyList(),
    val showFacebookConnectDialog: Boolean = false,
    val isFacebookConnecting: Boolean = false,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val statusBanner: String = "Welcome to 5-Player 21-Card Marriage!",
    val bannerHighlight: Boolean = false,
    val scoreBreakdowns: List<ScoreBreakdown>? = null,
    val showScoreboardDialog: Boolean = false,
    val showRulesDialog: Boolean = false,
    val showSettingsDialog: Boolean = false,
    val showModeLobbyDialog: Boolean = false,
    val showFirstTimeGuideDialog: Boolean = false,
    val hasSeenFirstTimeGuide: Boolean = false,
    val isDealingAnimation: Boolean = false,
    val dealingProgress: Float = 0f,
    val activeFlyEvent: CardFlyEvent? = null,
    val winnerPlayer: Player? = null,
    val lastDiscardedBy: String = "",
    val turnTimerSeconds: Int = 30,
    val isGroupMode: Boolean = false,
    val lastSelectedCardId: String? = null
)

class MarriageGameViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("marriage_prefs", Context.MODE_PRIVATE)

    private val defaultFacebookFriends = listOf(
        FacebookFriend("fb_1", "Anish Shrestha", "😎", isOnline = true),
        FacebookFriend("fb_2", "Bikash Sharma", "🤠", isOnline = true),
        FacebookFriend("fb_3", "Sunita Thapa", "👸", isOnline = true),
        FacebookFriend("fb_4", "Sandeep KC", "🦁", isOnline = true),
        FacebookFriend("fb_5", "Neha Adhikari", "🌸", isOnline = true),
        FacebookFriend("fb_6", "Dipesh Rai", "⚡", isOnline = false)
    )

    private val _uiState = MutableStateFlow(MarriageUiState())
    val uiState: StateFlow<MarriageUiState> = _uiState.asStateFlow()

    init {
        val hasSeen = prefs.getBoolean("has_seen_first_time_guide", false)
        val soundPref = prefs.getBoolean("sound_enabled", true)
        val fbConnected = prefs.getBoolean("fb_connected", true) // Default enabled for seamless 5P experience
        val fbName = prefs.getString("fb_name", "Bibek Maharjan") ?: "Bibek Maharjan"
        val fbUser = if (fbConnected) FacebookUser(name = fbName, isConnected = true) else FacebookUser()
        SoundManager.isSoundEnabled = soundPref

        _uiState.update {
            it.copy(
                hasSeenFirstTimeGuide = hasSeen,
                showFirstTimeGuideDialog = !hasSeen,
                soundEnabled = soundPref,
                facebookUser = fbUser,
                facebookFriends = defaultFacebookFriends
            )
        }

        initializeGame()
    }

    fun initializeGame() {
        val fbName = _uiState.value.facebookUser.name.ifBlank { "You" }
        val isFb = _uiState.value.facebookUser.isConnected
        val defaultLobbySeats = listOf(
            LobbySeat(0, if (isFb) "$fbName (You)" else "You", if (isFb) "👤" else "👑", isBot = false, isReady = true, isHost = true, pingMs = 18, isFacebookFriend = isFb),
            LobbySeat(1, "Aarav", "🦁", isBot = true, isReady = true, pingMs = 34),
            LobbySeat(2, "Priya", "🦊", isBot = true, isReady = true, pingMs = 26),
            LobbySeat(3, "Rohan", "🐯", isBot = true, isReady = true, pingMs = 42),
            LobbySeat(4, "Maya", "🦚", isBot = true, isReady = true, pingMs = 29)
        )

        val initialPlayers = listOf(
            Player(id = 0, name = if (isFb) "$fbName (You)" else "You", avatarEmoji = if (isFb) "👤" else "👑", isHuman = true, isFacebookFriend = isFb),
            Player(id = 1, name = "Aarav", avatarEmoji = "🦁", isHuman = false),
            Player(id = 2, name = "Priya", avatarEmoji = "🦊", isHuman = false),
            Player(id = 3, name = "Rohan", avatarEmoji = "🐯", isHuman = false),
            Player(id = 4, name = "Maya", avatarEmoji = "🦚", isHuman = false)
        )

        _uiState.update {
            it.copy(
                players = initialPlayers,
                lobbySeats = defaultLobbySeats,
                gamePhase = GamePhase.IDLE,
                statusBanner = "5-Player Table Ready. Tap 'Deal' to start!"
            )
        }
        dealNewRound()
    }

    private var dealJob: Job? = null
    private var pendingDealtData: PendingDealtData? = null

    private data class PendingDealtData(
        val updatedPlayers: List<Player>,
        val deck: List<Card>,
        val firstDiscard: Card,
        val cutTiplu: Card
    )

    fun dealNewRound() {
        SoundManager.playDeal()
        dealJob?.cancel()

        val deck = MarriageEngine.createFullDeck().toMutableList()

        // Cut one card for Tiplu (under the stock pile)
        val cutTiplu = deck.removeAt(deck.size - 1)

        // Deal 21 cards to each of the 5 players (105 cards total)
        val updatedPlayers = _uiState.value.players.map { player ->
            val handCards = (1..21).map { deck.removeAt(deck.size - 1) }
            val (melds, unmelded) = HandOrganizer.autoGroupHand(handCards, cutTiplu, false)
            player.copy(
                hand = handCards,
                melds = melds,
                unmeldedCards = unmelded,
                isSeen = false,
                malPoints = 0,
                roundPoints = 0,
                lastActionText = "Cards Dealt (21)",
                isThinking = false,
                chatBubble = ""
            )
        }

        // Put one card to open discard pile
        val firstDiscard = deck.removeAt(deck.size - 1)
        pendingDealtData = PendingDealtData(updatedPlayers, deck, firstDiscard, cutTiplu)

        _uiState.update {
            it.copy(
                isDealingAnimation = true,
                dealingProgress = 0f,
                gamePhase = GamePhase.DEALING,
                statusBanner = "Shuffling 3 Decks (156 Cards)... Dealing 21 Cards to 5 Players",
                selectedCardIds = emptySet(),
                scoreBreakdowns = null,
                winnerPlayer = null,
                showScoreboardDialog = false,
                activeFlyEvent = null
            )
        }

        dealJob = viewModelScope.launch {
            val steps = 8
            for (step in 1..steps) {
                delay(125)
                _uiState.update { it.copy(dealingProgress = step / steps.toFloat()) }
            }
            finalizeDealing()
        }
    }

    fun skipDealingAnimation() {
        if (_uiState.value.isDealingAnimation) {
            dealJob?.cancel()
            finalizeDealing()
        }
    }

    private fun finalizeDealing() {
        val data = pendingDealtData ?: return
        val currentHumanIndex = 0
        val isCurrentHuman = data.updatedPlayers.getOrNull(currentHumanIndex)?.isHuman == true

        _uiState.update {
            it.copy(
                isDealingAnimation = false,
                dealingProgress = 1f,
                players = data.updatedPlayers,
                stockPile = data.deck,
                discardPile = listOf(data.firstDiscard),
                tipluCard = data.cutTiplu,
                isTipluRevealedToHuman = false,
                currentPlayerIndex = 0,
                gamePhase = if (_uiState.value.gameMode == GameMode.PASS_AND_PLAY) GamePhase.PASS_PHONE_CURTAIN else GamePhase.AWAITING_DRAW,
                statusBanner = if (isCurrentHuman) "Your Turn! Draw a card from Stock or Discard pile." else "${data.updatedPlayers[0].name}'s Turn...",
                bannerHighlight = isCurrentHuman
            )
        }

        if (isCurrentHuman && _uiState.value.gameMode != GameMode.PASS_AND_PLAY) {
            SoundManager.playYourTurn()
        }

        refreshHumanHandMelds()

        // If player 0 is a bot (e.g. In spectator mode or bot slot), trigger bot
        if (!isCurrentHuman && _uiState.value.gameMode != GameMode.PASS_AND_PLAY) {
            triggerAiTurn(0)
        }
    }

    // --- Human Player Actions ---

    fun onHumanDrawStock() {
        val state = _uiState.value
        val currentPlayer = state.players.getOrNull(state.currentPlayerIndex) ?: return
        if (!currentPlayer.isHuman || state.gamePhase != GamePhase.AWAITING_DRAW) return
        if (state.stockPile.isEmpty()) {
            reshuffleDiscardToStock()
            return
        }

        SoundManager.playDraw()
        val drawnCard = state.stockPile.first()
        val remainingStock = state.stockPile.drop(1)

        val flyEvent = CardFlyEvent(
            id = System.currentTimeMillis(),
            card = drawnCard,
            source = DrawSource.STOCK,
            targetPlayerIndex = state.currentPlayerIndex,
            isFaceUp = true
        )

        _uiState.update {
            it.copy(
                stockPile = remainingStock,
                activeFlyEvent = flyEvent
            )
        }

        viewModelScope.launch {
            delay(320)
            val latestState = _uiState.value
            val latestPlayer = latestState.players.getOrNull(latestState.currentPlayerIndex) ?: return@launch
            val updatedHand = latestPlayer.hand + drawnCard

            val updatedPlayer = latestPlayer.copy(
                hand = updatedHand,
                lastActionText = "Drew from Stock"
            )
            val updatedPlayers = latestState.players.toMutableList().also { it[latestState.currentPlayerIndex] = updatedPlayer }

            _uiState.update {
                it.copy(
                    players = updatedPlayers,
                    gamePhase = GamePhase.AWAITING_DISCARD,
                    statusBanner = "${latestPlayer.name} drew ${drawnCard.shortName}. Select 1 card to discard.",
                    bannerHighlight = false,
                    selectedCardIds = setOf(drawnCard.id),
                    activeFlyEvent = null
                )
            }

            refreshHumanHandMelds()
        }
    }

    fun onHumanDrawDiscard() {
        val state = _uiState.value
        val currentPlayer = state.players.getOrNull(state.currentPlayerIndex) ?: return
        if (!currentPlayer.isHuman || state.gamePhase != GamePhase.AWAITING_DRAW) return
        if (state.discardPile.isEmpty()) return

        SoundManager.playDraw()
        val drawnCard = state.discardPile.last()
        val remainingDiscard = state.discardPile.dropLast(1)

        val flyEvent = CardFlyEvent(
            id = System.currentTimeMillis(),
            card = drawnCard,
            source = DrawSource.DISCARD,
            targetPlayerIndex = state.currentPlayerIndex,
            isFaceUp = true
        )

        _uiState.update {
            it.copy(
                discardPile = remainingDiscard,
                activeFlyEvent = flyEvent
            )
        }

        viewModelScope.launch {
            delay(320)
            val latestState = _uiState.value
            val latestPlayer = latestState.players.getOrNull(latestState.currentPlayerIndex) ?: return@launch
            val updatedHand = latestPlayer.hand + drawnCard

            val updatedPlayer = latestPlayer.copy(
                hand = updatedHand,
                lastActionText = "Picked ${drawnCard.shortName} from Discard"
            )
            val updatedPlayers = latestState.players.toMutableList().also { it[latestState.currentPlayerIndex] = updatedPlayer }

            _uiState.update {
                it.copy(
                    players = updatedPlayers,
                    gamePhase = GamePhase.AWAITING_DISCARD,
                    statusBanner = "${latestPlayer.name} picked ${drawnCard.shortName} from discard. Select 1 card to discard.",
                    bannerHighlight = false,
                    selectedCardIds = setOf(drawnCard.id),
                    activeFlyEvent = null
                )
            }

            refreshHumanHandMelds()
        }
    }

    fun toggleGroupMode() {
        SoundManager.playButtonTap()
        _uiState.update { it.copy(isGroupMode = !it.isGroupMode) }
    }

    fun toggleCardSelection(cardId: String) {
        val state = _uiState.value
        val currentIndex = state.currentPlayerIndex
        val currentPlayer = state.players.getOrNull(currentIndex)
        val isHuman = currentPlayer?.isHuman == true

        if (state.isGroupMode) {
            // Multi-selection mode for grouping melds
            SoundManager.playSelect()
            _uiState.update { current ->
                val currentSelected = current.selectedCardIds
                val newSelected = if (currentSelected.contains(cardId)) {
                    currentSelected - cardId
                } else {
                    currentSelected + cardId
                }
                current.copy(selectedCardIds = newSelected, lastSelectedCardId = cardId)
            }
        } else if (isHuman && state.gamePhase == GamePhase.AWAITING_DISCARD) {
            // In Discard Phase: single selection or double-tap to discard
            if (state.selectedCardIds.contains(cardId)) {
                // Tapping the currently selected card again confirms discard immediately!
                onHumanDiscardCard(cardId)
            } else {
                // Tapping another card makes it the active card to discard
                SoundManager.playSelect()
                val card = currentPlayer.hand.find { it.id == cardId }
                _uiState.update {
                    it.copy(
                        selectedCardIds = setOf(cardId),
                        lastSelectedCardId = cardId,
                        statusBanner = "Selected ${card?.shortName ?: "card"}. Tap Discard or tap Discard Pile to discard."
                    )
                }
            }
        } else {
            // Default selection behavior
            SoundManager.playSelect()
            _uiState.update { current ->
                val currentSelected = current.selectedCardIds
                val newSelected = if (currentSelected.contains(cardId)) {
                    currentSelected - cardId
                } else {
                    currentSelected + cardId
                }
                current.copy(selectedCardIds = newSelected, lastSelectedCardId = cardId)
            }
        }
    }

    fun onHumanDiscardCard(cardId: String) {
        val state = _uiState.value
        val currentIndex = state.currentPlayerIndex
        val currentPlayer = state.players.getOrNull(currentIndex) ?: return
        if (!currentPlayer.isHuman) return

        if (state.gamePhase == GamePhase.AWAITING_DRAW) {
            SoundManager.playButtonTap()
            _uiState.update {
                it.copy(
                    statusBanner = "Draw a card first! Tap the Stock or Discard pile to draw.",
                    bannerHighlight = true
                )
            }
            return
        }

        if (state.gamePhase != GamePhase.AWAITING_DISCARD) return

        val cardToDiscard = currentPlayer.hand.find { it.id == cardId } ?: return

        SoundManager.playDiscard()
        val updatedHand = currentPlayer.hand.filterNot { it.id == cardId }
        val (newMelds, newUnmelded) = HandOrganizer.autoGroupHand(updatedHand, state.tipluCard, currentPlayer.isSeen)
        val mal = if (currentPlayer.isSeen) MarriageEngine.calculatePlayerMal(currentPlayer.copy(hand = updatedHand, melds = newMelds), state.tipluCard) else 0

        val updatedPlayer = currentPlayer.copy(
            hand = updatedHand,
            melds = newMelds,
            unmeldedCards = newUnmelded,
            malPoints = mal,
            lastActionText = "Discarded ${cardToDiscard.shortName}"
        )
        val updatedPlayers = state.players.toMutableList().also { it[currentIndex] = updatedPlayer }
        val updatedDiscard = state.discardPile + cardToDiscard

        val nextIndex = (currentIndex + 1) % state.players.size
        val nextPlayer = updatedPlayers[nextIndex]

        val isPassAndPlay = state.gameMode == GameMode.PASS_AND_PLAY

        _uiState.update {
            it.copy(
                players = updatedPlayers,
                discardPile = updatedDiscard,
                selectedCardIds = emptySet(),
                lastSelectedCardId = null,
                currentPlayerIndex = nextIndex,
                gamePhase = if (isPassAndPlay) GamePhase.PASS_PHONE_CURTAIN else GamePhase.AWAITING_DRAW,
                statusBanner = if (nextPlayer.isHuman && !isPassAndPlay) {
                    "${currentPlayer.name} discarded ${cardToDiscard.shortName}. Your turn to draw!"
                } else {
                    "${currentPlayer.name} discarded ${cardToDiscard.shortName}. ${nextPlayer.name}'s turn..."
                },
                lastDiscardedBy = currentPlayer.name,
                bannerHighlight = nextPlayer.isHuman
            )
        }

        if (nextPlayer.isHuman && !isPassAndPlay) {
            SoundManager.playYourTurn()
        }

        if (!nextPlayer.isHuman && !isPassAndPlay) {
            triggerAiTurn(nextIndex)
        }
    }

    fun onHumanDiscardSelected() {
        val state = _uiState.value
        val currentIndex = state.currentPlayerIndex
        val currentPlayer = state.players.getOrNull(currentIndex) ?: return
        if (!currentPlayer.isHuman) return

        if (state.gamePhase == GamePhase.AWAITING_DRAW) {
            SoundManager.playButtonTap()
            _uiState.update {
                it.copy(
                    statusBanner = "Draw a card first! Tap the Stock or Discard pile to draw.",
                    bannerHighlight = true
                )
            }
            return
        }

        if (state.gamePhase != GamePhase.AWAITING_DISCARD) return

        val selectedIds = state.selectedCardIds
        val cardIdToDiscard = when {
            selectedIds.size == 1 -> selectedIds.first()
            selectedIds.isNotEmpty() -> state.lastSelectedCardId ?: selectedIds.last()
            currentPlayer.hand.size > 21 -> {
                // If user has 22 cards and clicks discard without selecting, select the last unmelded card
                val candidate = currentPlayer.unmeldedCards.lastOrNull()?.id ?: currentPlayer.hand.last().id
                val card = currentPlayer.hand.find { it.id == candidate }
                _uiState.update {
                    it.copy(
                        selectedCardIds = setOf(candidate),
                        lastSelectedCardId = candidate,
                        statusBanner = "Selected ${card?.shortName}. Tap Discard again to confirm!"
                    )
                }
                return
            }
            else -> {
                _uiState.update { it.copy(statusBanner = "Select a card in your hand to discard.") }
                return
            }
        }

        onHumanDiscardCard(cardIdToDiscard)
    }

    fun onHumanUnlockSeen() {
        val state = _uiState.value
        val currentIndex = state.currentPlayerIndex
        val currentPlayer = state.players.getOrNull(currentIndex) ?: return
        if (currentPlayer.isSeen) return

        val (melds, _) = HandOrganizer.autoGroupHand(currentPlayer.hand, state.tipluCard, false)
        val pureCount = MarriageEngine.countPureSets(melds)

        if (pureCount < 3) {
            _uiState.update {
                it.copy(
                    statusBanner = "Cannot Unlock yet! You need 3 Pure Sets (Tunnellas or Pure Sequences). Currently have $pureCount."
                )
            }
            return
        }

        SoundManager.playUnlockSeen()
        val updatedPlayer = currentPlayer.copy(
            isSeen = true,
            lastActionText = "Unlocked Tiplu (Seen!)"
        )
        val updatedPlayers = state.players.toMutableList().also { it[currentIndex] = updatedPlayer }

        _uiState.update {
            it.copy(
                players = updatedPlayers,
                isTipluRevealedToHuman = true,
                statusBanner = "★ SEEN UNLOCKED! Tiplu is ${state.tipluCard?.shortName}. Jokers & Mal bonuses are now active!"
            )
        }

        refreshHumanHandMelds()
    }

    fun onHumanDeclareWin() {
        val state = _uiState.value
        val currentIndex = state.currentPlayerIndex
        val currentPlayer = state.players.getOrNull(currentIndex) ?: return

        if (!currentPlayer.isSeen) {
            _uiState.update { it.copy(statusBanner = "You must Unlock (Seen) first before declaring a win!") }
            return
        }

        val (melds, unmelded) = HandOrganizer.autoGroupHand(currentPlayer.hand, state.tipluCard, true)
        val canWin = MarriageEngine.canDeclareWin(melds, state.tipluCard, true)

        if (!canWin && unmelded.size > 1) {
            _uiState.update {
                it.copy(statusBanner = "Cannot declare: All 21 cards must form 7 valid melds with at least 3 Pure Sets!")
            }
            return
        }

        // Win validated!
        concludeRound(winnerId = currentIndex)
    }

    fun onHumanAutoGroup() {
        val state = _uiState.value
        val currentIndex = state.currentPlayerIndex
        val currentPlayer = state.players.getOrNull(currentIndex) ?: return
        val (melds, unmelded) = HandOrganizer.autoGroupHand(currentPlayer.hand, state.tipluCard, currentPlayer.isSeen)
        val mal = if (currentPlayer.isSeen) MarriageEngine.calculatePlayerMal(currentPlayer, state.tipluCard) else 0

        val updatedPlayer = currentPlayer.copy(
            melds = melds,
            unmeldedCards = unmelded,
            malPoints = mal
        )
        val updatedPlayers = state.players.toMutableList().also { it[currentIndex] = updatedPlayer }

        _uiState.update {
            it.copy(
                players = updatedPlayers,
                selectedCardIds = emptySet(),
                statusBanner = "Hand auto-organized into ${melds.size} melds."
            )
        }
    }

    fun onHumanSortBySuit() {
        val state = _uiState.value
        val currentIndex = state.currentPlayerIndex
        val currentPlayer = state.players.getOrNull(currentIndex) ?: return
        val sortedHand = HandOrganizer.sortBySuit(currentPlayer.hand)
        val (melds, unmelded) = HandOrganizer.autoGroupHand(sortedHand, state.tipluCard, currentPlayer.isSeen)

        val updatedPlayer = currentPlayer.copy(
            hand = sortedHand,
            melds = melds,
            unmeldedCards = unmelded
        )
        val updatedPlayers = state.players.toMutableList().also { it[currentIndex] = updatedPlayer }

        _uiState.update {
            it.copy(players = updatedPlayers, selectedCardIds = emptySet())
        }
    }

    fun onHumanSortByRank() {
        val state = _uiState.value
        val currentIndex = state.currentPlayerIndex
        val currentPlayer = state.players.getOrNull(currentIndex) ?: return
        val sortedHand = HandOrganizer.sortByRank(currentPlayer.hand)
        val (melds, unmelded) = HandOrganizer.autoGroupHand(sortedHand, state.tipluCard, currentPlayer.isSeen)

        val updatedPlayer = currentPlayer.copy(
            hand = sortedHand,
            melds = melds,
            unmeldedCards = unmelded
        )
        val updatedPlayers = state.players.toMutableList().also { it[currentIndex] = updatedPlayer }

        _uiState.update {
            it.copy(players = updatedPlayers, selectedCardIds = emptySet())
        }
    }

    fun createManualMeldFromSelected() {
        val state = _uiState.value
        val currentIndex = state.currentPlayerIndex
        val currentPlayer = state.players.getOrNull(currentIndex) ?: return
        val selectedIds = state.selectedCardIds
        if (selectedIds.size < 3) {
            _uiState.update { it.copy(statusBanner = "Select at least 3 cards to form a meld.") }
            return
        }

        val selectedCards = currentPlayer.hand.filter { selectedIds.contains(it.id) }
        val evaluatedMeld = MarriageEngine.evaluateMeld(selectedCards, state.tipluCard, currentPlayer.isSeen)
        if (evaluatedMeld.isValid) {
            SoundManager.playMeldFormed()
        }

        val updatedMelds = currentPlayer.melds + evaluatedMeld
        val remainingUnmelded = currentPlayer.hand.filterNot { card ->
            updatedMelds.flatMap { it.cards }.any { it.id == card.id }
        }

        val updatedPlayer = currentPlayer.copy(
            melds = updatedMelds,
            unmeldedCards = remainingUnmelded
        )
        val updatedPlayers = state.players.toMutableList().also { it[currentIndex] = updatedPlayer }

        _uiState.update {
            it.copy(
                players = updatedPlayers,
                selectedCardIds = emptySet(),
                statusBanner = "Formed ${evaluatedMeld.description}"
            )
        }
    }

    fun disbandMeld(meldId: String) {
        val state = _uiState.value
        val currentIndex = state.currentPlayerIndex
        val currentPlayer = state.players.getOrNull(currentIndex) ?: return
        val updatedMelds = currentPlayer.melds.filterNot { it.id == meldId }
        val remainingUnmelded = currentPlayer.hand.filterNot { card ->
            updatedMelds.flatMap { it.cards }.any { it.id == card.id }
        }

        val updatedPlayer = currentPlayer.copy(
            melds = updatedMelds,
            unmeldedCards = remainingUnmelded
        )
        val updatedPlayers = state.players.toMutableList().also { it[currentIndex] = updatedPlayer }

        _uiState.update {
            it.copy(
                players = updatedPlayers,
                statusBanner = "Meld disbanded into hand."
            )
        }
    }

    fun dismissPassCurtain() {
        _uiState.update {
            it.copy(
                gamePhase = GamePhase.AWAITING_DRAW,
                statusBanner = "${_uiState.value.players[_uiState.value.currentPlayerIndex].name}'s turn. Draw from Stock or Discard!"
            )
        }
    }

    private fun refreshHumanHandMelds() {
        val state = _uiState.value
        val currentIndex = state.currentPlayerIndex
        val player = state.players.getOrNull(currentIndex) ?: return
        val (melds, unmelded) = HandOrganizer.autoGroupHand(player.hand, state.tipluCard, player.isSeen)
        val mal = if (player.isSeen) MarriageEngine.calculatePlayerMal(player, state.tipluCard) else 0

        val updatedPlayer = player.copy(
            melds = melds,
            unmeldedCards = unmelded,
            malPoints = mal
        )
        val updatedPlayers = state.players.toMutableList().also { it[currentIndex] = updatedPlayer }
        _uiState.update { it.copy(players = updatedPlayers) }
    }

    // --- AI / Computer Opponent Engine (for 5 Players) ---

    private fun triggerAiTurn(playerIndex: Int) {
        viewModelScope.launch {
            if (_uiState.value.gamePhase == GamePhase.ROUND_ENDED || _uiState.value.gamePhase == GamePhase.GAME_ENDED) return@launch
            val aiPlayer = _uiState.value.players.getOrNull(playerIndex) ?: return@launch
            if (aiPlayer.isHuman) return@launch

            val stepDelay = _uiState.value.aiSpeed.delayMs

            // 1. Thinking / Draw phase
            setPlayerThinking(playerIndex, true, "Thinking...")
            delay(stepDelay)

            val stateBeforeDraw = _uiState.value
            val topDiscard = stateBeforeDraw.discardPile.lastOrNull()
            val decision = MarriageAI.decideDrawSource(aiPlayer, topDiscard, stateBeforeDraw.tipluCard)

            var drawnCard: Card
            var newStock = stateBeforeDraw.stockPile
            var newDiscard = stateBeforeDraw.discardPile
            val drawSource: DrawSource
            if (decision == DrawSource.DISCARD && topDiscard != null) {
                drawnCard = topDiscard
                newDiscard = newDiscard.dropLast(1)
                drawSource = DrawSource.DISCARD
                updatePlayerAction(playerIndex, "Picked ${drawnCard.shortName} from Discard")
            } else {
                if (newStock.isEmpty()) {
                    reshuffleDiscardToStock()
                    newStock = _uiState.value.stockPile
                }
                drawnCard = newStock.first()
                newStock = newStock.drop(1)
                drawSource = DrawSource.STOCK
                updatePlayerAction(playerIndex, "Drew from Stock")
            }

            SoundManager.playDraw()
            _uiState.update {
                it.copy(
                    stockPile = newStock,
                    discardPile = newDiscard,
                    activeFlyEvent = CardFlyEvent(
                        id = System.currentTimeMillis(),
                        card = if (drawSource == DrawSource.DISCARD) drawnCard else null,
                        source = drawSource,
                        targetPlayerIndex = playerIndex,
                        isFaceUp = drawSource == DrawSource.DISCARD
                    )
                )
            }

            delay(280)
            _uiState.update { it.copy(activeFlyEvent = null) }

            val updatedHandWithDraw = aiPlayer.hand + drawnCard
            var currentAi = aiPlayer.copy(hand = updatedHandWithDraw)

            delay(stepDelay)

            // 2. Check if AI can Unlock (Seen)
            if (!currentAi.isSeen && MarriageAI.shouldUnlockSeen(currentAi, stateBeforeDraw.tipluCard)) {
                currentAi = currentAi.copy(isSeen = true)
                updatePlayerAction(playerIndex, "★ UNLOCKED SEEN!")
                SoundManager.playUnlockSeen()
                _uiState.update {
                    it.copy(statusBanner = "${currentAi.name} showed 3 Pure Sets and UNLOCKED Tiplu!")
                }
                triggerRandomEmojiBanter(playerIndex, listOf("⭐", "🔥", "🃏", "👀"))
                delay(stepDelay)
            }

            // 3. Check if AI can Declare Win
            if (MarriageAI.canDeclareWin(currentAi, stateBeforeDraw.tipluCard)) {
                val winningDiscard = MarriageAI.chooseCardToDiscard(currentAi, stateBeforeDraw.tipluCard)
                val finalHand = currentAi.hand.filterNot { it.id == winningDiscard.id }
                currentAi = currentAi.copy(hand = finalHand)

                val playersList = _uiState.value.players.toMutableList().also { it[playerIndex] = currentAi }
                _uiState.update {
                    it.copy(
                        players = playersList,
                        discardPile = it.discardPile + winningDiscard,
                        lastDiscardedBy = currentAi.name
                    )
                }

                concludeRound(winnerId = playerIndex)
                return@launch
            }

            // 4. Discard phase
            val cardToDiscard = MarriageAI.chooseCardToDiscard(currentAi, stateBeforeDraw.tipluCard)
            SoundManager.playDiscard()
            val updatedHandAfterDiscard = currentAi.hand.filterNot { it.id == cardToDiscard.id }
            val (melds, unmelded) = HandOrganizer.autoGroupHand(updatedHandAfterDiscard, stateBeforeDraw.tipluCard, currentAi.isSeen)
            val mal = if (currentAi.isSeen) MarriageEngine.calculatePlayerMal(currentAi, stateBeforeDraw.tipluCard) else 0

            val finalizedAi = currentAi.copy(
                hand = updatedHandAfterDiscard,
                melds = melds,
                unmeldedCards = unmelded,
                malPoints = mal,
                lastActionText = "Discarded ${cardToDiscard.shortName}",
                isThinking = false
            )

            val nextPlayerIndex = (playerIndex + 1) % _uiState.value.players.size
            val allPlayers = _uiState.value.players.toMutableList().also { it[playerIndex] = finalizedAi }
            val nextPlayer = allPlayers[nextPlayerIndex]

            val isPassAndPlay = _uiState.value.gameMode == GameMode.PASS_AND_PLAY

            _uiState.update {
                it.copy(
                    players = allPlayers,
                    discardPile = it.discardPile + cardToDiscard,
                    currentPlayerIndex = nextPlayerIndex,
                    lastDiscardedBy = finalizedAi.name,
                    gamePhase = if (isPassAndPlay && nextPlayer.isHuman) GamePhase.PASS_PHONE_CURTAIN else GamePhase.AWAITING_DRAW,
                    statusBanner = if (nextPlayer.isHuman && !isPassAndPlay) {
                        "${finalizedAi.name} discarded ${cardToDiscard.shortName}. Your turn to draw!"
                    } else {
                        "${finalizedAi.name} discarded ${cardToDiscard.shortName}. ${nextPlayer.name}'s turn..."
                    },
                    bannerHighlight = nextPlayer.isHuman
                )
            }

            if (nextPlayer.isHuman && !isPassAndPlay) {
                SoundManager.playYourTurn()
            }

            // If next player is not human, trigger their AI turn
            if (!nextPlayer.isHuman) {
                triggerAiTurn(nextPlayerIndex)
            }
        }
    }

    private fun setPlayerThinking(index: Int, thinking: Boolean, status: String) {
        val players = _uiState.value.players.toMutableList()
        if (index in players.indices) {
            players[index] = players[index].copy(isThinking = thinking, lastActionText = status)
            _uiState.update { it.copy(players = players) }
        }
    }

    private fun updatePlayerAction(index: Int, actionText: String) {
        val players = _uiState.value.players.toMutableList()
        if (index in players.indices) {
            players[index] = players[index].copy(lastActionText = actionText)
            _uiState.update { it.copy(players = players) }
        }
    }

    private fun triggerRandomEmojiBanter(playerIndex: Int, emojis: List<String>) {
        val emoji = emojis.randomOrNull() ?: return
        val players = _uiState.value.players.toMutableList()
        if (playerIndex in players.indices) {
            players[playerIndex] = players[playerIndex].copy(chatBubble = emoji)
            _uiState.update { it.copy(players = players) }
            viewModelScope.launch {
                delay(2200)
                val currentPlayers = _uiState.value.players.toMutableList()
                if (playerIndex in currentPlayers.indices) {
                    currentPlayers[playerIndex] = currentPlayers[playerIndex].copy(chatBubble = "")
                    _uiState.update { it.copy(players = currentPlayers) }
                }
            }
        }
    }

    fun sendQuickReaction(emoji: String) {
        val myIndex = 0
        SoundManager.playChatPop()
        triggerRandomEmojiBanter(myIndex, listOf(emoji))
        // Occasional responsive reaction from an AI
        viewModelScope.launch {
            delay(1200)
            val randomOpponent = (1..4).random()
            val responseEmojis = when (emoji) {
                "🔥" -> listOf("👏", "😱", "💪")
                "🎉" -> listOf("👏", "👑", "😎")
                "🃏" -> listOf("👀", "🤔", "🎯")
                else -> listOf("👍", "✨", "👋")
            }
            triggerRandomEmojiBanter(randomOpponent, responseEmojis)
        }
    }

    private fun reshuffleDiscardToStock() {
        val discard = _uiState.value.discardPile
        if (discard.size <= 1) return
        val top = discard.last()
        val rest = discard.dropLast(1).shuffled()
        _uiState.update {
            it.copy(
                stockPile = rest,
                discardPile = listOf(top),
                statusBanner = "Stock pile was empty. Discard pile reshuffled into Stock!"
            )
        }
    }

    private fun concludeRound(winnerId: Int) {
        SoundManager.playVictory()
        val state = _uiState.value
        val winner = state.players.find { it.id == winnerId }
        val breakdowns = MarriageEngine.calculateRoundSettlement(winnerId, state.players, state.tipluCard)

        val updatedPlayersWithCumulative = state.players.map { p ->
            val b = breakdowns.find { it.playerId == p.id }
            p.copy(
                totalScore = b?.cumulativeScore ?: p.totalScore,
                roundPoints = b?.netRoundScore ?: 0,
                isThinking = false
            )
        }

        _uiState.update {
            it.copy(
                players = updatedPlayersWithCumulative,
                gamePhase = GamePhase.ROUND_ENDED,
                winnerPlayer = winner,
                scoreBreakdowns = breakdowns,
                showScoreboardDialog = true,
                isTipluRevealedToHuman = true,
                statusBanner = "🏆 ${winner?.name} WON THE ROUND! Check Scoreboard for Mal settlement."
            )
        }
    }

    // --- Mode Selection & Online 5-Player Room Setup ---

    fun setGameMode(mode: GameMode) {
        _uiState.update { it.copy(gameMode = mode) }
        when (mode) {
            GameMode.OFFLINE_BOTS -> setupOffline5PlayerBots()
            GameMode.FACEBOOK_5P_ROOM -> setupFacebook5PlayerLobby()
            GameMode.ONLINE_MULTIPLAYER -> setupOnline5PlayerLobby()
            GameMode.PASS_AND_PLAY -> setupPassAndPlay5Humans()
        }
    }

    fun setupOffline5PlayerBots() {
        val botPlayers = listOf(
            Player(id = 0, name = "You", avatarEmoji = "👑", isHuman = true),
            Player(id = 1, name = "Aarav", avatarEmoji = "🦁", isHuman = false),
            Player(id = 2, name = "Priya", avatarEmoji = "🦊", isHuman = false),
            Player(id = 3, name = "Rohan", avatarEmoji = "🐯", isHuman = false),
            Player(id = 4, name = "Maya", avatarEmoji = "🦚", isHuman = false)
        )
        _uiState.update {
            it.copy(
                players = botPlayers,
                gameMode = GameMode.OFFLINE_BOTS,
                showModeLobbyDialog = false,
                statusBanner = "Starting Offline 5-Player Game vs 4 Computer Bots..."
            )
        }
        dealNewRound()
    }

    fun setupOnline5PlayerLobby() {
        val code = "MAR" + (100..999).random()
        val defaultSeats = listOf(
            LobbySeat(0, "You", "👑", isBot = false, isReady = true, isHost = true, pingMs = 22),
            LobbySeat(1, "Aarav", "🦁", isBot = true, isReady = true, pingMs = 38),
            LobbySeat(2, "Priya", "🦊", isBot = true, isReady = true, pingMs = 31),
            LobbySeat(3, "Rohan", "🐯", isBot = true, isReady = true, pingMs = 45),
            LobbySeat(4, "Maya", "🦚", isBot = true, isReady = true, pingMs = 29)
        )
        _uiState.update {
            it.copy(
                onlineRoomCode = code,
                lobbySeats = defaultSeats,
                gameMode = GameMode.ONLINE_MULTIPLAYER,
                showModeLobbyDialog = true
            )
        }
    }

    fun toggleLobbySeatBot(seatIndex: Int) {
        if (seatIndex == 0) return // Host is always human
        val seats = _uiState.value.lobbySeats.toMutableList()
        val current = seats[seatIndex]
        val newIsBot = !current.isBot
        seats[seatIndex] = current.copy(
            isBot = newIsBot,
            name = if (newIsBot) "Bot ${current.name}" else "Player ${seatIndex + 1}"
        )
        _uiState.update { it.copy(lobbySeats = seats) }
    }

    fun startOnlineGameFromLobby() {
        val seats = _uiState.value.lobbySeats
        val onlinePlayers = seats.mapIndexed { index, seat ->
            Player(
                id = index,
                name = seat.name,
                avatarEmoji = seat.avatarEmoji,
                isHuman = !seat.isBot && index == 0,
                isOnlineRemote = !seat.isBot && index != 0,
                pingMs = seat.pingMs
            )
        }

        _uiState.update {
            it.copy(
                players = onlinePlayers,
                showModeLobbyDialog = false,
                gameMode = GameMode.ONLINE_MULTIPLAYER,
                statusBanner = "Online 5-Player Room #${_uiState.value.onlineRoomCode} Connected! Dealing cards..."
            )
        }
        dealNewRound()
    }

    fun setupPassAndPlay5Humans(customNames: List<String> = listOf("Player 1", "Player 2", "Player 3", "Player 4", "Player 5")) {
        val emojis = listOf("👑", "🦁", "🦊", "🐯", "🦚")
        val passPlayers = customNames.mapIndexed { index, name ->
            Player(
                id = index,
                name = name,
                avatarEmoji = emojis.getOrElse(index) { "🃏" },
                isHuman = true
            )
        }
        _uiState.update {
            it.copy(
                players = passPlayers,
                gameMode = GameMode.PASS_AND_PLAY,
                showModeLobbyDialog = false,
                statusBanner = "5-Player Pass & Play Game Started!"
            )
        }
        dealNewRound()
    }

    fun joinOnlineRoom(code: String) {
        val formattedCode = code.trim().uppercase()
        val isFbCode = formattedCode.startsWith("FB-")
        val fbSeats = if (isFbCode) {
            val fbFriends = _uiState.value.facebookFriends.take(4)
            listOf(
                LobbySeat(0, "${_uiState.value.facebookUser.name} (You)", "👤", isBot = false, isReady = true, isHost = false, pingMs = 19, isFacebookFriend = true),
                LobbySeat(1, fbFriends.getOrNull(0)?.name ?: "Anish Shrestha", fbFriends.getOrNull(0)?.avatarEmoji ?: "😎", isBot = false, isReady = true, isHost = true, pingMs = 24, isFacebookFriend = true),
                LobbySeat(2, fbFriends.getOrNull(1)?.name ?: "Bikash Sharma", fbFriends.getOrNull(1)?.avatarEmoji ?: "🤠", isBot = false, isReady = true, pingMs = 31, isFacebookFriend = true),
                LobbySeat(3, fbFriends.getOrNull(2)?.name ?: "Sunita Thapa", fbFriends.getOrNull(2)?.avatarEmoji ?: "👸", isBot = false, isReady = true, pingMs = 28, isFacebookFriend = true),
                LobbySeat(4, fbFriends.getOrNull(3)?.name ?: "Sandeep KC", fbFriends.getOrNull(3)?.avatarEmoji ?: "🦁", isBot = false, isReady = true, pingMs = 35, isFacebookFriend = true)
            )
        } else _uiState.value.lobbySeats

        _uiState.update {
            it.copy(
                onlineRoomCode = formattedCode,
                gameMode = if (isFbCode) GameMode.FACEBOOK_5P_ROOM else GameMode.ONLINE_MULTIPLAYER,
                lobbySeats = fbSeats,
                statusBanner = "Connected to Room #$formattedCode! 5 players joined."
            )
        }
    }

    // --- Facebook Online 5-Player System ---

    fun connectFacebook(customName: String = "Bibek Maharjan") {
        viewModelScope.launch {
            _uiState.update { it.copy(isFacebookConnecting = true) }
            delay(400)
            prefs.edit().putBoolean("fb_connected", true).putString("fb_name", customName).apply()
            val user = FacebookUser(name = customName, isConnected = true)

            val players = _uiState.value.players.toMutableList()
            if (players.isNotEmpty()) {
                players[0] = players[0].copy(
                    name = "$customName (You)",
                    avatarEmoji = "👤",
                    isFacebookFriend = true
                )
            }

            val seats = _uiState.value.lobbySeats.toMutableList()
            if (seats.isNotEmpty()) {
                seats[0] = seats[0].copy(
                    name = "$customName (Host)",
                    avatarEmoji = "👤",
                    isFacebookFriend = true
                )
            }

            _uiState.update {
                it.copy(
                    facebookUser = user,
                    players = players,
                    lobbySeats = seats,
                    isFacebookConnecting = false,
                    showFacebookConnectDialog = false,
                    statusBanner = "Connected to Facebook as $customName! Invite 5 friends to your table."
                )
            }
            SoundManager.playButtonTap()
        }
    }

    fun disconnectFacebook() {
        prefs.edit().putBoolean("fb_connected", false).apply()
        val user = FacebookUser(isConnected = false)
        val players = _uiState.value.players.toMutableList()
        if (players.isNotEmpty()) {
            players[0] = players[0].copy(name = "You", avatarEmoji = "👑", isFacebookFriend = false)
        }
        val seats = _uiState.value.lobbySeats.toMutableList()
        if (seats.isNotEmpty()) {
            seats[0] = seats[0].copy(name = "You", avatarEmoji = "👑", isFacebookFriend = false)
        }
        _uiState.update {
            it.copy(
                facebookUser = user,
                players = players,
                lobbySeats = seats,
                showFacebookConnectDialog = false,
                statusBanner = "Disconnected from Facebook. Playing as local guest."
            )
        }
    }

    fun setupFacebook5PlayerLobby() {
        val code = "FB-" + (1000..9999).random()
        val fbName = _uiState.value.facebookUser.name.ifBlank { "Bibek" }
        val seats = listOf(
            LobbySeat(0, "$fbName (Host)", "👤", isBot = false, isReady = true, isHost = true, pingMs = 18, isFacebookFriend = true),
            LobbySeat(1, "Empty (Invite FB Friend)", "👥", isBot = false, isReady = false, pingMs = 0),
            LobbySeat(2, "Empty (Invite FB Friend)", "👥", isBot = false, isReady = false, pingMs = 0),
            LobbySeat(3, "Empty (Invite FB Friend)", "👥", isBot = false, isReady = false, pingMs = 0),
            LobbySeat(4, "Empty (Invite FB Friend)", "👥", isBot = false, isReady = false, pingMs = 0)
        )
        _uiState.update {
            it.copy(
                onlineRoomCode = code,
                lobbySeats = seats,
                gameMode = GameMode.FACEBOOK_5P_ROOM,
                showModeLobbyDialog = true,
                statusBanner = "Facebook 5-Player Room #$code Created! Tap Invite Friends."
            )
        }
    }

    fun inviteFacebookFriend(friendId: String) {
        val friends = _uiState.value.facebookFriends.toMutableList()
        val friendIndex = friends.indexOfFirst { it.id == friendId }
        if (friendIndex == -1) return
        val friend = friends[friendIndex]
        friends[friendIndex] = friend.copy(isInvited = true)
        _uiState.update { it.copy(facebookFriends = friends) }

        // Find first empty seat
        val seats = _uiState.value.lobbySeats.toMutableList()
        val emptySeatIndex = seats.indexOfFirst { it.seatIndex > 0 && (!it.isFacebookFriend || it.isBot) }
        if (emptySeatIndex != -1) {
            viewModelScope.launch {
                delay(800)
                val updatedSeats = _uiState.value.lobbySeats.toMutableList()
                if (emptySeatIndex in updatedSeats.indices) {
                    updatedSeats[emptySeatIndex] = LobbySeat(
                        seatIndex = emptySeatIndex,
                        name = friend.name,
                        avatarEmoji = friend.avatarEmoji,
                        isBot = false,
                        isReady = true,
                        pingMs = (20..42).random(),
                        isFacebookFriend = true,
                        facebookId = friend.id
                    )
                    SoundManager.playYourTurn()
                    _uiState.update {
                        it.copy(
                            lobbySeats = updatedSeats,
                            statusBanner = "${friend.name} accepted your Facebook invite and joined Seat ${emptySeatIndex + 1}!"
                        )
                    }
                }
            }
        }
    }

    fun inviteAllFacebookFriends() {
        val availableFriends = _uiState.value.facebookFriends.filter { it.isOnline }.take(4)
        val friends = _uiState.value.facebookFriends.map { f ->
            if (availableFriends.any { it.id == f.id }) f.copy(isInvited = true) else f
        }
        _uiState.update {
            it.copy(
                facebookFriends = friends,
                statusBanner = "Invited 4 online Facebook friends to Room #${it.onlineRoomCode}..."
            )
        }

        viewModelScope.launch {
            availableFriends.forEachIndexed { index, friend ->
                delay(400L + (index * 300L))
                val seatIdx = index + 1
                val seats = _uiState.value.lobbySeats.toMutableList()
                if (seatIdx in seats.indices) {
                    seats[seatIdx] = LobbySeat(
                        seatIndex = seatIdx,
                        name = friend.name,
                        avatarEmoji = friend.avatarEmoji,
                        isBot = false,
                        isReady = true,
                        pingMs = (22..38).random(),
                        isFacebookFriend = true,
                        facebookId = friend.id
                    )
                    SoundManager.playDraw()
                    _uiState.update {
                        it.copy(
                            lobbySeats = seats,
                            statusBanner = "${friend.name} joined Seat ${seatIdx + 1}! (${seats.count { s -> s.isReady }}/5 Ready)"
                        )
                    }
                }
            }
            SoundManager.playVictory()
            _uiState.update {
                it.copy(
                    statusBanner = "All 5 Facebook players joined and ready! Tap 'START 5P MATCH'!"
                )
            }
        }
    }

    fun startFacebook5PlayerGame() {
        val seats = _uiState.value.lobbySeats
        val onlinePlayers = seats.mapIndexed { index, seat ->
            Player(
                id = index,
                name = seat.name,
                avatarEmoji = seat.avatarEmoji,
                isHuman = index == 0,
                isOnlineRemote = index != 0,
                isFacebookFriend = true,
                pingMs = seat.pingMs
            )
        }

        _uiState.update {
            it.copy(
                players = onlinePlayers,
                showModeLobbyDialog = false,
                gameMode = GameMode.FACEBOOK_5P_ROOM,
                statusBanner = "Facebook 5-Player Match Started! Dealing cards..."
            )
        }
        dealNewRound()
    }

    fun setShowFacebookConnectDialog(show: Boolean) {
        _uiState.update { it.copy(showFacebookConnectDialog = show) }
    }

    fun setAiSpeed(speed: GameSpeed) {
        _uiState.update { it.copy(aiSpeed = speed) }
    }

    fun setAiDifficulty(difficulty: AiDifficulty) {
        _uiState.update { it.copy(aiDifficulty = difficulty) }
    }

    // --- Dialogs & Theme Settings ---

    fun setShowModeLobby(show: Boolean) {
        _uiState.update { it.copy(showModeLobbyDialog = show) }
    }

    fun setShowScoreboard(show: Boolean) {
        _uiState.update { it.copy(showScoreboardDialog = show) }
    }

    fun setShowRules(show: Boolean) {
        _uiState.update { it.copy(showRulesDialog = show) }
    }

    fun setShowSettings(show: Boolean) {
        _uiState.update { it.copy(showSettingsDialog = show) }
    }

    fun setShowFirstTimeGuide(show: Boolean) {
        _uiState.update { it.copy(showFirstTimeGuideDialog = show) }
    }

    fun completeFirstTimeGuide(dontShowAgain: Boolean) {
        if (dontShowAgain) {
            prefs.edit().putBoolean("has_seen_first_time_guide", true).apply()
        }
        _uiState.update { it.copy(showFirstTimeGuideDialog = false, hasSeenFirstTimeGuide = true) }
    }

    fun setTableTheme(theme: TableTheme) {
        _uiState.update { it.copy(activeTheme = theme) }
    }

    fun toggleSound() {
        _uiState.update { current ->
            val newSound = !current.soundEnabled
            SoundManager.isSoundEnabled = newSound
            prefs.edit().putBoolean("sound_enabled", newSound).apply()
            if (newSound) {
                SoundManager.playButtonTap()
            }
            current.copy(soundEnabled = newSound)
        }
    }

    fun toggleHaptics() {
        _uiState.update { it.copy(hapticsEnabled = !it.hapticsEnabled) }
    }
}
