package strategy.util.fight;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import hydra.GameConstants;
import hydra.model.BotItemType;
import strategy.util.BotItemInfo;

public interface FightServiceUtils {

	static String createKey(Integer characterHp, String code,
			Map<BotItemType, List<BotItemInfo>> equipableCharacterEquipement) {
		StringBuilder builder = new StringBuilder();
		builder.append(characterHp);
		builder.append(code);
		equipableCharacterEquipement.entrySet().stream()
				.forEach(entry -> builder.append(entry.getKey()).append(hash(entry.getValue())));
		return builder.toString();
	}

	static int hash(List<BotItemInfo> botItemInfos) {
		return botItemInfos.stream().map(bii -> hash(bii)).reduce(0, (a, b) -> a + b);
	}

	static int hash(BotItemInfo botItemInfo) {
		return Objects.hash(botItemInfo.botItemDetails().getCode(),
				getQuantityTakingIntoAccount(botItemInfo.quantity(), botItemInfo.botItemDetails().getType()));
	}

	static int getQuantityTakingIntoAccount(int initialQuantity, BotItemType type) {
		switch (type) {
		case RING:
			return initialQuantity > 2 ? 2 : initialQuantity;
		case UTILITY:
			return initialQuantity > GameConstants.MAX_ITEM_IN_SLOT ? GameConstants.MAX_ITEM_IN_SLOT : initialQuantity;
		default:
			return 1;
		}
	}

}
