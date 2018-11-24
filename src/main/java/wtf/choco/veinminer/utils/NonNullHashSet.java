package wtf.choco.veinminer.utils;

import java.util.Collection;
import java.util.HashSet;

import com.google.common.base.Preconditions;

/**
 * A more specific implementation of HashSet that prevents the addition of null values. If a null
 * value is added to this collection, a NullPointerException will be thrown.
 *
 * @param <T> the type of element to store in this collection
 *
 * @author Parker Hawke - 2008Choco
 */
public class NonNullHashSet<T> extends HashSet<T> {

	private static final long serialVersionUID = -8852239071748358532L;

	@Override
	public boolean add(T element) {
		Preconditions.checkNotNull(element, "NonNullHashSet does not support the addition of null values");
		return super.add(element);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		Preconditions.checkNotNull(c, "NonNullHashSet does not support the addition of null values");
		Preconditions.checkState(!c.contains(null), "NonNullHashSet does not support the addition of null values");

		return super.addAll(c);
	}

}