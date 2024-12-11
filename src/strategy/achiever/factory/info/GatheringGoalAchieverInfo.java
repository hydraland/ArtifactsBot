package strategy.achiever.factory.info;

import org.apache.commons.lang3.builder.ToStringBuilder;

import hydra.model.BotItemType;
import hydra.model.BotResourceSkill;
import strategy.achiever.factory.goals.ArtifactGoalAchiever;
import strategy.util.Bornes;

public class GatheringGoalAchieverInfo extends SimpleGoalAchieverInfo {

	private final BotResourceSkill resourceSkill;
	private final int level;
	private final String boxCode;

	public GatheringGoalAchieverInfo(String code, BotItemType type, BotResourceSkill resourceSkill, int level,
			String boxCode, ArtifactGoalAchiever goalAchiever) {
		super(code, type, goalAchiever);
		this.resourceSkill = resourceSkill;
		this.level = level;
		this.boxCode = boxCode;
	}

	@Override
	public boolean isGathering() {
		return true;
	}

	@Override
	public BotResourceSkill getBotResourceSkill() {
		return resourceSkill;
	}

	@Override
	public boolean isLevelInBorne(Bornes borne, INFO_TYPE infoType) {
		return INFO_TYPE.GATHERING.equals(infoType) && (level < borne.max() && level >= borne.min())
				|| (level == 1 && borne.max() == 1);
	}

	@Override
	public boolean isLevel(int skillLevel, INFO_TYPE infoType) {
		return INFO_TYPE.GATHERING.equals(infoType) && skillLevel == level;
	}

	@Override
	public int getLevel() {
		return level;
	}

	@Override
	public final boolean isMatchBoxCode(String aBoxCode) {
		return boxCode.equals(aBoxCode);
	}

	public final String getBoxCode() {
		return boxCode;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("code", code);
		builder.append("type", type);
		builder.append("level", level);
		builder.append("resourceSkill", resourceSkill);
		builder.append("boxCode", boxCode);
		builder.append("goalAchiever", goalAchiever);
		return builder.toString();
	}
}
