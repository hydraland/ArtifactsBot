package strategy.achiever.factory.custom;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.ToStringBuilder;

import hydra.GameConstants;
import hydra.dao.BankDAO;
import hydra.dao.CharacterDAO;
import hydra.dao.GrandExchangeDAO;
import hydra.dao.ItemDAO;
import hydra.model.BotCharacter;
import hydra.model.BotCraftSkill;
import hydra.model.BotEffect;
import hydra.model.BotInventoryItem;
import hydra.model.BotItem;
import hydra.model.BotItemDetails;
import hydra.model.BotItemReader;
import hydra.model.BotItemType;
import hydra.model.BotMonster;
import strategy.achiever.factory.util.ItemService;
import strategy.util.BotItemInfo;
import strategy.util.CharacterService;
import strategy.util.MoveService;
import strategy.util.OptimizeResult;
import strategy.util.fight.FightService;

public final class UselessEquipmentManagerGoalAchiever extends AbstractCustomGoalAchiever {
	private static final float MIN_FREE_SPACE_PER_CENT = 0.1f;
	private long oldCall;
	private static final long ONE_DAY = 1000 * 60 * 60 * 24l;
	private static final int SELL_MIN_CHARACTER_GOLD = 1000;
	private final FightService fightService;
	private final CharacterDAO characterDAO;
	private final List<BotMonster> monsters;
	private final ItemDAO itemDAO;
	private final BankDAO bankDAO;
	private final MoveService moveService;
	private final GrandExchangeDAO grandExchangeDAO;
	private final ItemService itemService;

	public UselessEquipmentManagerGoalAchiever(CharacterDAO characterDAO, ItemDAO itemDAO, BankDAO bankDAO,
			GrandExchangeDAO grandExchangeDAO, FightService fightService, List<BotMonster> botMonsters,
			MoveService moveService, ItemService itemService, CharacterService characterService) {
		super(characterService);
		this.characterDAO = characterDAO;
		this.itemDAO = itemDAO;
		this.bankDAO = bankDAO;
		this.grandExchangeDAO = grandExchangeDAO;
		this.fightService = fightService;
		this.monsters = botMonsters;
		this.moveService = moveService;
		this.itemService = itemService;
		oldCall = 0;
	}

	// Gère les équipements inutiles, ignore les tools et les items utilisés en
	// craft
	@Override
	public boolean isRealisable(BotCharacter character) {
		int maxSlot = bankDAO.getBankDetail().getSlots();
		int freeBankSpace = maxSlot - bankDAO.viewItems().size();
		return System.currentTimeMillis() - oldCall > ONE_DAY && freeBankSpace <= Math.round(maxSlot*MIN_FREE_SPACE_PER_CENT);
	}

	@Override
	public boolean execute() {
		// Recherche tous les équipements utiles en combat
		Set<String> useEquipments = searchUsefulItems();

		// Parcourt des items et rechercher les inutiles
		Map<BotCraftSkill, List<BotItemReader>> uselessItemsToRecycle = new EnumMap<>(BotCraftSkill.class);
		List<BotItemReader> deleteItems = new ArrayList<>();
		List<BotItemReader> sellItems = new ArrayList<>();
		BotCharacter character = characterDAO.getCharacter();
		List<? extends BotItemReader> inventoryItemList = characterService.getInventoryIgnoreEmpty().stream()
				.<BotItemReader>map(this::botInventoryItemToBotItem).toList();
		searchUselessItem(useEquipments, uselessItemsToRecycle, deleteItems, sellItems, inventoryItemList,
				character.getGold(), GameConstants.MAX_INVENTORY_SLOT);

		// Suppression des items
		if (!deleteItems(deleteItems)) {
			return false;
		}

		// On recycle les items
		if (!recycleItems(uselessItemsToRecycle)) {
			return false;
		}

		// Vente des items
		if (!sellItems(sellItems)) {
			return false;
		}

		// se déplacer à la banque pour mettre les autres items
		uselessItemsToRecycle = new EnumMap<>(BotCraftSkill.class);
		deleteItems = new ArrayList<>();
		sellItems = new ArrayList<>();
		searchUselessItem(useEquipments, uselessItemsToRecycle, deleteItems, sellItems, bankDAO.viewItems(),
				character.getGold(), characterService.getInventoryFreeSlotNumber()/2);
		if (!uselessItemsToRecycle.isEmpty() || !deleteItems.isEmpty() || !sellItems.isEmpty()) {
			if (!moveService.moveToBank()) {
				return false;
			}
			if (characterService.isInventorySlotFull() || !withdrawItems(deleteItems)) {
				return false;
			}
			for (List<BotItemReader> botItems : uselessItemsToRecycle.values()) {
				if (characterService.isInventorySlotFull() || !withdrawItems(botItems)) {
					return false;
				}
			}
			if (characterService.isInventorySlotFull() || !withdrawItems(sellItems)) {
				return false;
			}

			// Suppression des items
			if (!deleteItems(deleteItems)) {
				return false;
			}

			// On recycle les items
			if (!recycleItems(uselessItemsToRecycle)) {
				return false;
			}

			if (!sellItems(sellItems)) {
				return false;
			}
		}
		// On met à jour que si le résultat est ok
		oldCall = System.currentTimeMillis();
		return true;
	}

	private boolean sellItems(List<BotItemReader> sellItems) {
		if (!sellItems.isEmpty()) {
			if (!moveService.moveToGrandEchange()) {
				return false;
			}
			for (BotItemReader botItem : sellItems) {
				if (!grandExchangeDAO.sell(botItem,
						grandExchangeDAO.estimateItemPrice(botItem.getCode(), characterDAO.getCharacter().getGold()))) {
					return false;
				}
			}
		}
		return true;
	}

	private Set<String> searchUsefulItems() {
		Set<String> useEquipments = new HashSet<>();
		// add tools and item with effect INVENTORY_SPACE
		useEquipments
				.addAll(characterService.getInventoryIgnoreEmpty().stream().map(bii -> itemDAO.getItem(bii.getCode()))
						.filter(bid -> itemService.isTools(bid.getCode()) || addInventorySpace(bid))
						.map(bid -> bid.getCode()).toList());
		useEquipments.addAll(bankDAO.viewItems().stream().map(bii -> itemDAO.getItem(bii.getCode()))
				.filter(bid -> itemService.isTools(bid.getCode()) || addInventorySpace(bid)).map(bid -> bid.getCode())
				.toList());

		Map<String, OptimizeResult> optimizeEquipementsPossesed = fightService.optimizeEquipementsPossesed(monsters,
				useEquipments.stream().collect(Collectors.toMap(Function.identity(), t -> 1)));
		for (OptimizeResult optimizeResult : optimizeEquipementsPossesed.values()) {
			BotItemInfo[] bestEqt = optimizeResult.bestEqt();
			for (int i = 0; i < bestEqt.length; i++) {
				if (bestEqt[i] != null) {
					useEquipments.add(bestEqt[i].botItemDetails().getCode());
				}
			}
		}

		return useEquipments;
	}

	private boolean addInventorySpace(BotItemDetails item) {
		return item.getEffects().stream()
				.anyMatch(bie -> BotEffect.INVENTORY_SPACE.equals(bie.getName()) && bie.getValue() > 0);
	}

	private boolean recycleItems(Map<BotCraftSkill, List<BotItemReader>> uselessItemsToRecycle) {
		for (Entry<BotCraftSkill, List<BotItemReader>> entry : uselessItemsToRecycle.entrySet()) {
			List<BotItemReader> botItems = entry.getValue();
			if (!moveService.moveTo(entry.getKey())) {
				return false;
			}
			for (BotItemReader botItem : botItems) {
				if (!characterDAO.recycle(botItem).ok()) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean deleteItems(List<BotItemReader> deleteItems) {
		for (BotItemReader botItem : deleteItems) {
			if (!characterDAO.deleteItem(botItem).ok()) {
				return false;
			}
		}
		return true;
	}

	private boolean withdrawItems(List<BotItemReader> deleteItems) {
		for (BotItemReader botItem : deleteItems) {
			if (!bankDAO.withdraw(botItem)) {
				return false;
			}
		}
		return true;
	}

	private void searchUselessItem(Set<String> useEquipments, Map<BotCraftSkill, List<BotItemReader>> uselessItemsToRecycle,
			List<BotItemReader> deleteItems, List<BotItemReader> sellItems, List<? extends BotItemReader> itemList, int characterGold, long maxItems) {
		boolean sellPossible = grandExchangeDAO.isSellPossible() && characterGold > SELL_MIN_CHARACTER_GOLD;
		int itemCounter = 0;
		for (BotItemReader botItem : itemList) {
			if(itemCounter == maxItems) {
				break;
			}
			BotItemDetails botItemDetail = itemDAO.getItem(botItem.getCode());
			if (!BotItemType.UTILITY.equals(botItemDetail.getType())
					&& !BotItemType.CURRENCY.equals(botItemDetail.getType())
					&& !BotItemType.RESOURCE.equals(botItemDetail.getType())
					&& !BotItemType.CONSUMABLE.equals(botItemDetail.getType())
					&& !useEquipments.contains(botItem.getCode())
					&& !itemDAO.useInCraft(botItem.getCode()).useInCraft()) {
				if (botItemDetail.getCraft() == null) {
					if (botItemDetail.isTradeable() && sellPossible) {
						sellItems.add(botItem);
					} else {
						deleteItems.add(botItem);
					}
				} else {
					List<BotItemReader> botItems = uselessItemsToRecycle.computeIfAbsent(botItemDetail.getCraft().getSkill(),
							k -> new ArrayList<>());
					botItems.add(botItem);
				}
				itemCounter++;
			}
		}
	}

	private BotItem botInventoryItemToBotItem(BotInventoryItem botInventoryItem) {
		BotItem botItem = new BotItem();
		botItem.setCode(botInventoryItem.getCode());
		botItem.setQuantity(botInventoryItem.getQuantity());
		return botItem;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		return builder.toString();
	}
}
