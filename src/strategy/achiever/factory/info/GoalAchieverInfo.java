package strategy.achiever.factory.info;

import hydra.model.BotCraftSkill;
import hydra.model.BotItemType;
import hydra.model.BotResourceSkill;
import strategy.achiever.factory.goals.ArtifactGoalAchiever;
import strategy.util.Bornes;

public interface GoalAchieverInfo {
	enum INFO_TYPE {GATHERING, CRAFTING}
	public boolean isNeedTaskMasterResource();
	public boolean isNeedRareResource();
	public String getItemCode();
	public BotItemType getItemType();
	boolean isCraft();
	boolean isGathering();
	BotCraftSkill getBotCraftSkill();
	BotResourceSkill getBotResourceSkill();
	boolean isLevelInBorne(Bornes borne, INFO_TYPE infoType);
	boolean isLevel(int skillLevel, INFO_TYPE infoType);
	int getLevel();
	default boolean isMatchBoxCode(String boxCode) {
		return false;
	}
	public ArtifactGoalAchiever getGoal();
}
