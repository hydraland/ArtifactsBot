package strategy.util.fight;

public final record FightDetails(boolean win, int nbTurn, int characterTurn, int characterLossHP, int restoreTurn,
		int characterDmg, int monsterDmg) {
	public int diffPower() {
		return characterDmg - monsterDmg;
	}
}