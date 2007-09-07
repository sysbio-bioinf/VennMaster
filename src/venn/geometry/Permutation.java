/*
 * Created on 05.07.2004
 *
 */
package venn.geometry;

/**
 * @author muellera
 *
 */
public class Permutation 
{
	private int[] 	indices,
					invIndices;
	
	public Permutation()
	{
		indices = null;
		invIndices = null;
	}
	
	public Permutation(int size)
	{
		indices = new int[size];
		for(int i=0; i<size; ++i)
		{
			indices[i] = i;
		}
		updateInverseIndices();
	}
	
	public Permutation(int[] perm)
	{
		assign(perm);
	}
	
	public void assign(int[] perm)
	{
		indices = perm;
		updateInverseIndices();		
	}
	
	public int size()
	{
		if( indices != null )
		{
			return indices.length;
		}
		else
		{
			return 0;
		}
	}
	
	public Permutation createInverse()
	{
		return new Permutation((int[])invIndices.clone());
	}
	
	private void updateInverseIndices()
	{
		if( indices == null )
			return;
		
		if( invIndices == null || invIndices.length != indices.length )
		{
			invIndices = new int[indices.length];
		}
		for(int i=0; i<indices.length; ++i)
		{
			invIndices[indices[i]] = i;
		}
	}
	
	public int map(int i)
	{
		if( indices == null )
			return i;
		else
			return indices[i]; 
	}
	
	public int invMap(int i)
	{
		if( indices == null )
			return i;
		else
			return invIndices[i];
	}
	
	public String toString()
	{
		if( indices != null )
		{
			StringBuffer buf = new StringBuffer();
			buf.append("< ");
			
			for(int i=0; i<indices.length; ++i)
			{
				buf.append(indices[i]);
				buf.append(" ");
			}
			buf.append(">");
			return buf.toString();
		}
		else
		{
			return "< >";
		}
	}
}
