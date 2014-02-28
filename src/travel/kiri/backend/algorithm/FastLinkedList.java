package travel.kiri.backend.algorithm;

import java.util.AbstractSequentialList;
import java.util.ListIterator;

/**
 * A custom linked list that provides O(1) {@link #addAll(java.util.Collection)}
 * performance, if the parameter is also a {@link FastLinkedList}.
 * FIXME implement this. Note coba dulu pakai extends {@link AbstractSequentialList}, kalau sulit
 * dilepas saja tidak apa2, refer ke dijsktra.h/EdgeList.
 * @author PascalAlfadian
 *
 * @param <E> The object type.
 */
public class FastLinkedList<E> extends AbstractSequentialList<E>{

	@Override
	public ListIterator<E> listIterator(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

}
