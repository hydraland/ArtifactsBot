package strategy.util.fight;

import java.util.Map;

public interface HPRecovery {

	boolean restoreHP(Map<String, Integer> reservedItems);

}
