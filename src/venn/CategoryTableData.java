package venn;

import java.text.DecimalFormat;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;

import junit.framework.Assert;
import sun.java2d.pipe.GeneralCompositePipe;
import venn.db.AbstractGOCategoryProperties;
import venn.db.GOCategoryProperties3p;
import venn.db.GOCategoryProperties3p3fdr;
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
    AllParameters params;
	
	public CategoryTableData(VennPanel venn)
	{
		this.venn = venn;
        venn.addChangeListener( this );
	}

	public void setParameters(AllParameters params) {
		this.params = params;
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
    
    public AbstractGOCategoryProperties getGoProperties( int row )
    {
        IVennDataModel model = venn.getDataModel();
        if( model == null )
            return null;        
        return (AbstractGOCategoryProperties) model.getGroupProperties(row);
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
        
        AbstractGOCategoryProperties prop = (AbstractGOCategoryProperties)model.getGroupProperties(rowIndex);
        
		switch( columnIndex )
        {
            case 0:
                return new Integer(1+rowIndex);
        
			case 1: // activated
				return new Boolean( venn.getActivated(rowIndex) );

            case 2: // ID
                if( prop != null )
                    return new Long(prop.getID());
                else
                    return null;
                
			case 3: // category name
				return model.getGroupName(rowIndex);
                					
			case 4: // number of elements (genes) = nChanged
                int n = model.getGroupElements(rowIndex).cardinality();
                if( prop != null )
                    Assert.assertEquals(n,prop.getNChange());
                if (params.logNumElements) {
                	n = AbstractGOCategoryProperties.log(n);
                }
				return new Integer( n );

            case 5: // nTotal
                if( prop != null )
                	if (params.logNumElements) {
                		return new Integer(prop.getNTotalLog());
                	}
                	else {
                		return new Integer(prop.getNTotal());
                	}
                else
                    return null;
                
			case 6: // p-value
				if( prop != null )
					//return new Float(prop.pValue);
//					return format.format(prop.pValue);
					return format.format(prop.getPFDRValue());
				else
					return null;
                
//            case 7: // FDR
//                if( prop != null && prop instanceof GOCategoryProperties3p3fdr)
//                    //return new Float(prop.FDR);
//                    return format.format(((GOCategoryProperties3p3fdr) prop).FDR);
//                else
//                    return null;
                    
            case 8: // minDist
            	if( prop != null )
            	{
            		if( prop.getMeanDist() >= 1 )
            			return new Float( prop.getMeanDist() );
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