package strategy.achiever.factory.util;

public class Cumulator {

	private int value;
	private final int initValue;

	public Cumulator(int initValue) {
		this.initValue = initValue;
		this.value = 0;
	}

	public int getValue() {
		return value+initValue;
	}

	public void addValue(int value) {
		this.value += value;
	}
	
	public int getDiffValue() {
		return value;
	}

	@Override
	public String toString() {
		return "Cumulator [value=" + value + ", initValue=" + initValue + "]";
	}
}
