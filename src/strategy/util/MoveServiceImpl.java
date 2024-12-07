package strategy.util;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import hydra.dao.CharacterDAO;
import hydra.dao.MapDAO;
import hydra.model.BotBox;
import hydra.model.BotCharacter;
import hydra.model.BotCraftSkill;
import hydra.model.BotInventoryItem;
import strategy.achiever.factory.util.Coordinate;
import strategy.achiever.factory.util.ItemService;

public class MoveServiceImpl implements MoveService {

	private final CharacterDAO characterDAO;
	private List<Coordinate> bankLocation;
	private List<Coordinate> grandExchangesLocation;
	private Map<BotCraftSkill, List<Coordinate>> workshopLocation;
	private final CharacterService characterService;
	private final ItemService itemService;

	public MoveServiceImpl(CharacterDAO characterDAO, MapDAO mapDAO, CharacterService characterService,
			ItemService itemService) {
		this.characterDAO = characterDAO;
		this.characterService = characterService;
		this.itemService = itemService;
		initBankLocation(mapDAO);
		initGrandExchangesLocation(mapDAO);
		initWorksShopsLocation(mapDAO);
	}

	private void initBankLocation(MapDAO mapDAO) {
		bankLocation = new ArrayList<>();
		List<BotBox> banksBox = mapDAO.getBanksBox();
		for (BotBox bankBox : banksBox) {
			bankLocation.add(new Coordinate(bankBox.getX(), bankBox.getY()));
		}
	}

	private void initGrandExchangesLocation(MapDAO mapDAO) {
		grandExchangesLocation = new ArrayList<>();
		List<BotBox> banksBox = mapDAO.getGrandExchangesBox();
		for (BotBox bankBox : banksBox) {
			grandExchangesLocation.add(new Coordinate(bankBox.getX(), bankBox.getY()));
		}
	}

	private void initWorksShopsLocation(MapDAO mapDAO) {
		workshopLocation = new EnumMap<>(BotCraftSkill.class);
		List<BotBox> workshopsBox = mapDAO.getWorkshopsBox();
		for (BotBox workshopBox : workshopsBox) {
			String code = workshopBox.getContent().getCode().toUpperCase();
			BotCraftSkill skill = BotCraftSkill.valueOf(code);
			List<Coordinate> locations = workshopLocation.computeIfAbsent(skill, s -> new ArrayList<>());
			locations.add(new Coordinate(workshopBox.getX(), workshopBox.getY()));
		}
	}

	@Override
	public boolean moveToBank() {
		return moveTo(bankLocation);
	}

	@Override
	public boolean moveToGrandEchange() {
		return moveTo(grandExchangesLocation);
	}

	@Override
	public boolean moveTo(BotCraftSkill craftSkill) {
		return moveTo(workshopLocation.get(craftSkill));
	}

	@Override
	public boolean moveTo(List<Coordinate> coordinates) {
		BotCharacter character = characterDAO.getCharacter();
		int x = character.getX();
		int y = character.getY();
		List<BotInventoryItem> teleportItems = characterService
				.getFilterEquipementInInventory(itemService.getAllTeleportItemCode(), "");
		Coordinate coordinate = MoveService.searchClosestLocation(x, y, coordinates);
		int minDistance = MoveService.calculManhattanDistance(x, y, coordinate.x(), coordinate.y());
		BotInventoryItem useItem = null;
		for (BotInventoryItem teleportItem : teleportItems) {
			Coordinate coord = itemService.getTeleportItemValue(teleportItem.getCode());
			int distance = MoveService.calculManhattanDistance(coord.x(), coord.y(), coordinate.x(), coordinate.y());
			if (distance < minDistance) {
				useItem = new BotInventoryItem();
				useItem.setCode(teleportItem.getCode());
				useItem.setQuantity(1);
				minDistance = distance;
			}
		}

		if (useItem != null) {
			if(!characterDAO.use(useItem).ok()) {
				return false;
			}
			character = characterDAO.getCharacter();
			x = character.getX();
			y = character.getY();
		}
		
		return (x == coordinate.x() && y == coordinate.y()) || characterDAO.move(coordinate.x(), coordinate.y()).ok();
	}
}
