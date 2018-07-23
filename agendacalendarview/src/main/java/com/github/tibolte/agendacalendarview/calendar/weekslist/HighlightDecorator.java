package com.github.tibolte.agendacalendarview.calendar.weekslist;

import android.support.annotation.ColorInt;

import com.github.tibolte.agendacalendarview.models.IDayItem;

/**
 * Instances of this interface are used to manage highlighting spans of days in the WeekListView
 */
public interface HighlightDecorator
{
	@ColorInt int getHighlightLineColor();
	@ColorInt int getHighlightTextColor();
	boolean highlightPast();
	boolean isHighlighted(IDayItem dayItem);
	boolean highlightMorning(IDayItem dayItem);
	boolean highlightEvening(IDayItem dayItem);
}
