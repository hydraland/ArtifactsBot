package strategy.util;

import java.util.List;

import hydra.model.BotItem;
import hydra.model.BotItemReader;

public interface BankRecorder {

	void putItem(BotItem item);

	List<? extends BotItemReader> viewItems();
	
	BotItemReader getItem(String code);

	BotItemReader remove(String code, int quantity);
}