package com.example.engine

import com.example.model.Card
import com.example.model.JokerRole
import com.example.model.Meld
import com.example.model.MeldType
import com.example.model.Player
import com.example.model.Rank
import com.example.model.ScoreBreakdown
import com.example.model.Suit
import java.util.UUID

object MarriageEngine {

    /**
     * Generates a 3-deck standard card set (156 cards total).
     */
    fun createFullDeck(): List<Card> {
        val cards = mutableListOf<Card>()
        for (deckIdx in 0..2) {
            for (suit in Suit.values()) {
                for (rank in Rank.values()) {
                    cards.add(
                        Card(
                            id = "D${deckIdx}_${suit.name}_${rank.name}_${UUID.randomUUID().toString().take(4)}",
                            deckIndex = deckIdx,
                            suit = suit,
                            rank = rank
                        )
                    )
                }
            }
        }
        return cards.shuffled()
    }

    /**
     * Tunnella: Exactly 3 identical cards (same suit AND same rank).
     */
    fun isTunnella(cards: List<Card>): Boolean {
        if (cards.size != 3) return false
        val first = cards[0]
        return cards.all { it.suit == first.suit && it.rank == first.rank }
    }

    /**
     * Pure Sequence: 3 or more consecutive cards of the same suit WITHOUT wildcards.
     */
    fun isPureSequence(cards: List<Card>): Boolean {
        if (cards.size < 3) return false
        val suit = cards[0].suit
        if (!cards.all { it.suit == suit }) return false

        // Sort by rank values
        val rankValues = cards.map { it.rank.value }.sorted()
        if (isConsecutive(rankValues)) return true

        // Check Ace-low sequence (A-2-3-...) where Ace value 14 is treated as 1
        if (cards.any { it.rank == Rank.ACE }) {
            val aceLowValues = cards.map { if (it.rank == Rank.ACE) 1 else it.rank.value }.sorted()
            if (isConsecutive(aceLowValues)) return true
        }

        return false
    }

    private fun isConsecutive(sortedInts: List<Int>): Boolean {
        for (i in 0 until sortedInts.size - 1) {
            if (sortedInts[i + 1] != sortedInts[i] + 1) return false
        }
        return true
    }

    /**
     * Marriage: Tiplu + Poplu + Jhiplu of the Tiplu suit.
     */
    fun isMarriage(cards: List<Card>, tiplu: Card?): Boolean {
        if (tiplu == null || cards.size != 3) return false
        val hasTiplu = cards.any { it.getJokerRole(tiplu) == JokerRole.TIPLU }
        val hasPoplu = cards.any { it.getJokerRole(tiplu) == JokerRole.POPLU }
        val hasJhiplu = cards.any { it.getJokerRole(tiplu) == JokerRole.JHIPLU }
        return hasTiplu && hasPoplu && hasJhiplu
    }

    /**
     * Dirty Sequence: 3 or more cards in sequence of the same suit using wildcards.
     */
    fun isDirtySequence(cards: List<Card>, tiplu: Card?): Boolean {
        if (cards.size < 3) return false
        if (tiplu == null) return false

        val wildcards = cards.filter { it.isWildcard(tiplu) }
        val naturalCards = cards.filter { !it.isWildcard(tiplu) }

        if (naturalCards.isEmpty()) return true // All jokers can form anything
        val naturalSuit = naturalCards[0].suit
        if (!naturalCards.all { it.suit == naturalSuit }) return false

        // Check natural card ranks for duplicates
        val naturalRanks = naturalCards.map { it.rank.value }.sorted()
        if (naturalRanks.distinct().size != naturalRanks.size) return false // duplicate naturals cannot form straight sequence

        // Test normal sequence
        val span = naturalRanks.last() - naturalRanks.first() + 1
        val missingCount = span - naturalRanks.size
        if (missingCount <= wildcards.size && span <= cards.size) return true

        // Test Ace-low sequence
        if (naturalCards.any { it.rank == Rank.ACE }) {
            val aceLow = naturalCards.map { if (it.rank == Rank.ACE) 1 else it.rank.value }.sorted()
            val aceSpan = aceLow.last() - aceLow.first() + 1
            val aceMissing = aceSpan - aceLow.size
            if (aceMissing <= wildcards.size && aceSpan <= cards.size) return true
        }

        return false
    }

    /**
     * Dirty Set / Triplet: 3 or more cards of the same rank (or completed by wildcards).
     */
    fun isDirtySet(cards: List<Card>, tiplu: Card?): Boolean {
        if (cards.size < 3) return false
        if (tiplu == null) {
            // Unlocked phase or pure set test: must be same rank
            val firstRank = cards[0].rank
            return cards.all { it.rank == firstRank }
        }

        val wildcards = cards.filter { it.isWildcard(tiplu) }
        val naturalCards = cards.filter { !it.isWildcard(tiplu) }

        if (naturalCards.isEmpty()) return true
        val targetRank = naturalCards[0].rank
        return naturalCards.all { it.rank == targetRank }
    }

    /**
     * Evaluates a single meld group of cards.
     */
    fun evaluateMeld(cards: List<Card>, tiplu: Card?, isSeen: Boolean): Meld {
        val id = UUID.randomUUID().toString()
        if (cards.isEmpty()) {
            return Meld(id, cards, MeldType.INVALID, false, "Empty")
        }

        // Check Tunnella first
        if (isTunnella(cards)) {
            return Meld(id, cards, MeldType.TUNNELLA, true, "Tunnella (Pure)")
        }

        // Check Pure Sequence
        if (isPureSequence(cards)) {
            return Meld(id, cards, MeldType.PURE_SEQUENCE, true, "Pure Sequence")
        }

        // If player hasn't seen the Tiplu yet, only pure melds are accepted
        if (!isSeen || tiplu == null) {
            return Meld(id, cards, MeldType.INVALID, false, "Not a Pure Sequence or Tunnella")
        }

        // If player is seen, evaluate Joker melds
        if (isMarriage(cards, tiplu)) {
            return Meld(id, cards, MeldType.MARRIAGE, true, "★ Marriage (10 pts bonus!)")
        }

        if (isDirtySequence(cards, tiplu)) {
            return Meld(id, cards, MeldType.DIRTY_SEQUENCE, true, "Dirty Sequence")
        }

        if (isDirtySet(cards, tiplu)) {
            return Meld(id, cards, MeldType.DIRTY_SET, true, "Set / Triplet")
        }

        return Meld(id, cards, MeldType.INVALID, false, "Invalid Combination")
    }

    /**
     * Checks how many pure sets (Pure Sequences or Tunnellas) exist in a list of candidate melds.
     */
    fun countPureSets(melds: List<Meld>): Int {
        return melds.count { it.isValid && (it.type == MeldType.TUNNELLA || it.type == MeldType.PURE_SEQUENCE) }
    }

    /**
     * Check if a player can "Seen" (unlock Tiplu).
     * Rule: Requires at least 3 Pure Sets (3 Tunnellas, 3 Pure Sequences, or mixed).
     */
    fun canPlayerSeeTiplu(melds: List<Meld>): Boolean {
        return countPureSets(melds) >= 3
    }

    /**
     * Validates if a player has 7 valid melds totaling 21 cards for declaring a win.
     */
    fun canDeclareWin(melds: List<Meld>, tiplu: Card?, isSeen: Boolean): Boolean {
        if (!isSeen || tiplu == null) return false
        val validMelds = melds.filter { it.isValid }
        val totalCardsInMelds = validMelds.sumOf { it.cards.size }
        val pureCount = countPureSets(validMelds)

        return validMelds.size >= 7 && totalCardsInMelds >= 21 && pureCount >= 3
    }

    /**
     * Calculates Mal (points value of jokers, tunnellas, marriages) held in hand.
     */
    fun calculatePlayerMal(player: Player, tiplu: Card?): Int {
        if (tiplu == null || !player.isSeen) return 0

        var totalMal = 0
        val hand = player.hand

        // 1. Tunnellas (5 pts each, Joker Tunnella = 15 pts)
        val groupedByIdentical = hand.groupBy { "${it.suit.name}_${it.rank.name}" }
        for ((_, cardList) in groupedByIdentical) {
            val tunnellaSets = cardList.size / 3
            if (tunnellaSets > 0) {
                val sampleCard = cardList.first()
                val isJokerTunnella = sampleCard.isWildcard(tiplu)
                val pointsPerTunnella = if (isJokerTunnella) 15 else 5
                totalMal += (tunnellaSets * pointsPerTunnella)
            }
        }

        // 2. Marriage Bonus (10 pts per set of Tiplu + Poplu + Jhiplu)
        val tipluSuit = tiplu.suit
        val tiplusInHand = hand.count { it.getJokerRole(tiplu) == JokerRole.TIPLU }
        val poplusInHand = hand.count { it.getJokerRole(tiplu) == JokerRole.POPLU }
        val jhiplusInHand = hand.count { it.getJokerRole(tiplu) == JokerRole.JHIPLU }
        val marriagesCount = minOf(tiplusInHand, poplusInHand, jhiplusInHand)
        totalMal += (marriagesCount * 10)

        // 3. Individual Jokers
        // Tiplu: 3 pts each
        totalMal += tiplusInHand * 3
        // Poplu: 2 pts each
        totalMal += poplusInHand * 2
        // Jhiplu: 2 pts each
        totalMal += jhiplusInHand * 2
        // Alter Jokers (same rank, other suits): 5 pts each
        val altersInHand = hand.count { it.getJokerRole(tiplu) == JokerRole.ALTER }
        totalMal += altersInHand * 5

        return totalMal
    }

    /**
     * Calculate round settlement for all players.
     */
    fun calculateRoundSettlement(
        winnerId: Int,
        players: List<Player>,
        tiplu: Card?
    ): List<ScoreBreakdown> {
        val winnerBaseGamePoints = 10 // Base points awarded to game winner from each player
        val unseenPenalty = 20        // Penalty for not even reaching "Seen"

        val updatedPlayersWithMal = players.map { p ->
            val mal = calculatePlayerMal(p, tiplu)
            p.copy(malPoints = mal)
        }

        val breakdowns = mutableListOf<ScoreBreakdown>()

        for (player in updatedPlayersWithMal) {
            val isWinner = player.id == winnerId
            val isSeen = player.isSeen

            val tiplus = if (isSeen && tiplu != null) player.hand.count { it.getJokerRole(tiplu) == JokerRole.TIPLU } else 0
            val poplus = if (isSeen && tiplu != null) player.hand.count { it.getJokerRole(tiplu) == JokerRole.POPLU } else 0
            val jhiplus = if (isSeen && tiplu != null) player.hand.count { it.getJokerRole(tiplu) == JokerRole.JHIPLU } else 0
            val alters = if (isSeen && tiplu != null) player.hand.count { it.getJokerRole(tiplu) == JokerRole.ALTER } else 0
            val marriages = if (isSeen && tiplu != null) minOf(tiplus, poplus, jhiplus) else 0
            val tunnellas = player.hand.groupBy { "${it.suit.name}_${it.rank.name}" }.count { it.value.size >= 3 }

            var netScore = 0
            var penalty = 0

            if (isWinner) {
                // Winner receives base points from all other players
                val otherPlayersCount = players.size - 1
                netScore += (otherPlayersCount * winnerBaseGamePoints)

                // Winner collects / pays Mal difference with each other player
                for (other in updatedPlayersWithMal) {
                    if (other.id != winnerId) {
                        if (!other.isSeen) {
                            // Unseen player pays their penalty + winner's full Mal!
                            netScore += unseenPenalty
                            netScore += player.malPoints
                        } else {
                            // Seen player pays the mal difference
                            val malDiff = player.malPoints - other.malPoints
                            netScore += malDiff
                        }
                    }
                }
            } else {
                // Losing player
                if (!isSeen) {
                    // Unseen player pays base winner points + unseen penalty + winner's full Mal
                    val winner = updatedPlayersWithMal.find { it.id == winnerId }
                    val winnerMal = winner?.malPoints ?: 0
                    penalty = unseenPenalty + winnerBaseGamePoints
                    netScore = -(penalty + winnerMal)
                } else {
                    // Seen player pays winner base points + mal difference with winner
                    val winner = updatedPlayersWithMal.find { it.id == winnerId }
                    val winnerMal = winner?.malPoints ?: 0
                    val malDiff = winnerMal - player.malPoints
                    netScore = -(winnerBaseGamePoints + malDiff)
                }
            }

            breakdowns.add(
                ScoreBreakdown(
                    playerId = player.id,
                    playerName = player.name,
                    isWinner = isWinner,
                    isSeen = isSeen,
                    tunnellasCount = tunnellas,
                    marriagesCount = marriages,
                    tipluCount = tiplus,
                    popluCount = poplus,
                    jhipluCount = jhiplus,
                    alterCount = alters,
                    malPoints = player.malPoints,
                    penaltyPoints = penalty,
                    netRoundScore = netScore,
                    cumulativeScore = player.totalScore + netScore
                )
            )
        }

        return breakdowns
    }
}
