package hydra.dao;

import java.util.List;

import hydra.model.BotBox;

public interface MapDAO {

	List<BotBox> getResourcesBox();

	List<BotBox> getMonstersBox();

	List<BotBox> getTasksBox();

	List<BotBox> getWorkshopsBox();

	List<BotBox> getGrandExchangesBox();

	List<BotBox> getBanksBox();

	List<BotBox> getAllBox();

}