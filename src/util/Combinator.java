package util;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;

public class Combinator<T> implements Iterator<T[]>, Iterable<T[]> {

	private Element<T>[] elements;
	private T[] currentCombinaison;
	private int currentNext;

	@SuppressWarnings("unchecked")
	public Combinator(Class<T> aClass, int nbList) {
		this.elements = (Element<T>[]) Array.newInstance(Element.class, nbList);
		this.currentCombinaison = (T[]) Array.newInstance(aClass, nbList);
		this.currentNext = nbList - 1;
	}

	public void set(int index, List<T> aList) {
		this.elements[index] = new Element<>(aList);
		this.currentCombinaison[index] = this.elements[index].reset(index == elements.length-1);
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

		private List<E> aList;
		private Iterator<E> innerIter;

		public Element(List<E> aList) {
			this.aList = aList;
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
			innerIter = aList.iterator();
			if(initOnly || aList.isEmpty()) {
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
