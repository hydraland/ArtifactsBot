package hydra.dao.simulate;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Collections;
import java.util.List;

import hydra.dao.MapDAO;
import hydra.model.BotBox;

public final class MapDAOSimulator implements MapDAO, Simulator<List<BotBox>> {

	private static final String GET_WORKSHOPS_BOX = "getWorkshopsBox";
	private static final String CLASS_NAME = "MapDAOSimulator";
	private List<BotBox> botBox;
	private final SimulatorListener simulatorListener;
	private final ByteArrayOutputStream memoryStream;

	public MapDAOSimulator(SimulatorListener simulatorListener) {
		this.simulatorListener = simulatorListener;
		memoryStream = new ByteArrayOutputStream();
	}

	@Override
	public List<BotBox> getResourcesBox() {
		simulatorListener.call(CLASS_NAME, "getResourcesBox", 0, false);
		return botBox.stream().filter(bb -> bb.getContent() != null && "resource".equals(bb.getContent().getType()))
				.toList();
	}

	@Override
	public List<BotBox> getMonstersBox() {
		simulatorListener.call(CLASS_NAME, "getMonstersBox", 0, false);
		return botBox.stream().filter(bb -> bb.getContent() != null && "monster".equals(bb.getContent().getType()))
				.toList();
	}

	@Override
	public List<BotBox> getTasksBox() {
		simulatorListener.call(CLASS_NAME, "getTasksBox", 0, false);
		return botBox.stream().filter(bb -> bb.getContent() != null && "tasks_master".equals(bb.getContent().getType()))
				.toList();
	}

	@Override
	public List<BotBox> getWorkshopsBox() {
		simulatorListener.call(CLASS_NAME, GET_WORKSHOPS_BOX, 0, false);
		return botBox.stream().filter(bb -> bb.getContent() != null && "workshop".equals(bb.getContent().getType()))
				.toList();
	}

	@Override
	public List<BotBox> getGrandExchangesBox() {
		simulatorListener.call(CLASS_NAME, GET_WORKSHOPS_BOX, 0, false);
		return botBox.stream()
				.filter(bb -> bb.getContent() != null && "grand_exchange".equals(bb.getContent().getType())).toList();
	}

	@Override
	public List<BotBox> getBanksBox() {
		simulatorListener.call(CLASS_NAME, GET_WORKSHOPS_BOX, 0, false);
		return botBox.stream().filter(bb -> bb.getContent() != null && "bank".equals(bb.getContent().getType()))
				.toList();
	}

	@Override
	public List<BotBox> getAllBox() {
		simulatorListener.call(CLASS_NAME, "getAllBox", 0, false);
		return botBox;
	}

	@Override
	public void load(boolean persistant) {
		botBox = Simulator.load(persistant, new File("MapDAOSimulator.xml"), memoryStream);
		if (botBox == null) {
			botBox = Collections.emptyList();
		}
	}

	@Override
	public void save(boolean persistant) {
		Simulator.save(persistant, new File("MapDAOSimulator.xml"), memoryStream, botBox);
	}

	@Override
	public void set(List<BotBox> value) {
		this.botBox = value;
	}
}
