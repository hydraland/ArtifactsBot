package strategy.util;

import java.util.Arrays;

import strategy.util.fight.FightDetails;

public final record OptimizeResult(FightDetails fightDetails, BotItemInfo[] bestEqt) {
	public static final int UTILITY1_INDEX = 0;
	public static final int UTILITY2_INDEX = 1;
	public static final int WEAPON_INDEX = 2;
	public static final int BODY_ARMOR_INDEX = 3;
	public static final int BOOTS_INDEX = 4;
	public static final int HELMET_INDEX = 5;
	public static final int SHIELD_INDEX = 6;
	public static final int LEG_ARMOR_INDEX = 7;
	public static final int AMULET_INDEX = 8;
	public static final int RING1_INDEX = 9;
	public static final int RING2_INDEX = 10;
	public static final int ARTIFACT1_INDEX = 11;
	public static final int ARTIFACT2_INDEX = 12;
	public static final int ARTIFACT3_INDEX = 13;

	@Override
	public String toString() {
		return "OptimizeResult [eval=" + fightDetails + ", bestEqt=" + Arrays.toString(bestEqt) + "]";
	}
}
