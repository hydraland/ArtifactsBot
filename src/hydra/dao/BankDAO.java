package hydra.dao;

import java.util.List;

import hydra.model.BotBankDetail;
import hydra.model.BotInventoryItem;
import hydra.model.BotItemReader;

public interface BankDAO {

	boolean deposit(BotInventoryItem item);

	boolean withdraw(BotItemReader item);

	public BotBankDetail getBankDetail();

	boolean buyExtension();

	boolean depositGold(int quantity);

	boolean withdrawGold(int quantity);

	List<? extends BotItemReader> viewItems();

	BotItemReader getItem(String code);
}