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
	protected List<Coordinate> coordinates;
	private final CharacterDAO characterDAO;
	private final BotMonster monster;
	private boolean finish;
	private boolean root;
	private final MonsterEquipementService monsterEquipementService;
	protected final MapDAO mapDao;
	private final FightService fightService;
	private final MoveService moveService;
	private final CharacterService characterService;
	private final GoalParameter goalParameter;

	public ItemMonsterGoalAchiever(CharacterDAO characterDAO, MapDAO mapDao, String resourceCode, int rate,
			List<Coordinate> coordinates, BotMonster monster, MonsterEquipementService monsterEquipementService,
			FightService fightService, MoveService moveService, CharacterService characterService,
			GoalParameter goalParameter) {
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
		return fightService.optimizeEquipementsPossesed(monster, EMPTY_RESERVED_ITEMS, goalParameter.isUseUtilities(isEventMonster())).fightDetails()
				.win();
	}

	protected boolean isEventMonster() {
		return false;
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
		if (!goalParameter.getHPRecoveryFactory().createHPRecovery().restoreHP(reservedItems)) {
			return false;
		}
		if (!monsterEquipementService.equipBestEquipement(monster, reservedItems, goalParameter.isUseUtilities(isEventMonster()))) {
			return false;
		}

		if (moveService.moveTo(coordinates)) {
			FightResponse response = characterDAO.fight();
			if (response.ok() && response.fight().isWin()) {
				List<BotDropReceived> drops = response.fight().getDrops();
				for (BotDropReceived botDrop : drops) {
					String dropCode = botDrop.getCode();
					if (dropCode.equals(this.resourceCode)) {
						if (!root) {
							ResourceGoalAchiever.reserveItem(dropCode, reservedItems, 1);
						}
						this.finish = true;
					}
				}
				return goalParameter.getHPRecoveryFactory().createHPRecovery().restoreHP(reservedItems);
			}
			goalParameter.getHPRecoveryFactory().createHPRecovery().restoreHP(reservedItems);
		}
		return false;
	}

	@Override
	public final boolean isFinish() {
		return this.finish;
	}

	@Override
	public final String getCode() {
		return this.resourceCode;
	}

	@Override
	public final void clear() {
		this.finish = false;
	}

	@Override
	public final void setRoot() {
		this.root = true;
	}

	@Override
	public final void unsetRoot() {
		this.root = false;
	}

	@Override
	public final double getRate() {
		return (1d / this.rate);
	}

	public final int getMonsterLevel() {
		return monster.getLevel();
	}

	public final String getMonsterCode() {
		return monster.getCode();
	}

	@Override
	public final boolean acceptAndSetMultiplierCoefficient(int coefficient, Cumulator cumulator, int maxItem) {
		cumulator.addValue(1);
		return coefficient == 1 && cumulator.getValue() <= maxItem;
	}

	@Override
	public final String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("resourceCode", resourceCode);
		builder.append("rate", rate);
		builder.append("root", root);
		builder.append("monster", monster);
		return builder.toString();
	}
}
