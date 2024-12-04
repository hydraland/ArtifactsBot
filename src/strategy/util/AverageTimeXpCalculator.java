package strategy.util;

import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.builder.ToStringBuilder;

public final class AverageTimeXpCalculator {

	private final SortedSet<Double> averageXp;
	private final double initAverageValue;
	private final int maxSize;

	public AverageTimeXpCalculator(int maxSize) {
		this(Double.MAX_VALUE, maxSize);
	}

	public AverageTimeXpCalculator(double initialValue, int maxSize) {
		this.maxSize = maxSize;
		averageXp = new TreeSet<>();
		this.initAverageValue = initialValue;
	}

	public void add(int xp, long time) {
		if (xp >= 0) { // Lors du passage de niveau il est probablement négatif, donc on l'ignore
			averageXp.add((double) xp / time);
			if (averageXp.size() > maxSize) {
				averageXp.removeFirst();
				averageXp.removeLast();
			}
		}
	}

	public double getAverage() {
		return averageXp.isEmpty() ? initAverageValue
				: averageXp.stream().reduce(0d, (a, b) -> a + b) / averageXp.size();
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("initAverageValue", initAverageValue);
		builder.append("maxSize", maxSize);
		builder.append("averageXp", averageXp);
		return builder.toString();
	}
}
