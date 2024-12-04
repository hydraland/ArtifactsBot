package util;

import java.util.ArrayList;
import java.util.List;

public final class ListenerAdapter<T> {
    private final List<EventListener<T>> listeners;
    
    public ListenerAdapter() {
    	listeners = new ArrayList<>();
	}

    public void addEventListener(EventListener<T> listener) {
        listeners.add(listener);
    }

    public void removeEventListener(EventListener<T> listener) {
        listeners.remove(listener);
    }

    public void fire(T item) {
        listeners.forEach(listener -> listener.actionPerformed(item));
    }
}