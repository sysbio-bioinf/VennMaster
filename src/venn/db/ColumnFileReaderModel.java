package venn.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import venn.geometry.FileFormatException;

public class ColumnFileReaderModel extends AbstractVennDataModel {

	private ArrayList<String> aElements; // array of aElements (key names)
	ArrayList<String> aGroups; // array of aGroups (group names)
	ArrayList<BitSet> sets; // contains one BitSet for each group
	// the index of a set bit corresponds to the index in
	// aElements
	ArrayList<ElementProperties> elementProperties;
//	private Set<String> columnNames = new HashSet<String>();
	private int columnCount;
	private Integer groupKeyIdx = 0; // running index for groups in aGroups
	private Integer elementKeyIdx = 0; // running index for groups in aElements

	/**
	 * reads several column files and generates the corresponding DataMaodel
	 * 
	 * @param reader
	 * @throws IOException
	 *             , FileFormatException
	 */
	private void read(File[] files) throws IOException, FileFormatException {

		HashMap<String, Integer> groupIdx = new HashMap<String, Integer>();
		HashMap<String, Integer> elementsIdx = new HashMap<String, Integer>();
		groupKeyIdx = 0;
		elementKeyIdx = 0;
		columnCount = 0;
		aElements = new ArrayList<String>();
		aGroups = new ArrayList<String>();
		sets = new ArrayList<BitSet>();
		elementProperties = new ArrayList<ElementProperties>();
		for (File file : files) {
			readSingleColumnFile(file, groupIdx, elementsIdx);
		}

		// for (String elementName : elementsIdx.keySet()) {
		// aElements.set(elementsIdx.get(elementName), elementName);
		// }
		// for (String groupName : groupIdx.keySet()) {
		// aGroups.set(groupIdx.get(groupName), groupName);
		// }
	}

	private void readSingleColumnFile(File file,
			HashMap<String, Integer> groupIdx,
			HashMap<String, Integer> elementsIdx) throws IOException {

		BufferedReader r = new BufferedReader(new FileReader(file));

		// readHeader

		String header;
		// skip ahead to first non empty, non comment line
		while ((header = r.readLine()) != null) {
			if (header.trim().isEmpty() || header.trim().startsWith("#")) {
				continue;
			} else {
				break;
			}
		}

		if (header == null) {
			throw new FileFormatException("no header detected", file);
		}
		header = header.trim();
		String[] splitHeader = header.split("\t");
		int headerLength = splitHeader.length;
		if (columnCount == 0) {
			columnCount = headerLength;
		} else {
			if (headerLength != columnCount) {
				throw new FileFormatException("incorrect column count", file);
			}
		}
		String groupName = splitHeader[0];
//		for (int i = 1; i < splitHeader.length; i++) {
//			columnNames.add(splitHeader[i]);
//		}

		// add group to the data model
		if (groupIdx.get(groupName) != null) {
			throw new FileFormatException("duplicate group names", file);
		}
		groupIdx.put(groupName, groupKeyIdx);
		groupKeyIdx++;
		aGroups.add(groupName);
		BitSet groupBitSet = new BitSet();
		sets.add(groupBitSet);

		// we need the splitHeader array to determin the column names for each
		// later line in the file

		// read each following line

		String line;

		while ((line = r.readLine()) != null) {
			line = line.trim();
			if (line.isEmpty() || line.startsWith("#")) {
				continue;
			}

			String[] splitLine = line.split("\t");
			if (splitLine.length != columnCount) {
				throw new FileFormatException("incorrect number of columns",
						file);
			}

			// add element to map,list and bitset.
			String elementName = splitLine[0];
			Integer eIdx = elementsIdx.get(elementName);
			if (eIdx == null) {
				eIdx = elementKeyIdx;
				elementsIdx.put(elementName, eIdx);
				elementKeyIdx++;
				elementProperties.add(new ElementProperties(elementName,
						DiffExprValue.UNKNOWN, null));
				aElements.add(elementName);
			}
			sets.get(groupIdx.get(groupName)).set(elementsIdx.get(elementName));

			// fill properties for this element
			for (int i = 1; i < splitLine.length; i++) {
				String columnNumberString = splitLine[i];
				String columnName = splitHeader[i];
				elementProperties.get(elementsIdx.get(elementName)).setColumn(
						columnName, groupName, getNumber(columnNumberString));

			}

		}
	}

	private Number getNumber(String columnNumberString) {
		try {
			if (columnNumberString.contains(".")) {
				// contains '.' -> assume a float
				return Float.parseFloat(columnNumberString);
			} else {
				// no '.' -> assume integer
				return Integer.parseInt(columnNumberString);
			}
		} catch (NumberFormatException e) {
			throw new FileFormatException("illegal number format: '"
					+ columnNumberString + "'");
		}

	}

	/**
	 * Read list file format.
	 * 
	 * @param fileName
	 *            Path to the .list file
	 * 
	 */
	public void loadFromFiles(File[] files) throws IOException,
			FileFormatException {
		read(files);
	}

	public String getGroupName(int idx) {
		return aGroups.get(idx);
	}

	public ColumnFileReaderModel() {
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see venn.AbstractVennDataModel#getNumGroups()
	 */
	public int getNumGroups() {
		return aGroups.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see venn.AbstractVennDataModel#getNumElements()
	 */
	public int getNumElements() {
		return aElements.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see venn.AbstractVennDataModel#getGroupElements(int)
	 */
	public BitSet getGroupElements(int groupID) {
		if (groupID < 0 || groupID >= getNumGroups())
			throw new IndexOutOfBoundsException("group ID out of bounds");

		return sets.get(groupID);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see venn.AbstractVennDataModel#getElementName(int)
	 */
	public String getElementName(int elementID) {

		return aElements.get(elementID);
	}

	/**
	 * A ListReadModel returns no properties
	 */
	public Object getGroupProperties(int groupID) {
		return null;
	}

	@Override
	public ElementProperties getElementProperties(int elementID) {
		return elementProperties.get(elementID);
	}

}
