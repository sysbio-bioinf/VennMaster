/*
 * Created on 23.05.2005
 *
 */
package venn.db;

import venn.geometry.FileFormatException;

import java.io.*;
import java.util.*;

import junit.framework.Assert;


/**
 * @author muellera
 *
 */
public class GeneOntologyReaderModel 
extends AbstractVennDataModel implements Serializable
{
	private ArrayList	aElements,		// array of aElements (key names)
						aGroups,		// array of aGroups (group names)	
						groupKeys,		// maps group numbers (integer) to a BitSet of key numbers (integer)
                        properties;     // group properties
	
	private HashMap		groupMap;	// maps groupID's to group numbers (need this only for import ...)
	
	private BitSet[]	sets;
	
	private boolean 	valid;			// valid state? 

    /**
     * 
     */
    public GeneOntologyReaderModel() 
    {
        super();
        
		aElements = new ArrayList();
		aGroups = new ArrayList();
		groupMap = new HashMap();
		groupKeys = new ArrayList();
        properties = new ArrayList();
        
		valid = false;
    }
	
	/**
	 * Read "Gene Ontology Miner" format.
	 *   
	 * @param categoryList Path to the category file
	 * @param geneList Path to the gene file
	 */
	public void loadFromFile(String categoryList,String geneList )
		throws IOException, FileFormatException
	{
		clear();
		
		readElements(new FileReader(categoryList));
		readGeneList(new FileReader(geneList));
		
		updateBitSets();
	}
	
	public void clear()
	{    
		aElements.clear();
		aGroups.clear();
		groupKeys.clear();
        properties.clear();
		valid = false;
	}
    
    public boolean isValid()
    {
        return valid;
    }
	
	/**
	 * Reads a gene list from a file.
	 * 
	 * @param reader
	 * @throws IOException
	 * @throws FileFormatException
	 */
	private void readElements(Reader reader)
		throws IOException, FileFormatException
	{
		aGroups.clear();
		groupMap.clear();
        properties.clear();

		boolean header = true;
						
		LineNumberReader in = new LineNumberReader(reader);
		
		int groupNumber = 0;
					
		int numTokens = -1;
		
		while( in.ready() )
		{
			String line = in.readLine();
			line.trim();
					
			if( line.length() > 0 )
			{			
				if( header )
				{
					header = false;
					continue;
				}
				StringTokenizer tok = new StringTokenizer(line,"\t");			
				if( tok.countTokens() != 9 && tok.countTokens() != 12 )
					throw new FileFormatException("Gene summary export (.se) file in line "+in.getLineNumber()+ " wrong number of fields");
				if (numTokens == -1) {
					numTokens = tok.countTokens();
				} else {
					if (tok.countTokens() != numTokens) {
						throw new FileFormatException("Gene summary export (.se) file in line "+in.getLineNumber()+ " number of fields has changed");
					}
				}
				
				int 	groupID = 0, 
						nTotal = 0, 
						nUnder = 0, 
						nOver = 0, 
						nChange = 0;
				double 	pUnder = 1.0, 
						pOver = 1.0, 
						pChange = 1.0,
						fdrUnder = -777.0,
						fdrOver = -888.0,
						fdrChange = -999.0;
				String term = null;
				try 
				{
					String tmp = tok.nextToken().trim();
					if( tmp.equalsIgnoreCase("null") )
						continue;
					if( tmp.equalsIgnoreCase("all") )
						groupID = 0;
					else
						groupID = Integer.parseInt(tmp);
					nTotal =  Integer.parseInt(tok.nextToken().trim());
					nUnder =  Integer.parseInt(tok.nextToken().trim());
					nOver =  Integer.parseInt(tok.nextToken().trim());
					nChange =  Integer.parseInt(tok.nextToken().trim());
					pUnder = Double.parseDouble(tok.nextToken().trim().replace(',','.'));
					pOver = Double.parseDouble(tok.nextToken().trim().replace(',','.'));
					pChange = Double.parseDouble(tok.nextToken().trim().replace(',','.'));
					if (numTokens == 12) {
						fdrUnder = Double.parseDouble(tok.nextToken().trim().replace(',','.'));
						fdrOver = Double.parseDouble(tok.nextToken().trim().replace(',','.'));
						fdrChange = Double.parseDouble(tok.nextToken().trim().replace(',','.'));
					}
					term = tok.nextToken().trim();
				}
				catch( NumberFormatException e )
				{
					System.err.println( e.getStackTrace() );
					throw new FileFormatException("Gene summary export (.se) file in line "+in.getLineNumber());
				}
//				double pValue = 1.0;
//				if( pUnder < pValue ) {
//					pValue = pUnder;
//				}
//				if( pOver < pValue ) {
//					pValue = pOver;
//				}
//				if( pChange < pValue ) {
//					pValue = pChange;
//				}
				
				if (numTokens == 9) {
					groupMap.put(new Integer(groupID),new Integer(groupNumber));
					properties.add(new GOCategoryProperties3p(groupID,nTotal,nChange,pUnder, pOver, pChange));
					aGroups.add(term);
					++groupNumber;
					//System.out.println(in.getLineNumber()+ " : "+line);
				} else {
					assert numTokens == 12;
					groupMap.put(new Integer(groupID),new Integer(groupNumber));
					properties.add(new GOCategoryProperties3p3fdr(groupID,nTotal,nChange,
							pUnder, pOver, pChange,
							fdrUnder, fdrOver, fdrChange));
					aGroups.add(term);
					++groupNumber;
					//System.out.println(in.getLineNumber()+ " : "+line);
                }
			}
		}
	}
			
	/**
	 * Reads a gene list from a file. The elements must be read before!!
	 * 
	 * Format:
	 * [group ID] [group name] [gene name] [aberration] [gene name2]
	 * 
	 * @param reader
	 * @throws IOException
	 * @throws FileFormatException
	 */
	private void readGeneList(Reader reader)
		throws IOException, FileFormatException
	{
	    /*
		if( groupMap==null )
			throw new IllegalStateException("YOU HAVE TO CALL readElements BEFORE readGeneList");
			*/
		
		Map 	keyMap;		// maps key names to logical numbers in the range 0..n-1
		
		groupKeys.clear();
		groupKeys.ensureCapacity(aGroups.size());
		for(int i=0; i<aGroups.size(); ++i)
		{
			groupKeys.add(new HashSet());
		}
		 
		keyMap = new HashMap();
		
		LineNumberReader in = new LineNumberReader(reader);
		
		int keyNumber = 0;
												
		while( in.ready() )
		{
			String line = in.readLine();
			line.trim();
					
			if( line.length() > 0 )
			{			
				StringTokenizer tok = new StringTokenizer(line,"\t");			
				if( tok.countTokens() < 3 )
					throw new FileFormatException("Gene category summary (.gce) file at line "+in.getLineNumber() +" wrong number of fields");
				
				String 	strID = tok.nextToken().trim();
				if( strID.length() < 3 || !strID.startsWith("GO:") )
					continue; // ignore unwanted records
				
				String 	groupName = tok.nextToken().trim(),
						keyName = tok.nextToken().trim();
				
				if( strID.length() < 4 )
					throw new FileFormatException("Gene category summary (.gce) file at line "+in.getLineNumber());
				strID = strID.substring(3);
				
				int goID = 0;
				try {
					if( !strID.equalsIgnoreCase("all") )
						goID = Integer.parseInt(strID);
				}
				catch( NumberFormatException e )
				{
					throw new FileFormatException("Gene category summary (.gce) file in line "+in.getLineNumber()+" illegal field value in first column");
				}
				
				Integer gval = (Integer)groupMap.get(new Integer(goID));
				if( gval == null )
				{ // entry not found in category list -> continue
					continue;
				}
				// System.out.println(in.getLineNumber()+" "+strID+" : "+keyName +" "+goID+" : " + aGroups.get(gval.intValue()));
				
				Integer kval = (Integer)keyMap.get(keyName);
				if( kval == null )
				{ // append new key
					kval = new Integer(keyNumber);
					keyMap.put(keyName,kval);
					aElements.add(keyNumber,keyName);
					++keyNumber;
				}
				Set myGroup = (Set)groupKeys.get(gval.intValue());
				myGroup.add(kval);
			}
		}
		Assert.assertEquals(groupKeys.size(), aGroups.size() );		
	}


	public String getKeyName(int idx)
	{
		return (String)aElements.get(idx);
	}

	public String getGroupName(int idx)
	{
		return (String)aGroups.get(idx);
	}
		
	public void show()
	{
		// print aGroups
		for(int i=0; i<aGroups.size(); ++i )
		{
			System.out.print(aGroups.get(i) + "["+ i+"] : { ");
			Set list = (Set)groupKeys.get(i);
			Iterator iter = list.iterator();
			while( iter.hasNext() )
			{
				System.out.print(iter.next()+" ");
			}
			System.out.println("}");
		}
		
		// print aElements
		for(int i=0; i<aElements.size(); ++i )
		{
			System.out.println(i+" : " + aElements.get(i));
		}
	}

	/* (non-Javadoc)
	 * @see geometry.CategoryMapper#getKeySize()
	 */
	public int getKeySize()
	{
		return aElements.size();
	}

	/* (non-Javadoc)
	 * @see geometry.CategoryMapper#getGroupSize()
	 */
	public int getGroupSize()
	{
		return aGroups.size();
	}
	
	/* (non-Javadoc)
	 * @see geometry.CategoryMapper#reduceTo(java.util.BitSet)
	 */
    /*
	public void reduceTo(BitSet sel)
	{
		LinkedList 	glist = new LinkedList(),
					klist = new LinkedList();
		
		for( int i=0; i<getGroupSize(); ++i )
		{
			if( !sel.get(i) )
			{ // this group has to be removed
				glist.add(aGroups.get(i));
				klist.add(groupKeys.get(i));
			}
		}
		aGroups.removeAll(glist);
		groupKeys.removeAll(klist);
	}
    */

    /* (non-Javadoc)
     * @see venn.AbstractVennDataModel#getNumGroups()
     */
    public int getNumGroups() 
    {
        return aGroups.size();
    }

    /* (non-Javadoc)
     * @see venn.AbstractVennDataModel#getNumElements()
     */
    public int getNumElements() 
    {
        return aElements.size();
    }

	private void updateBitSets()
	{
		int n = aElements.size();
		
		sets = new BitSet[aGroups.size()];
		for(int i=0; i<sets.length; ++i)
		{
			sets[i] = new BitSet(n);
			Set list = (Set)groupKeys.get(i);
			Iterator iter = list.iterator();
			while( iter.hasNext() )
			{
				sets[i].set(((Integer)iter.next()).intValue());
			}
		}
		
		valid = true;
		
//		fireChangeEvent();
		notifySucc();
	}
    /* (non-Javadoc)
     * @see venn.AbstractVennDataModel#getGroupElements(int)
     */
    public BitSet getGroupElements(int groupID)
    {
        if( groupID < 0 || groupID >= getNumGroups() )
            throw new IndexOutOfBoundsException("group ID out of bounds");
        
        return sets[groupID];
    }
    
    public Object getGroupProperties(int groupID)
    {
        if( groupID < 0 || groupID >= getNumGroups() )
            throw new IndexOutOfBoundsException("group ID out of bounds");
        
        return properties.get(groupID);
    }
    

    /* (non-Javadoc)
     * @see venn.AbstractVennDataModel#getElementName(int)
     */
    public String getElementName(int elementID) 
    {

        return (String)aElements.get(elementID);
    }

}

