package strategy.util;

import java.util.List;

import hydra.model.BotItem;

public interface BankRecorder {

	void putItem(BotItem item);

	BotItem remove(BotItem botItem);

	List<BotItem> viewItems();
	
	BotItem getItem(String code);
}