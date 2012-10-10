package venn.diagram;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import venn.db.IVennDataModel;

/**
 * Visitor used to export the complete list of elements for each possible
 * intersection.
 * 
 * @author behrens
 */
public class GraphInfoExporter implements IIntersectionTreeVisitor {

	public GraphInfoExporter(Writer writer, IVennDataModel dataModel) {
		this.writer = writer;
		this.dataModel = dataModel;
		intersections = new ArrayList<String>();
		elements = new ArrayList<List<String>>();
	}

	Writer writer;
	List<String> intersections;
	List<List<String>> elements;
	private IVennDataModel dataModel;

	@Override
	public void visit(int depth, IntersectionTreeNode node) {
		if (node.card > 0 && !node.copy) {

			intersections.add(getIntersectionName(node.path));
			elements.add(getNodeElementsList(node));
		}
	}

	protected String getIntersectionName(BitSet set) {
		if (set == null)
			return "";
		StringBuffer buf = new StringBuffer();
		buf.append("{");
		for (int i = set.nextSetBit(0); i >= 0; i = set.nextSetBit(i + 1)) {
			buf.append(dataModel.getGroupName(i));
			if (i + 1 < set.length())
				buf.append(" , ");

		}
		buf.append("}");
		return buf.toString();
	}

	private List<String> getNodeElementsList(IntersectionTreeNode node) {
		BitSet el = node.vennObject.getElements();

		ArrayList<String> elementsList = new ArrayList<String>();
		for (int i = el.nextSetBit(0); i >= 0; i = el.nextSetBit(i + 1)) {
			elementsList.add(dataModel.getElementName(i));
		}
		return elementsList;
	}

	public void writeAndClose() {
		try {
			writeHeader();
			writeElements();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void writeElements() throws IOException {
		int maxListSize = getMaxListSize();
		for (int i = 0; i < maxListSize; i++) {
			for (int j = 0; j < elements.size(); j++) {
				List<String> elementList = elements.get(j);
				try {
					writer.append(elementList.get(i));
				} catch (IndexOutOfBoundsException e) {
					// ignore
				}
				if (j < elements.size() - 1) {
					writer.append("\t");
				}
			}
			writer.append("\n");
		}
	}

	private int getMaxListSize() {

		int max = 0;
		for (List<String> list : elements) {
			max = max < list.size() ? list.size() : max;
		}
		return max;
	}

	private void writeHeader() throws IOException {
		String intersectionString = StringUtils.join(intersections, "\t");
		writer.append(intersectionString);
		writer.append("\n");

	}

}
