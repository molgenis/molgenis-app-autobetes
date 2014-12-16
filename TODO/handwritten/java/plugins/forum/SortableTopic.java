package plugins.forum;

import lmd.Post;
import lmd.Topic;

public class SortableTopic implements Comparable<SortableTopic>
{
	private Topic t;
	private Post p;

	/**
	 * Sort topics on last post
	 * @param t
	 * @param p
	 */
	public SortableTopic(Topic t, Post p)
	{
		this.t = t;
		this.p = p;
	}
	
	public int compareTo(SortableTopic other)
	{
		return p.getCreated().compareTo(other.p.getCreated());
	}
	
	public Topic getTopic()
	{
		return t;
	}
}
