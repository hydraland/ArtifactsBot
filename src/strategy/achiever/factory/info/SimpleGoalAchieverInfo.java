package strategy.achiever.factory.info;

import org.apache.commons.lang3.builder.ToStringBuilder;

import hydra.model.BotCraftSkill;
import hydra.model.BotItemType;
import hydra.model.BotResourceSkill;
import strategy.achiever.factory.goals.ArtifactGoalAchiever;
import strategy.util.Bornes;

public class SimpleGoalAchieverInfo implements GoalAchieverInfo {

	protected final String code;
	protected final BotItemType type;
	protected final ArtifactGoalAchiever goalAchiever;

	public SimpleGoalAchieverInfo(String code, BotItemType type, ArtifactGoalAchiever goalAchiever) {
		this.code = code;
		this.type = type;
		this.goalAchiever = goalAchiever;
	}

	@Override
	public boolean isNeedTaskMasterResource() {
		return false;
	}

	@Override
	public final String getItemCode() {
		return code;
	}

	@Override
	public final BotItemType getItemType() {
		return type;
	}

	@Override
	public boolean isCraft() {
		return false;
	}

	@Override
	public boolean isGathering() {
		return false;
	}

	@Override
	public BotCraftSkill getBotCraftSkill() {
		return null;
	}

	@Override
	public BotResourceSkill getBotResourceSkill() {
		return null;
	}

	@Override
	public boolean isLevelInBorne(Bornes borne, INFO_TYPE infoType) {
		return false;
	}

	@Override
	public boolean isLevel(int skillLevel, INFO_TYPE infoType) {
		return false;
	}

	@Override
	public int getLevel() {
		return 0;
	}

	@Override
	public boolean isNeedRareResource() {
		return false;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("code", code);
		builder.append("type", type);
		builder.append("goalAchiever", goalAchiever);
		return builder.toString();
	}

	@Override
	public final ArtifactGoalAchiever getGoal() {
		return goalAchiever;
	}
}
