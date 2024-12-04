package strategy.achiever.factory.custom;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.builder.ToStringBuilder;

import hydra.dao.BankDAO;
import hydra.dao.CharacterDAO;
import hydra.dao.GrandExchangeDAO;
import hydra.dao.ItemDAO;
import hydra.model.BotCharacter;
import hydra.model.BotCraftSkill;
import hydra.model.BotInventoryItem;
import hydra.model.BotItemDetails;
import hydra.model.BotItemType;
import strategy.achiever.GoalParameter;
import strategy.util.CharacterService;
import strategy.util.MoveService;

public final class EquipmentManagerGoalAchiever extends AbstractCustomGoalAchiever {

	private static final int SELL_MIN_CHARACTER_GOLD = 1000;
	private final GoalParameter parameter;
	private final CharacterDAO characterDAO;
	private final ItemDAO itemDAO;
	private final BankDAO bankDAO;
	private final MoveService moveService;
	private final GrandExchangeDAO grandExchangeDAO;

	public EquipmentManagerGoalAchiever(CharacterDAO characterDAO, ItemDAO itemDAO, BankDAO bankDAO,
			GrandExchangeDAO grandExchangeDAO, MoveService moveService, GoalParameter parameter,
			CharacterService characterService) {
		super(characterService);
		this.characterDAO = characterDAO;
		this.itemDAO = itemDAO;
		this.bankDAO = bankDAO;
		this.grandExchangeDAO = grandExchangeDAO;
		this.moveService = moveService;
		this.parameter = parameter;
	}

	@Override
	public boolean isRealisable(BotCharacter character) {
		return characterService.getInventoryFreeSlotNumber() < parameter.getMinFreeSlot()
				|| characterService.getFreeInventorySpace() < parameter.getMinFreeInventorySpace();
	}

	@Override
	public boolean execute() {
		BotCharacter character = characterDAO.getCharacter();
		List<BotInventoryItem> sellItems = new ArrayList<>();
		Map<BotCraftSkill, List<BotInventoryItem>> recycleItems = new EnumMap<>(BotCraftSkill.class);
		List<BotInventoryItem> depositItems = new ArrayList<>();
		searchExtraEquipments(sellItems, recycleItems, depositItems, character.getGold());

		// dépot des items conservés
		return (depositPreservedItems(depositItems) && recycleItems(recycleItems) && sellItems(sellItems));
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		return builder.toString();
	}

	private boolean recycleItems(Map<BotCraftSkill, List<BotInventoryItem>> recycleItems) {
		if (!recycleItems.isEmpty()) {
			for (Entry<BotCraftSkill, List<BotInventoryItem>> entry : recycleItems.entrySet()) {
				List<BotInventoryItem> botItems = entry.getValue();
				if (!moveService.moveTo(entry.getKey())) {
					return false;
				}
				for (BotInventoryItem botItem : botItems) {
					if (!characterDAO.recycle(botItem).ok()) {
						return false;
					}
				}
			}
		}
		return true;
	}

	private boolean sellItems(List<BotInventoryItem> sellItems) {
		if (!sellItems.isEmpty()) {
			if (!moveService.moveToGrandEchange()) {
				return false;
			}
			for (BotInventoryItem sellItem : sellItems) {
				if (!grandExchangeDAO.sell(sellItem, grandExchangeDAO.estimateItemPrice(sellItem.getCode(),
						characterDAO.getCharacter().getGold()))) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean depositPreservedItems(List<BotInventoryItem> depositItems) {
		if (!depositItems.isEmpty()) {
			if (!moveService.moveToBank()) {
				return false;
			}
			for (BotInventoryItem depositItem : depositItems) {
				if (!bankDAO.deposit(depositItem)) {
					return false;
				}
			}
		}
		return true;
	}

	private void searchExtraEquipments(List<BotInventoryItem> sellItems,
			Map<BotCraftSkill, List<BotInventoryItem>> recycleItems, List<BotInventoryItem> depositItems,
			int characterGold) {
		boolean sellPossible = grandExchangeDAO.isSellPossible() && characterGold > SELL_MIN_CHARACTER_GOLD;
		for (BotInventoryItem botItem : characterService.getInventoryIgnoreEmpty()) {
			BotItemDetails botItemDetail = itemDAO.getItem(botItem.getCode());
			if (!BotItemType.UTILITY.equals(botItemDetail.getType())
					&& !BotItemType.CURRENCY.equals(botItemDetail.getType())
					&& !BotItemType.RESOURCE.equals(botItemDetail.getType())
					&& !BotItemType.CONSUMABLE.equals(botItemDetail.getType())) {
				int nbRreservedElements = BotItemType.RING.equals(botItemDetail.getType()) ? 2 : 1;
				if (botItem.getQuantity() > nbRreservedElements) {
					botItem.setQuantity(botItem.getQuantity() - nbRreservedElements);
					if (botItemDetail.getCraft() == null) {
						if (botItemDetail.isTradeable() && sellPossible) {
							sellItems.add(botItem);
						}
					} else {
						List<BotInventoryItem> botItems = recycleItems
								.computeIfAbsent(botItemDetail.getCraft().getSkill(), k -> new ArrayList<>());
						botItems.add(botItem);
					}
					BotInventoryItem depositItem = new BotInventoryItem();
					depositItem.setCode(botItem.getCode());
					depositItem.setQuantity(nbRreservedElements);
					depositItems.add(depositItem);
				} else {
					depositItems.add(botItem);
				}
			}
		}
	}
}
