/*
 * VennMaster/geometry/StringSet.java
 * 
 * Created on 30.06.2004
 * 
 */
package venn.geometry;

import java.io.*;
import java.util.*;

import junit.framework.Assert;

/**
 * @author muellera
 */
public class StringSet
implements CategoryMapper
{
	private ArrayList	keys,		// array of keys
						groups,		// array of groups	
						groupKeys;	// maps group numbers to a LinkedList of key numbers
						
	private BitSet[]	sets; 
	
	public StringSet()
	{
		keys = new ArrayList();
		groups = new ArrayList();
		groupKeys = new ArrayList();
		sets = null;
	}
	
	/**
	 * Read in tab separated file with
	 * key \t group
	 * pairs (one value per line).
	 * 
	 * @param name
	 */
	public void loadFromFile(String name)
		throws IOException, FileFormatException
	{
		read(new FileReader(name));
	}
	
	public void clear()
	{
		keys.clear();
		groups.clear();
		groupKeys.clear();
		sets = null;
	}
	
	public boolean hasData()
	{
		return sets != null;
	}

	/**
	 * Reads a bitset from a file.
	 * Format:
	 * 
	 * # comment
	 * <key>\t<group>
	 * 
	 * @param reader
	 * @throws IOException
	 * @throws FileFormatException
	 */
	public void read(Reader reader)
		throws IOException, FileFormatException
	{
		clear();
		Map keyMap,		// maps key names to logical numbers in the range 0..n-1
				groupMap;	// maps group names to logical numbers in the range 0..m-1
				
		keyMap = new HashMap();
		groupMap = new HashMap();
	
		
		LineNumberReader in = new LineNumberReader(reader);
		
		int groupNumber = 0,
			 keyNumber = 0;
												
		while( in.ready() )
		{
			String line = in.readLine();
			line.trim();
					
			if( line.length() > 0 )
			{
				if( line.charAt(0) == '#' )
					continue;
					
				StringTokenizer tok = new StringTokenizer(line,"\t");			
				if( tok.countTokens() != 2)
				{
					throw new FileFormatException("Error in line "+in.getLineNumber());
				}				
				String 	key = tok.nextToken().trim(),
						group = tok.nextToken().trim();
				
				Integer gval = (Integer)groupMap.get(group);
				if( gval == null )
				{ // append new group
					gval = new Integer(groupNumber);
					groupMap.put(group,gval);

					// extend the number of groups			
					groupKeys.add(new LinkedList());
					groups.add(groupNumber,group);
					++groupNumber;
				}
				
				Integer kval = (Integer)keyMap.get(key);
				if( kval == null )
				{ // append new key
					kval = new Integer(keyNumber);
					keyMap.put(key,kval);
					
					keys.add(keyNumber,key);
					++keyNumber;
				}
				
				LinkedList myGroup = (LinkedList)groupKeys.get(gval.intValue());
				myGroup.add(kval);
			}
		}
		Assert.assertEquals(groupKeys.size(),groups.size() );
		updateBitSets();
	}
	
	private void updateBitSets()
	{
		int n = keys.size();
		
		sets = new BitSet[groups.size()];
		for(int i=0; i<sets.length; ++i)
		{
			sets[i] = new BitSet(n);
			
			LinkedList list = (LinkedList)groupKeys.get(i);
			Iterator iter = list.iterator();
			while( iter.hasNext() )
			{
				sets[i].set(((Integer)iter.next()).intValue());
			}
		}
	}

	public String getKeyName(int idx)
	{
		return (String)keys.get(idx);
	}

	public String getGroupName(int idx)
	{
		return (String)groups.get(idx);
	}
	
	public BitSet[] getBitSets()
	{
		return sets;
	}
	
	public void show()
	{
		// print groups
		for(int i=0; i<groups.size(); ++i )
		{
			System.out.print(groups.get(i) + "["+ i+"] : { ");
			LinkedList list = (LinkedList)groupKeys.get(i);
			Iterator iter = list.iterator();
			while( iter.hasNext() )
			{
				System.out.print(iter.next()+" ");
			}
			System.out.println("}");
		}
		
		// print keys
		for(int i=0; i<keys.size(); ++i )
		{
			System.out.println(i+" : " + keys.get(i));
		}
		
		// print bitsets
		
		for(int i=0; i<sets.length; ++i)
		{
			System.out.println(i+" : "+sets[i]);
		}
	}

	/* (non-Javadoc)
	 * @see geometry.CategoryMapper#getKeySize()
	 */
	public int getKeySize()
	{
		return keys.size();
	}

	/* (non-Javadoc)
	 * @see geometry.CategoryMapper#getGroupSize()
	 */
	public int getGroupSize()
	{
		return groups.size();
	}
	
	/* (non-Javadoc)
	 * @see geometry.CategoryMapper#reduceTo(java.util.BitSet)
	 */
	public void reduceTo(BitSet sel)
	{
		LinkedList 	glist = new LinkedList(),
					klist = new LinkedList();
		
		//show();
		//System.out.println("reduceTo "+sel);
		
		for( int i=0; i<getGroupSize(); ++i )
		{
			if( !sel.get(i) )
			{ // this group has to be removed
				glist.add(groups.get(i));
				klist.add(groupKeys.get(i));
			}
		}
		groups.removeAll(glist);
		groupKeys.removeAll(klist);
		
		// TODO: now the elements could be reduced
		updateBitSets();
		//show();
	}	
}
