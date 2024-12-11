package strategy.achiever;

public interface EventNotification {
	static final String MONSTER_EVENT_TYPE = "monster";
	static final String RESOURCE_EVENT_TYPE = "resource";
	boolean fireEvent(String type, String code);
}
