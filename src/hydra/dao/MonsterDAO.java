package hydra.dao;

import java.util.List;

import hydra.model.BotMonster;

public interface MonsterDAO {

	BotMonster getMonster(String code);

	List<BotMonster> getMonsters();

}