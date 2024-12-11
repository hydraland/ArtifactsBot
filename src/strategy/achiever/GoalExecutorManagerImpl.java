package strategy.achiever;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import hydra.dao.CharacterDAO;
import hydra.dao.EventsDAO;
import hydra.dao.util.CharacterCache;
import hydra.model.BotCharacter;
import strategy.Strategy;

public class GoalExecutorManagerImpl implements EventNotification, GoalExecutoManager {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	private final CharacterDAO characterDao;
	private final CharacterCache characterCache;
	private final EventWatcher eventWatcher;
	private final AtomicBoolean interruptAvalaible;
	private final Interruptor interruptor;
	private final Strategy strategy;

	public GoalExecutorManagerImpl(Strategy strategy, CharacterDAO characterDao, EventsDAO eventsDAO,
			CharacterCache characterCache, Interruptor interruptor) {
		this.strategy = strategy;
		this.characterDao = characterDao;
		this.characterCache = characterCache;
		this.interruptor = interruptor;
		this.eventWatcher = new EventWatcher(eventsDAO, this);
		interruptAvalaible = new AtomicBoolean(true);
		launchEventWatcher();
	}

	@Override
	public void execute() {
		interruptAvalaible.set(true);
		for (GoalAchiever goalArchieved : strategy.getGoalAchievers()) {
			LOGGER.info("Exécution du but");
			LOGGER.info(goalArchieved.toString());
			BotCharacter character = characterDao.getCharacter();
			if (goalArchieved.isRealisableAfterSetRoot(character)) {
				LOGGER.info("But réalisable");
				goalArchieved.clear();
				Map<String, Integer> reservedItems = new HashMap<>();
				boolean result = goalArchieved.execute(reservedItems);
				if (!result) {
					if (interruptor.isInterrupted()) {
						LOGGER.info("But interrompu");
						// On sort de la boucle
						break;
					} else {
						LOGGER.info("Echec du but");
						characterCache.reset();// On force le rechargement
					}
				} else {
					LOGGER.info("Succès du but");
				}
				manageInventory(strategy);
			}
		}
		if (interruptor.isInterrupted()) {
			interruptAvalaible.set(false);
			interruptor.reset();
			LOGGER.info("Exécution du Event Goal");
			GoalAchiever eventGoalArchieved = strategy.getEventGoalAchiever();
			LOGGER.info(eventGoalArchieved.toString());
			BotCharacter character = characterDao.getCharacter();
			if (eventGoalArchieved.isRealisableAfterSetRoot(character)) {
				LOGGER.info("Event Goal réalisable");
				eventGoalArchieved.clear();
				eventGoalArchieved.execute(new HashMap<>());
				characterCache.reset();
				manageInventory(strategy);
			}
		}
	}

	private void launchEventWatcher() {
		Thread.ofVirtual().start(eventWatcher);
	}

	private void manageInventory(Strategy strategy) {
		for (GoalAchiever customGoalCollector : strategy.getManagedInventoryCustomGoal()) {
			BotCharacter character = characterDao.getCharacter();
			if (customGoalCollector.isRealisableAfterSetRoot(character)) {
				LOGGER.info("Execute Custom Goal : " + customGoalCollector.toString());
				customGoalCollector.clear();
				boolean result = customGoalCollector.execute(new HashMap<>());
				if (!result) {
					LOGGER.info("Echec du but");
					characterCache.reset();// On force le rechargement
				} else {
					LOGGER.info("Succès du but");
				}
			}
		}
	}

	@Override
	public boolean fireEvent(String type, String code) {
		LOGGER.info("New Event " + type + "," + code);
		if (strategy.isAcceptEvent(type, code)) {
			if (interruptAvalaible.get()) {
				LOGGER.info("Prise en compte de l'Event");
				strategy.initializeGoal(type, code);
				interruptor.interrupt();
			} else {
				return false;
			}
		}
		return true;
	}
}
