package venn;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import venn.gui.VennPanel;

/**
 * Beautiful colors for the table background.
 * @author muellera
 *
 */
class CategoryTableRenderer 
implements TableCellRenderer
{
    private static final long serialVersionUID = 1L;
    private VennPanel venn;
    
    public CategoryTableRenderer( VennPanel venn )
    {
        this.venn = venn;
    }

    // TODO: use a JLabel pool to recycle used labels!
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) 
    {            
        JLabel  label = new JLabel();
        label.setOpaque(true);
        if( value != null )
            label.setText( value.toString() );
        else
            label.setText("");
        
        if( isSelected )
        {
            label.setBackground( table.getSelectionBackground() );
            label.setForeground( table.getSelectionForeground() );
        }
        else
        {
            Color col = venn.getVennObject(row).getFillColor();
            label.setBackground( col );
            label.setForeground( Color.BLACK );
        }
        if( hasFocus )
        {
            label.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        }
        int modelColumn = table.convertColumnIndexToModel( column );
        if( modelColumn == 2 )
        {
            label.setToolTipText("Double click for geneontology.org");
        }

        return label;
    }
    
}