package hydra.dao;

import hydra.model.BotInventoryItem;
import hydra.model.BotItem;

public interface GrandExchangeDAO {

	boolean sell(BotInventoryItem sellItem, int price);
	
	boolean sell(BotItem sellItem, int price);

	boolean isSellPossible();

	int estimateItemPrice(String code, int characterGold);

}