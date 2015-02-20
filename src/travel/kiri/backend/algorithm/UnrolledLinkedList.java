package travel.kiri.backend.algorithm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * A custom linked list that:
 * <ul>
 *   <li>Provides O(1) {@link #addAll(FastLinkedList))}
 *       performance.</li>
 *   <li>Memory-efficient by using ArrayList whenever possible, to prevent
 *       usage of "next" references, where costly esp in 64-bit machine.</lo>
 * </ul>
 * @author PascalAlfadian
 *
 * @param <E> The object type.
 * 
 */
public class UnrolledLinkedList<E> implements Iterable<E> {

	/**
	 * ArrayList storage for the nodes.
	 */
	private ArrayList<E> internalArray;
	
	/**
	 * Reference to the next list, if available
	 */
	private UnrolledLinkedList<E> nextList;
	
	private class FastLinkedListIterator implements ListIterator<E> {
		UnrolledLinkedList<E> currentList;
		int currentIndex, globalIndex;

		public FastLinkedListIterator(UnrolledLinkedList<E> firstList) {
			currentList = firstList;
			currentIndex = -1;
			globalIndex = -1;
		}

		@Override
		public boolean hasNext() {
			// true, if there is still next element in array, or otherwise still have next in the linked list
			return currentIndex < currentList.internalArray.size() - 1
					|| (currentList.nextList != null && currentList.nextList.internalArray
							.size() > 0);
		}

		@Override
		public E next() throws NoSuchElementException {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}			
			globalIndex++;
			currentIndex++;
			if (currentIndex < currentList.internalArray.size()) {
				// do nothing, will return later;
			} else {
				currentIndex = 0;
				currentList = currentList.nextList;
			}
			return currentList.internalArray.get(currentIndex);
		}

		@Override
		public boolean hasPrevious() {
			throw new UnsupportedOperationException();
		}

		@Override
		public E previous() throws NoSuchElementException {
			throw new UnsupportedOperationException();
		}

		@Override
		public int nextIndex() {
			return globalIndex + 1;
		}

		@Override
		public int previousIndex() {
			return globalIndex - 1;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void set(E e) {
			currentList.internalArray.set(currentIndex, e);
		}

		@Override
		public void add(E e) {
			currentList.add(e);
		}
		
	}
	
	public UnrolledLinkedList() {
		this.internalArray = new ArrayList<E>();
		this.nextList = null;
	}

	/**
	 * Add an element to list. The add position is not defined.
	 * @param e the node element to add
	 */
	public void add(E e)
	{
		internalArray.add(e);
	}
	
	@Override
	public Iterator<E> iterator() {
		return new FastLinkedListIterator(this);
	}

	public void addAll(UnrolledLinkedList<E> elements) {
		UnrolledLinkedList<E> list = this;
		while (list.nextList != null) {
			list = list.nextList;
		}
		list.nextList = elements;
	}

	public int size() {
		int totalSize = 0;
		UnrolledLinkedList<E> list = this;
		while (list != null) {
			totalSize += internalArray.size();
			list = list.nextList;
		}
		return totalSize;
	}
	
	/**
	 * Cleans up unused memory.
	 */
	public void cleanUpMemory() {
		this.internalArray.trimToSize();
	}
}
