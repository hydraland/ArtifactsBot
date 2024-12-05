package strategy.achiever.factory.goals;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import hydra.dao.MapDAO;
import hydra.model.BotBox;
import hydra.model.BotInventoryItem;
import strategy.achiever.factory.util.Coordinate;
import strategy.util.CharacterService;

public interface ResourceGoalAchiever extends ArtifactGoalAchiever {
	String getCode();

	static List<Coordinate> searchCoordinates(MapDAO mapDao, String code, boolean isMonster) {
		List<BotBox> boxes = isMonster ? mapDao.getMonstersBox() : mapDao.getResourcesBox();
		List<Coordinate> currentCoordinates = boxes.stream().filter(box -> box.getContent().getCode().equals(code))
				.<Coordinate>map(box -> new Coordinate(box.getX(), box.getY())).toList();
		return currentCoordinates.isEmpty() ? null : currentCoordinates;
	}

	static int reserveInInventory(CharacterService characterService, String code, Map<String, Integer> reservedItems,
			int itemNumber) {
		Optional<BotInventoryItem> firstEquipementInInventory = characterService
				.getFirstEquipementInInventory(Arrays.asList(code));
		if (firstEquipementInInventory.isPresent()) {
			int itemQuantityInInventoryUnreserve = firstEquipementInInventory.get().getQuantity()
					- reservedItems.getOrDefault(code, 0);
			int itemQuantityReservable = Math.min(itemQuantityInInventoryUnreserve, itemNumber);
			reserveItem(code, reservedItems, itemQuantityReservable);
			return itemQuantityReservable;
		}
		return 0;
	}

	static void reserveItem(String code, Map<String, Integer> reservedItems, int itemNumber) {
		if (reservedItems.containsKey(code)) {
			reservedItems.put(code, itemNumber + reservedItems.get(code));
		} else {
			reservedItems.put(code, itemNumber);
		}
	}
}
