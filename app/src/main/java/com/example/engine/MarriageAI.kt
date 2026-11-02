package com.example.engine

import com.example.model.Card
import com.example.model.DrawSource
import com.example.model.MeldType
import com.example.model.Player
import com.example.model.Rank

object MarriageAI {

    /**
     * Determines whether the AI should draw from the Discard pile or Stock pile.
     */
    fun decideDrawSource(
        player: Player,
        topDiscardCard: Card?,
        tiplu: Card?
    ): DrawSource {
        if (topDiscardCard == null) return DrawSource.STOCK

        val simulatedHand = player.hand + topDiscardCard
        val (currentMelds, _) = HandOrganizer.autoGroupHand(player.hand, tiplu, player.isSeen)
        val (simulatedMelds, _) = HandOrganizer.autoGroupHand(simulatedHand, tiplu, player.isSeen)

        // 1. If discard card completes a pure set that enables "Seen" unlock
        val currentPure = currentMelds.count { it.type == MeldType.TUNNELLA || it.type == MeldType.PURE_SEQUENCE }
        val simPure = simulatedMelds.count { it.type == MeldType.TUNNELLA || it.type == MeldType.PURE_SEQUENCE }
        if (!player.isSeen && simPure > currentPure) {
            return DrawSource.DISCARD
        }

        // 2. If Seen: check if discard card creates a new valid meld (e.g. Marriage or Set)
        if (player.isSeen && simulatedMelds.size > currentMelds.size) {
            return DrawSource.DISCARD
        }

        // 3. If discard card is a valuable Joker and AI is Seen
        if (player.isSeen && topDiscardCard.isWildcard(tiplu)) {
            return DrawSource.DISCARD
        }

        return DrawSource.STOCK
    }

    /**
     * Determines whether the AI has 3 pure sets and should Unlock / Seen.
     */
    fun shouldUnlockSeen(player: Player, tiplu: Card?): Boolean {
        if (player.isSeen) return false
        val (melds, _) = HandOrganizer.autoGroupHand(player.hand, tiplu, false)
        return MarriageEngine.canPlayerSeeTiplu(melds)
    }

    /**
     * Determines which card to discard from a 22-card hand.
     */
    fun chooseCardToDiscard(player: Player, tiplu: Card?): Card {
        val hand = player.hand
        if (hand.isEmpty()) return hand.first()

        val (melds, unmelded) = HandOrganizer.autoGroupHand(hand, tiplu, player.isSeen)

        // If we have unmelded cards, pick the worst candidate among them
        val candidates = if (unmelded.isNotEmpty()) unmelded else hand

        // Score each card: higher score = more isolated/useless -> better to discard
        val scoredCards = candidates.map { card ->
            var discardScore = 0

            // If card is a joker and player is seen, never discard unless forced
            if (player.isSeen && card.isWildcard(tiplu)) {
                discardScore -= 100
            }

            // Check if card has identical duplicates in hand (potential tunnella)
            val identicalCount = hand.count { it.suit == card.suit && it.rank == card.rank }
            if (identicalCount == 2) {
                discardScore -= 30 // Keep pair
            } else if (identicalCount >= 3) {
                discardScore -= 80 // Tunnella, protect!
            }

            // Check if card has adjacent ranks in same suit (potential sequence)
            val sameSuitCards = hand.filter { it.suit == card.suit && it != card }
            val hasAdjacent = sameSuitCards.any {
                Math.abs(it.rank.value - card.rank.value) == 1 ||
                Math.abs(it.rank.value - card.rank.value) == 2
            }
            if (hasAdjacent) {
                discardScore -= 20
            }

            // Check if card has same rank in other suits (potential triplet)
            val sameRankCount = hand.count { it.rank == card.rank && it != card }
            if (sameRankCount >= 2) {
                discardScore -= 15
            }

            // High card penalty (discarding high rank singletons is generally safer)
            discardScore += (card.rank.value / 2)

            Pair(card, discardScore)
        }

        // Return candidate with highest discard score
        return scoredCards.maxByOrNull { it.second }?.first ?: candidates.first()
    }

    /**
     * Checks if AI can declare a win.
     */
    fun canDeclareWin(player: Player, tiplu: Card?): Boolean {
        if (!player.isSeen || tiplu == null) return false
        val (melds, unmelded) = HandOrganizer.autoGroupHand(player.hand, tiplu, true)
        val validMelds = melds.filter { it.isValid }
        val totalCards = validMelds.sumOf { it.cards.size }
        val pureCount = MarriageEngine.countPureSets(validMelds)

        // For winning 21 cards must form 7 melds, with 1 discard left over (or 21 cards melded)
        return validMelds.size >= 7 && totalCards >= 21 && pureCount >= 3 && unmelded.size <= 1
    }
}
