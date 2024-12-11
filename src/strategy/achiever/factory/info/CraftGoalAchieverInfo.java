package strategy.achiever.factory.info;

import org.apache.commons.lang3.builder.ToStringBuilder;

import hydra.model.BotCraftSkill;
import hydra.model.BotItemType;
import strategy.achiever.factory.goals.ArtifactGoalAchiever;
import strategy.util.Bornes;

public class CraftGoalAchieverInfo extends SimpleGoalAchieverInfo {

	private final boolean needTaskMasterResource;
	private final BotCraftSkill craftSkill;
	private final int level;
	private final boolean needRareResource;

	public CraftGoalAchieverInfo(String code, BotItemType type, BotCraftSkill craftSkill, int level,
			boolean needTaskMasterResource, boolean neddRareResource, ArtifactGoalAchiever goalAchiever) {
		super(code, type, goalAchiever);
		this.craftSkill = craftSkill;
		this.level = level;
		this.needTaskMasterResource = needTaskMasterResource;
		this.needRareResource = neddRareResource;
	}

	@Override
	public boolean isNeedTaskMasterResource() {
		return needTaskMasterResource;
	}

	@Override
	public boolean isCraft() {
		return true;
	}

	@Override
	public BotCraftSkill getBotCraftSkill() {
		return craftSkill;
	}

	@Override
	public final boolean isNeedRareResource() {
		return needRareResource;
	}

	@Override
	public boolean isLevelInBorne(Bornes borne, INFO_TYPE infoType) {
		return INFO_TYPE.CRAFTING.equals(infoType) && (level < borne.max() && level >= borne.min())
				|| (level == 1 && borne.max() == 1);
	}

	@Override
	public boolean isLevel(int skillLevel, INFO_TYPE infoType) {
		return INFO_TYPE.CRAFTING.equals(infoType) && skillLevel == level;
	}

	@Override
	public int getLevel() {
		return level;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("level", level);
		builder.append("craftSkill", craftSkill);
		builder.append("needRareResource", needRareResource);
		builder.append("needTaskMasterResource", needTaskMasterResource);
		builder.append("goalAchiever", goalAchiever);
		return builder.toString();
	}
}
