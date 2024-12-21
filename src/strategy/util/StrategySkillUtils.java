package strategy.util;

import java.util.List;
import java.util.function.Predicate;

import hydra.dao.ItemDAO;
import hydra.model.BotCraftSkill;
import hydra.model.BotItemType;
import hydra.model.BotResourceSkill;
import strategy.achiever.factory.goals.ArtifactGoalAchiever;
import strategy.achiever.factory.info.GoalAchieverInfo;
import strategy.achiever.factory.info.GoalAchieverInfo.INFO_TYPE;

public interface StrategySkillUtils {

	public static Bornes getBorneLevel(int skillLevel, int[] levelList) {
		for (int i = 0; i < levelList.length; i++) {
			if (skillLevel >= levelList[i] && skillLevel < levelList[i + 1]) {
				return new Bornes(i > 0 ? levelList[i - 1] : levelList[0], levelList[i], levelList[i + 1]);
			}
		}
		return new Bornes(levelList[0], levelList[0], levelList[levelList.length - 1]);
	}

	public static List<String> getEquipements(ItemDAO itemDao, BotItemType itemType) {
		return itemDao.getItems(itemType).stream().map(item -> item.getCode()).toList();
	}

	public static int getMinSkillIndex(int[] skillLevel) {
		int minLevel = Integer.MAX_VALUE;
		int searchIndex = -1;
		for (int j = 0; j < skillLevel.length; j++) {
			if (skillLevel[j] < minLevel) {
				minLevel = skillLevel[j];
				searchIndex = j;
			}
		}
		return searchIndex;
	}

	public static Predicate<GoalAchieverInfo<ArtifactGoalAchiever>> createFilterCraftPredicate(BotCraftSkill skill, Bornes borne) {
		return p -> p.isCraft() && skill.equals(p.getBotCraftSkill()) && p.isLevelInBorne(borne, INFO_TYPE.CRAFTING);
	}

	public static Predicate<GoalAchieverInfo<ArtifactGoalAchiever>> createFilterResourcePredicate(Bornes borne,
			BotResourceSkill resourceSkill) {
		return p -> p.isGathering() && resourceSkill.equals(p.getBotResourceSkill())
				&& p.isLevelInBorne(borne, INFO_TYPE.GATHERING);
	}

	public static Predicate<GoalAchieverInfo<ArtifactGoalAchiever>> createFilterResourceAndCraftPredicate(Bornes borne,
			BotResourceSkill resourceSkill, BotCraftSkill craftSkill) {
		return p -> (p.isGathering() && resourceSkill.equals(p.getBotResourceSkill())
				&& p.isLevelInBorne(borne, INFO_TYPE.GATHERING))
				|| (p.isCraft() && craftSkill.equals(p.getBotCraftSkill())
						&& p.isLevelInBorne(borne, INFO_TYPE.CRAFTING));
	}
}
