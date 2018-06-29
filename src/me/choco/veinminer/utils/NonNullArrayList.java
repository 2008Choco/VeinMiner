package me.choco.veinminer.utils;

import java.util.ArrayList;
import java.util.Collection;

import com.google.common.base.Preconditions;

/**
 * A more specific implementation of ArrayList that prevents the addition of null values. If a null
 * value is added to this collection, a NullPointerException will be thrown
 * 
 * @author Parker Hawke - 2008Choco
 * @param <T> the type of element to store in this collection
 */
public class NonNullArrayList<T> extends ArrayList<T> {
	
	private static final long serialVersionUID = -8852239071748358532L;
	
	@Override
	public void add(int index, T element) {
		Preconditions.checkNotNull(element, "NonNullArrayList does not support the addition of null values");
		super.add(index, element);
	}
	
	@Override
	public boolean add(T element) {
		Preconditions.checkNotNull(element, "NonNullArrayList does not support the addition of null values");
		return super.add(element);
	}
	
	@Override
	public boolean addAll(Collection<? extends T> c) {
		Preconditions.checkNotNull(c, "NonNullArrayList does not support the addition of null values");
		Preconditions.checkState(!c.contains(null), "NonNullArrayList does not support the addition of null values");
		
		return super.addAll(c);
	}
	
	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		Preconditions.checkNotNull(c, "NonNullArrayList does not support the addition of null values");
		Preconditions.checkState(!c.contains(null), "NonNullArrayList does not support the addition of null values");
		
		return super.addAll(index, c);
	}
	
}