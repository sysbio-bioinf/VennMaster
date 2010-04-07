/*
 * Created on 23.05.2005
 *
 */
package venn.db;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;
import venn.geometry.FileFormatException;


/**
 * @author muellera
 *
 */
public class GeneOntologyReaderModel 
extends AbstractVennDataModel implements Serializable
{
	private List<String> elements;  // array of aElements (key names)
	private	List<String> groups;  // array of aGroups (group names)	
	private List<Set<Integer>> groupKeys;  // maps group numbers (integer) to a BitSet of key numbers (integer)
    private List<AbstractGOCategoryProperties> properties;  // group properties
	
	private Map<Integer, Integer>		groupMap;  // maps groupID's to group numbers (need this only for import ...)
	
	private BitSet[]	sets;
	
	private Set<Integer> removedLines;

    /**
     * @throws IOException 
     * @throws FileFormatException 
     * 
     */
    public GeneOntologyReaderModel(String categoryListName,String geneListName) throws FileFormatException, IOException 
    {
        super();
        
        loadFromFile(new FileReader(categoryListName), new FileReader(geneListName));
    }
    
    public GeneOntologyReaderModel(Reader categoryList, Reader geneList) throws FileFormatException, IOException {
        super();
        
        loadFromFile(categoryList, geneList);
	}
	
	/**
	 * Read "Gene Ontology Miner" format.
	 *   
	 * @param categoryList Path to the category file
	 * @param geneList Path to the gene file
	 */
	private void loadFromFile(Reader categoryList, Reader geneList )
		throws IOException, FileFormatException
	{
        readElements(categoryList);
		readGeneList(geneList);
		
		updateBitSets();
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
		groups = new ArrayList<String>();
		groupMap = new HashMap<Integer, Integer>();
        properties = new ArrayList<AbstractGOCategoryProperties>();

		boolean header = true;
						
		LineNumberReader in = new LineNumberReader(reader);
		
		int groupNumber = 0;
					
		int numTokens = -1;
		
		removedLines = new HashSet<Integer>();
		
		String line;
		while((line = in.readLine()) != null)
		{
//			line.trim();
			
			boolean missingValue = false;
			if( line.length() > 0 )
			{			
				if( header )
				{
					header = false;
					continue;
				}
				String[] tokens = line.split("\t", -1);
				if( tokens.length != 9 && tokens.length != 12 )
					throw new FileFormatException("Gene summary export (.se) file in line "+in.getLineNumber()+ " wrong number of fields");
				if (numTokens == -1) {
					numTokens = tokens.length;
				} else {
					if (tokens.length != numTokens) {
						throw new FileFormatException("Gene summary export (.se) file in line "+in.getLineNumber()+ " number of fields has changed");
					}
				}
				
				int 	groupID = 0, 
						nTotal = 0;
				@SuppressWarnings("unused")
				int		nUnder = 0;
				@SuppressWarnings("unused")
				int		nOver = 0;
				int		nChange = 0;
				double 	pUnder = 1.0, 
						pOver = 1.0, 
						pChange = 1.0,
						fdrUnder = -777.0,
						fdrOver = -888.0,
						fdrChange = -999.0;
				String term = null;
				
				String pUnderStr, pOverStr, pChangeStr;
				String fdrUnderStr = null, fdrOverStr = null, fdrChangeStr = null;
				
				try 
				{
					String tmp = tokens[0].trim();
					if( tmp.equalsIgnoreCase("null") )
						continue;
					if( tmp.equalsIgnoreCase("all") )
						groupID = 0;
					else
						groupID = Integer.parseInt(tmp);
					nTotal =  Integer.parseInt(tokens[1].trim());
					nUnder =  Integer.parseInt(tokens[2].trim());
					nOver =  Integer.parseInt(tokens[3].trim());
					nChange =  Integer.parseInt(tokens[4].trim());
					pUnderStr = tokens[5].trim();
					if (pUnderStr.length() > 0) {
						pUnder = Double.parseDouble(pUnderStr.replace(',','.'));
					} else {
						missingValue = true;
//						pUnder = 1.0;
					}
					pOverStr = tokens[6].trim();
					if (pOverStr.length() > 0) {
						pOver = Double.parseDouble(pOverStr.replace(',','.'));
					} else {
						missingValue = true;
//						pOver = 1.0;
					}
					pChangeStr = tokens[7].trim();
					if (pChangeStr.length() > 0) {
						pChange = Double.parseDouble(pChangeStr.replace(',','.'));
					} else {
						missingValue = true;
//						pChange = 1.0;
					}
					if (numTokens == 9) {
						term = tokens[8];
					}
					if (numTokens == 12) {
						fdrUnderStr = tokens[8].trim();
						if (fdrUnderStr.length() > 0) {
							fdrUnder = Double.parseDouble(fdrUnderStr.replace(',','.'));
						} else {
							missingValue = true;
//							fdrUnder = 1.0;
						}
						fdrOverStr = tokens[9].trim();
						if (fdrOverStr.length() > 0) {
							fdrOver = Double.parseDouble(fdrOverStr.replace(',','.'));
						} else {
							missingValue = true;
//							fdrOver = 1.0;
						}
						fdrChangeStr = tokens[10].trim();
						if (fdrChangeStr.length() > 0) {
							fdrChange = Double.parseDouble(fdrChangeStr.replace(',','.'));
						} else {
							missingValue = true;
//							fdrChange = 1.0;
						}
						term = tokens[11].trim();
					}
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
				
				if (missingValue) {
					// note: missing p/fdr values in the file seems to contain single white spaces instead (here, not in HT files)
					removedLines.add(in.getLineNumber());
					continue;
				}
				
				if (numTokens == 9) {
					groupMap.put(Integer.valueOf(groupID),Integer.valueOf(groupNumber));
					properties.add(new GOCategoryProperties3p(groupID,nTotal,nChange,pUnder, pOver, pChange));
					groups.add(term);
					++groupNumber;
				} else {
					assert numTokens == 12;
					groupMap.put(Integer.valueOf(groupID),Integer.valueOf(groupNumber));
					properties.add(new GOCategoryProperties3p3fdr(groupID,nTotal,nChange,
							pUnder, pOver, pChange,
							fdrUnder, fdrOver, fdrChange));
					groups.add(term);
					++groupNumber;
                }
			}
		}
		
		reader.close();
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
		
		Map<String, Integer> 	keyMap;		// maps key names to logical numbers in the range 0..n-1
		
		elements = new ArrayList<String>();
		groupKeys = new ArrayList<Set<Integer>>();
//		groupKeys.ensureCapacity(aGroups.size());
		for(int i=0; i<groups.size(); ++i)
		{
			groupKeys.add(new HashSet<Integer>());
		}
		 
		keyMap = new HashMap<String, Integer>();
		
		LineNumberReader in = new LineNumberReader(reader);
		
		int keyNumber = 0;
												
		String line;
		while((line = in.readLine()) != null)
		{
//			line.trim();
					
			if( line.length() > 0 )
			{			
//				StringTokenizer tok = new StringTokenizer(line,"\t");
				String[] tokens = line.split("\t", -1);
				if( tokens.length < 3 )
				throw new FileFormatException("Gene category summary (.gce) file at line "+in.getLineNumber() +" wrong number of fields");
				
				String 	strID = tokens[0].trim();
				if( strID.length() < 3 || !strID.startsWith("GO:") )
					continue; // ignore unwanted records
				
				@SuppressWarnings("unused")
				String 	groupName = tokens[1].trim();
				String keyName = tokens[2].trim();
				
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
				
				Integer gval = groupMap.get(Integer.valueOf(goID));
				if( gval == null )
				{ // entry not found in category list -> continue
					continue;
				}
				
				Integer kval = keyMap.get(keyName);
				if( kval == null )
				{ // append new key
					kval = Integer.valueOf(keyNumber);
					keyMap.put(keyName,kval);
					elements.add(keyNumber,keyName);
					++keyNumber;
				}
				Set<Integer> myGroup = groupKeys.get(gval.intValue());
				myGroup.add(kval);
			}
		}

		reader.close();

		Assert.assertEquals(groupKeys.size(), groups.size() );
	}


	public String getGroupName(int idx)
	{
		return groups.get(idx);
	}
		
	public void show()
	{
		// print aGroups
		for(int i=0; i<groups.size(); ++i )
		{
			System.out.print(groups.get(i) + "["+ i+"] : { ");
			Set<Integer> list = groupKeys.get(i);
			Iterator<Integer> iter = list.iterator();
			while( iter.hasNext() )
			{
				System.out.print(iter.next()+" ");
			}
			System.out.println("}");
		}
		
		// print aElements
		for(int i=0; i<elements.size(); ++i )
		{
			System.out.println(i+" : " + elements.get(i));
		}
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
        return groups.size();
    }

    /* (non-Javadoc)
     * @see venn.AbstractVennDataModel#getNumElements()
     */
    public int getNumElements() 
    {
        return elements.size();
    }

	private void updateBitSets()
	{
		int n = elements.size();
		
		sets = new BitSet[groups.size()];
		for(int i=0; i<sets.length; ++i)
		{
			sets[i] = new BitSet(n);
			Set<Integer> list = groupKeys.get(i);
			Iterator<Integer> iter = list.iterator();
			while( iter.hasNext() )
			{
				sets[i].set(iter.next().intValue());
			}
		}
		
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

        return elements.get(elementID);
    }
    
    public Set<Integer> getRemovedLines() {
    	return removedLines;
    }
    
}

