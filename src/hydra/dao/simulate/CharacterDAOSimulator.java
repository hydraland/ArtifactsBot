package hydra.dao.simulate;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.SplittableRandom;
import java.util.function.IntUnaryOperator;

import hydra.GameConstants;
import hydra.dao.CharacterDAO;
import hydra.dao.ItemDAO;
import hydra.dao.MapDAO;
import hydra.dao.MonsterDAO;
import hydra.dao.ResourceDAO;
import hydra.dao.response.CraftResponse;
import hydra.dao.response.DeleteItemResponse;
import hydra.dao.response.EquipResponse;
import hydra.dao.response.FightResponse;
import hydra.dao.response.GatheringResponse;
import hydra.dao.response.MoveResponse;
import hydra.dao.response.RecycleResponse;
import hydra.dao.response.RestResponse;
import hydra.dao.response.UseResponse;
import hydra.model.BotBox;
import hydra.model.BotCharacter;
import hydra.model.BotCharacterInventorySlot;
import hydra.model.BotDropDescription;
import hydra.model.BotDropReceived;
import hydra.model.BotEffect;
import hydra.model.BotFight;
import hydra.model.BotGatheringDetails;
import hydra.model.BotInventoryItem;
import hydra.model.BotItem;
import hydra.model.BotItemDetails;
import hydra.model.BotItemEffect;
import hydra.model.BotItemReader;
import hydra.model.BotMonster;
import hydra.model.BotRecycleDetails;
import hydra.model.BotResource;
import strategy.achiever.factory.util.Coordinate;
import strategy.achiever.factory.util.ItemService;
import strategy.achiever.factory.util.ItemServiceImpl;
import strategy.util.CharacterService;
import strategy.util.CharacterServiceImpl;
import strategy.util.MoveService;
import strategy.util.fight.FightDetails;
import strategy.util.fight.FightService;
import strategy.util.fight.FightServiceImpl;
import util.CacheManager;
import util.PermanentCacheManager;

public final class CharacterDAOSimulator implements CharacterDAO, Simulator<BotCharacter> {

	private static final String RECYCLE = "recycle";
	private static final String FIGHT = "fight";
	private static final String UNEQUIP = "unequip";
	private static final String EQUIP = "equip";
	private static final String CRAFT = "craft";
	private static final String COLLECT = "collect";
	private static final String CLASS_NAME = "CharacterDAOSimulator";
	// C'est par tranche de 5
	protected static final int[] LEVEL_UP_MAX_XP_EVOLUTION = new int[] { 100, 150, 400, 700, 1000, 1200, 1500, 1800,
			1800 };
	protected static final int FIRST_LEVEL_MAX_XP = 150;
	private final FilteredInnerCallSimulatorListener simulatorListener;
	BotCharacter botCharacter;
	private final CharacterService characterService;
	private final ItemDAO itemDAO;
	private final MapDAO mapDAO;
	private final FightService fightService;
	private final MonsterDAO monsterDAO;
	private final SplittableRandom random;
	private final ByteArrayOutputStream memoryStream;
	private final ResourceDAO resourceDAO;
	private final CacheManager<String, Optional<BotBox>> monsterBoxCache;
	private ItemService itemService;

	// TODO prendre en compte l'interruption
	public CharacterDAOSimulator(FilteredInnerCallSimulatorListener simulatorListener, ItemDAO itemDAO, MapDAO mapDAO,
			MonsterDAO monsterDAO, ResourceDAO resourceDAO) {
		this.simulatorListener = simulatorListener;
		this.itemDAO = itemDAO;
		this.mapDAO = mapDAO;
		this.monsterDAO = monsterDAO;
		this.resourceDAO = resourceDAO;
		this.characterService = new FilteredCallCharacterService(simulatorListener,
				new CharacterServiceImpl(this, itemDAO));
		this.fightService = new FightServiceImpl(this, null, itemDAO, characterService, null);
		this.random = new SplittableRandom();
		memoryStream = new ByteArrayOutputStream();
		this.monsterBoxCache = new PermanentCacheManager<>();
	}

	@Override
	public MoveResponse move(int x, int y) {
		if (x == botCharacter.getX() && y == botCharacter.getY()) {
			simulatorListener.call(CLASS_NAME, "move", 0, true);
			return new MoveResponse(false);
		}
		int distance = MoveService.calculManhattanDistance(x, y, botCharacter.getX(), botCharacter.getY());
		botCharacter.setX(x);
		botCharacter.setY(y);
		simulatorListener.call(CLASS_NAME, "move", distance * 5, false);
		return new MoveResponse(true);
	}

	@Override
	public FightResponse fight() {
		// Déterminer quel monstre est sur la case
		simulatorListener.startInnerCall();
		Optional<BotBox> searchBoxMonster;
		String key = botCharacter.getX() + ":" + botCharacter.getY();
		if (monsterBoxCache.contains(key)) {
			searchBoxMonster = monsterBoxCache.get(key);
		} else {
			searchBoxMonster = mapDAO.getMonstersBox().stream()
					.filter(bb -> bb.getX() == botCharacter.getX() && bb.getY() == botCharacter.getY()).findFirst();
			monsterBoxCache.add(key, searchBoxMonster);
		}
		simulatorListener.stopInnerCall();
		if (searchBoxMonster.isEmpty()) {
			simulatorListener.call(CLASS_NAME, FIGHT, 0, true);
			return new FightResponse(false, null, true);
		}
		String monsterCode = searchBoxMonster.get().getContent().getCode();
		simulatorListener.startInnerCall();
		BotMonster monster = monsterDAO.getMonster(monsterCode);
		// On prend comme hypothèse que les HP du perso sont full, ce qui est
		// théoriquement le cas
		FightDetails calculateFightResult = fightService.calculateFightResult(monster);
		simulatorListener.stopInnerCall();
		BotFight botFight = new BotFight();
		botFight.setTurns(calculateFightResult.nbTurn());
		botFight.setResult(calculateFightResult.win() ? "win" : "loss");
		if (botFight.isWin()) {
			List<BotDropReceived> drops = generateDrop(monster.getDrops());
			save(false);
			for (BotDropReceived itemReceived : drops) {
				if (checkDepositInInventory(itemReceived.getCode(), itemReceived.getQuantity())) {
					depositInInventory(itemReceived.getCode(), itemReceived.getQuantity());
				} else {
					// erreur, restauration de l'ancien perso
					load(false);
					simulatorListener.call(CLASS_NAME, FIGHT, 0, true);
					return new FightResponse(false, null, false);
				}
			}
			botFight.setDrops(drops);

			// On met une valeur arbitraire pour le moment
			botFight.setXp(
					(botCharacter.getLevel() - monster.getLevel()) > GameConstants.MAX_LEVEL_DIFFERENCE_FOR_XP ? 0 : 9);
			botFight.setGold(random.nextInt(monster.getMinGold(), monster.getMaxGold() + 1));
			botCharacter.setGold(botFight.getGold());
			botCharacter.setHp(botCharacter.getHp() - calculateFightResult.characterLossHP());
			if (monsterCode.equals(botCharacter.getTask())
					&& botCharacter.getTaskProgress() < botCharacter.getTaskTotal()) {
				botCharacter.setTaskProgress(botCharacter.getTaskProgress() + 1);
			}
		} else {
			botFight.setDrops(Collections.emptyList());
			botCharacter.setHp(1);
			botCharacter.setX(0);
			botCharacter.setY(0);
		}
		int turn = calculateFightResult.nbTurn() * 2 - (botFight.isWin() ? 1 : 0);
		int cooldown = Math.max(Math.round(turn - (botCharacter.getHaste() * 0.01f * turn)), 5);
		// update potion number
		updateUtility(BotCharacterInventorySlot.UTILITY1, calculateFightResult);
		updateUtility(BotCharacterInventorySlot.UTILITY2, calculateFightResult);

		simulatorListener.call(CLASS_NAME, FIGHT, cooldown, false);
		return new FightResponse(true, botFight, false);
	}

	private void updateUtility(BotCharacterInventorySlot slot, FightDetails calculateFightResult) {
		int utilityQuantity = characterService.getUtilitySlotQuantity(slot);
		if (utilityQuantity > 0) {
			BotItemDetails item = itemDAO.getItem(CharacterService.getSlotValue(botCharacter, slot));
			final Optional<BotItemEffect> effectRestore = item.getEffects().stream()
					.filter(bie -> BotEffect.RESTORE.equals(bie.getName())).findAny();
			int quantity = 0;
			if (effectRestore.isPresent()) {
				quantity = Math.min(utilityQuantity, calculateFightResult.restoreTurn());
			} else {
				quantity = 1;
			}
			if (quantity > 0) {
				setSlotValue(slot, CharacterService.getSlotValue(botCharacter, slot), quantity, x -> -x);
			}
		}
	}

	private List<BotDropReceived> generateDrop(List<BotDropDescription> drops) {
		List<BotDropReceived> result = new ArrayList<>();
		for (BotDropDescription drop : drops) {
			double rate = 1d / drop.getRate();
			if (rate >= random.nextDouble()) {
				BotDropReceived dropReceived = new BotDropReceived();
				dropReceived.setCode(drop.getCode());
				dropReceived.setQuantity(random.nextInt(drop.getMinQuantity(), drop.getMaxQuantity() + 1));
				result.add(dropReceived);
			}
		}
		return result;
	}

	private List<BotItem> generateRecycleDetails(List<BotItem> items, int quantity) {
		List<BotItem> result = new ArrayList<>();
		for (int i = 0; i < quantity; i++) {
			// On fait le mininum
			int indexItemChoosed = random.nextInt(items.size());
			BotItemReader item = items.get(indexItemChoosed);
			int nbReceived = Math.min(item.getQuantity() / 3, 1);
			Optional<BotItem> searchItemInResult = result.stream().filter(bi -> bi.getCode().equals(item.getCode()))
					.findFirst();
			if (searchItemInResult.isEmpty()) {
				BotItem newItem = new BotItem();
				newItem.setCode(item.getCode());
				newItem.setQuantity(nbReceived);
				result.add(newItem);
			} else {
				BotItem currentItem = searchItemInResult.get();
				currentItem.setQuantity(currentItem.getQuantity() + nbReceived);
			}
		}
		return result;
	}

	@Override
	public RecycleResponse recycle(String code, int quantity) {
		BotItemDetails item = getItem(code);
		if (checkWithdrawInInventory(code, quantity) && item.getCraft() != null) {
			BotRecycleDetails botRecycleDetails = new BotRecycleDetails();
			botRecycleDetails.setItems(generateRecycleDetails(item.getCraft().getItems(), quantity));

			save(false);
			withdrawInInventory(code, quantity);
			for (BotItemReader itemReceived : botRecycleDetails.getItems()) {
				if (checkDepositInInventory(itemReceived.getCode(), itemReceived.getQuantity())) {
					depositInInventory(itemReceived.getCode(), itemReceived.getQuantity());
				} else {
					// erreur, restauration de l'ancien perso
					load(false);
					simulatorListener.call(CLASS_NAME, RECYCLE, 0, true);
					return new RecycleResponse(false, null);
				}
			}
			simulatorListener.call(CLASS_NAME, RECYCLE, 2 * quantity, false);
			return new RecycleResponse(true, botRecycleDetails);
		}
		simulatorListener.call(CLASS_NAME, RECYCLE, 0, true);
		return new RecycleResponse(false, null);
	}

	@Override
	public GatheringResponse collect() {
		// Déterminer quel ressource est sur la case
		simulatorListener.startInnerCall();
		Optional<BotBox> searchBoxResource = mapDAO.getResourcesBox().stream()
				.filter(bb -> bb.getX() == botCharacter.getX() && bb.getY() == botCharacter.getY()).findFirst();
		simulatorListener.stopInnerCall();
		if (searchBoxResource.isEmpty()) {
			simulatorListener.call(CLASS_NAME, COLLECT, 0, true);
			return new GatheringResponse(false, null, true);
		}
		String resourceCode = searchBoxResource.get().getContent().getCode();
		simulatorListener.startInnerCall();
		BotResource resource = resourceDAO.getResource(resourceCode);
		simulatorListener.stopInnerCall();
		List<BotDropReceived> drops = generateDrop(resource.getDrops());

		save(false);
		for (BotDropReceived itemReceived : drops) {
			if (checkDepositInInventory(itemReceived.getCode(), itemReceived.getQuantity())) {
				depositInInventory(itemReceived.getCode(), itemReceived.getQuantity());
			} else {
				// erreur, restauration de l'ancien perso
				load(false);
				simulatorListener.call(CLASS_NAME, COLLECT, 0, true);
				return new GatheringResponse(false, null, false);
			}
		}

		BotGatheringDetails botGatheringDetails = new BotGatheringDetails();
		botGatheringDetails.setItems(drops.stream().map(bdr -> {
			BotItem item = new BotItem();
			item.setCode(bdr.getCode());
			item.setQuantity(bdr.getQuantity());
			return item;
		}).toList());
		// On met une valeur arbitraire ppur le moment
		botGatheringDetails.setXp((characterService.getLevel(resource.getSkill())
				- resource.getLevel()) > GameConstants.MAX_LEVEL_DIFFERENCE_FOR_XP ? 0 : 13);
		initItemService();
		int cooldown = 25 - Math.round((characterService.getLevel(resource.getSkill()) - resource.getLevel() + 1) / 10f)
				+ (itemService.isTools(botCharacter.getWeaponSlot(), resource.getSkill())
						? Math.round(itemService.getToolValue(botCharacter.getWeaponSlot()) * 0.25f)
						: 0);
		simulatorListener.call(CLASS_NAME, COLLECT, cooldown, false);
		return new GatheringResponse(true, botGatheringDetails, false);
	}

	@Override
	public DeleteItemResponse deleteItem(BotItemReader item) {
		if (checkWithdrawInInventory(item.getCode(), item.getQuantity())) {
			withdrawInInventory(item.getCode(), item.getQuantity());
			simulatorListener.call(CLASS_NAME, "deleteItem", 3, false);
			return new DeleteItemResponse(true, item);
		}
		simulatorListener.call(CLASS_NAME, "deleteItem", 0, true);
		return new DeleteItemResponse(false, null);
	}

	@Override
	public EquipResponse equip(String code, BotCharacterInventorySlot slot, int quantity) {
		IntUnaryOperator operator = x -> x;
		if (checkSlot(slot, quantity, operator) && checkWithdrawInInventory(code, quantity)) {
			withdrawInInventory(code, quantity);
			setSlotValue(slot, code, quantity, operator);
			updateEffect(getItem(CharacterService.getSlotValue(botCharacter, slot)), operator);
			simulatorListener.call(CLASS_NAME, EQUIP, 3, false);
			return new EquipResponse(true);
		}
		simulatorListener.call(CLASS_NAME, EQUIP, 3, true);
		return new EquipResponse(false);
	}

	@Override
	public EquipResponse unequip(BotCharacterInventorySlot slot, int quantity) {
		IntUnaryOperator operator = x -> -x;
		String code = CharacterService.getSlotValue(botCharacter, slot);
		if (checkSlot(slot, quantity, operator) && checkDepositInInventory(code, quantity)) {
			depositInInventory(code, quantity);
			setSlotValue(slot, code, quantity, operator);
			updateEffect(getItem(code), operator);
			simulatorListener.call(CLASS_NAME, UNEQUIP, 3, false);
			return new EquipResponse(true);
		}
		simulatorListener.call(CLASS_NAME, UNEQUIP, 0, true);
		return new EquipResponse(false);
	}

	private void updateEffect(BotItemDetails botItemDetails, IntUnaryOperator operator) {
		for (BotItemEffect effect : botItemDetails.getEffects()) {
			switch (effect.getName()) {
			case ATTACK_AIR:
				botCharacter.setAttackAir(botCharacter.getAttackAir() + operator.applyAsInt(effect.getValue()));
				break;
			case ATTACK_EARTH:
				botCharacter.setAttackEarth(botCharacter.getAttackEarth() + operator.applyAsInt(effect.getValue()));
				break;
			case ATTACK_FIRE:
				botCharacter.setAttackFire(botCharacter.getAttackFire() + operator.applyAsInt(effect.getValue()));
				break;
			case ATTACK_WATER:
				botCharacter.setAttackWater(botCharacter.getAttackWater() + operator.applyAsInt(effect.getValue()));
				break;
			case DMG_AIR:
				botCharacter.setDmgAir(botCharacter.getDmgAir() + operator.applyAsInt(effect.getValue()));
				break;
			case DMG_EARTH:
				botCharacter.setDmgEarth(botCharacter.getDmgEarth() + operator.applyAsInt(effect.getValue()));
				break;
			case DMG_FIRE:
				botCharacter.setDmgFire(botCharacter.getDmgFire() + operator.applyAsInt(effect.getValue()));
				break;
			case DMG_WATER:
				botCharacter.setDmgWater(botCharacter.getDmgWater() + operator.applyAsInt(effect.getValue()));
				break;
			case HASTE:
				botCharacter.setHaste(botCharacter.getHaste() + operator.applyAsInt(effect.getValue()));
				break;
			case HP:
				int diffHp = botCharacter.getMaxHp() - botCharacter.getHp();
				botCharacter.setMaxHp(botCharacter.getMaxHp() + operator.applyAsInt(effect.getValue()));
				botCharacter.setHp(Math.max(1, botCharacter.getMaxHp() - diffHp));
				break;
			case INVENTORY_SPACE:
				botCharacter.setInventoryMaxItems(
						botCharacter.getInventoryMaxItems() + operator.applyAsInt(effect.getValue()));
				break;
			case RES_AIR:
				botCharacter.setResAir(botCharacter.getResAir() + operator.applyAsInt(effect.getValue()));
				break;
			case RES_EARTH:
				botCharacter.setResEarth(botCharacter.getResEarth() + operator.applyAsInt(effect.getValue()));
				break;
			case RES_FIRE:
				botCharacter.setResFire(botCharacter.getResFire() + operator.applyAsInt(effect.getValue()));
				break;
			case RES_WATER:
				botCharacter.setResWater(botCharacter.getResWater() + operator.applyAsInt(effect.getValue()));
				break;
			default:
				break;
			}
		}
	}

	@Override
	public CraftResponse craft(String code, int quantity) {
		save(false);
		BotItemDetails item = getItem(code);
		if (item.getCraft() != null) {
			for (BotItemReader sourceItem : item.getCraft().getItems()) {
				if (checkWithdrawInInventory(sourceItem.getCode(), sourceItem.getQuantity() * quantity)) {
					withdrawInInventory(sourceItem.getCode(), sourceItem.getQuantity() * quantity);
				} else {
					load(false);
					simulatorListener.call(CLASS_NAME, CRAFT, 0, true);
					return new CraftResponse(false);
				}
			}
			// TODO set XP arbitrary
			// faire un for sur la quantite car si diff > 10 on doit mettre 0
			// int xp = (characterService.getLevel(item.getCraft().getSkill()) -
			// item.getLevel()) > GameConstants.MAX_LEVEL_DIFFERENCE_FOR_XP ? 0 : 200;
			depositInInventory(code, quantity);
			simulatorListener.call(CLASS_NAME, CRAFT, 5 * quantity, false);
			return new CraftResponse(true);
		}
		simulatorListener.call(CLASS_NAME, CRAFT, 0, true);
		return new CraftResponse(false);
	}

	@Override
	public BotCharacter getCharacter() {
		simulatorListener.call(CLASS_NAME, "getCharacter", 0, false);
		return botCharacter;
	}

	@Override
	public RestResponse rest() {
		int missingPV = botCharacter.getMaxHp() - botCharacter.getHp();
		int cooldown = Math.max(missingPV / 5 + (missingPV % 5 == 0 ? 0 : 1), 3);
		simulatorListener.call(CLASS_NAME, "rest", cooldown, false);
		botCharacter.setHp(botCharacter.getMaxHp());
		return new RestResponse(true, missingPV);
	}

	@Override
	public UseResponse use(String code, int quantity) {
		initItemService();
		if (itemService.isTeleportItem(code)) {
			if (checkWithdrawInInventory(code, quantity)) {
				simulatorListener.call(CLASS_NAME, "use", 3, false);
				Coordinate coordinate = itemService.getTeleportItemValue(code);
				botCharacter.setX(coordinate.x());
				botCharacter.setY(coordinate.y());
				withdrawInInventory(code, quantity);
				return new UseResponse(true);
			}
			simulatorListener.call(CLASS_NAME, "use", 0, false);
			return new UseResponse(false);
		}

		int restoreValue = getRestoreValue(code) * quantity;
		if (checkWithdrawInInventory(code, quantity) && restoreValue > 0) {
			simulatorListener.call(CLASS_NAME, "use", 3, false);
			botCharacter.setHp(Math.min(botCharacter.getHp() + restoreValue, botCharacter.getMaxHp()));
			withdrawInInventory(code, quantity);
			return new UseResponse(true);
		}
		simulatorListener.call(CLASS_NAME, "use", 0, false);
		return new UseResponse(false);
	}

	private int getRestoreValue(String code) {
		Optional<BotItemEffect> restoreVal = getItem(code).getEffects().stream()
				.filter(bie -> BotEffect.HEAL.equals(bie.getName())).findFirst();
		return restoreVal.isEmpty() ? 0 : restoreVal.get().getValue();
	}

	@Override
	public void load(boolean persistant) {
		botCharacter = Simulator.load(persistant, new File("CharacterDAOSimulator.xml"), memoryStream);
		if (botCharacter == null) {
			botCharacter = new BotCharacter();
		}
	}

	@Override
	public void save(boolean persistant) {
		Simulator.save(persistant, new File("CharacterDAOSimulator.xml"), memoryStream, botCharacter);
	}

	@Override
	public void set(BotCharacter value) {
		this.botCharacter = value;
	}

	public final CharacterService getCharacterService() {
		return characterService;
	}

	public void withdrawInInventory(String code, int quantity) {
		BotInventoryItem botInventoryItem = characterService.getFirstEquipementInInventory(Arrays.asList(code)).get();
		botInventoryItem.setQuantity(botInventoryItem.getQuantity() - quantity);
		if (botInventoryItem.getQuantity() == 0) {
			botInventoryItem.setCode("");
		}
	}

	public void depositInInventory(String code, int quantity) {
		Optional<BotInventoryItem> firstEquipementInInventory = characterService
				.getFirstEquipementInInventory(Arrays.asList(code));
		if (firstEquipementInInventory.isEmpty()) {
			BotInventoryItem botInventoryItem = botCharacter.getInventory().stream()
					.filter(bii -> bii.getQuantity() == 0).findFirst().get();
			botInventoryItem.setCode(code);
			botInventoryItem.setQuantity(quantity);
		} else {
			BotInventoryItem botInventoryItem = firstEquipementInInventory.get();
			botInventoryItem.setQuantity(botInventoryItem.getQuantity() + quantity);
		}
	}

	public boolean checkDepositInInventory(String code, int quantity) {
		Optional<BotInventoryItem> firstEquipementInInventory = characterService
				.getFirstEquipementInInventory(Arrays.asList(code));
		return characterService.getFreeInventorySpace() >= quantity
				&& (firstEquipementInInventory.isPresent() || characterService.getInventoryFreeSlotNumber() > 0);
	}

	public boolean checkWithdrawInInventory(String code, int quantity) {
		Optional<BotInventoryItem> firstEquipementInInventory = characterService
				.getFirstEquipementInInventory(Arrays.asList(code));
		return firstEquipementInInventory.isPresent() && firstEquipementInInventory.get().getQuantity() >= quantity;
	}

	private boolean checkSlot(BotCharacterInventorySlot slot, int quantity, IntUnaryOperator operator) {
		return (quantity == 1 && !BotCharacterInventorySlot.UTILITY1.equals(slot)
				&& !BotCharacterInventorySlot.UTILITY2.equals(slot)) || checkSlotUtility(slot, quantity, operator);
	}

	private boolean checkSlotUtility(BotCharacterInventorySlot slot, int quantity, IntUnaryOperator operator) {
		int currentQuantity = BotCharacterInventorySlot.UTILITY1.equals(slot) ? botCharacter.getUtility1SlotQuantity()
				: botCharacter.getUtility2SlotQuantity();
		int newQuantity = currentQuantity + operator.applyAsInt(quantity);
		return newQuantity >= 0 && newQuantity <= GameConstants.MAX_ITEM_IN_SLOT;
	}

	private void setSlotValue(BotCharacterInventorySlot slot, String code, int quantity, IntUnaryOperator operator) {
		if (operator.applyAsInt(quantity) == -1 && !BotCharacterInventorySlot.UTILITY1.equals(slot)
				&& !BotCharacterInventorySlot.UTILITY2.equals(slot)) {
			code = "";
		}
		switch (slot) {
		case WEAPON:
			botCharacter.setWeaponSlot(code);
			break;
		case AMULET:
			botCharacter.setAmuletSlot(code);
			break;
		case ARTIFACT1:
			botCharacter.setArtifact1Slot(code);
			break;
		case ARTIFACT2:
			botCharacter.setArtifact2Slot(code);
			break;
		case ARTIFACT3:
			botCharacter.setArtifact3Slot(code);
			break;
		case BODY_ARMOR:
			botCharacter.setBodyArmorSlot(code);
			break;
		case BOOTS:
			botCharacter.setBootsSlot(code);
			break;
		case UTILITY1:
			botCharacter.setUtility1Slot(code);
			botCharacter
					.setUtility1SlotQuantity(botCharacter.getUtility1SlotQuantity() + operator.applyAsInt(quantity));
			if (botCharacter.getUtility1SlotQuantity() == 0) {
				botCharacter.setUtility1Slot("");
			}
			break;
		case UTILITY2:
			botCharacter.setUtility2Slot(code);
			botCharacter
					.setUtility2SlotQuantity(botCharacter.getUtility2SlotQuantity() + operator.applyAsInt(quantity));
			if (botCharacter.getUtility2SlotQuantity() == 0) {
				botCharacter.setUtility2Slot("");
			}
			break;
		case HELMET:
			botCharacter.setHelmetSlot(code);
			break;
		case LEG_ARMOR:
			botCharacter.setLegArmorSlot(code);
			break;
		case RING1:
			botCharacter.setRing1Slot(code);
			break;
		case RING2:
			botCharacter.setRing2Slot(code);
			break;
		case SHIELD:
			botCharacter.setShieldSlot(code);
			break;
		default:
			throw new IllegalArgumentException("Value  " + slot + " not authorize");
		}
	}

	private BotItemDetails getItem(String code) {
		simulatorListener.startInnerCall();
		try {
			return itemDAO.getItem(code);
		} finally {
			simulatorListener.stopInnerCall();
		}
	}

	private void initItemService() {
		if (itemService == null) {
			simulatorListener.startInnerCall();
			itemService = new ItemServiceImpl(itemDAO);
			simulatorListener.stopInnerCall();
		}
	}
}