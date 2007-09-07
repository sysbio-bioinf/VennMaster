/*
 * VennMaster/geometry/CategoryMapper.java
 * 
 * Created on 30.06.2004
 * 
 */
package venn.geometry;

import java.util.BitSet;

/**
 * Maps group/element indices to strings.
 * 
 * @author muellera
 */
public interface CategoryMapper
{
	/**
	 * Removes all data from the mapper.
	 *
	 */
	public void clear();
	
	/**
	 * 
	 * @return True if there is a valid data set.
	 */
	public boolean hasData();
	
	public String getKeyName(int idx);
	public String getGroupName(int idx);
	
	/**
	 * 
	 * @return An array of BitSets representing the categories. So BitSet no. i is a set
	 * of all elements contained in group i. So if there is a one at position j in BitSet i
	 * the element j is contained in group i. The names can be mapped back with getKeyName(j)
	 * and getGroupName(j).
	 */
	public BitSet[] getBitSets();
	
	/**
	 * 
	 * @return The number of keys in this mapper.
	 */
	public int getKeySize();
	
	/**
	 * 
	 * @return The number of groups.
	 */
	public int getGroupSize();

	/**
	 * Reduces the categories to the given selection
	 * @param sel
	 */
	// public void reduceTo(BitSet sel);

		
}
