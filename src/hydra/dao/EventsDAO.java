package hydra.dao;

import java.util.List;

import hydra.model.BotActiveEvent;
import hydra.model.BotEvent;

public interface EventsDAO {
	List<BotActiveEvent> getActiveEvents();

	List<BotEvent> getAllEvents();
}
