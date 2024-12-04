package hydra.dao;

import java.util.List;

import hydra.model.BotBankDetail;
import hydra.model.BotInventoryItem;
import hydra.model.BotItem;

public interface BankDAO {

	boolean deposit(BotInventoryItem item);

	boolean withdraw(BotItem item);

	public BotBankDetail getBankDetail();

	boolean buyExtension();

	boolean depositGold(int quantity);

	boolean withdrawGold(int quantity);

	List<BotItem> viewItems();

	BotItem getItem(String code);
}