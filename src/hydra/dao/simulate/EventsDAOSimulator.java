package hydra.dao.simulate;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Collections;
import java.util.List;

import hydra.dao.EventsDAO;
import hydra.model.BotActiveEvent;
import hydra.model.BotEvent;

public final class EventsDAOSimulator implements EventsDAO, Simulator<List<BotEvent>> {

	private final SimulatorListener simulatorListener;
	private List<BotEvent> botEvents;
	private final ActiveEventsSimulator activeEventsSimulator;
	private final ByteArrayOutputStream memoryStream;

	public EventsDAOSimulator(SimulatorListener simulatorListener, ActiveEventsSimulator activeEventsSimulator) {
		this.simulatorListener = simulatorListener;
		this.activeEventsSimulator = activeEventsSimulator;
		memoryStream = new ByteArrayOutputStream();
	}

	@Override
	public List<BotActiveEvent> getActiveEvents() {
		simulatorListener.call("EventsDAOSimulator", "getActiveEvents", 0, false);
		return activeEventsSimulator.simulate(botEvents);
	}

	@Override
	public List<BotEvent> getAllEvents() {
		simulatorListener.call("EventsDAOSimulator", "getAllEvents", 0, false);
		return botEvents;
	}

	@Override
	public void load(boolean persistant) {
		botEvents = Simulator.load(persistant, new File("EventsDAOSimulator.xml"), memoryStream);
		if (botEvents == null) {
			botEvents = Collections.emptyList();
		}
	}

	@Override
	public void save(boolean persistant) {
		Simulator.save(persistant, new File("EventsDAOSimulator.xml"), memoryStream, botEvents);
	}

	@Override
	public void set(List<BotEvent> value) {
		this.botEvents = value;
	}

}
