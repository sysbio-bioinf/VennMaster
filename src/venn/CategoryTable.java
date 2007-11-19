package venn;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.BitSet;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

import venn.db.AbstractGOCategoryProperties;
import venn.db.IVennDataModel;
import venn.gui.ExcelAdapter;
import venn.gui.VennPanel;

public class CategoryTable implements ListSelectionListener, ChangeListener {
	private JTable					categoryTable;
	private CategoryTableData		categoryTableModel;     // GO categories
    private VennPanel 				venn;
    private boolean 				isUpdating;
    private JList           selectionInfo;          // show infos about the current mouse selection
	@SuppressWarnings("unused")
	private ExcelAdapter	excelAdapter;           // <- don't touch this!! This is a decorator! It will get a compiler warning!
    private boolean 	isTableChanging;
    private JSplitPane 	catSplit;
    private TableColumn pValueCol,
    					nTotalCol;
    
	public CategoryTable(VennPanel venn) {
		this.venn = venn;
		
		categoryTableModel = new CategoryTableData(venn);

		DefaultTableColumnModel cm = new DefaultTableColumnModel();

		TableColumn col;
                
		col = new TableColumn(0,10);
        col.setHeaderValue("#");
        cm.addColumn(col);
        
        col = new TableColumn(1,18);
		col.setHeaderValue("Active");
		col.setCellEditor(new DefaultCellEditor(new JCheckBox()));
		col.setCellRenderer(new CheckboxRenderer(venn));
		cm.addColumn(col);
		
		col = new TableColumn(2,50);
		col.setHeaderValue("TermID");
		cm.addColumn(col);
        
        
        col = new TableColumn(3,195);
        col.setHeaderValue("Category");
        cm.addColumn(col);
        
        
		col = new TableColumn(4,50);
		col.setHeaderValue("nChanged");
		cm.addColumn(col);
		
        nTotalCol = new TableColumn(5,50);
//        nTotalCol.setHeaderValue("nTotal");
        cm.addColumn(nTotalCol);
        
        pValueCol = new TableColumn(6,50);
//        pValueCol.setHeaderValue("pValue");
        cm.addColumn(pValueCol);
        
//        col = new TableColumn(7,50);
//        col.setHeaderValue("FDR");
//        cm.addColumn(col);

        categoryTable = new JTable(categoryTableModel,cm);
		categoryTable.setSelectionBackground( Color.ORANGE );
		categoryTable.setSelectionForeground( Color.BLUE );

        categoryTable.setDefaultRenderer(Object.class, new CategoryTableRenderer( venn ) );
        categoryTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);		
        
        categoryTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        categoryTable.setColumnSelectionAllowed(true);
        categoryTable.setRowSelectionAllowed(true);
        
        categoryTable.getSelectionModel().addListSelectionListener( this );
        categoryTable.addMouseListener(
                new MouseAdapter() {
                    @Override
					public void mouseClicked(MouseEvent e) {
                        if( e.getClickCount() < 2 )
                            return;
                        JTable table = (JTable)e.getSource();
                        int row = table.rowAtPoint(e.getPoint()),
                            col = table.columnAtPoint(e.getPoint());
//                        System.out.println(row + ":"+col);
                        if( row>=0 && col>=0 )
                        {
                            col = table.convertColumnIndexToModel(col);
                            if( col == 2 )
                            {
                                CategoryTableData model = (CategoryTableData)table.getModel();
                                AbstractGOCategoryProperties props = model.getGoProperties(row);
                                if( props != null )
                                {
                                    BrowserControl.displayURL( createGoURL( props.getID() ) );
                                }
                            }
                        }
                    }
                }
        );

        selectionInfo = new JList(new DefaultListModel());
	  	selectionInfo.setEnabled(true);
        selectionInfo.setToolTipText("Double click for link to genecard");
        selectionInfo.addMouseListener(
                new MouseAdapter() {
                    @Override
					public void mouseClicked(MouseEvent e) {
                        if( e.getClickCount() < 2 )
                            return;
                        JList list = (JList)e.getSource();
                        Object obj = list.getSelectedValue();
                        if( obj != null )
                        {
                            String str = obj.toString();
                            if( isGeneName(str) )
                                BrowserControl.displayURL( createGenecardURL( str ) );
                        }
                    }
                }
                );        
        
        excelAdapter = new ExcelAdapter(categoryTable);

		catSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,   
				new JScrollPane(categoryTable),
				new JScrollPane(selectionInfo) );
		catSplit.setDividerLocation(170);

		venn.addChangeListener(this);
	}

	public void setParameters(AllParameters params) {
		setNTotalHeader(params);
		categoryTableModel.setParameters(params);
		repaint();
	}

	public JSplitPane getJSplitPane() {
		return catSplit;
	}

	public void repaint() {
		categoryTable.repaint();
	}
	
	public void setDataModel(IVennDataModel dataModel) {
		setPValueHeader(dataModel);
	}
	
	private void setPValueHeader(IVennDataModel model) {
		if (model == null) {
			return;
		}
		
		final int numGroups = model.getNumGroups();

		String label = null;
		for (int i = 0; i < numGroups; i++) {
			AbstractGOCategoryProperties prop = (AbstractGOCategoryProperties) model.getGroupProperties(i);
			if (label == null) {
				label = prop.getPFDRLabel();
			} else {
				if (! prop.getPFDRLabel().equals(label)) {
					throw new IllegalStateException();
				}
			}
		}
		
		if (label != null) {
			pValueCol.setHeaderValue(label);
		} else {
			pValueCol.setHeaderValue("---");
		}
	}
	
	private void setNTotalHeader(AllParameters params) {
		if (params.logTotals) {
	        nTotalCol.setHeaderValue("nTotal log" + Constants.WHICH_NTOTAL_LOG);
		} else {
	        nTotalCol.setHeaderValue("nTotal");
		}
	}
	
	/**
	 * set table selection from view selection
	 */
    private void updateCategoryTableSelection()
    {       
        // After adding a listener to the table selection model a
        // valueChanged event is fired. So the flag "isTableChanging" is
        // removed in the valueChanged() method of VennMaster.
        isTableChanging = true;
        categoryTable.getSelectionModel().removeListSelectionListener(this);
        
        try {
            categoryTable.getSelectionModel().setValueIsAdjusting(true);            
            BitSet bsel = venn.getUnfilteredSelection();
            categoryTable.clearSelection();
            categoryTable.setColumnSelectionInterval(0,categoryTable.getColumnCount()-1);
            for( int i=bsel.nextSetBit(0); i>=0; i=bsel.nextSetBit(i+1) )
            {
                categoryTable.addRowSelectionInterval(i,i);
            }            
            categoryTable.getSelectionModel().setValueIsAdjusting(false);
        }
        finally {
            categoryTable.getSelectionModel().addListSelectionListener(this);
        }        
    }

    /**
     * Category table changed its selection.
     */
	public void valueChanged(ListSelectionEvent e) 
    {
        if( e.getSource() == categoryTable.getSelectionModel() )
        {
            if( isUpdating || isTableChanging || e.getValueIsAdjusting() )
            {
                isTableChanging = false;
                return;
            }
            
            // set view selection from table selection
            BitSet set = new BitSet();
            int[] rows = categoryTable.getSelectedRows();
            for( int i=0; i<rows.length; ++i )
            {
                set.set( rows[i] );
            }
            venn.setUnfilteredSelection(set);
            
            updateSelectionInfo();
            
            return;
        }
        assert false;
    }

	/**
	 * A view changed its selection.
	 * 
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent event)
	{
		assert event.getSource() == venn;

		updateCategoryTableSelection(); // set table selection from view selection
        updateSelectionInfo();
		return;
	}

	
    /**
     *  Updates the selection info field.
     */
	private void updateSelectionInfo()
	{
        isUpdating = true;
        
        try {
    	
    		//selectionInfo.setText( venn.getSelectionInfo() );
    		//selectionInfo.moveCaretPosition(0);
            //selectionInfo.select(0,0);
            DefaultListModel listModel = (DefaultListModel)selectionInfo.getModel();
            
            String[] str = venn.getUnfilteredSelectionInfo().split("\n");
            listModel.clear();
            for( int i=0; i<str.length; ++i )
            {
                listModel.addElement(str[i]);
            }
        }
        finally {
            isUpdating = false;
        }
    }

	public void invalidate() {
		categoryTable.invalidate();
	}
	
    private boolean isGeneName(String str) 
    {
        if( str.length() <= 2 )
            return false;
        
        str = str.toUpperCase();
        
        
        char C = str.charAt(0);
               
        return C>='A' && C<='Z';
    }

    /**
     * Creates a link to the genecards.org website for the given gene.
     * @param gene
     * @return URL
     */
    private String createGenecardURL( String gene )
    {
        return "http://www.genecards.org/cgi-bin/carddisp?"+gene.trim();
    }

    /**
     * Creates a link to the godatabase.org website for the given GoID.
     * @param termID
     * @return URL
     */
	private String createGoURL(long termID) 
    {
        DecimalFormat format = new DecimalFormat("0000000");
        
        return "http://godatabase.org/cgi-bin/go.cgi?action=replace_tree&query=GO:" + format.format(termID);
    }

}
