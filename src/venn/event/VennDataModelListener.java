/*
 * Created on 13.05.2005
 *
 */
package venn.event;

import java.util.EventListener;


/**
 * Event listener for IVennDataModel objects.
 * 
 * @author muellera
 *
 */
public interface VennDataModelListener extends EventListener {
    
    /**
     * This method is called when the data set changed.
     * @param e
     */
    void dataChanged( VennDataModelEvent e );
}
