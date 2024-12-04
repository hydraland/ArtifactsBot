package hydra.dao.simulate;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import hydra.GameConstants;
import hydra.dao.ItemDAO;
import hydra.dao.response.UseInCraftResponse;
import hydra.model.BotCraftSkill;
import hydra.model.BotItem;
import hydra.model.BotItemDetails;
import hydra.model.BotItemType;
import util.LimitedTimeCacheManager;

public final class ItemDAOSimulator implements ItemDAO, Simulator<List<BotItemDetails>> {

	private static final String ITEM_DAO_SIMULATOR = "ItemDAOSimulator";
	private static final String GET_ITEMS = "getItems";
	List<BotItemDetails> items;
	private final SimulatorListener simulatorListener;
	private final Map<String, UseInCraftResponse> useInCraftMap;
	private final ByteArrayOutputStream memoryStream;
	private Map<String, BotItemDetails> codeToItem;
	private final LimitedTimeCacheManager<String, List<BotItemDetails>> itemLevelCache;

	public ItemDAOSimulator(SimulatorListener simulatorListener) {
		this.simulatorListener = simulatorListener;
		useInCraftMap = new HashMap<>();
		memoryStream = new ByteArrayOutputStream();
		itemLevelCache = new LimitedTimeCacheManager<>(600);
	}

	@Override
	public List<BotItemDetails> getResourceItems() {
		simulatorListener.call(ITEM_DAO_SIMULATOR, "getResourceItems", 0, false);
		return items.stream().filter(bid -> BotItemType.RESOURCE.equals(bid.getType())).toList();
	}

	@Override
	public List<BotItemDetails> getItems() {
		simulatorListener.call(ITEM_DAO_SIMULATOR, GET_ITEMS, 0, false);
		return items;
	}

	@Override
	public List<BotItemDetails> getItems(BotCraftSkill skill) {
		simulatorListener.call(ITEM_DAO_SIMULATOR, GET_ITEMS, 0, false);
		return items.stream().filter(item -> item.getCraft() != null && skill.equals(item.getCraft().getSkill()))
				.toList();
	}

	@Override
	public List<BotItemDetails> getItems(BotItemType type) {
		simulatorListener.call(ITEM_DAO_SIMULATOR, GET_ITEMS, 0, false);
		return items.stream().filter(item -> type.equals(item.getType())).toList();
	}

	@Override
	public List<BotItemDetails> getItems(BotItemType type, Integer minLevel, Integer maxLevel) {
		simulatorListener.call(ITEM_DAO_SIMULATOR, GET_ITEMS, 0, false);
		String key = minLevel+type.name()+maxLevel;
		if(itemLevelCache.contains(key)) {
			return itemLevelCache.get(key);
		}
		List<BotItemDetails> result = items.stream().filter(
				item -> type.equals(item.getType()) && item.getLevel() >= minLevel && item.getLevel() <= maxLevel)
				.toList();
		itemLevelCache.add(key, result);
		return result;
	}

	@Override
	public List<BotItemDetails> getItems(Integer minLevel, Integer maxLevel) {
		simulatorListener.call(ITEM_DAO_SIMULATOR, GET_ITEMS, 0, false);
		return items.stream().filter(item -> item.getLevel() >= minLevel && item.getLevel() <= maxLevel).toList();
	}

	@Override
	public List<BotItemDetails> getTaskItems() {
		simulatorListener.call(ITEM_DAO_SIMULATOR, "getTaskItems", 0, false);
		List<BotItemDetails> resourceItems = getItems(BotItemType.RESOURCE);
		return resourceItems.stream().filter(bid -> GameConstants.SUBTYPE_RESOURCE_TASK.equals(bid.getSubtype()))
				.toList();
	}

	@Override
	public UseInCraftResponse useInCraft(String code) {
		simulatorListener.call(ITEM_DAO_SIMULATOR, "useInCraft", 0, false);
		return useInCraftMap.get(code);
	}

	private void initializeCraftUseCache(List<BotItemDetails> items) {
		UseInCraftResponse falseResponse = new UseInCraftResponse(false, false);
		// On initialise tous le item à faux
		items.stream().forEach(bid -> useInCraftMap.put(bid.getCode(), falseResponse));
		UseInCraftResponse trueResponse = new UseInCraftResponse(false, true);
		for (BotItemDetails item : items) {
			if (item.getCraft() != null) {
				for (BotItem craftItem : item.getCraft().getItems()) {
					useInCraftMap.put(craftItem.getCode(), trueResponse);
				}
			}
		}
	}

	@Override
	public BotItemDetails getItem(String code) {
		simulatorListener.call(ITEM_DAO_SIMULATOR, "getItem", 0, false);
		return codeToItem.get(code);
	}

	@Override
	public void load(boolean persistant) {
		List<BotItemDetails> itemsLoaded = Simulator.load(persistant, new File("ItemDAOSimulator.xml"), memoryStream);
		if (itemsLoaded == null) {
			itemsLoaded = Collections.emptyList();
		}
		set(itemsLoaded);
	}

	@Override
	public void save(boolean persistant) {
		Simulator.save(persistant, new File("ItemDAOSimulator.xml"), memoryStream, items);
	}

	@Override
	public void set(List<BotItemDetails> value) {
		this.items = value;
		initializeCraftUseCache(value);
		initCodeToItemMap(value);
	}

	private void initCodeToItemMap(List<BotItemDetails> value) {
		codeToItem = value.stream().collect(Collectors.toMap(BotItemDetails::getCode, Function.identity()));
	}
}
