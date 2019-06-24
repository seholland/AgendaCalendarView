package com.github.tibolte.agendacalendarview.agenda;

import com.github.tibolte.agendacalendarview.CalendarManager;
import com.github.tibolte.agendacalendarview.models.CalendarEvent;
import com.github.tibolte.agendacalendarview.utils.DateHelper;

import android.content.Context;
import android.util.AttributeSet;

import java.util.Calendar;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * StickyListHeadersListView to scroll chronologically through events.
 */
public class AgendaListView extends StickyListHeadersListView {
	
	// region Constructors
	
	public AgendaListView(Context context) {
		super(context);
	}
	
	public AgendaListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public AgendaListView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	
	// endregion
	
	// region Public methods
	
	public void scrollToCurrentDate(Calendar today)
	{
		final int finalToIndex = CalendarManager.getInstance().getIndexForDay(today);
		post(()->setSelection(finalToIndex));
	}
	
	public List<CalendarEvent> getEvents()
	{
		try
		{
			return ((AgendaAdapter) getAdapter()).getEvents();
		}
		catch(Throwable tr)
		{
			return null;
		}
	}
	
	// endregion
}
