package hydra.dao.simulate;

import java.util.List;
import java.util.Map;

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
import strategy.achiever.factory.GoalFactory;
import strategy.achiever.factory.util.GameService;
import strategy.util.CharacterService;
import strategy.util.MoveService;
import strategy.util.fight.FightService;

public interface SimulatorManager {

	void init(BankDAO bankDAO, BotCharacter botCharacter, EventsDAO eventsDAO, ItemDAO itemDAO, MapDAO mapDAO,
			MonsterDAO monsterDAO, ResourceDAO resourceDAO, TaskDAO taskDAO, boolean sellPossible,
			Map<String, Integer> estimateItemPrice);

	void load(boolean persistant);

	void save(boolean persistant);

	void setValue(BotCharacter botCharacter, List<BotItem> bankItems);

	GoalFactory createFactory(GoalParameter goalParameter);

	CharacterDAO getCharacterDAOSimulator();

	EventsDAO getEventsDAOSimulator();

	GrandExchangeDAO getGrandExchangeDAOSimulator();

	ItemDAO getItemDAOSimulator();

	MonsterDAO getMonsterDAOSimulator();

	ResourceDAO getResourceDAOSimulator();

	TaskDAO getTaskDAOSimulator();

	BankDAO getBankDAOSimulator();

	MapDAO getMapDAOSimulator();

	CharacterService getCharacterServiceSimulator();

	FightService getFightService();

	MoveService getMoveService();

	GameService getGameService();
}
