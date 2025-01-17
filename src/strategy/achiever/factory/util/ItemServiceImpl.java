package strategy.achiever.factory.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import hydra.dao.ItemDAO;
import hydra.model.BotEffect;
import hydra.model.BotItemDetails;
import hydra.model.BotItemType;
import hydra.model.BotResourceSkill;

public final class ItemServiceImpl implements ItemService {
	private static final List<BotEffect> TOOLS_EFFECT = Arrays.<BotEffect>asList(BotEffect.FISHING, BotEffect.MINING,
			BotEffect.WOODCUTTING, BotEffect.ALCHEMY);
	private final Map<String, ToolStruct> toolsItemCache;
	private final Map<String, Coordinate> teleportItemCache;
	private final List<BotItemDetails> usefullArtifactsCache;

	public ItemServiceImpl(ItemDAO itemDao) {
		toolsItemCache = new HashMap<>();
		initTools(itemDao);
		teleportItemCache = itemDao.getItems().stream()
				.filter(bid -> bid.getEffects().stream().anyMatch(bie -> BotEffect.TELEPORT_X.equals(bie.getName()))).collect(Collectors.toMap(BotItemDetails::getCode, this::createCoordinate));
		usefullArtifactsCache = itemDao.getItems().stream().filter(bid -> bid.getEffects().stream().anyMatch(bie -> BotEffect.INVENTORY_SPACE.equals(bie.getName()) && bie.getValue() > 0)).toList();
	}

	private void initTools(ItemDAO itemDao) {
		List<BotItemDetails> toolItems = itemDao.getItems().stream()
				.filter(bid -> bid.getEffects().stream().anyMatch(bie -> TOOLS_EFFECT.contains(bie.getName())))
				.toList();
		for (BotItemDetails tool : toolItems) {
			int effectValue = isEffect(tool, BotEffect.FISHING);
			if (effectValue != 0) {
				toolsItemCache.put(tool.getCode(),
						new ToolStruct(BotResourceSkill.FISHING, effectValue, tool.getType()));
			}
			effectValue = isEffect(tool, BotEffect.MINING);
			if (effectValue != 0) {
				toolsItemCache.put(tool.getCode(),
						new ToolStruct(BotResourceSkill.MINING, effectValue, tool.getType()));
			}
			effectValue = isEffect(tool, BotEffect.WOODCUTTING);
			if (effectValue != 0) {
				toolsItemCache.put(tool.getCode(),
						new ToolStruct(BotResourceSkill.WOODCUTTING, effectValue, tool.getType()));
			}
			effectValue = isEffect(tool, BotEffect.ALCHEMY);
			if (effectValue != 0) {
				toolsItemCache.put(tool.getCode(),
						new ToolStruct(BotResourceSkill.ALCHEMY, effectValue, tool.getType()));
			}
		}
	}

	@Override
	public List<String> getToolsCode(BotResourceSkill botResourceSkill) {
		return toolsItemCache.entrySet().stream()
				.filter(entry -> entry.getValue().botResourceSkill.equals(botResourceSkill)).map(Entry::getKey)
				.toList();
	}

	@Override
	public List<String> getToolsCode() {
		return toolsItemCache.entrySet().stream().map(Entry::getKey).toList();
	}

	@Override
	public boolean isTools(String code) {
		return toolsItemCache.containsKey(code);
	}

	@Override
	public boolean isTools(String code, BotResourceSkill botResourceSkill) {
		if (toolsItemCache.containsKey(code)) {
			return toolsItemCache.get(code).botResourceSkill == botResourceSkill;
		}
		return false;
	}

	@Override
	public int getToolValue(String code) {
		return toolsItemCache.get(code).value;
	}

	private int isEffect(BotItemDetails item, BotEffect effect) {
		Optional<Integer> search = item.getEffects().stream().filter(bie -> effect == bie.getName())
				.<Integer>map(bie -> bie.getValue()).findFirst();
		if (search.isPresent()) {
			return search.get();
		} else {
			return 0;
		}
	}

	private record ToolStruct(BotResourceSkill botResourceSkill, int value, BotItemType type) {
	}

	@Override
	public BotItemType getToolType(String code) {
		return toolsItemCache.get(code).type;
	}

	@Override
	public boolean isTeleportItem(String code) {
		return teleportItemCache.containsKey(code);
	}

	@Override
	public Coordinate getTeleportItemValue(String code) {
		return teleportItemCache.get(code);
	}
	
	@Override
	public Set<String> getAllTeleportItemCode(){
		return teleportItemCache.keySet();
	}

	private Coordinate createCoordinate(BotItemDetails item) {
		int x = item.getEffects().stream().filter(bie -> BotEffect.TELEPORT_X.equals(bie.getName())).map(bie -> bie.getValue()).findFirst().get();
		int y = item.getEffects().stream().filter(bie -> BotEffect.TELEPORT_Y.equals(bie.getName())).map(bie -> bie.getValue()).findFirst().get();
		return new Coordinate(x, y);
	}

	@Override
	public List<BotItemDetails> getUsefullArtifacts() {
		return usefullArtifactsCache;
	}
}
