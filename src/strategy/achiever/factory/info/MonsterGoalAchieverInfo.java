package strategy.achiever.factory.info;

import hydra.model.BotItemType;

public class MonsterGoalAchieverInfo<T> extends SimpleGoalAchieverInfo<T> {

	private final String monsterCode;

	public MonsterGoalAchieverInfo(String code, BotItemType type, String monsterCode, T goalAchiever) {
		super(code, type, goalAchiever);
		this.monsterCode = monsterCode;
	}
	
	@Override
	public String getMonsterCode() {
		return monsterCode;
	}
}
