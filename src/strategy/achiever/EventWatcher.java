package strategy.achiever;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import hydra.dao.EventsDAO;
import hydra.model.BotActiveEvent;
import hydra.model.BoxContent;
import util.Utility;

public final class EventWatcher implements Runnable {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	private final EventsDAO eventsDAO;
	private List<String> oldEventsName;
	private EventNotification eNotification;

	public EventWatcher(EventsDAO eventsDAO, EventNotification eNotification) {
		this.eventsDAO = eventsDAO;
		this.eNotification = eNotification;
		oldEventsName = new ArrayList<>();
	}

	@Override
	public void run() {
		LOGGER.info("Initialisation EventWatcher");
		while(true) {
			List<BotActiveEvent> events = eventsDAO.getActiveEvents();
			List<String> newCurrentEventsName = new ArrayList<>();
			for (BotActiveEvent botEvent : events) {
				if(!oldEventsName.contains(botEvent.getName())){
					//Nouvel évènement on notifie
					BoxContent content = botEvent.getMap().getContent();
					eNotification.fireEvent(content.getType(), content.getCode());
				}
				newCurrentEventsName.add(botEvent.getName());
			}
			oldEventsName = newCurrentEventsName;
			Utility.sleep(300);
		}
	}
}
