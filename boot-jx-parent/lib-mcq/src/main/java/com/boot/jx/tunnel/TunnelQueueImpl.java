package com.boot.jx.tunnel;

import java.util.Collection;
import java.util.Iterator;

import org.redisson.api.RQueue;

import com.boot.jx.tunnel.ITunnelDefs.TunnelQueue;

/**
 * WIP
 * 
 * @author lalittanwar
 *
 * @param <E>
 */
public class TunnelQueueImpl<E> implements TunnelQueue<E> {

	RQueue<E> queue;

	public TunnelQueueImpl(RQueue<E> rQueue) {
		queue = rQueue;
	}

	@Override
	public boolean add(E e) {
		return queue.add(e);
	}

	@Override
	public boolean offer(E e) {
		return queue.offer(e);
	}

	@Override
	public E remove() {
		return queue.remove();
	}

	@Override
	public E poll() {
		return queue.poll();
	}

	@Override
	public E element() {
		return queue.element();
	}

	@Override
	public E peek() {
		return queue.peek();
	}

	@Override
	public int size() {
		return queue.size();
	}

	@Override
	public boolean isEmpty() {
		return queue.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return queue.contains(o);
	}

	@Override
	public Iterator<E> iterator() {
		return queue.iterator();
	}

	@Override
	public Object[] toArray() {
		return queue.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return queue.toArray(a);
	}

	@Override
	public boolean remove(Object o) {
		return queue.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return queue.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		return queue.addAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return queue.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return queue.retainAll(c);
	}

	@Override
	public void clear() {
		queue.clear();
	}

}
