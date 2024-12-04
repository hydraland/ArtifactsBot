package hydra.dao.simulate;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Collections;
import java.util.List;

import hydra.dao.ResourceDAO;
import hydra.model.BotResource;

public final class ResourceDAOSimulator implements ResourceDAO, Simulator<List<BotResource>> {

	private List<BotResource> botResources;
	private final SimulatorListener simulatorListener;
	private final ByteArrayOutputStream memoryStream;

	public ResourceDAOSimulator(SimulatorListener simulatorListener) {
		this.simulatorListener = simulatorListener;
		memoryStream = new ByteArrayOutputStream();
	}

	@Override
	public List<BotResource> getAllResources() {
		simulatorListener.call("ResourceDAOSimulator", "getAllResources", 0, false);
		return botResources;
	}
	
	@Override
	public void load(boolean persistant) {
		botResources = Simulator.load(persistant, new File("ResourceDAOSimulator.xml"), memoryStream);
		if (botResources == null) {
			botResources = Collections.emptyList();
		}
	}

	@Override
	public void save(boolean persistant) {
		Simulator.save(persistant, new File("ResourceDAOSimulator.xml"), memoryStream, botResources);
	}

	@Override
	public void set(List<BotResource> value) {
		botResources = value;
	}

	@Override
	public BotResource getResource(String code) {
		return botResources.stream().filter(br -> br.getCode().equals(code)).findFirst()
				.get();
	}
}
