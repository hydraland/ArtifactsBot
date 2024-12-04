package strategy.achiever.factory;

import java.util.List;

import strategy.achiever.GoalAchiever;
import strategy.achiever.factory.util.Coordinate;
import strategy.achiever.factory.util.Cumulator;
import strategy.util.MoveService;

public interface ArtifactGoalAchiever extends GoalAchiever {
	double getRate();

	boolean acceptAndSetMultiplierCoefficient(int coefficient, Cumulator cumulator, int maxItem);

	default boolean acceptAndSetMultiplierCoefficient() {
		return acceptAndSetMultiplierCoefficient(1);
	}

	default boolean acceptAndSetMultiplierCoefficient(int coefficient) {
		return acceptAndSetMultiplierCoefficient(coefficient, new Cumulator(0), Integer.MAX_VALUE);
	}

	static Coordinate searchClosestLocation(int x, int y, List<Coordinate> coordinates) {
		return coordinates.stream()
				.min((coord1, coord2) -> Integer.compare(
						MoveService.calculManhattanDistance(x, y, coord1.x(), coord1.y()),
						MoveService.calculManhattanDistance(x, y, coord2.x(), coord2.y())))
				.get();
	}
}
