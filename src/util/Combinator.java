package util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;

public final class Combinator<T> implements Iterator<T[]>, Iterable<T[]> {

	private Element<T>[] elements;
	private T[] currentCombinaison;
	private int currentNext;

	@SuppressWarnings("unchecked")
	public Combinator(Class<T> aClass, int nbList) {
		this.elements = (Element<T>[]) Array.newInstance(Element.class, nbList);
		this.currentCombinaison = (T[]) Array.newInstance(aClass, nbList);
		this.currentNext = nbList - 1;
	}

	public void set(int index, Collection<T> aCollection) {
		this.elements[index] = new Element<>(aCollection);
		this.currentCombinaison[index] = this.elements[index].reset(index == elements.length-1);
	}
	
	public int size(int index) {
		return this.elements[index].aCollection.size();
	}

	@Override
	public boolean hasNext() {
		currentNext = this.elements.length - 1;
		for (int i = elements.length - 1; i >= 0; i--) {
			if (this.elements[i].hasNext()) {
				return true;
			} else {
				this.currentCombinaison[i] = this.elements[i].reset(false);
				currentNext--;
			}
		}
		return false;
	}

	@Override
	public T[] next() {
		this.currentCombinaison[currentNext] = elements[currentNext].next();
		return this.currentCombinaison;
	}

	public final class Element<E> implements Iterator<E> {

		private Collection<E> aCollection;
		private Iterator<E> innerIter;

		public Element(Collection<E> aCollection) {
			this.aCollection = aCollection;
		}

		@Override
		public boolean hasNext() {
			return innerIter.hasNext();
		}

		@Override
		public E next() {
			return innerIter.next();
		}

		public E reset(boolean initOnly) {
			innerIter = aCollection.iterator();
			if(initOnly || aCollection.isEmpty()) {
				return null;
			}
			return innerIter.next();
		}
	}

	@Override
	public Iterator<T[]> iterator() {
		return this;
	}
}
