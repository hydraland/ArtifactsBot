package hydra.dao.simulate;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Collections;

import hydra.dao.GrandExchangeDAO;
import hydra.model.BotInventoryItem;
import hydra.model.BotItemReader;

public final class GrandExchangeDAOSimulator implements GrandExchangeDAO, Simulator<GrandExchangeStruct> {

	private static final String CLASS_NAME = "GrandExchangeDAOSimulator";
	private static final int COOLDOWN = 3;
	private static final int DEFAULT_PRICE = 100;
	private final SimulatorListener simulatorListener;
	private GrandExchangeStruct grandExchangeStruct;
	private final CharacterDAOSimulator characterDAOSimulator;
	private final ByteArrayOutputStream memoryStream;

	public GrandExchangeDAOSimulator(SimulatorListener simulatorListener, CharacterDAOSimulator characterDAOSimulator) {
		this.simulatorListener = simulatorListener;
		this.characterDAOSimulator = characterDAOSimulator;
		memoryStream = new ByteArrayOutputStream();
	}

	@Override
	public boolean sell(BotInventoryItem sellItem, int price) {
		return sell(sellItem.getCode(), sellItem.getQuantity());
	}

	@Override
	public boolean sell(BotItemReader sellItem, int price) {
		return sell(sellItem.getCode(), sellItem.getQuantity());
	}

	private boolean sell(String code, int quantity) {
		if (characterDAOSimulator.checkWithdrawInInventory(code, quantity)) {
			simulatorListener.call(CLASS_NAME, "sell", COOLDOWN, false);
			characterDAOSimulator.withdrawInInventory(code, quantity);
			return true;
		}
		simulatorListener.call(CLASS_NAME, "sell", 0, true);
		return false;
	}

	@Override
	public boolean isSellPossible() {
		simulatorListener.call(CLASS_NAME, "isSellPossible", 0, false);
		return grandExchangeStruct.isSellPossible();
	}

	@Override
	public int estimateItemPrice(String code, int characterGold) {
		simulatorListener.call(CLASS_NAME, "estimateItemPrice", 0, false);
		return grandExchangeStruct.getEstimateItemPrice().getOrDefault(code, DEFAULT_PRICE);
	}
	
	@Override
	public void load(boolean persistant) {
		grandExchangeStruct = Simulator.load(persistant, new File("GrandExchangeDAOSimulator.xml"), memoryStream);
		if (grandExchangeStruct == null) {
			grandExchangeStruct = new GrandExchangeStruct(false, Collections.emptyMap());
		}
	}

	@Override
	public void save(boolean persistant) {
		Simulator.save(persistant, new File("GrandExchangeDAOSimulator.xml"), memoryStream, grandExchangeStruct);
	}

	@Override
	public void set(GrandExchangeStruct value) {
		this.grandExchangeStruct = value;
	}
}
