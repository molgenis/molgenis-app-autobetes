package org.molgenis.autobetes.pumpobjectsparser;

public class TimeOffset
{
public long tsOfCorrection;
public int timeOffset;
/**
 * Indicate the timeoffset in hours(timeOffset) at a certain time(tsOfCorrection)
 * @param tsOfCorrection
 * @param timeOffset
 */
public TimeOffset(long tsOfCorrection, int timeOffset)
{
	super();
	this.tsOfCorrection = tsOfCorrection;
	this.timeOffset = timeOffset;
}
@Override
public String toString()
{
	return "TimeOffset [tsOfCorrection=" + tsOfCorrection + ", timeOffset=" + timeOffset + "]";
}

}
