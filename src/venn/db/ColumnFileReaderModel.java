package venn.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import venn.event.IFilterChainSucc;
import venn.event.IFilterUser;
import venn.geometry.FileFormatException;

public class ColumnFileReaderModel extends AbstractVennDataModel implements
		IFilterChainSucc, IFilterUser {

	private ArrayList<String> aElements; // array of aElements (key names)
	ArrayList<String> aGroups; // array of aGroups (group names)
	ArrayList<String> expressionProperties;
	private ArrayList<BitSet> sets; // contains one BitSet for each group
	// the index of a set bit corresponds to the index in
	// aElements

	ArrayList<ElementProperties> elementProperties; // per element

	// private Set<String> columnNames = new HashSet<String>();
	private int columnCount;
	private Integer groupKeyIdx = 0; // running index for groups in aGroups
	private Integer elementKeyIdx = 0; // running index for groups in aElements

	// these are manually modified
	private Number[] maxThreshold;
	private Number[] minThreshold;

	ArrayList<DescriptiveStatistics> statsPerExpressionProperty; // statistics
																	// per
																	// ExpressionProperty

	// the filtered element per expressionPropertyIndex, represented by their
	// indices
	ArrayList<BitSet>[] filteredElementsPerExpressionProperty;

	// elements passing all filtering steps.
	ArrayList<BitSet> allFilteredElements;
	private Integer totalElementCount;

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
			expressionProperties = new ArrayList<String>(headerLength - 1);
			for (int i = 1; i < splitHeader.length; i++) {
				expressionProperties.add(splitHeader[i]);
			}
		} else {
			if (headerLength != columnCount) {
				throw new FileFormatException("incorrect column count", file);
			}
			ArrayList<String> newExpressionProperties = new ArrayList<String>(
					headerLength - 1);
			for (int i = 1; i < splitHeader.length; i++) {
				newExpressionProperties.add(splitHeader[i]);
			}
			if (!expressionProperties.equals(newExpressionProperties)) {
				throw new FileFormatException("inconsistent column names", file);
			}

		}
		String groupName = splitHeader[0];
		// for (int i = 1; i < splitHeader.length; i++) {
		// columnNames.add(splitHeader[i]);
		// }

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
				ElementProperties ep = new ElementProperties(elementName,
						DiffExprValue.UNKNOWN, null);
				// ep.setExpressionPropertiesSize(expressionProperties.size(),aGroups.size());
				elementProperties.add(ep);
				aElements.add(elementName);
			}
			sets.get(groupIdx.get(groupName)).set(elementsIdx.get(elementName));

			// fill expression properties for this element
			for (int i = 1; i < splitLine.length; i++) {
				String expressionPropertyNumberString = splitLine[i];
				elementProperties.get(elementsIdx.get(elementName))
						.setExpressionPropertyValue(i - 1,
								groupIdx.get(groupName),
								getNumber(expressionPropertyNumberString));
			}

		}

		init();
	}

	private void init() {
		filteredElementsPerExpressionProperty = new ArrayList[expressionProperties
				.size()];
		for (int i = 0; i < filteredElementsPerExpressionProperty.length; i++) {
			filteredElementsPerExpressionProperty[i] = deepCopy(sets);
		}

		allFilteredElements = deepCopy(sets);

		calculateStatInfo();
		setInitThesholds();
		

	}

	private void setInitThesholds() {
		maxThreshold = new Number[expressionProperties.size()];
		minThreshold = new Number[expressionProperties.size()];
		for (int i = 0; i < expressionProperties.size(); i++) {
			maxThreshold[i] = statsPerExpressionProperty.get(i).getMax();
			minThreshold[i] = statsPerExpressionProperty.get(i).getMin();
		}

	}

	private ArrayList<BitSet> deepCopy(ArrayList<BitSet> original) {

		ArrayList<BitSet> copy = new ArrayList<BitSet>(original.size());
		for (BitSet bitSet : original) {
			BitSet copyBitSet = new BitSet(bitSet.size());
			copyBitSet.or(bitSet);
			copy.add(copyBitSet);
		}

		return copy;

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

	private void calculateStatInfo() {
		statsPerExpressionProperty = new ArrayList<DescriptiveStatistics>(
				expressionProperties.size());
		for (int i = 0; i < expressionProperties.size(); i++) {
			DescriptiveStatistics stats = new DescriptiveStatistics();
			// ArrayList<Number> valuesPerExpressionProperty = new
			// ArrayList<Number>();
			for (int j = 0; j < aGroups.size(); j++) {
				for (int k = 0; k < aElements.size(); k++) {
					Number value = elementProperties.get(k)
							.getExpressionPropertyValue(i, j);
					if (value != null) {
						stats.addValue(value.doubleValue());
						// SummaryStatistics
						// valuesPerExpressionProperty.add(value);
					}
				}
			}
			statsPerExpressionProperty.add(stats);
		}
	}

	// private void setStatInfo(ArrayList<Number> valuesPerExpressionProperty,
	// int i) {
	// Number localMax = valuesPerExpressionProperty.get(0);
	// Number localMin = localMax;
	//
	//
	// for (int i = 1; i < array.length; i++) {
	//
	// }
	// for (Number number : valuesPerExpressionProperty) {
	//
	// }
	//
	// }

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

		// return sets.get(groupID);
		return allFilteredElements.get(groupID);
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

	public int getFilteredElementCount() {

		int counter = 0;
		for (BitSet bitSet : allFilteredElements) {
			counter += bitSet.cardinality();
		}
		return counter;
	}

	public int getTotalElementCount() {

		if (totalElementCount == null) {
			int counter = 0;
			for (BitSet bitSet : sets) {
				counter += bitSet.cardinality();
			}
			totalElementCount = counter;
		}
		return totalElementCount;
	}

	/**
	 * get the data needed for the thresholdPanel in the GUI
	 * 
	 * @return
	 */
	public Number[][] getTableData() {

		Number[][] tableData = new Number[expressionProperties.size()][7];

		// String[] colNames = { "Name", "Mean", "Median", "Max", "Min",
		// "Thr. min", "Thr. max", "Count selected" };

		for (int expressionPropertyIndex = 0; expressionPropertyIndex < tableData.length; expressionPropertyIndex++) {
			tableData[expressionPropertyIndex][0] = getMean(expressionPropertyIndex);
			tableData[expressionPropertyIndex][1] = getMedian(expressionPropertyIndex);
			tableData[expressionPropertyIndex][2] = getMax(expressionPropertyIndex);
			tableData[expressionPropertyIndex][3] = getMin(expressionPropertyIndex);
			tableData[expressionPropertyIndex][4] = getMinThreshold(expressionPropertyIndex);// set
																								// to
																								// old
																								// value
			tableData[expressionPropertyIndex][5] = getMaxThreshold(expressionPropertyIndex);// set
																								// to
																								// old
																								// value
			tableData[expressionPropertyIndex][6] = getCurrentlySelectedCount(expressionPropertyIndex);

		}

		return tableData;

	}

	public Number getMaxThreshold(int expressionPropertyIndex) {
		return this.maxThreshold[expressionPropertyIndex];
	}

	public Number getMinThreshold(int expressionPropertyIndex) {
		return this.minThreshold[expressionPropertyIndex];
	}

	/**
	 * this will not update the filtering. the caller has to make sure he calls
	 * {@link ColumnFileReaderModel#filterUpdateSingle(int), filterUpdateSingle}
	 * or {@link ColumnFileReaderModel#filterUpdateAll(), filterUpdateAll()}
	 * after the update of thresholds.
	 * 
	 * @param expressionPropertyIndex
	 * @param value
	 */
	public void setMaxThreshold(int expressionPropertyIndex, Number value) {
		this.maxThreshold[expressionPropertyIndex] = value;
	}

	public void setMinThreshold(int expressionPropertyIndex, Number value) {

		this.minThreshold[expressionPropertyIndex] = value;
	}

	public Number getCurrentlySelectedCount(int expressionPropertyIndex) {

		BitSet allElements = projection(filteredElementsPerExpressionProperty[expressionPropertyIndex]);// projection
																										// groups.
		return allElements.cardinality();
	}

	private BitSet projection(ArrayList<BitSet> sets) {

		BitSet projBitSet = new BitSet(sets.get(0).size());
		for (BitSet bitSet : sets) {
			projBitSet.or(bitSet);
		}

		return projBitSet;
	}

	public Number getMin(int expressionPropertyIndex) {
		return statsPerExpressionProperty.get(expressionPropertyIndex).getMin();
	}

	public Number getMax(int expressionPropertyIndex) {
		return statsPerExpressionProperty.get(expressionPropertyIndex).getMax();
	}

	public Number getMedian(int expressionPropertyIndex) {
		return statsPerExpressionProperty.get(expressionPropertyIndex)
				.getPercentile(0.5);
	}

	public Number getMean(int expressionPropertyIndex) {
		return statsPerExpressionProperty.get(expressionPropertyIndex)
				.getMean();
	}

	public String[] getExpressionPropertyNames() {

		return expressionProperties.toArray(new String[1]);
	}

	/**
	 * will filter the model based on the currently set min and max threshold
	 * values for all expression properties
	 */
	public void filterUpdateAll() {

		for (int i = 0; i < expressionProperties.size(); i++) {
			filterUpdate(i);
		}
		updateGrandTotalFilter();
		notifySucc();

	}

	/**
	 * 
	 * will filter and update the model based on the currently set min and max
	 * threshold values for a single given expression property.
	 * 
	 * @param expressionPropertyIndex
	 */
	public void filterUpdateSingle(int expressionPropertyIndex) {

		filterUpdate(expressionPropertyIndex);
		updateGrandTotalFilter();
		notifySucc();

	}

	private void filterUpdate(int expressionPropertyIndex) {

		// ArrayList<BitSet> newSet = new ArrayList<BitSet>();
		// BitSet newBitSet = new BitSet(aElements.size());
		// newSet.add()
		ArrayList<BitSet> filteredBitset = filteredElementsPerExpressionProperty[expressionPropertyIndex];
		for (int groupIdx = 0; groupIdx < aGroups.size(); groupIdx++) {
			BitSet groupBitSet = filteredBitset.get(groupIdx);
			for (int elementIndex = 0; elementIndex < this.aElements.size(); elementIndex++) {
				ElementProperties elementProperty = this.elementProperties
						.get(elementIndex);
				Number value = elementProperty.getExpressionPropertyValue(
						expressionPropertyIndex, groupIdx);

				if (value != null
						&& value.floatValue() >= minThreshold[expressionPropertyIndex]
								.floatValue()
						&& value.floatValue() <= maxThreshold[expressionPropertyIndex]
								.floatValue()) {

					groupBitSet.set(elementIndex);
				} else {
					groupBitSet.clear(elementIndex);
				}
			}
		}

	}

	private void updateGrandTotalFilter() {
		// this is essentially the intersection of all filtered Elements per
		// expressionIndex

		for (int groupIdx = 0; groupIdx < aGroups.size(); groupIdx++) {
			allFilteredElements.get(groupIdx).set(0, aElements.size());
			// set all bits so we can use logical and for each expression
			// property to generate filtered bitset.
		}
		for (ArrayList<BitSet> bitSet : filteredElementsPerExpressionProperty) {
			for (int i = 0; i < aGroups.size(); i++) {
				allFilteredElements.get(i).and(bitSet.get(i));
			}
		}

	}

	//
	/**
	 * 
	 * generates a HashSet containing the sequence of integers from 0 to @param
	 * size -1
	 * 
	 * @param size
	 * @return
	 */
	private HashSet<Integer> sequence(int size) {
		HashSet<Integer> set = new HashSet<Integer>(size);

		for (int i = 0; i < size; i++) {
			set.add(i);
		}
		return set;
	}

	@Override
	public void filterChanged() {
		filterUpdateAll();
	}

	@Override
	public void predChanged() {
		filterUpdateAll();
	}
}
