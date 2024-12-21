package strategy.achiever.factory.info;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

import hydra.model.BotCraftSkill;
import hydra.model.BotItemType;
import hydra.model.BotResourceSkill;
import strategy.util.Bornes;

public class MultiGoalAchieverInfo<T> extends SimpleGoalAchieverInfo<T> {

	private static final String EXCEPTION_MESSAGE = "Value  %s not authorize";
	private boolean needTaskMasterResource;
	private boolean needRareResource;
	private final List<Integer> craftLevels;
	private final List<Integer> gatheringLevels;
	private BotCraftSkill botCraftSkill;
	private BotResourceSkill botResourceSkill;
	private final List<String> boxCodes;

	public MultiGoalAchieverInfo(String code, BotItemType type, T goalAchiever) {
		super(code, type, goalAchiever);
		needTaskMasterResource = false;
		needRareResource = false;
		craftLevels = new ArrayList<>();
		gatheringLevels = new ArrayList<>();
		boxCodes = new ArrayList<>();
		botCraftSkill = null;
		botResourceSkill = null;
	}

	@Override
	public boolean isNeedTaskMasterResource() {
		return needTaskMasterResource;
	}

	public void setNeedTaskMasterResource(boolean needTaskMasterResource) {
		this.needTaskMasterResource = needTaskMasterResource;
	}

	public void setNeedRareResource(boolean containtsRareResource) {
		this.needRareResource = containtsRareResource;
	}

	@Override
	public final boolean isNeedRareResource() {
		return needRareResource;
	}

	@Override
	public boolean isCraft() {
		return botCraftSkill != null;
	}

	public void setCraft(BotCraftSkill botCraftSkill) {
		this.botCraftSkill = botCraftSkill;
	}

	@Override
	public boolean isGathering() {
		return botResourceSkill != null;
	}

	public void setGathering(BotResourceSkill botResourceSkill, String boxCode) {
		this.botResourceSkill = botResourceSkill;
		boxCodes.add(boxCode);
	}

	public void addLevel(int level, INFO_TYPE infoType) {
		var levelList = switch (infoType) {
		case CRAFTING -> craftLevels;
		case GATHERING -> gatheringLevels;
		default -> throw new IllegalArgumentException(String.format(EXCEPTION_MESSAGE, infoType));
		};
		levelList.add(level);
	}

	@Override
	public BotCraftSkill getBotCraftSkill() {
		return botCraftSkill;
	}

	@Override
	public BotResourceSkill getBotResourceSkill() {
		return botResourceSkill;
	}

	@Override
	public boolean isLevelInBorne(Bornes borne, INFO_TYPE infoType) {
		var levelList = switch (infoType) {
		case CRAFTING -> craftLevels;
		case GATHERING -> gatheringLevels;
		default -> throw new IllegalArgumentException(String.format(EXCEPTION_MESSAGE, infoType));
		};
		var result = levelList.stream()
				.<Boolean>map(
						level -> (level < borne.max() && level >= borne.min()) || (level == 1 && borne.max() == 1))
				.reduce((a, b) -> a || b);
		return result.isPresent() && result.get();
	}

	@Override
	public int getLevel() {
		return 0;
	}

	@Override
	public boolean isLevel(int skillLevel, INFO_TYPE infoType) {
		switch (infoType) {
		case CRAFTING:
			return craftLevels.contains(skillLevel);
		case GATHERING:
			return gatheringLevels.contains(skillLevel);
		default:
			throw new IllegalArgumentException(String.format(EXCEPTION_MESSAGE, infoType));
		}
	}

	@Override
	public final boolean isMatchBoxCode(String aBoxCode) {
		return boxCodes.contains(aBoxCode);
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("code", code);
		builder.append("type", type);
		builder.append("needTaskMasterResource", needTaskMasterResource);
		builder.append("needRareResource", needRareResource);
		builder.append("botCraftSkill", botCraftSkill);
		builder.append("botResourceSkill", botResourceSkill);
		builder.append("craftLevels", craftLevels);
		builder.append("gatheringLevels", gatheringLevels);
		builder.append("goalAchiever", goalAchiever);
		return builder.toString();
	}
}
