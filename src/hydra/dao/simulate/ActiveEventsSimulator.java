package hydra.dao.simulate;

import java.util.List;

import hydra.model.BotActiveEvent;
import hydra.model.BotEvent;

public interface ActiveEventsSimulator {

	List<BotActiveEvent> simulate(List<BotEvent> botEvents);

}
