package org.molgenis.autobetes.controller;

public class TimestampLastUpdate
{
	private long timestamp;
	
	
	public TimestampLastUpdate(long timestamp){
		this.timestamp = timestamp;
	}
	
	public long getTimestamp()
	{
		return timestamp;
	}
	public void setTimestamp(long timestamp){
		this.timestamp = timestamp;
	}
	
}
