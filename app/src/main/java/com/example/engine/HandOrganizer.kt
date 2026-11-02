package com.example.engine

import com.example.model.Card
import com.example.model.Meld
import com.example.model.MeldType
import com.example.model.Rank
import com.example.model.Suit
import java.util.UUID

object HandOrganizer {

    fun sortBySuit(cards: List<Card>): List<Card> {
        return cards.sortedWith(
            compareBy<Card> { it.suit.ordinal }
                .thenBy { it.rank.value }
                .thenBy { it.deckIndex }
        )
    }

    fun sortByRank(cards: List<Card>): List<Card> {
        return cards.sortedWith(
            compareBy<Card> { it.rank.value }
                .thenBy { it.suit.ordinal }
                .thenBy { it.deckIndex }
        )
    }

    /**
     * Intelligent hand grouping into melds and unmelded cards.
     */
    fun autoGroupHand(cards: List<Card>, tiplu: Card?, isSeen: Boolean): Pair<List<Meld>, List<Card>> {
        val remainingCards = cards.toMutableList()
        val formedMelds = mutableListOf<Meld>()

        // 1. Detect Tunnellas (3 identical cards: same rank, same suit)
        val identicalGroups = remainingCards.groupBy { "${it.suit.name}_${it.rank.name}" }
        for ((_, cardList) in identicalGroups) {
            if (cardList.size >= 3) {
                val tunnellaCards = cardList.take(3)
                formedMelds.add(
                    Meld(
                        id = UUID.randomUUID().toString(),
                        cards = tunnellaCards,
                        type = MeldType.TUNNELLA,
                        isValid = true,
                        description = "Tunnella (${tunnellaCards.first().shortName})"
                    )
                )
                tunnellaCards.forEach { remainingCards.remove(it) }
            }
        }

        // 2. Detect Pure Sequences (3+ consecutive cards of same suit)
        val bySuit = remainingCards.groupBy { it.suit }
        for ((suit, suitCards) in bySuit) {
            val distinctRankCards = suitCards.distinctBy { it.rank }.sortedBy { it.rank.value }.toMutableList()
            
            // Check for sequences of length 3+
            var i = 0
            while (i < distinctRankCards.size) {
                val currentSeq = mutableListOf(distinctRankCards[i])
                var j = i + 1
                while (j < distinctRankCards.size && distinctRankCards[j].rank.value == currentSeq.last().rank.value + 1) {
                    currentSeq.add(distinctRankCards[j])
                    j++
                }

                if (currentSeq.size >= 3) {
                    val seqTake = currentSeq.take(3)
                    formedMelds.add(
                        Meld(
                            id = UUID.randomUUID().toString(),
                            cards = seqTake,
                            type = MeldType.PURE_SEQUENCE,
                            isValid = true,
                            description = "Pure Sequence (${seqTake.first().rank.display}-${seqTake.last().rank.display}${suit.symbol})"
                        )
                    )
                    seqTake.forEach { remainingCards.remove(it) }
                    i = j
                } else {
                    i++
                }
            }

            // Check Ace-low (A-2-3) if Ace wasn't used
            val hasAce = remainingCards.find { it.suit == suit && it.rank == Rank.ACE }
            val hasTwo = remainingCards.find { it.suit == suit && it.rank == Rank.TWO }
            val hasThree = remainingCards.find { it.suit == suit && it.rank == Rank.THREE }
            if (hasAce != null && hasTwo != null && hasThree != null) {
                val aceLowSeq = listOf(hasAce, hasTwo, hasThree)
                formedMelds.add(
                    Meld(
                        id = UUID.randomUUID().toString(),
                        cards = aceLowSeq,
                        type = MeldType.PURE_SEQUENCE,
                        isValid = true,
                        description = "Pure Sequence (A-2-3${suit.symbol})"
                    )
                )
                aceLowSeq.forEach { remainingCards.remove(it) }
            }
        }

        // 3. If Player is Unseen, we stop grouping pure melds here
        if (!isSeen || tiplu == null) {
            val sortedUnmelded = sortBySuit(remainingCards)
            return Pair(formedMelds, sortedUnmelded)
        }

        // 4. If Player is Seen: Check Marriage combination (Tiplu + Poplu + Jhiplu)
        val tipluCard = remainingCards.find { it.getJokerRole(tiplu) == com.example.model.JokerRole.TIPLU }
        val popluCard = remainingCards.find { it.getJokerRole(tiplu) == com.example.model.JokerRole.POPLU }
        val jhipluCard = remainingCards.find { it.getJokerRole(tiplu) == com.example.model.JokerRole.JHIPLU }

        if (tipluCard != null && popluCard != null && jhipluCard != null) {
            val marriageCards = listOf(jhipluCard, tipluCard, popluCard)
            formedMelds.add(
                Meld(
                    id = UUID.randomUUID().toString(),
                    cards = marriageCards,
                    type = MeldType.MARRIAGE,
                    isValid = true,
                    description = "★ Marriage Bonus (${tiplu.shortName})"
                )
            )
            marriageCards.forEach { remainingCards.remove(it) }
        }

        // 5. Build Sets / Triplets with remaining cards
        val byRank = remainingCards.filter { !it.isWildcard(tiplu) }.groupBy { it.rank }
        for ((rank, rankCards) in byRank) {
            if (rankCards.size >= 3) {
                val setCards = rankCards.take(3)
                formedMelds.add(
                    Meld(
                        id = UUID.randomUUID().toString(),
                        cards = setCards,
                        type = MeldType.DIRTY_SET,
                        isValid = true,
                        description = "Triplet (${rank.display})"
                    )
                )
                setCards.forEach { remainingCards.remove(it) }
            }
        }

        // 6. Build Dirty Sequences / Sets using available wildcards
        val availableWildcards = remainingCards.filter { it.isWildcard(tiplu) }.toMutableList()
        val naturalCards = remainingCards.filter { !it.isWildcard(tiplu) }.toMutableList()

        // Try pairing pairs with 1 wildcard to form dirty sets
        val remainingByRank = naturalCards.groupBy { it.rank }
        for ((rank, rankCards) in remainingByRank) {
            if (rankCards.size == 2 && availableWildcards.isNotEmpty()) {
                val wildcard = availableWildcards.removeAt(0)
                val dirtySetCards = listOf(rankCards[0], rankCards[1], wildcard)
                formedMelds.add(
                    Meld(
                        id = UUID.randomUUID().toString(),
                        cards = dirtySetCards,
                        type = MeldType.DIRTY_SET,
                        isValid = true,
                        description = "Set with Joker (${rank.display})"
                    )
                )
                rankCards.forEach { naturalCards.remove(it) }
                remainingCards.remove(rankCards[0])
                remainingCards.remove(rankCards[1])
                remainingCards.remove(wildcard)
            }
        }

        // Leftover wildcards can form pure joker melds if 3 wildcards exist
        while (availableWildcards.size >= 3) {
            val jokerTrio = listOf(availableWildcards.removeAt(0), availableWildcards.removeAt(0), availableWildcards.removeAt(0))
            formedMelds.add(
                Meld(
                    id = UUID.randomUUID().toString(),
                    cards = jokerTrio,
                    type = MeldType.DIRTY_SET,
                    isValid = true,
                    description = "Joker Trio"
                )
            )
            jokerTrio.forEach { remainingCards.remove(it) }
        }

        val sortedUnmelded = sortBySuit(remainingCards)
        return Pair(formedMelds, sortedUnmelded)
    }
}
