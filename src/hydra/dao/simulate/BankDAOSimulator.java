package hydra.dao.simulate;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Collections;
import java.util.List;

import hydra.dao.BankDAO;
import hydra.model.BotBankDetail;
import hydra.model.BotInventoryItem;
import hydra.model.BotItem;
import hydra.model.BotItemReader;

public final class BankDAOSimulator implements BankDAO, Simulator<BankStruct> {

	private static final String CLASS_NAME = "BankDAOSimulator";
	private static final int COOLDOWN = 3;
	private final SimulatorListener simulatorListener;
	private BankStruct bankStruct;
	private final CharacterDAOSimulator characterDAOSimulator;
	private final ByteArrayOutputStream memoryStream;

	public BankDAOSimulator(SimulatorListener simulatorListener, CharacterDAOSimulator characterDAOSimulator) {
		this.simulatorListener = simulatorListener;
		this.characterDAOSimulator = characterDAOSimulator;
		memoryStream = new ByteArrayOutputStream();
	}

	@Override
	public boolean deposit(BotInventoryItem item) {
		int maxSlots = bankStruct.getBankDetail().getSlots();
		int fillSlot = bankStruct.getStock().size() + (bankStruct.getStock().containsKey(item.getCode()) ? 0 : 1);
		if (fillSlot <= maxSlots
				&& characterDAOSimulator.checkWithdrawInInventory(item.getCode(), item.getQuantity())) {
			simulatorListener.call(CLASS_NAME, "deposit", COOLDOWN, false);
			bankStruct.getStock().merge(item.getCode(), item.getQuantity(),
					(current, newVal) -> current != null ? current + newVal : newVal);
			characterDAOSimulator.withdrawInInventory(item.getCode(), item.getQuantity());
			return true;
		}
		simulatorListener.call(CLASS_NAME, "deposit", 0, true);
		return false;
	}

	@Override
	public boolean withdraw(BotItemReader item) {
		if (bankStruct.getStock().getOrDefault(item.getCode(), 0) >= item.getQuantity()
				&& characterDAOSimulator.checkDepositInInventory(item.getCode(), item.getQuantity())) {
			simulatorListener.call(CLASS_NAME, "withdraw", COOLDOWN, false);
			characterDAOSimulator.depositInInventory(item.getCode(), item.getQuantity());
			return true;
		}
		simulatorListener.call(CLASS_NAME, "withdraw", 0, true);
		return false;
	}

	@Override
	public BotBankDetail getBankDetail() {
		return bankStruct.getBankDetail();
	}

	@Override
	public boolean buyExtension() {
		int gold = characterDAOSimulator.botCharacter.getGold();
		int nextExpansionCost = bankStruct.getBankDetail().getNextExpansionCost();
		if (gold >= nextExpansionCost) {
			simulatorListener.call(CLASS_NAME, "buyExtension", COOLDOWN, false);
			characterDAOSimulator.botCharacter.setGold(gold - nextExpansionCost);
			bankStruct.getBankDetail().setNextExpansionCost(nextExpansionCost * 2);
			return true;
		}
		simulatorListener.call(CLASS_NAME, "buyExtension", 0, true);
		return false;
	}

	@Override
	public boolean depositGold(int quantity) {
		if (characterDAOSimulator.botCharacter.getGold() < quantity) {
			simulatorListener.call(CLASS_NAME, "depositGold", 0, true);
			return false;
		}
		simulatorListener.call(CLASS_NAME, "depositGold", COOLDOWN, false);
		bankStruct.getBankDetail().setGold(bankStruct.getBankDetail().getGold() + quantity);
		characterDAOSimulator.botCharacter.setGold(characterDAOSimulator.botCharacter.getGold() - quantity);
		return true;
	}

	@Override
	public boolean withdrawGold(int quantity) {
		if (bankStruct.getBankDetail().getGold() < quantity) {
			simulatorListener.call(CLASS_NAME, "withdrawGold", 0, true);
			return false;
		}
		simulatorListener.call(CLASS_NAME, "withdrawGold", COOLDOWN, false);
		bankStruct.getBankDetail().setGold(bankStruct.getBankDetail().getGold() - quantity);
		characterDAOSimulator.botCharacter.setGold(characterDAOSimulator.botCharacter.getGold() + quantity);
		return true;
	}

	@Override
	public void load(boolean persistant) {
		bankStruct = Simulator.load(persistant, new File("BankDAOSimulator.xml"), memoryStream);
		if (bankStruct == null) {
			bankStruct = new BankStruct(new BotBankDetail(), Collections.emptyMap());
		}
	}

	@Override
	public void save(boolean persistant) {
		Simulator.save(persistant, new File("BankDAOSimulator.xml"), memoryStream, bankStruct);
	}

	@Override
	public void set(BankStruct value) {
		this.bankStruct = value;
	}

	@Override
	public List<? extends BotItemReader> viewItems() {
		return bankStruct.getStock().entrySet().stream().<BotItemReader>map(entry -> {
			BotItem botItem = new BotItem();
			botItem.setCode(entry.getKey());
			botItem.setQuantity(entry.getValue());
			return botItem;
		}).toList();
	}

	@Override
	public BotItemReader getItem(String code) {
		int quantity = bankStruct.getStock().getOrDefault(code, 0);
		BotItem item = new BotItem();
		item.setCode(code);
		item.setQuantity(quantity);
		return item;
	}
}
