package strategy.achiever.factory.custom;

import hydra.model.BotInventoryItem;

interface CustomCondition {
	boolean accept(BotInventoryItem botInventoryItem);
}
