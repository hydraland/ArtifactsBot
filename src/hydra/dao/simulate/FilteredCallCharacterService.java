package hydra.dao.simulate;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import hydra.dao.BankDAO;
import hydra.model.BotCharacterInventorySlot;
import hydra.model.BotCraftSkill;
import hydra.model.BotInventoryItem;
import hydra.model.BotResourceSkill;
import strategy.util.BotItemInfo;
import strategy.util.CharacterService;

public final class FilteredCallCharacterService implements CharacterService {

	private final FilteredInnerCallSimulatorListener simulatorListener;
	private final CharacterService characterService;

	public FilteredCallCharacterService(FilteredInnerCallSimulatorListener simulatorListener,
			CharacterService characterService) {
		this.simulatorListener = simulatorListener;
		this.characterService = characterService;
	}

	@Override
	public boolean isPossess(String code, BankDAO bankDAO) {
		simulatorListener.startInnerCall();
		try {
			return characterService.isPossess(code, bankDAO);
		} finally {
			simulatorListener.stopInnerCall();
		}
	}

	@Override
	public boolean isPossess(String code, int quantity, BankDAO bankDAO) {
		simulatorListener.startInnerCall();
		try {
			return characterService.isPossess(code, quantity, bankDAO);
		} finally {
			simulatorListener.stopInnerCall();
		}
	}

	@Override
	public int getFreeInventorySpace() {
		simulatorListener.startInnerCall();
		try {
			return characterService.getFreeInventorySpace();
		} finally {
			simulatorListener.stopInnerCall();
		}
	}

	@Override
	public int getUtilitySlotQuantity(BotCharacterInventorySlot slot) {
		simulatorListener.startInnerCall();
		try {
			return characterService.getUtilitySlotQuantity(slot);
		} finally {
			simulatorListener.stopInnerCall();
		}
	}

	@Override
	public int getQuantityInInventory(String code) {
		simulatorListener.startInnerCall();
		try {
			return characterService.getQuantityInInventory(code);
		} finally {
			simulatorListener.stopInnerCall();
		}
	}

	@Override
	public int getLevel(BotCraftSkill key) {
		simulatorListener.startInnerCall();
		try {
			return characterService.getLevel(key);
		} finally {
			simulatorListener.stopInnerCall();
		}
	}

	@Override
	public int getLevel(BotResourceSkill key) {
		simulatorListener.startInnerCall();
		try {
			return characterService.getLevel(key);
		} finally {
			simulatorListener.stopInnerCall();
		}
	}

	@Override
	public Map<BotCharacterInventorySlot, List<BotItemInfo>> getEquipableCharacterEquipement(
			Map<String, Integer> reservedItems) {
		simulatorListener.startInnerCall();
		try {
			return characterService.getEquipableCharacterEquipement(reservedItems);
		} finally {
			simulatorListener.stopInnerCall();
		}
	}

	@Override
	public int getCharacterHPWihtoutEquipment() {
		simulatorListener.startInnerCall();
		try {
			return characterService.getCharacterHPWihtoutEquipment();
		} finally {
			simulatorListener.stopInnerCall();
		}
	}

	@Override
	public Map<BotCharacterInventorySlot, List<BotItemInfo>> getEquipableCharacterEquipementInBank(BankDAO bankDAO,
			Map<String, Integer> reservedItems) {
		simulatorListener.startInnerCall();
		try {
			return characterService.getEquipableCharacterEquipementInBank(bankDAO, reservedItems);
		} finally {
			simulatorListener.stopInnerCall();
		}
	}

	@Override
	public int getQuantityEquipableForUtility(BotItemInfo botItemInfo, BotCharacterInventorySlot slot) {
		simulatorListener.startInnerCall();
		try {
			return characterService.getQuantityEquipableForUtility(botItemInfo, slot);
		} finally {
			simulatorListener.stopInnerCall();
		}
	}

	@Override
	public boolean isInventorySlotFull() {
		simulatorListener.startInnerCall();
		try {
			return characterService.isInventorySlotFull();
		} finally {
			simulatorListener.stopInnerCall();
		}
	}

	@Override
	public long getInventoryFreeSlotNumber() {
		simulatorListener.startInnerCall();
		try {
			return characterService.getInventoryFreeSlotNumber();
		} finally {
			simulatorListener.stopInnerCall();
		}
	}

	@Override
	public long getInventoryMaxSlot() {
		simulatorListener.startInnerCall();
		try {
			return characterService.getInventoryMaxSlot();
		} finally {
			simulatorListener.stopInnerCall();
		}
	}

	@Override
	public int getInventoryItemNumber() {
		simulatorListener.startInnerCall();
		try {
			return characterService.getInventoryItemNumber();
		} finally {
			simulatorListener.stopInnerCall();
		}
	}

	@Override
	public List<String> getNoPotionEquipedItems() {
		simulatorListener.startInnerCall();
		try {
			return characterService.getNoPotionEquipedItems();
		} finally {
			simulatorListener.stopInnerCall();
		}
	}

	@Override
	public List<BotInventoryItem> getFilterEquipementInInventory(Collection<String> equipementNames,
			String excludeEquipementName) {
		simulatorListener.startInnerCall();
		try {
			return characterService.getFilterEquipementInInventory(equipementNames, excludeEquipementName);
		} finally {
			simulatorListener.stopInnerCall();
		}
	}

	@Override
	public Optional<BotInventoryItem> getFirstEquipementInInventory(List<String> equipementNames) {
		simulatorListener.startInnerCall();
		try {
			return characterService.getFirstEquipementInInventory(equipementNames);
		} finally {
			simulatorListener.stopInnerCall();
		}
	}

	@Override
	public boolean inventoryConstaints(String code, int number) {
		simulatorListener.startInnerCall();
		try {
			return characterService.inventoryConstaints(code, number);
		} finally {
			simulatorListener.stopInnerCall();
		}
	}

	@Override
	public List<BotInventoryItem> getInventoryIgnoreEmpty() {
		simulatorListener.startInnerCall();
		try {
			return characterService.getInventoryIgnoreEmpty();
		} finally {
			simulatorListener.stopInnerCall();
		}
	}

	@Override
	public boolean isPossessAny(List<String> codes, BankDAO bankDAO) {
		simulatorListener.startInnerCall();
		try {
			return characterService.isPossessAny(codes, bankDAO);
		} finally {
			simulatorListener.stopInnerCall();
		}
	}

	@Override
	public boolean isPossessOnSelf(String code) {
		simulatorListener.startInnerCall();
		try {
			return characterService.isPossessOnSelf(code);
		} finally {
			simulatorListener.stopInnerCall();
		}
	}

}
