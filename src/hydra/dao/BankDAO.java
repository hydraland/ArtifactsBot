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

	default boolean withdraw(BotItemReader item) {
		return withdraw(item.getCode(), item.getQuantity());
	}

	boolean withdraw(String code, int quantity);

	public BotBankDetail getBankDetail();

	boolean buyExtension();

	boolean depositGold(int quantity);

	boolean withdrawGold(int quantity);

	List<? extends BotItemReader> viewItems();

	BotItemReader getItem(String code);
}