package hydra.model;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class BotFight implements Serializable {
	private static final long serialVersionUID = 1L;

	private int xp;
	private int gold;
	private int turns;
	private String result;// TODO remplacer par un enum
	private List<BotDropReceived> drops;

	public int getXp() {
		return xp;
	}

	public void setXp(int xp) {
		this.xp = xp;
	}

	public int getGold() {
		return gold;
	}

	public void setGold(int gold) {
		this.gold = gold;
	}

	public int getTurns() {
		return turns;
	}

	public void setTurns(int turns) {
		this.turns = turns;
	}

	public List<BotDropReceived> getDrops() {
		return drops;
	}

	public void setDrops(List<BotDropReceived> drops) {
		this.drops = drops;
	}

	public boolean isWin() {
		return this.result.equals("win");
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("xp", xp);
		builder.append("gold", gold);
		builder.append("turns", turns);
		builder.append("result", result);
		builder.append("drops", drops);
		return builder.toString();
	}
}
