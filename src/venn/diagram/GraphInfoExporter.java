package venn.diagram;

import java.io.IOException;
import java.io.Writer;
import java.util.BitSet;

/**
 * Visitor used to export the complete list of elements for each possible
 * intersection.
 * 
 * @author behrens
 */
public class GraphInfoExporter implements IIntersectionTreeVisitor {

	private VennArrangement arrangement;

	GraphInfoExporter(Writer writer, VennArrangement arrangement) {
		this.writer = writer;
		this.arrangement = arrangement;
	}

	Writer writer;

	@Override
	public void visit(int depth, IntersectionTreeNode node) {
		if (node.card > 0) {
			try {
				writer.append(getNodeInfo(node));
				writer.append("\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private String getNodeInfo(IntersectionTreeNode node) {

		return mapGroupSet(node.path) + " : "
				+ getSelectedNodeElementsString(node);

	}

	protected String mapGroupSet(BitSet set) {
		if (set == null)
			return "";
		StringBuffer buf = new StringBuffer();
		buf.append("{");
		for (int i = set.nextSetBit(0); i >= 0; i = set.nextSetBit(i + 1)) {
			buf.append(arrangement.getDataModel().getGroupName(i));
			if (i + 1 < set.length())
				buf.append(" , ");

		}
		buf.append("}");
		return buf.toString();
	}

	private String getSelectedNodeElementsString(IntersectionTreeNode node) {
		BitSet el = node.vennObject.getElements();

		StringBuffer buf = new StringBuffer();
		buf.append("{");
		for (int i = el.nextSetBit(0); i >= 0; i = el.nextSetBit(i + 1)) {
			buf.append(arrangement.getDataModel().getElementName(i));
			if (i + 1 < el.length())
				buf.append(" , ");
		}
		buf.append("}");

		String text = buf.toString();
		return text;
	}

}
