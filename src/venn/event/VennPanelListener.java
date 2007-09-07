/*
 * Created on 13.05.2005
 *
 */
package venn.event;


/**
 * @author muellera
 *
 */
public interface VennPanelListener {
    
    /**
     * A new group set was selected with a mouse click.
     * 
     * @param e The selection event.
     */
    void groupSelected( GroupSelectionEvent e );
    
    /**
     * The mouse was hold above a group set (this event will be fired if the tooltip 
     * text appears - or would appear if its deactivated).
     * 
     * @param e
     */
    void groupHighlight( GroupSelectionEvent e );
}
