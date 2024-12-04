package hydra.model;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class BotMonster implements Serializable {
	private static final long serialVersionUID = 1L;
	private int attackAir;
	private int attackEarth;
	private int attackFire;
	private int attackWater;
	private int resAir;
	private int resEarth;
	private int resFire;
	private int resWater;
	private int hp;
	private int level;
	private String code;
	private int minGold;
	private int maxGold;
	private List<BotDropDescription> drops;

	public int getAttackAir() {
		return attackAir;
	}

	public void setAttackAir(int attackAir) {
		this.attackAir = attackAir;
	}

	public int getAttackEarth() {
		return attackEarth;
	}

	public void setAttackEarth(int attackEarth) {
		this.attackEarth = attackEarth;
	}

	public int getAttackFire() {
		return attackFire;
	}

	public void setAttackFire(int attackFire) {
		this.attackFire = attackFire;
	}

	public int getAttackWater() {
		return attackWater;
	}

	public void setAttackWater(int attackWater) {
		this.attackWater = attackWater;
	}

	public int getResAir() {
		return resAir;
	}

	public void setResAir(int resAir) {
		this.resAir = resAir;
	}

	public int getResEarth() {
		return resEarth;
	}

	public void setResEarth(int resEarth) {
		this.resEarth = resEarth;
	}

	public int getResFire() {
		return resFire;
	}

	public void setResFire(int resFire) {
		this.resFire = resFire;
	}

	public int getResWater() {
		return resWater;
	}

	public void setResWater(int resWater) {
		this.resWater = resWater;
	}

	public int getHp() {
		return hp;
	}

	public void setHp(int hp) {
		this.hp = hp;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public final String getCode() {
		return code;
	}

	public void setDrops(List<BotDropDescription> drops) {
		this.drops = drops;
	}

	public final List<BotDropDescription> getDrops() {
		return drops;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public final int getMinGold() {
		return minGold;
	}

	public final void setMinGold(int minGold) {
		this.minGold = minGold;
	}

	public final int getMaxGold() {
		return maxGold;
	}

	public final void setMaxGold(int maxGold) {
		this.maxGold = maxGold;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("code", code);
		return builder.toString();
	}
}
