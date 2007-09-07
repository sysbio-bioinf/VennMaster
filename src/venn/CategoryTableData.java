package venn;

import java.text.DecimalFormat;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;

import junit.framework.Assert;
import venn.db.GOCategoryProperties;
import venn.db.IVennDataModel;
import venn.gui.VennPanel;

/**
 *
 * This data table wrapper accesses the CategoryMapper object to draw
 * a table with categories and selections.
 *  
 * @author mueller
 *
 */
class CategoryTableData extends AbstractTableModel
    implements ChangeListener
{
	/**
     * 
     */
    private static final long serialVersionUID = 1L;
    VennPanel venn;
	
	public CategoryTableData(VennPanel venn)
	{
		this.venn = venn;
        venn.addChangeListener( this );
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount()
	{
		return 10;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount()
	{
		return venn.numOfCategories();
	}
    
    public GOCategoryProperties getGoProperties( int row )
    {
        IVennDataModel model = venn.getDataModel();
        if( model == null || model.getNumGroups() == 0 )
            return null;        
        return (GOCategoryProperties)model.getGroupProperties(row);
    }

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int rowIndex, int columnIndex)
	{
        // NumberFormat format = NumberFormat.getInstance();
        
        DecimalFormat format = new DecimalFormat("#0.0000");
        
        IVennDataModel model = venn.getDataModel();
        if( model == null || model.getNumGroups() == 0 )
            return null;
		
		if( rowIndex < 0 || rowIndex>= model.getNumGroups() )
			return null;
        
        GOCategoryProperties prop = (GOCategoryProperties)model.getGroupProperties(rowIndex);
        
		switch( columnIndex )
        {
            case 0:
                return new Integer(1+rowIndex);
        
			case 1: // activated
				return new Boolean( venn.getActivated(rowIndex) );

            case 2: // ID
                if( prop != null )
                    return new Long(prop.ID);
                else
                    return null;
                
			case 3: // category name
				return model.getGroupName(rowIndex);
                					
			case 4: // number of elements (genes) = nChanged
                int n = model.getGroupElements(rowIndex).cardinality();
                if( prop != null )
                    Assert.assertEquals(n,prop.nChange);
				return new Integer( n );

            case 5: // nTotal
                if( prop != null )
                    return new Integer(prop.nTotal);
                else
                    return null;
                
			case 6: // p-value
				if( prop != null )
					//return new Float(prop.pValue);
					return format.format(prop.pValue);
				else
					return null;
                
            case 7: // FDR
                if( prop != null )
                    //return new Float(prop.FDR);
                    return format.format(prop.FDR);
                else
                    return null;
                    
            case 8: // minDist
            	if( prop != null )
            	{
            		if( prop.meanDist >= 1 )
            			return new Float( prop.meanDist );
            	}
            	return null;
		}
		return null;
	}
	
	public void setValueAt(Object aValue, int rowIndex, int columnIndex )
	{
		if( columnIndex == 1 )
            venn.setActivated( rowIndex, ((Boolean)aValue).booleanValue() );
		else
			super.setValueAt(aValue,rowIndex,columnIndex);
	}
	
	
	public boolean isCellEditable(int row,int column)
	{
		if( column == 1 )
			return true;
		else
			return false;
	}

    public void stateChanged(ChangeEvent e)
    {
        // TODO?
        //fireTableDataChanged();
    }
}