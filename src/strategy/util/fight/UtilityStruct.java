package strategy.util.fight;

final record UtilityStruct(double eval, boolean utilityUsed) {
	private static final double UTILITY_SEUIL = 1.5d;

	public boolean utilityUsed() {
		return (utilityUsed && eval <= UTILITY_SEUIL) || eval <= 1;
	}
}