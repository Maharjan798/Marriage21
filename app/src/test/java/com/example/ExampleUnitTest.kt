package com.example

import com.example.engine.MarriageEngine
import com.example.model.Card
import com.example.model.GameMode
import com.example.model.JokerRole
import com.example.model.Player
import com.example.model.Rank
import com.example.model.Suit
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testDeckGeneration() {
    val deck = MarriageEngine.createFullDeck()
    assertEquals(156, deck.size)
  }

  @Test
  fun test5PlayerDealCardCounts() {
    val fullDeck = MarriageEngine.createFullDeck().toMutableList()
    val cutTiplu = fullDeck.removeAt(fullDeck.size - 1)
    val players = (0..4).map { id ->
      val hand = (1..21).map { fullDeck.removeAt(fullDeck.size - 1) }
      Player(id = id, name = "Player $id", avatarEmoji = "👑", isHuman = id == 0, hand = hand)
    }
    val firstDiscard = fullDeck.removeAt(fullDeck.size - 1)

    assertEquals(5, players.size)
    players.forEach { assertEquals(21, it.hand.size) }
    assertNotNull(cutTiplu)
    assertNotNull(firstDiscard)
    assertEquals(49, fullDeck.size) // 156 - (5 * 21) - 1 - 1 = 49
  }

  @Test
  fun test5PlayerSettlement() {
    val tiplu = Card("T", 0, Suit.SPADES, Rank.EIGHT)
    val p0 = Player(0, "You", "👑", true, isSeen = true, malPoints = 15)
    val p1 = Player(1, "Aarav", "🦁", false, isSeen = true, malPoints = 5)
    val p2 = Player(2, "Priya", "🦊", false, isSeen = false, malPoints = 0)
    val p3 = Player(3, "Rohan", "🐯", false, isSeen = false, malPoints = 0)
    val p4 = Player(4, "Maya", "🦚", false, isSeen = true, malPoints = 10)

    val breakdowns = MarriageEngine.calculateRoundSettlement(0, listOf(p0, p1, p2, p3, p4), tiplu)
    assertEquals(5, breakdowns.size)
    val winnerBreakdown = breakdowns.find { it.isWinner }
    assertNotNull(winnerBreakdown)
    assertTrue(winnerBreakdown!!.netRoundScore > 0)
  }

  @Test
  fun testTunnellaDetection() {
    val tunnellaCards = listOf(
      Card("1", 0, Suit.HEARTS, Rank.SEVEN),
      Card("2", 1, Suit.HEARTS, Rank.SEVEN),
      Card("3", 2, Suit.HEARTS, Rank.SEVEN)
    )
    assertTrue(MarriageEngine.isTunnella(tunnellaCards))

    val notTunnella = listOf(
      Card("1", 0, Suit.HEARTS, Rank.SEVEN),
      Card("2", 1, Suit.SPADES, Rank.SEVEN),
      Card("3", 2, Suit.HEARTS, Rank.SEVEN)
    )
    assertFalse(MarriageEngine.isTunnella(notTunnella))
  }

  @Test
  fun testPureSequenceDetection() {
    val pureSeq = listOf(
      Card("1", 0, Suit.SPADES, Rank.FOUR),
      Card("2", 0, Suit.SPADES, Rank.FIVE),
      Card("3", 0, Suit.SPADES, Rank.SIX)
    )
    assertTrue(MarriageEngine.isPureSequence(pureSeq))

    val aceLowSeq = listOf(
      Card("1", 0, Suit.DIAMONDS, Rank.ACE),
      Card("2", 0, Suit.DIAMONDS, Rank.TWO),
      Card("3", 0, Suit.DIAMONDS, Rank.THREE)
    )
    assertTrue(MarriageEngine.isPureSequence(aceLowSeq))
  }

  @Test
  fun testJokerRolesAndMarriage() {
    val tiplu = Card("T", 0, Suit.SPADES, Rank.EIGHT)
    val poplu = Card("P", 0, Suit.SPADES, Rank.NINE)
    val jhiplu = Card("J", 0, Suit.SPADES, Rank.SEVEN)
    val alter = Card("A", 0, Suit.HEARTS, Rank.EIGHT)

    assertEquals(JokerRole.TIPLU, tiplu.getJokerRole(tiplu))
    assertEquals(JokerRole.POPLU, poplu.getJokerRole(tiplu))
    assertEquals(JokerRole.JHIPLU, jhiplu.getJokerRole(tiplu))
    assertEquals(JokerRole.ALTER, alter.getJokerRole(tiplu))

    val marriageCards = listOf(jhiplu, tiplu, poplu)
    assertTrue(MarriageEngine.isMarriage(marriageCards, tiplu))
  }

  @Test
  fun testCardFlyEventModel() {
    val card = Card("C1", 0, Suit.HEARTS, Rank.TEN)
    val flyStock = com.example.model.CardFlyEvent(
      card = card,
      source = com.example.model.DrawSource.STOCK,
      targetPlayerIndex = 0,
      isFaceUp = true
    )
    assertEquals(com.example.model.DrawSource.STOCK, flyStock.source)
    assertEquals(0, flyStock.targetPlayerIndex)
    assertTrue(flyStock.isFaceUp)
    assertEquals("10♥", flyStock.card?.shortName)

    val flyDiscard = com.example.model.CardFlyEvent(
      card = card,
      source = com.example.model.DrawSource.DISCARD,
      targetPlayerIndex = 2,
      isFaceUp = true
    )
    assertEquals(com.example.model.DrawSource.DISCARD, flyDiscard.source)
    assertEquals(2, flyDiscard.targetPlayerIndex)
  }
}
