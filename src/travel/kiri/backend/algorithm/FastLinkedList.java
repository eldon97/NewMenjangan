package travel.kiri.backend.algorithm;

import java.util.AbstractSequentialList;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import com.sun.org.apache.bcel.internal.generic.FADD;

/**
 * A custom linked list that provides O(1) {@link #addAll(java.util.Collection)}
 * performance, if the parameter is also a {@link FastLinkedList}.
 * FIXME implement this. Note coba dulu pakai extends {@link AbstractSequentialList}, kalau sulit
 * dilepas saja tidak apa2, refer ke dijsktra.h/EdgeList.
 * @author PascalAlfadian
 *
 * @param <E> The object type.
 * 
 * FIXME sementara pakai linked list punya java dulu, nanti ganti jadi AbstractSequentialList<E>
 */
public class FastLinkedList<E> extends AbstractSequentialList<E>{

	private class Node {
		/** The value that is contained in this list node. */
		E info;
		/** The next node in the list. */
		Node next;
	}
	
	private class FastLinkedListIterator implements ListIterator<E> {

		Node currentNode;
		int currentIndex;
		
		public FastLinkedListIterator() {
			currentNode = null;
			currentIndex = -1;
		}
		
		public FastLinkedListIterator(int index) throws IndexOutOfBoundsException {
			this();
			if (index < 0) {
				throw new IndexOutOfBoundsException();
			}
			while (index-- > 0) {
				if (hasNext()) {
					next();
				} else {
					throw new IndexOutOfBoundsException();
				}
			}
		}
		
		@Override
		public boolean hasNext() {
			return head != null && currentNode != tail;
		}

		@Override
		public E next() throws NoSuchElementException {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			if (currentNode == null) {
				currentNode = head;
			} else {
				currentNode = currentNode.next;
			}
			currentIndex++;
			return currentNode.info;
		}

		@Override
		public boolean hasPrevious() {
			throw new UnsupportedOperationException("This is slow, not implemented!");
		}

		@Override
		public E previous() throws NoSuchElementException {
			throw new UnsupportedOperationException("This is slow, not implemented!");
		}

		@Override
		public int nextIndex() {
			return currentIndex + 1;
		}

		@Override
		public int previousIndex() {
			return currentIndex - 1;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void set(E e) {
			currentNode.info = e;
		}

		@Override
		public void add(E e) {
			throw new UnsupportedOperationException();
		}
		
	}
	
	private Node head = null;
	private Node tail = null;
	private int size = 0;
	
	/**
	 * Insert first operation to the linked list. TODO refactor to addFirst.
	 * @param e the node element to insert
	 */
	public void push(E e)
	{
		if (head == null) {
			head = new Node();
			head.info = e;
			head.next = null;
			tail = head;
		} else {
			Node newNode = new Node();
			newNode.info = e;
			newNode.next = head;
			head = newNode;
		}
		size++;
	}
	
	@Override
	public ListIterator<E> listIterator(int index) throws IndexOutOfBoundsException {
		return new FastLinkedListIterator(index);
	}

	@Override
	public int size() {
		return size;
	}
	
	@Override
	public void clear() {
		head = tail = null;
	}
	
	public void addAll(FastLinkedList<E> other) {
		if (head == null) {
			this.head = other.head;
			this.tail = other.tail;
		} else {
			tail.next = other.head;
		}
		size += other.size;
	}
}
