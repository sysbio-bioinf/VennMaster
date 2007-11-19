package venn.event;



public class DuplFilterChainSucc implements IFilterChainSucc {
	private IFilterChainSucc succ1, succ2;
	
	public DuplFilterChainSucc(IFilterChainSucc succ1, IFilterChainSucc succ2) {
		this.succ1 = succ1;
		this.succ2 = succ2;
	}
	
//	@Override
	public void predChanged() {
		if (succ1 != null) {
			succ1.predChanged();
		}
		if (succ2 != null) {
			succ2.predChanged();
		}
	}

}
