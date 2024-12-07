package strategy.achiever.factory.goals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

import hydra.dao.CharacterDAO;
import hydra.dao.MapDAO;
import hydra.dao.response.FightResponse;
import hydra.model.BotCharacter;
import hydra.model.BotDropReceived;
import hydra.model.BotMonster;
import strategy.achiever.GoalParameter;
import strategy.achiever.factory.util.Coordinate;
import strategy.achiever.factory.util.Cumulator;
import strategy.util.CharacterService;
import strategy.util.MonsterEquipementService;
import strategy.util.MoveService;
import strategy.util.fight.FightService;

public class ItemMonsterGoalAchiever implements ResourceGoalAchiever {

	private static final HashMap<String, Integer> EMPTY_RESERVED_ITEMS = new HashMap<>();
	private final String resourceCode;
	private final int rate;
	private List<Coordinate> coordinates;
	private final CharacterDAO characterDAO;
	private final BotMonster monster;
	private boolean finish;
	private boolean root;
	private final MonsterEquipementService monsterEquipementService;
	private final MapDAO mapDao;
	private final FightService fightService;
	private final MoveService moveService;
	private final CharacterService characterService;
	private final GoalParameter goalParameter;

	public ItemMonsterGoalAchiever(CharacterDAO characterDAO, MapDAO mapDao, String resourceCode, int rate,
			List<Coordinate> coordinates, BotMonster monster, MonsterEquipementService monsterEquipementService,
			FightService fightService, MoveService moveService, CharacterService characterService, GoalParameter goalParameter) {
		this.mapDao = mapDao;
		this.resourceCode = resourceCode;
		this.rate = rate;
		this.coordinates = coordinates;
		this.monster = monster;
		this.characterDAO = characterDAO;
		this.monsterEquipementService = monsterEquipementService;
		this.fightService = fightService;
		this.moveService = moveService;
		this.characterService = characterService;
		this.goalParameter = goalParameter;
		this.finish = false;
	}

	@Override
	public boolean isRealisable(BotCharacter character) {
		monsterEquipementService.reset();
		return fightService.optimizeEquipementsPossesed(monster, EMPTY_RESERVED_ITEMS).fightDetails().eval() > 1d
				&& getCoordinates() != null;
	}

	private List<Coordinate> getCoordinates() {
		return this.coordinates != null ? this.coordinates
				: ResourceGoalAchiever.searchCoordinates(mapDao, monster.getCode(), true);
	}

	@Override
	public boolean execute(Map<String, Integer> reservedItems) {
		if (!root) {
			int nbReserved = ResourceGoalAchiever.reserveInInventory(characterService, getCode(), reservedItems, 1);
			if (nbReserved == 1) {
				this.finish = true;
				return true;
			}
		}

		if (!monsterEquipementService.equipBestEquipement(monster, reservedItems)) {
			return false;
		}

		if (coordinates == null) {
			coordinates = ResourceGoalAchiever.searchCoordinates(mapDao, monster.getCode(), true);
			if (coordinates == null) {
				return false;// le monstre n'est plus présent.
			}
		}
		if (!goalParameter.getHPRecoveryFactory().createHPRecovery().restoreHP(reservedItems)) {
			return false;
		}
		if (moveService.moveTo(coordinates)) {
			FightResponse response = characterDAO.fight();
			if (response.monsterNotFound()) {
				this.coordinates = null;
			}
			if (response.ok() && response.fight().isWin()) {
				List<BotDropReceived> drops = response.fight().getDrops();
				for (BotDropReceived botDrop : drops) {
					String dropCode = botDrop.getCode();
					if (dropCode.equals(this.resourceCode)) {
						ResourceGoalAchiever.reserveItem(dropCode, reservedItems, 1);
						this.finish = true;
					}
				}
				return goalParameter.getHPRecoveryFactory().createHPRecovery().restoreHP(reservedItems);
			}
		}
		return false;
	}

	@Override
	public boolean isFinish() {
		return this.finish;
	}

	@Override
	public String getCode() {
		return this.resourceCode;
	}

	@Override
	public void clear() {
		this.finish = false;
	}

	@Override
	public void setRoot() {
		this.root = true;
	}

	@Override
	public void unsetRoot() {
		this.root = false;
	}

	@Override
	public double getRate() {
		return (1d / this.rate);
	}

	public int getMonsterLevel() {
		return monster.getLevel();
	}

	public String getMonsterCode() {
		return monster.getCode();
	}

	@Override
	public boolean acceptAndSetMultiplierCoefficient(int coefficient, Cumulator cumulator, int maxItem) {
		cumulator.addValue(1);
		return coefficient == 1 && cumulator.getValue() <= maxItem;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("resourceCode", resourceCode);
		builder.append("rate", rate);
		builder.append("root", root);
		builder.append("monster", monster);
		return builder.toString();
	}
}
