package strategy.util;

import java.util.Objects;

import hydra.GameConstants;
import hydra.model.BotItemDetails;
import hydra.model.BotItemType;

//Ne pas utiliser en clé de Map.
public final record BotItemInfo(BotItemDetails botItemDetails, int quantity, ItemOrigin origin) {

	@Override
	public final int hashCode() {
		// Sert à l'optimisation
		return Objects.hash(botItemDetails.getCode(), getQuantityTakingIntoAccount(quantity, botItemDetails.getType()));
	}

	private int getQuantityTakingIntoAccount(int initialQuantity, BotItemType type) {
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