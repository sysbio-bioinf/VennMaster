package venn.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import venn.CategoryTable;
import venn.db.ColumnFileReaderModel;
import venn.db.IVennDataModel;

public class ThesholdPanel extends JPanel implements TableModelListener {
	private JSplitPane splitPane;
	private JTable thresholdTable;
	private JList thresholdInfo;
	private IVennDataModel dataModel;
	private VennPanel catTable;

	public ThesholdPanel(IVennDataModel dataModel, VennPanel venn) {
		super(new BorderLayout());
		this.dataModel = dataModel;
		this.catTable = venn;
		if (dataModel != null
				&& dataModel.getClass() == ColumnFileReaderModel.class) {
			initFull((ColumnFileReaderModel) dataModel);
		}
		initEmpty();
	}

	private void initEmpty() {
		this.removeAll();
	}

	public void setDataModel(IVennDataModel model) {
		this.dataModel = model;
		if (model != null && model.getClass() == ColumnFileReaderModel.class) {
			initFull((ColumnFileReaderModel) model);
		} else {
			initEmpty();
		}
	}

	private void initFull(ColumnFileReaderModel model) {
		this.removeAll();

		// int groupCount = dataModel.getNumGroups();

		thresholdInfo = new JList(new DefaultListModel());
		thresholdTable = new JTable();
		ThresholdTableModel tableModel = new ThresholdTableModel(model);
		tableModel.addTableModelListener(this);

		thresholdTable.setModel(tableModel);
		thresholdTable.setEnabled(true);
		// thresholdTable.setSelectionBackground(Color.ORANGE);
		// thresholdTable.setSelectionForeground(Color.BLUE);

		// thresholdTable.setDefaultRenderer(Object.class, new
		// CategoryTableRenderer( venn ) );
		thresholdTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

		// thresholdTable
		// .setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		// thresholdTable.setColumnSelectionAllowed(true);
		// thresholdTable.setRowSelectionAllowed(true);

		splitPane = new JSplitPane();
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(
				thresholdTable), new JScrollPane(thresholdInfo));
		splitPane.setDividerLocation(170);
		updateInfo();

		this.add(splitPane, BorderLayout.CENTER);

	}

	class ThresholdTableModel extends AbstractTableModel {
		private static final int MIN_THR_INDX = 5;
		private static final int MAX_THR_INDX = 6;
		private static final int CURR_SELECTED_IDX = 7;
		String[] colNames = { "Name", "Mean", "Median", "Max", "Min",
				"Thr. min", "Thr. max", "Count selected" };
		String[] groupNames;
		Number[][] data;// Number[row-1][column]
		private ColumnFileReaderModel model;

		public ThresholdTableModel(ColumnFileReaderModel model) {
			this.model = model;
			this.data = model.getTableData();
			this.groupNames = model.getExpressionPropertyNames();
		}

		@Override
		public int getRowCount() {
			return data.length;
		}

		@Override
		public int getColumnCount() {

			return colNames.length;
		}

		@Override
		public String getColumnName(int columnIndex) {
			return colNames[columnIndex];
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			// if (columnIndex == 0 || columnIndex == 7) {
			return String.class;
			// }
			// return Number.class;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {

			if (columnIndex > 4 && columnIndex < 7)
				return true;
			return false;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				return groupNames[rowIndex];
			}
			return data[rowIndex][columnIndex - 1].toString();
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			// only for editable fields min threshold, max threshold
			Number n;
			try {
				if (aValue.toString().contains(".")) {
					n = Float.parseFloat(aValue.toString());
				} else {
					n = Integer.parseInt(aValue.toString());
				}
			} catch (NumberFormatException e) {
				return;
			}

			if (columnIndex == MIN_THR_INDX) {
				model.setMinThreshold(rowIndex, n);
			} else if (columnIndex == MAX_THR_INDX) {
				model.setMaxThreshold(rowIndex, n);
			}
			model.filterUpdateSingle(rowIndex);
			data = model.getTableData();
			updateInfo();

			catTable.setActivated(0, false);

			fireTableCellUpdated(rowIndex, CURR_SELECTED_IDX);

			catTable.setActivated(0, true);

			fireTableCellUpdated(rowIndex, CURR_SELECTED_IDX);

		}

	}

	@Override
	public void tableChanged(TableModelEvent e) {

		// // System.out.println("EVENT: " + e.getFirstRow());
		// if (e.getColumn() == e.ALL_COLUMNS) {
		// ((ColumnFileReaderModel) this.dataModel).filterUpdateSingle(e
		// .getFirstRow());
		//
		// }

	}

	private void updateInfo() {

		// selectionInfo.setText( venn.getSelectionInfo() );
		// selectionInfo.moveCaretPosition(0);
		// selectionInfo.select(0,0);
		DefaultListModel listModel = (DefaultListModel) thresholdInfo
				.getModel();

		listModel.clear();
		listModel.addElement(getInfoString());

	}

	private String getInfoString() {

		return "Elements selected: "
				+ ((ColumnFileReaderModel) dataModel).getFilteredElementCount()
				+ "/"
				+ ((ColumnFileReaderModel) dataModel).getTotalElementCount();

	}

}
