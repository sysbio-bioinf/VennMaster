/*
 * Created on 13.05.2005
 *
 */
package venn.event;

import java.util.EventObject;

/**
 * @author muellera
 * This event has to be fired if the VennDataModel changes its contents.
 *
 */
public class VennDataModelEvent extends EventObject {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param source
     */
    public VennDataModelEvent(Object source) {
        super(source);
    }    
}
