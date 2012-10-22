/*CST425Lab3.Vivin Paliath & Paul Spaude, Oct 2012*/
package edu.asupoly.cst425.lab3.domain;

public class ConversationState
{
	private final String id;
	private final User user;
	private int pageNmbr;
	
	public ConversationState(String id, User user, int beginPageNmbr)
	{
		this.id = id;
		this.user = user;
		this.setPageNmbr(beginPageNmbr);
	}

	public User getUser() {
		return user;
	}

	public String getId() {
		return id;
	}

	public synchronized int getPageNmbr() {
		return pageNmbr;
	}

	public synchronized void setPageNmbr(int pageNmbr) {
		this.pageNmbr = pageNmbr;
	}

} //end class ConversationState 
