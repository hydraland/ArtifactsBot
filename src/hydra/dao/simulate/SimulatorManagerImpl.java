package hydra.dao.simulate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import hydra.dao.BankDAO;
import hydra.dao.CharacterDAO;
import hydra.dao.EventsDAO;
import hydra.dao.GrandExchangeDAO;
import hydra.dao.ItemDAO;
import hydra.dao.MapDAO;
import hydra.dao.MonsterDAO;
import hydra.dao.ResourceDAO;
import hydra.dao.TaskDAO;
import hydra.model.BotCharacter;
import hydra.model.BotItem;
import strategy.achiever.GoalParameter;
import strategy.achiever.factory.ArtifactGoalFactory;
import strategy.achiever.factory.GoalFactory;
import strategy.achiever.factory.MonsterEquipementService;
import strategy.achiever.factory.MonsterEquipementServiceImpl;
import strategy.achiever.factory.util.GameService;
import strategy.achiever.factory.util.GameServiceImpl;
import strategy.util.CharacterService;
import strategy.util.MoveService;
import strategy.util.MoveServiceImpl;
import strategy.util.fight.FightService;
import strategy.util.fight.FightServiceImpl;

public final class SimulatorManagerImpl implements SimulatorManager {

	private final CharacterDAOSimulator characterDAOSimulator;
	private final EventsDAOSimulator eventsDAOSimulator;
	private final GrandExchangeDAOSimulator grandExchangeDAOSimulator;
	private final ItemDAOSimulator itemDAOSimulator;
	private final MonsterDAOSimulator monsterDAOSimulator;
	private final ResourceDAOSimulator resourceDAOSimulator;
	private final TaskDAOSimulator taskDAOSimulator;
	private final BankDAOSimulator bankDAOSimulator;
	private final MapDAOSimulator mapDAOSimulator;
	private MoveService moveService;
	private GameService gameService;
	private FightService fightService;

	public SimulatorManagerImpl(SimulatorListener simulatorListener, ActiveEventsSimulator activeEventsSimulator) {
		FilteredInnerCallSimulatorListener filteredInnerCallSimulatorListener = new FilteredInnerCallSimulatorListener(
				simulatorListener);
		eventsDAOSimulator = new EventsDAOSimulator(filteredInnerCallSimulatorListener, activeEventsSimulator);
		monsterDAOSimulator = new MonsterDAOSimulator(filteredInnerCallSimulatorListener);
		resourceDAOSimulator = new ResourceDAOSimulator(filteredInnerCallSimulatorListener);
		mapDAOSimulator = new MapDAOSimulator(filteredInnerCallSimulatorListener);
		itemDAOSimulator = new ItemDAOSimulator(filteredInnerCallSimulatorListener);
		characterDAOSimulator = new CharacterDAOSimulator(filteredInnerCallSimulatorListener, itemDAOSimulator,
				mapDAOSimulator, monsterDAOSimulator, resourceDAOSimulator);
		taskDAOSimulator = new TaskDAOSimulator(filteredInnerCallSimulatorListener, characterDAOSimulator,
				mapDAOSimulator);
		grandExchangeDAOSimulator = new GrandExchangeDAOSimulator(filteredInnerCallSimulatorListener,
				characterDAOSimulator);
		bankDAOSimulator = new BankDAOSimulator(filteredInnerCallSimulatorListener, characterDAOSimulator);
	}

	@Override
	public void load(boolean persistant) {
		eventsDAOSimulator.load(persistant);
		monsterDAOSimulator.load(persistant);
		resourceDAOSimulator.load(persistant);
		mapDAOSimulator.load(persistant);
		itemDAOSimulator.load(persistant);
		characterDAOSimulator.load(persistant);
		taskDAOSimulator.load(persistant);
		grandExchangeDAOSimulator.load(persistant);
		bankDAOSimulator.load(persistant);
	}

	@Override
	public void save(boolean persistant) {
		characterDAOSimulator.save(persistant);
		eventsDAOSimulator.save(persistant);
		grandExchangeDAOSimulator.save(persistant);
		itemDAOSimulator.save(persistant);
		monsterDAOSimulator.save(persistant);
		resourceDAOSimulator.save(persistant);
		taskDAOSimulator.save(persistant);
		mapDAOSimulator.save(persistant);
		bankDAOSimulator.save(persistant);
	}

	@Override
	public void init(BankDAO bankDAO, BotCharacter botCharacter, EventsDAO eventsDAO, ItemDAO itemDAO, MapDAO mapDAO,
			MonsterDAO monsterDAO, ResourceDAO resourceDAO, TaskDAO taskDAO, boolean sellPossible,
			Map<String, Integer> estimateItemPrice) {
		characterDAOSimulator.set(botCharacter);
		eventsDAOSimulator.set(new ArrayList<>(eventsDAO.getAllEvents()));
		grandExchangeDAOSimulator.set(new GrandExchangeStruct(sellPossible, estimateItemPrice));
		itemDAOSimulator.set(new ArrayList<>(itemDAO.getItems()));
		monsterDAOSimulator.set(new ArrayList<>(monsterDAO.getMonsters()));
		resourceDAOSimulator.set(new ArrayList<>(resourceDAO.getAllResources()));
		taskDAOSimulator.set(
				new TaskStruct(new ArrayList<>(taskDAO.getAllTask()), new ArrayList<>(taskDAO.getAllTaskReward())));
		mapDAOSimulator.set(new ArrayList<>(mapDAO.getAllBox()));
		bankDAOSimulator.set(new BankStruct(bankDAO.getBankDetail(), bankItemsToMap(bankDAO.viewItems())));
	}

	@Override
	public GoalFactory createFactory(GoalParameter goalParameter) {
		moveService = new MoveServiceImpl(characterDAOSimulator, mapDAOSimulator);
		gameService = new GameServiceImpl(itemDAOSimulator);
		fightService = new FightServiceImpl(characterDAOSimulator, bankDAOSimulator, itemDAOSimulator,
				characterDAOSimulator.getCharacterService(), moveService, gameService);
		MonsterEquipementService monsterEquipementService = new MonsterEquipementServiceImpl(fightService);
		characterDAOSimulator.addEquipmentChangeListener(monsterEquipementService);
		return new ArtifactGoalFactory(resourceDAOSimulator, monsterDAOSimulator, mapDAOSimulator, itemDAOSimulator,
				characterDAOSimulator, grandExchangeDAOSimulator, bankDAOSimulator, taskDAOSimulator, goalParameter,
				gameService, characterDAOSimulator.getCharacterService(), moveService, fightService,
				monsterEquipementService);
	}

	@Override
	public void setValue(BotCharacter botCharacter, List<BotItem> bankItems) {
		characterDAOSimulator.set(botCharacter);
		characterDAOSimulator.save(false);
		characterDAOSimulator.load(false);
		BankStruct bankStruc = new BankStruct(bankDAOSimulator.getBankDetail(), bankItemsToMap(bankItems));
		bankDAOSimulator.set(bankStruc);
		bankDAOSimulator.save(false);
		bankDAOSimulator.load(false);
	}

	@Override
	public final CharacterDAO getCharacterDAOSimulator() {
		return characterDAOSimulator;
	}

	@Override
	public final EventsDAO getEventsDAOSimulator() {
		return eventsDAOSimulator;
	}

	@Override
	public final GrandExchangeDAO getGrandExchangeDAOSimulator() {
		return grandExchangeDAOSimulator;
	}

	@Override
	public final ItemDAO getItemDAOSimulator() {
		return itemDAOSimulator;
	}

	@Override
	public final MonsterDAO getMonsterDAOSimulator() {
		return monsterDAOSimulator;
	}

	@Override
	public final ResourceDAO getResourceDAOSimulator() {
		return resourceDAOSimulator;
	}

	@Override
	public final TaskDAO getTaskDAOSimulator() {
		return taskDAOSimulator;
	}

	@Override
	public final BankDAO getBankDAOSimulator() {
		return bankDAOSimulator;
	}

	@Override
	public final MapDAO getMapDAOSimulator() {
		return mapDAOSimulator;
	}

	@Override
	public final CharacterService getCharacterServiceSimulator() {
		return characterDAOSimulator.getCharacterService();
	}

	public final FightService getFightService() {
		return fightService;
	}

	public final MoveService getMoveService() {
		return moveService;
	}

	public final GameService getGameService() {
		return gameService;
	}

	private HashMap<String, Integer> bankItemsToMap(List<BotItem> bankItems) {
		return new HashMap<>(bankItems.stream().collect(Collectors.toMap(BotItem::getCode, BotItem::getQuantity)));
	}
}
