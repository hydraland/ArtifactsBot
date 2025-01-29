package strategy.achiever.factory.info;

import org.apache.commons.lang3.builder.ToStringBuilder;

import hydra.model.BotItemType;

public final class MonsterGoalAchieverInfo<T> extends SimpleGoalAchieverInfo<T> {

	private final String monsterCode;

	public MonsterGoalAchieverInfo(String code, BotItemType type, String monsterCode, T goalAchiever) {
		super(code, type, goalAchiever);
		this.monsterCode = monsterCode;
	}
	
	@Override
	public String getMonsterCode() {
		return monsterCode;
	}
	
	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("code", code);
		builder.append("monsterCode", monsterCode);
		builder.append("type", type);
		builder.append("goalAchiever", goalAchiever);
		return builder.toString();
	}
}
