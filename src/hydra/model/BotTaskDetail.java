package hydra.model;

import java.io.Serializable;

public class BotTaskDetail implements Serializable {
	private static final long serialVersionUID = 1L;
	private String code;
	private int level;
	private BotTaskType type;
	private int minQuantity;
	private int maxQuantity;
	private String skill;
	private BotRewards rewards;

	public final String getCode() {
		return code;
	}

	public final void setCode(String code) {
		this.code = code;
	}

	public final int getLevel() {
		return level;
	}

	public final void setLevel(int level) {
		this.level = level;
	}

	public final BotTaskType getType() {
		return type;
	}

	public final void setType(BotTaskType type) {
		this.type = type;
	}

	public final int getMinQuantity() {
		return minQuantity;
	}

	public final void setMinQuantity(int minQuantity) {
		this.minQuantity = minQuantity;
	}

	public final int getMaxQuantity() {
		return maxQuantity;
	}

	public final void setMaxQuantity(int maxQuantity) {
		this.maxQuantity = maxQuantity;
	}

	public final String getSkill() {
		return skill;
	}

	public final void setSkill(String skill) {
		this.skill = skill;
	}

	public final BotRewards getRewards() {
		return rewards;
	}

	public final void setRewards(BotRewards rewards) {
		this.rewards = rewards;
	}
}
