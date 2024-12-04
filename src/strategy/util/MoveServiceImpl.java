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
import strategy.achiever.factory.ArtifactGoalAchiever;
import strategy.achiever.factory.util.Coordinate;

public class MoveServiceImpl implements MoveService {

	private final CharacterDAO characterDAO;
	private List<Coordinate> bankLocation;
	private List<Coordinate> grandExchangesLocation;
	private Map<BotCraftSkill, List<Coordinate>> workshopLocation;

	public MoveServiceImpl(CharacterDAO characterDAO, MapDAO mapDAO) {
		this.characterDAO = characterDAO;
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
		Coordinate coordinate = ArtifactGoalAchiever.searchClosestLocation(x, y, coordinates);
		return (x == coordinate.x() && y == coordinate.y())
				|| characterDAO.move(coordinate.x(), coordinate.y()).ok();
	}
}
