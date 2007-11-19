/*
 * Created on 17.02.2006
 *
 */
package venn.db;

import venn.event.IFilterUser;
import venn.utility.SystemUtility;

public abstract class AbstractDataFilter
implements IDataFilter
{
    private IFilterUser user;

    public abstract boolean accept(int groupID);
    
    
//    public Object clone()
//    {
//        return SystemUtility.serialClone(this);
//    }
    
    @Override
	public Object clone() {
    	try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e.toString());
		}
    }
    
//    @Override
    public void setUser(IFilterUser user) {
    	if (this.user != null && user != null) {
    		throw new IllegalStateException(); // user must first be set to null before a new user can be set
    	}
    	this.user = user;
    }
    
    protected void notifyUser() {
    	if (user != null) {
    		user.filterChanged();
    	}
    }
}
