package strategy.util.fight;

import java.util.Set;

import hydra.model.BotMonster;
import strategy.util.BotItemInfo;

public interface CombinatoricsReducer {
	void reduceCombinatorics(Set<BotItemInfo> weapons, Set<BotItemInfo> bodyArmors, Set<BotItemInfo> boots,
			Set<BotItemInfo> helmets, Set<BotItemInfo> shields, Set<BotItemInfo> legArmors, Set<BotItemInfo> amulets,
			Set<BotItemInfo> rings, Set<BotItemInfo> artifacts, Set<BotItemInfo> utilities, BotMonster monster,
			int characterHpWithoutEqt);
}
