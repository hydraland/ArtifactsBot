package hydra.dao;

import java.util.List;

import hydra.model.BotBankDetail;
import hydra.model.BotInventoryItem;
import hydra.model.BotItemReader;

public interface BankDAO {

	default boolean deposit(BotInventoryItem item) {
		return deposit(item.getCode(), item.getQuantity());
	}
	
	boolean deposit(String code, int quantity);

	boolean withdraw(BotItemReader item);

	public BotBankDetail getBankDetail();

	boolean buyExtension();

	boolean depositGold(int quantity);

	boolean withdrawGold(int quantity);

	List<? extends BotItemReader> viewItems();

	BotItemReader getItem(String code);

}