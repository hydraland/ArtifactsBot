package hydra.dao;

import hydra.model.BotInventoryItem;
import hydra.model.BotItemReader;

public interface GrandExchangeDAO {

	boolean sell(BotInventoryItem sellItem, int price);
	
	boolean sell(BotItemReader sellItem, int price);

	boolean isSellPossible();

	int estimateItemPrice(String code, int characterGold);

}