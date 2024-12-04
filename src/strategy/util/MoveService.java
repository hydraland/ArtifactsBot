package strategy.util;

import java.util.List;

import hydra.model.BotCraftSkill;
import strategy.achiever.factory.util.Coordinate;

public interface MoveService {
	boolean moveToBank();

	boolean moveToGrandEchange();

	boolean moveTo(BotCraftSkill craftSkill);

	boolean moveTo(List<Coordinate> coordinates);

	static int calculManhattanDistance(int x, int y, int x1, int y1) {
		return Math.abs(x - x1) + Math.abs(y - y1);
	}
}
