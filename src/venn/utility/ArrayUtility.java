/*
 * VennMaster/geometry/ArrayUtility.java
 * 
 * Created on 30.06.2004
 * 
 */
package venn.utility;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

import venn.geometry.Permutation;

/**
 * @author muellera
 */
public class ArrayUtility
{
	/**
	 * 
	 * @param array
	 * @return Index of the maximum element of the given array
	 * Returns -1 if array is invalid.
	 */
	public static int maxElement(int[] array)
	{
		if( array == null || array.length == 0 )
			return -1;
			
		int imax = 0;
				
		for(int i=1; i<array.length; ++i)
		{
			if( array[i] > array[imax] )
			{
				imax = i;
			}
		}
		return imax;
	}
	
	public static int max(int[] array)
	{
		if( array == null || array.length == 0 )
			throw new IllegalArgumentException("array must have at least one element");
			
		int imax = 0;
				
		for(int i=1; i<array.length; ++i)
		{
			if( array[i] > array[imax] )
			{
				imax = i;
			}
		}
		return array[imax];
	}
	
	
	private static class SortElement
	implements Comparable
	{
		int index;
		Object object;
		Comparator comparator;
		
		public SortElement(int index, Object object,Comparator comparator)
		{
			this.index = index;
			this.object = object;
			this.comparator = comparator;
		}

		/* (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(Object arg0) 
		{
			return comparator.compare(object,
								((SortElement)arg0).object);
		}
	}

	/**
	 * Sorts the given objects and returns an array of target
	 * indices 0..n-1.
	 * @param obj
	 * @param comp
	 * @return the permutation
	 */
	public static int[] sort(Object[] obj, Comparator comp)
	{
		SortElement[] array = new SortElement[obj.length];
		for(int i=0; i<array.length; ++i )
		{
			array[i] = new SortElement(i,obj[i],comp);
		}
		
		Arrays.sort(array);
		
		int[] perm = new int[obj.length];
		
		for(int i=0; i<array.length; ++i)
		{
			obj[i] = array[i].object;
			perm[i] = array[i].index;
		}
		return perm;
	}
	
	public static void doubleVectorToStream( Writer writer, double[] v, String sep) throws IOException
	{
		StringBuffer buf = new StringBuffer();
		for( int i=0; i<v.length; ++i )
		{
			if( i>0 )
				buf.append(sep);
			buf.append(v[i]);
		}
		writer.write( buf.toString() );		
	}
	
	public static void main(String[] args)
	{
		Integer[] 	objects = new Integer[10];
		for(int i=0; i<objects.length; ++i)
		{
			objects[i] = new Integer((3+i*13)%(objects.length/2));
			System.out.print(objects[i]+" ");
		}
		System.out.println("");
		
		int[] order = sort(objects,new Comparator()
				{

					public int compare(Object o1, Object o2) 
					{
						int i1 = ((Integer)o1).intValue(),
							i2 = ((Integer)o2).intValue();
						
						if( i1 < i2 )
							return -1;
						else
							if( i1 > i2 )
								return +1;
						return 0;
					}
			
				});
		
		for(int i=0; i<objects.length; ++i )
			System.out.print(objects[i]+" ");
		System.out.println("");
		Permutation perm = new Permutation(order);
		System.out.println("order  = "+perm);
		System.out.println("iorder = "+perm.createInverse());
		for(int i=0; i<order.length; ++i )
			System.out.print(objects[perm.invMap(i)]+" ");
	}
}
