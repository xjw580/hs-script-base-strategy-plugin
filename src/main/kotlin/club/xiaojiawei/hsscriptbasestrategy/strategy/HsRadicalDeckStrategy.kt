package club.xiaojiawei.hsscriptbasestrategy.strategy

import club.xiaojiawei.hsscriptstrategysdk.DeckStrategy
import club.xiaojiawei.hsscriptcardsdk.bean.Card
import club.xiaojiawei.hsscriptcardsdk.bean.isValid
import club.xiaojiawei.hsscriptbase.config.log
import club.xiaojiawei.hsscriptcardsdk.data.CARD_INFO_TRIE
import club.xiaojiawei.hsscriptcardsdk.enums.CardTypeEnum
import club.xiaojiawei.hsscriptbase.enums.RunModeEnum
import club.xiaojiawei.hsscriptcardsdk.status.WAR
import club.xiaojiawei.hsscriptbasestrategy.util.DeckStrategyUtil

/**
 * @author 肖嘉威
 * @date 2024/10/17 17:58
 */
class HsRadicalDeckStrategy : DeckStrategy() {
    private val commonDeckStrategy = HsCommonDeckStrategy()

    override fun name(): String = "激进策略"

    override fun description(): String = "会在基础策略的基础上使用战吼，法术，地标牌（依旧不识别战吼或法术）"

    override fun getRunMode(): Array<RunModeEnum> =
        arrayOf(RunModeEnum.CASUAL, RunModeEnum.STANDARD, RunModeEnum.WILD, RunModeEnum.PRACTICE)

    override fun deckCode(): String = ""

    override fun id(): String = "e71234fa-1-radical-deck-97e9-1f4e126cd33b"

    override fun referWeight(): Boolean = true

    override fun referPowerWeight(): Boolean = true

    override fun referChangeWeight(): Boolean = true

    override fun referCardInfo(): Boolean = true

    override fun executeChangeCard(cards: HashSet<Card>) {
        commonDeckStrategy.executeChangeCard(cards)
    }

    override fun executeOutCard() {
        val me = WAR.me
        if (me.isValid()) {
            val rival = WAR.rival

            DeckStrategyUtil.RadicalpowerCard(me, rival)
            DeckStrategyUtil.cleanPlay()

            DeckStrategyUtil.RadicalpowerCard(me, rival)
            DeckStrategyUtil.cleanPlay()

            DeckStrategyUtil.RadicalpowerCard(me, rival)
            DeckStrategyUtil.cleanPlay()

            DeckStrategyUtil.RadicalpowerCard(me, rival)

    //        使用技能
            me.playArea.power?.let { powerCard ->
                if (me.usableResource >= powerCard.cost || powerCard.cost == 0) {
                    CARD_INFO_TRIE[powerCard.cardId]?.let { cardInfo ->
                        cardInfo.powerActions.firstOrNull()?.powerExec(powerCard, cardInfo.effectType, WAR)
                    } ?: let {
                        powerCard.action.power()
                    }
                }
            }
            DeckStrategyUtil.cleanPlay()

            me.playArea.cards.toList().forEach { card: Card ->
                if (card.isLaunchpad && me.usableResource >= card.launchCost()) {
                    card.action.launch()
                }
            }
        }
    }

    override fun executeDiscoverChooseCard(vararg cards: Card): Int =
        commonDeckStrategy.executeDiscoverChooseCard(*cards)
}
