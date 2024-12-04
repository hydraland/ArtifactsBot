package hydra.model;

import java.io.Serializable;
import java.util.List;

public class BotResource implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
	private String code;
	private BotResourceSkill skill;
	private int level;
	private List<BotDropDescription> drops;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public BotResourceSkill getSkill() {
		return skill;
	}
	public void setSkill(BotResourceSkill skill) {
		this.skill = skill;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public List<BotDropDescription> getDrops() {
		return drops;
	}
	public void setDrops(List<BotDropDescription> drops) {
		this.drops = drops;
	}
	@Override
	public String toString() {
		return "BotResource [name=" + name + ", code=" + code + ", skill=" + skill + ", level=" + level + ", drops="
				+ drops + "]";
	}
}
