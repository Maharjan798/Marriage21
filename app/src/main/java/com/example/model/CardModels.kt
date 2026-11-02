package com.example.model

import androidx.compose.ui.graphics.Color

enum class Suit(val symbol: String, val displayName: String, val isRed: Boolean) {
    SPADES("♠", "Spades", false),
    HEARTS("♥", "Hearts", true),
    DIAMONDS("♦", "Diamonds", true),
    CLUBS("♣", "Clubs", false);

    val color: Color
        get() = if (isRed) Color(0xFFDC2626) else Color(0xFF18181B)
}

enum class Rank(val value: Int, val display: String) {
    TWO(2, "2"),
    THREE(3, "3"),
    FOUR(4, "4"),
    FIVE(5, "5"),
    SIX(6, "6"),
    SEVEN(7, "7"),
    EIGHT(8, "8"),
    NINE(9, "9"),
    TEN(10, "10"),
    JACK(11, "J"),
    QUEEN(12, "Q"),
    KING(13, "K"),
    ACE(14, "A");

    fun nextRank(): Rank {
        return when (this) {
            ACE -> TWO
            TWO -> THREE
            THREE -> FOUR
            FOUR -> FIVE
            FIVE -> SIX
            SIX -> SEVEN
            SEVEN -> EIGHT
            EIGHT -> NINE
            NINE -> TEN
            TEN -> JACK
            JACK -> QUEEN
            QUEEN -> KING
            KING -> ACE
        }
    }

    fun prevRank(): Rank {
        return when (this) {
            TWO -> ACE
            THREE -> TWO
            FOUR -> THREE
            FIVE -> FOUR
            SIX -> FIVE
            SEVEN -> SIX
            EIGHT -> SEVEN
            NINE -> EIGHT
            TEN -> NINE
            JACK -> TEN
            QUEEN -> JACK
            KING -> QUEEN
            ACE -> KING
        }
    }
}

enum class JokerRole(val displayName: String, val badgeColor: Long, val basePoints: Int) {
    NONE("", 0x00000000, 0),
    TIPLU("TIPLU (Main)", 0xFFFFD700, 3),        // 3 points
    POPLU("POPLU (+1)", 0xFF64B5F6, 2),          // 2 points
    JHIPLU("JHIPLU (-1)", 0xFF81C784, 2),        // 2 points
    ALTER("ALTER JOKER", 0xFFFF8A65, 5)          // 5 points
}

data class Card(
    val id: String,          // Unique ID across the 3 decks (e.g. "D1_SPADES_8")
    val deckIndex: Int,      // 0, 1, or 2
    val suit: Suit,
    val rank: Rank,
    val isFaceUp: Boolean = true
) {
    val shortName: String
        get() = "${rank.display}${suit.symbol}"

    fun isIdenticalTo(other: Card): Boolean {
        return this.suit == other.suit && this.rank == other.rank
    }

    fun getJokerRole(tipluCard: Card?): JokerRole {
        if (tipluCard == null) return JokerRole.NONE
        if (this.suit == tipluCard.suit && this.rank == tipluCard.rank) {
            return JokerRole.TIPLU
        }
        if (this.suit == tipluCard.suit && this.rank == tipluCard.rank.nextRank()) {
            return JokerRole.POPLU
        }
        if (this.suit == tipluCard.suit && this.rank == tipluCard.rank.prevRank()) {
            return JokerRole.JHIPLU
        }
        if (this.rank == tipluCard.rank && this.suit != tipluCard.suit) {
            return JokerRole.ALTER
        }
        return JokerRole.NONE
    }

    fun isWildcard(tipluCard: Card?): Boolean {
        return getJokerRole(tipluCard) != JokerRole.NONE
    }
}

data class Deck(
    val deckCount: Int = 3,
    val cards: List<Card> = emptyList()
) {
    val size: Int get() = cards.size
    val isEmpty: Boolean get() = cards.isEmpty()
    
    fun shuffled(): Deck = Deck(deckCount, cards.shuffled())
    fun draw(): Pair<Card?, Deck> {
        if (cards.isEmpty()) return Pair(null, this)
        return Pair(cards.first(), Deck(deckCount, cards.drop(1)))
    }
}

enum class MeldType(val title: String, val isPure: Boolean, val colorHex: Long) {
    TUNNELLA("Tunnella (3 Identical)", true, 0xFF4CAF50),
    PURE_SEQUENCE("Pure Sequence", true, 0xFF2E7D32),
    DIRTY_SEQUENCE("Dirty Sequence", false, 0xFF1976D2),
    DIRTY_SET("Set / Triplet", false, 0xFF7B1FA2),
    MARRIAGE("Marriage (Tiplu+Poplu+Jhiplu)", false, 0xFFFFB300),
    INVALID("Incomplete / Invalid", false, 0xFFE53935)
}

data class Meld(
    val id: String,
    val cards: List<Card>,
    val type: MeldType,
    val isValid: Boolean,
    val description: String = ""
)

data class Player(
    val id: Int,
    val name: String,
    val avatarEmoji: String,
    val isHuman: Boolean,
    val hand: List<Card> = emptyList(),
    val melds: List<Meld> = emptyList(),
    val unmeldedCards: List<Card> = emptyList(),
    val isSeen: Boolean = false,               // Unlocked Tiplu
    val malPoints: Int = 0,                    // Bonus points from jokers & combinations
    val roundPoints: Int = 0,                  // Points scored in current round
    val totalScore: Int = 0,                   // Cumulative score
    val lastActionText: String = "",
    val isThinking: Boolean = false,
    val isOnlineRemote: Boolean = false,
    val pingMs: Int = 28,
    val chatBubble: String = "",
    val isFacebookFriend: Boolean = false,
    val facebookId: String? = null
)

data class FacebookUser(
    val id: String = "fb_1028491",
    val name: String = "Bibek Maharjan",
    val profilePicEmoji: String = "👤",
    val isConnected: Boolean = false,
    val friendsCount: Int = 184
)

data class FacebookFriend(
    val id: String,
    val name: String,
    val avatarEmoji: String,
    val isOnline: Boolean,
    val currentRoomCode: String? = null,
    val isInvited: Boolean = false
)

enum class GameMode(val title: String, val subtitle: String, val iconEmoji: String) {
    OFFLINE_BOTS("Offline (vs 4 AI)", "Single player vs 4 smart computer bots", "🤖"),
    FACEBOOK_5P_ROOM("Facebook 5P Table", "Invite & join 5 Facebook friends online", "👥"),
    ONLINE_MULTIPLAYER("Online 5-Player Room", "5-seat room with friends or bot fill", "🌐"),
    PASS_AND_PLAY("Pass & Play (5 Players)", "5 local players sharing one phone", "📱")
}

enum class AiDifficulty(val label: String, val desc: String) {
    BALANCED("Balanced", "Standard smart melding"),
    MASTER("Master", "Aggressive joker hoarding & fast declarations")
}

enum class GameSpeed(val label: String, val delayMs: Long) {
    FAST("Fast (0.4s)", 400L),
    NORMAL("Normal (0.8s)", 800L),
    RELAXED("Relaxed (1.4s)", 1400L)
}

data class LobbySeat(
    val seatIndex: Int,
    val name: String,
    val avatarEmoji: String,
    val isBot: Boolean,
    val isReady: Boolean,
    val isHost: Boolean = false,
    val pingMs: Int = 28,
    val isFacebookFriend: Boolean = false,
    val facebookId: String? = null
)

enum class GamePhase {
    IDLE,
    DEALING,
    AWAITING_DRAW,       // Current player must draw from stock or discard
    AWAITING_DISCARD,    // Current player has 22 cards and must discard (can also seen or declare)
    PASS_PHONE_CURTAIN,  // Pass & play privacy curtain
    ROUND_ENDED,
    GAME_ENDED
}

enum class DrawSource {
    STOCK,
    DISCARD
}

data class CardFlyEvent(
    val id: Long = System.currentTimeMillis(),
    val card: Card? = null,
    val source: DrawSource = DrawSource.STOCK,
    val targetPlayerIndex: Int = 0,
    val isFaceUp: Boolean = false
)

enum class TableTheme(val displayName: String, val primaryFelt: Long, val secondaryFelt: Long, val accent: Long) {
    ROYAL_EMERALD("Royal Emerald", 0xFF0D3B2E, 0xFF061F18, 0xFFFFD700),
    MIDNIGHT_SAPPHIRE("Midnight Sapphire", 0xFF0F2042, 0xFF081024, 0xFF64B5F6),
    CRIMSON_CASINO("Crimson Casino", 0xFF3E1015, 0xFF200609, 0xFFFFC107),
    OBSIDIAN_SLATE("Obsidian Velvet", 0xFF1E242B, 0xFF101418, 0xFF00E676)
}

data class ScoreBreakdown(
    val playerId: Int,
    val playerName: String,
    val isWinner: Boolean,
    val isSeen: Boolean,
    val tunnellasCount: Int,
    val marriagesCount: Int,
    val tipluCount: Int,
    val popluCount: Int,
    val jhipluCount: Int,
    val alterCount: Int,
    val malPoints: Int,
    val penaltyPoints: Int,
    val netRoundScore: Int,
    val cumulativeScore: Int
)
