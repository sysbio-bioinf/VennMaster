package venn;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import venn.diagram.IVennObject;
import venn.gui.VennPanel;

/**
 * Renderer for the category table checkboxes.
 * 
 * @author muellera
 *
 */
class CheckboxRenderer 
	implements TableCellRenderer
{        
    private VennPanel venn;
	
	public CheckboxRenderer( VennPanel venn )
	{
        this.venn = venn;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{           
        JCheckBox box = new JCheckBox();
        
        box.setOpaque(true);

        if( value != null )
            box.setSelected(((Boolean)value).booleanValue());
        else
            box.setSelected( false );

        if( isSelected )
        {
            box.setBackground( table.getSelectionBackground() );
            box.setForeground( table.getSelectionForeground() );
        }
        else
        {
        	if (! venn.existsFilteredVennObject(row)) {
        		box.setBackground(Color.WHITE);
        		box.setForeground(Color.BLACK);
        	} else {
        		IVennObject vo = venn.getUnfilteredVennObject(row);
        		Color col = vo.getFillColor();
        		box.setBackground( col );
        		box.setForeground( Color.BLACK );
        	}
        }
        
        
		return box;
	}
	
}