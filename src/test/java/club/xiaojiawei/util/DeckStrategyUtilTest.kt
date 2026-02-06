package club.xiaojiawei.util

import club.xiaojiawei.hsscriptbasestrategy.util.DeckStrategyUtil
import club.xiaojiawei.hsscriptcardsdk.bean.Card
import club.xiaojiawei.hsscriptcardsdk.bean.TEST_CARD_ACTION
import club.xiaojiawei.hsscriptcardsdk.enums.CardActionEnum
import club.xiaojiawei.hsscriptcardsdk.enums.CardTypeEnum
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * @author 肖嘉威
 * @date 2025/6/10 17:07
 */
class DeckStrategyUtilTest {

    private fun buildPoisonousMinio(entityId: String): Card {
        return Card(TEST_CARD_ACTION).apply {
            cardType = CardTypeEnum.MINION
            this.entityId = entityId
            isPoisonous = true
            atc = 2
            health = 4
            isTaunt = true
            isDeathRattle = true
        }
    }

    @Test
    @Ignore
    fun testCleanPlay() {
        DeckStrategyUtil.execAction = false
        val myCards = mutableListOf<Card>(
            Card(TEST_CARD_ACTION).apply {
                cardType = CardTypeEnum.MINION
                entityId = "m3"
                atc = 3
                health = 5
                isTaunt = true
            },
            Card(TEST_CARD_ACTION).apply {
                cardType = CardTypeEnum.MINION
                entityId = "m4"
                atc = 2
                health = 10
            },
        )
        val rivalCards = mutableListOf<Card>(
            Card(TEST_CARD_ACTION).apply {
                cardType = CardTypeEnum.HERO
                entityId = "rh1"
                atc = 0
                health = 28
            },
        )
        DeckStrategyUtil.cleanPlay(myPlayCards = myCards, rivalPlayCards = rivalCards)
        DeckStrategyUtil.execAction = true
    }



    /**
     * 测试剧毒随从的解场倾向
     */
    @Test
    @Ignore("这个测试无法验证结果")
    fun testPoisonousCleanPlay() {
        DeckStrategyUtil.execAction = false
        val myCards = mutableListOf<Card>(
            buildPoisonousMinio("m1"),
            buildPoisonousMinio("m2"),
            Card(TEST_CARD_ACTION).apply {
                cardType = CardTypeEnum.MINION
                entityId = "m3"
                atc = 3
                health = 5
                isTaunt = true
            },
            Card(TEST_CARD_ACTION).apply {
                cardType = CardTypeEnum.MINION
                entityId = "m4"
                atc = 0
                health = 2
            },
            Card(TEST_CARD_ACTION).apply {
                cardType = CardTypeEnum.HERO
                entityId = "mh1"
                atc = 0
                health = 30
            },
        )
        val rivalCards = mutableListOf<Card>(
            Card(TEST_CARD_ACTION).apply {
                cardType = CardTypeEnum.MINION
                entityId = "r1"
                atc = 8
                health = 8
            },
            Card(TEST_CARD_ACTION).apply {
                cardType = CardTypeEnum.MINION
                entityId = "r2"
                atc = 4
                health = 3
            },
            Card(TEST_CARD_ACTION).apply {
                cardType = CardTypeEnum.MINION
                entityId = "r3"
                atc = 5
                health = 4
            },
            Card(TEST_CARD_ACTION).apply {
                cardType = CardTypeEnum.MINION
                entityId = "r3"
                atc = 2
                health = 2
            },
            Card(TEST_CARD_ACTION).apply {
                cardType = CardTypeEnum.MINION
                entityId = "r4"
                atc = 2
                health = 2
                isDeathRattle = true
            },
            Card(TEST_CARD_ACTION).apply {
                cardType = CardTypeEnum.MINION
                entityId = "r5"
                atc = 1
                health = 1
            },
            Card(TEST_CARD_ACTION).apply {
                cardType = CardTypeEnum.HERO
                entityId = "rh1"
                atc = 0
                health = 28
            },
        )
        DeckStrategyUtil.cleanPlay(myPlayCards = myCards, rivalPlayCards = rivalCards)
        DeckStrategyUtil.execAction = true
    }

    @Test
    fun testParse() {
//        assertTrue("【奉献】卡牌描述解析出错") { DeckStrategyUtil.parseCard("CORE_CS2_093") == listOf(CardActionEnum.NO_POINT) }
//        assertTrue("【火球术】卡牌描述解析出错") { DeckStrategyUtil.parseCard("CORE_CS2_029") == listOf(CardActionEnum.POINT_RIVAL) }
//        assertTrue("【海盗火炮2】卡牌描述解析出错") { DeckStrategyUtil.parseCard("LETL_813_01") == listOf(CardActionEnum.POINT_RIVAL) }
//        assertTrue("【安戈洛宣传单】卡牌描述解析出错") { DeckStrategyUtil.parseCard("WORK_050") == listOf(CardActionEnum.NO_POINT) }
//        assertTrue("【强光护卫】卡牌描述解析出错") { DeckStrategyUtil.parseCard("TIME_015") == listOf(CardActionEnum.POINT_MY_HERO) }
//        assertTrue("【工匠光环】卡牌描述解析出错") { DeckStrategyUtil.parseCard("TOY_808") == listOf(CardActionEnum.NO_POINT) }
//        assertTrue("【嘉沃顿的故事】卡牌描述解析出错") { DeckStrategyUtil.parseCard("TLC_444") == listOf(CardActionEnum.POINT_MY_MINION) }
//        assertTrue("【淹没的地图】卡牌描述解析出错") { DeckStrategyUtil.parseCard("TLC_442") == listOf(CardActionEnum.NO_POINT) }
        assertTrue("【污手街供货商】卡牌描述解析出错") { DeckStrategyUtil.parseCard("CORE_CFM_753") == listOf(CardActionEnum.NO_POINT) }
    }

}