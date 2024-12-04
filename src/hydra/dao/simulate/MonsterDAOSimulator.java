package hydra.dao.simulate;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Collections;
import java.util.List;

import hydra.dao.MonsterDAO;
import hydra.model.BotMonster;

public final class MonsterDAOSimulator implements MonsterDAO, Simulator<List<BotMonster>> {

	private List<BotMonster> botMonsters;
	private final SimulatorListener simulatorListener;
	private final ByteArrayOutputStream memoryStream;

	public MonsterDAOSimulator(SimulatorListener simulatorListener) {
		this.simulatorListener = simulatorListener;
		memoryStream = new ByteArrayOutputStream();
	}

	@Override
	public BotMonster getMonster(String code) {
		simulatorListener.call("MonsterDAOSimulator", "getMonster", 0, false);
		return botMonsters.stream().filter(bm -> code.equals(bm.getCode())).findFirst().get();
	}

	@Override
	public List<BotMonster> getMonsters() {
		simulatorListener.call("MonsterDAOSimulator", "getMonsters", 0, false);
		return botMonsters;
	}
	
	@Override
	public void load(boolean persistant) {
		botMonsters = Simulator.load(persistant, new File("MonsterDAOSimulator.xml"), memoryStream);
		if (botMonsters == null) {
			botMonsters = Collections.emptyList();
		}
	}

	@Override
	public void save(boolean persistant) {
		Simulator.save(persistant, new File("MonsterDAOSimulator.xml"), memoryStream, botMonsters);
	}

	@Override
	public void set(List<BotMonster> value) {
		botMonsters = value;
	}
}
