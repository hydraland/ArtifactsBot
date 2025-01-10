package strategy.util.fight;

public final record FightDetails(boolean win, int nbTurn, int characterTurn, int characterLossHP, int restoreTurn, int characterDmg) {
}