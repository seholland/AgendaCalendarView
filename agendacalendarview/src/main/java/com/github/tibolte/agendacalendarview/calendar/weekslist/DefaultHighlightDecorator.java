package com.github.tibolte.agendacalendarview.calendar.weekslist;

import com.github.tibolte.agendacalendarview.models.IDayItem;

public class DefaultHighlightDecorator implements HighlightDecorator
{
	@Override
	public int getHighlightLineColor()
	{
		//The color doesn't matter here because we'll always return false for isHighlighted()
		return 0;
	}
	
	@Override
	public int getHighlightTextColor()
	{
		//The color doesn't matter here because we'll always return false for isHighlighted()
		return 0;
	}
	
	@Override
	public boolean highlightPast()
	{
		return false;
	}
	
	@Override
	public boolean isHighlighted(IDayItem dayItem)
	{
		return false;
	}
	
	@Override
	public boolean highlightMorning(IDayItem dayItem)
	{
		return false;
	}
	
	@Override
	public boolean highlightEvening(IDayItem dayItem)
	{
		return false;
	}
}
