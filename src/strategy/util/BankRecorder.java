package strategy.util;

import java.util.List;

import hydra.model.BotItem;
import hydra.model.BotItemReader;

public interface BankRecorder {

	void putItem(BotItem item);

	BotItemReader remove(BotItemReader botItem);

	List<? extends BotItemReader> viewItems();
	
	BotItemReader getItem(String code);
}