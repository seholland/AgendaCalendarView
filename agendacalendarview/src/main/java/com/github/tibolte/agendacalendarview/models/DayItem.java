package com.github.tibolte.agendacalendarview.models;

import com.github.tibolte.agendacalendarview.CalendarManager;
import com.github.tibolte.agendacalendarview.utils.DateHelper;

import java.util.Calendar;
import java.util.Date;

/**
 * Day model class.
 */
public class DayItem implements IDayItem
{
	private Calendar m_calendar;
	private boolean  mSelected = false;
	
	// region Constructor
	
	public DayItem(Calendar calendar)
	{
		m_calendar = Calendar.getInstance(CalendarManager.getInstance().getLocale());
		m_calendar.setTime(calendar.getTime());
	}
	
	public DayItem(DayItem original)
	{
		m_calendar = original.getCalendar();
		this.mSelected = original.isSelected();
	}
	// endregion
	
	// region Getters/Setters
	
	@Override
	public Date getDate()
	{
		return m_calendar.getTime();
	}
	
	@Override
	public void setDate(Date date)
	{
		m_calendar.setTime(date);
	}
	
	@Override
	public int getDayOfMonth()
	{
		return m_calendar.get(Calendar.DAY_OF_MONTH);
	}
	
	@Override
	public boolean isToday()
	{
		return DateHelper.sameDate(m_calendar, CalendarManager.getInstance().getToday());
	}
	
	@Override
	public boolean isSelected()
	{
		return mSelected;
	}
	
	@Override
	public void setSelected(boolean selected)
	{
		this.mSelected = selected;
	}
	
	@Override
	public boolean isFirstDayOfTheMonth()
	{
		return getDayOfMonth() == 1;
	}
	
	@Override
	public String getMonth()
	{
		return CalendarManager.getInstance().getMonthHalfNameFormat().format(m_calendar.getTime());
	}
	
	@Override
	public Calendar getCalendar()
	{
		Calendar calendar = Calendar.getInstance(CalendarManager.getInstance().getLocale());
		calendar.setTime(m_calendar.getTime());
		return calendar;
	}
	
	@Override
	public void setCalendar(Calendar calendar)
	{
		m_calendar = calendar;
	}
	// region Public methods
	
	// endregion
	
	@Override
	public String toString()
	{
		return "DayItem{" + "Calendar='" + m_calendar.toString() + ", value=" + getDayOfMonth() + '}';
	}
	
	@Override
	public IDayItem copy()
	{
		return new DayItem(this);
	}
	
	// endregion
}
