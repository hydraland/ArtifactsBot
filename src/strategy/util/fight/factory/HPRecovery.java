package strategy.util.fight.factory;

import java.util.Map;

public interface HPRecovery {

	boolean restoreHP(Map<String, Integer> reservedItems);
}
