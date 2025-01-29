package hydra.model;

import java.io.Serializable;
import java.util.List;

public final class BotItemDetails implements Serializable {
	private static final long serialVersionUID = 1L;
	private BotItemType type;
	private BotCraft craft;
	private String subtype;
	private String code;
	private int level;
	private boolean tradeable;
	private List<BotItemEffect> effects;

	public BotItemType getType() {
		return type;
	}

	public void setType(BotItemType type) {
		this.type = type;
	}

	public BotCraft getCraft() {
		return craft;
	}

	public void setCraft(BotCraft craft) {
		this.craft = craft;
	}

	public String getSubtype() {
		return subtype;
	}

	public void setSubtype(String subType) {
		this.subtype = subType;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public List<BotItemEffect> getEffects() {
		return effects;
	}

	public void setEffects(List<BotItemEffect> effects) {
		this.effects = effects;
	}

	public final boolean isTradeable() {
		return tradeable;
	}

	public final void setTradeable(boolean tradeable) {
		this.tradeable = tradeable;
	}

	@Override
	public String toString() {
		return "BotItemDetails [type=" + type + ", subtype=" + subtype + ", code=" + code + ", level=" + level + "]";
	}
}
