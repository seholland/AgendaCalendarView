package com.github.tibolte.agendacalendarview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.tibolte.agendacalendarview.models.CalendarEvent;
import com.github.tibolte.agendacalendarview.models.DayItem;
import com.github.tibolte.agendacalendarview.models.IDayItem;
import com.github.tibolte.agendacalendarview.models.IWeekItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class manages information about the calendar. (Events, weather info...)
 * Holds reference to the days list of the calendar.
 * As the app is using several views, we want to keep everything in one place.
 */
public class CalendarManager
{

	private static final String LOG_TAG = CalendarManager.class.getSimpleName();

	@SuppressLint("StaticFieldLeak")
	private static CalendarManager mInstance;

	private final Context    mContext;
	private       Locale     mLocale;
	private Calendar         mToday = Calendar.getInstance();
	private SimpleDateFormat mWeekdayFormatter;
	private SimpleDateFormat mMonthHalfNameFormat;
	private String           mNoEventText;
	private boolean          mShowPlaceholders;

	/// instances of classes provided from outside
	private IWeekItem mCleanWeek;

	/**
	 * List of days used by the calendar
	 */
	private List<IDayItem>      mDays   = new ArrayList<>();
	/**
	 * List of weeks used by the calendar
	 */
	private List<IWeekItem>     mWeeks  = new ArrayList<>();
	/**
	 * HashMap of events instances
	 */
	private TreeMap<Calendar, ArrayList<CalendarEvent>> mEvents = new TreeMap<>();

	// region Constructors

	public CalendarManager(Context context)
	{
		this.mContext = context;
	}

	public static CalendarManager getInstance(Context context)
	{
		if(mInstance == null)
		{
			mInstance = new CalendarManager(context);
		}
		return mInstance;
	}

	public static CalendarManager getInstance()
	{
		return mInstance;
	}

	// endregion

	// region Getters/Setters

	public Locale getLocale()
	{
		return mLocale;
	}

	public Context getContext()
	{
		return mContext;
	}

	public Calendar getToday()
	{
		return mToday;
	}

	public void setToday(Calendar today)
	{
		this.mToday = today;
	}
	
	public void setShowPlaceholders(boolean showPlaceholders)
	{
		this.mShowPlaceholders = showPlaceholders;
	}

	public List<IWeekItem> getWeeks()
	{
		return mWeeks;
	}

	public TreeMap<Calendar, ArrayList<CalendarEvent>> getEvents()
	{
		return mEvents;
	}
	
	public ArrayList<CalendarEvent> getEvents(@NonNull IDayItem dayItem)
	{
		return getEvents(dayItem.getCalendar());
	}
	
	public ArrayList<CalendarEvent> getEvents(@NonNull Calendar calendar)
	{
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.AM_PM, 0);
		ArrayList<CalendarEvent> events = mEvents.get(calendar);
		if(events == null)
		{
			events = new ArrayList<>();
			mEvents.put(calendar, events);
		}
		return events;
	}
	
	public void setEvents(List<CalendarEvent> events)
	{
		mEvents.clear();
		for(CalendarEvent calendarEvent : events)
		{
			getEvents(calendarEvent.getInstanceDay()).add(calendarEvent);
		}
	}
	
	public ArrayList<CalendarEvent> getEventList()
	{
		long start = System.currentTimeMillis();
		ArrayList<CalendarEvent> events = new ArrayList<>();
		
		//The keys are sorted chronologically in the TreeMap
		for(Map.Entry<Calendar, ArrayList<CalendarEvent>> entry : mEvents.entrySet())
		{
			ArrayList<CalendarEvent> dayEvents = entry.getValue();
			if(dayEvents != null)
			{
				events.addAll(dayEvents);
			}
		}
		Log.i(LOG_TAG, "Generated event list in " + (System.currentTimeMillis() - start) + " ms.");
		
		return events;
	}
	
	public CalendarEvent getEventAt(int position)
	{
		for(Map.Entry<Calendar, ArrayList<CalendarEvent>> entry : mEvents.entrySet())
		{
			ArrayList<CalendarEvent> dayEvents = entry.getValue();
			if(dayEvents != null)
			{
				if(dayEvents.size() <= position)
				{
					position -= dayEvents.size();
					continue;
				}
				
				return dayEvents.get(position);
			}
		}
		
		return null;
	}
	
	public int getIndexForDay(@NonNull Calendar calendar)
	{
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.AM_PM, 0);
		
		int idx = -1;
		
		//The keys are sorted chronologically in the TreeMap
		for(Map.Entry<Calendar, ArrayList<CalendarEvent>> entry : mEvents.entrySet())
		{
			if(entry.getKey().equals(calendar))
			{
				idx++;
				break;
			}
			else
			{
				ArrayList<CalendarEvent> dayEvents = entry.getValue();
				if(dayEvents != null)
				{
					idx += dayEvents.size();
				}
			}
		}
		
		return idx;
	}

	public List<IDayItem> getDays()
	{
		return mDays;
	}

	public SimpleDateFormat getWeekdayFormatter()
	{
		return mWeekdayFormatter;
	}

	public SimpleDateFormat getMonthHalfNameFormat()
	{
		return mMonthHalfNameFormat;
	}

	// endregion

	// region Public methods

	public void buildCal(Calendar minDate, Calendar maxDate, Locale locale, IWeekItem cleanWeek, @Nullable String noEventText, boolean showNoEventText)
	{
		if(minDate == null || maxDate == null)
		{
			throw new IllegalArgumentException("minDate and maxDate must be non-null.");
		}
		if(minDate.after(maxDate))
		{
			throw new IllegalArgumentException("minDate must be before maxDate.");
		}
		if(locale == null)
		{
			throw new IllegalArgumentException("Locale is null.");
		}

		setLocale(locale);
		if(noEventText != null)
		{
			mNoEventText = noEventText;
		}
		else
		{
			mNoEventText = mContext.getString(R.string.agenda_event_no_events);
		}
		mShowPlaceholders = showNoEventText;

		mDays.clear();
		mWeeks.clear();
		mEvents.clear();

		mCleanWeek = cleanWeek;

		Calendar mMinCal      = Calendar.getInstance(mLocale);
		Calendar mMaxCal      = Calendar.getInstance(mLocale);
		Calendar mWeekCounter = Calendar.getInstance(mLocale);

		mMinCal.setTime(minDate.getTime());
		mMaxCal.setTime(maxDate.getTime());

		// maxDate is exclusive, here we bump back to the previous day, as maxDate if December 1st, 2020,
		// we don't include that month in our list
		mMaxCal.add(Calendar.MINUTE, -1);

		// Now iterate we iterate between mMinCal and mMaxCal so we build our list of weeks
		mWeekCounter.setTime(mMinCal.getTime());
		int maxMonth = mMaxCal.get(Calendar.MONTH);
		int maxYear  = mMaxCal.get(Calendar.YEAR);

		int currentMonth = mWeekCounter.get(Calendar.MONTH);
		int currentYear  = mWeekCounter.get(Calendar.YEAR);

		// Loop through the weeks
		while((currentMonth <= maxMonth // Up to, including the month.
		       || currentYear < maxYear) // Up to the year.
		      && currentYear < maxYear + 1)
		{ // But not > next yr.

			Date date = mWeekCounter.getTime();
			// Build our week list
			int currentWeekOfYear = mWeekCounter.get(Calendar.WEEK_OF_YEAR);

			IWeekItem weekItem = cleanWeek.copy();
			weekItem.setWeekInYear(currentWeekOfYear);
			weekItem.setYear(currentYear);
			weekItem.setDate(date);
			weekItem.setMonth(currentMonth);
			weekItem.setLabel(mMonthHalfNameFormat.format(date));
			List<IDayItem> dayItems = getDayCells(mWeekCounter); // gather days for the built week
			weekItem.setDayItems(dayItems);
			mWeeks.add(weekItem);

			//            Log.d(LOG_TAG, String.format("Adding week: %s", weekItem));

			mWeekCounter.add(Calendar.WEEK_OF_YEAR, 1);

			currentMonth = mWeekCounter.get(Calendar.MONTH);
			currentYear = mWeekCounter.get(Calendar.YEAR);
		}
	}

	public void loadEvents(List<CalendarEvent> eventList, CalendarEvent noEvent)
	{
		//Convert the event list to a TreeMap
		TreeMap<Calendar, ArrayList<CalendarEvent>> eventMap = new TreeMap<>();
		for(CalendarEvent calendarEvent : eventList)
		{
			ArrayList<CalendarEvent> dayEvents = eventMap.get(calendarEvent.getInstanceDay());
			if(dayEvents == null)
			{
				dayEvents = new ArrayList<>();
				eventMap.put(calendarEvent.getInstanceDay(), dayEvents);
			}
			
			dayEvents.add(calendarEvent);
		}
		
		for(IWeekItem weekItem : getWeeks())
		{
			for(IDayItem dayItem : weekItem.getDayItems())
			{
//				Log.i(LOG_TAG, "Loading: " + dayItem.getDate().toString());
				Calendar dayItemAllBalls = dayItem.copy().getCalendar();
				
				ArrayList<CalendarEvent> currentEvents = eventMap.get(dayItemAllBalls);
				if(currentEvents != null && currentEvents.size() > 0)
				{
					for(CalendarEvent calendarEvent : currentEvents)
					{
						CalendarEvent copy = calendarEvent.copy();
						
						Calendar dayInstance = Calendar.getInstance();
						dayInstance.setTime(dayItem.getDate());
						copy.setInstanceDay(dayInstance);
						copy.setDayReference(dayItem);
						copy.setWeekReference(weekItem);
						copy.setShowPlaceholders(mShowPlaceholders);
						// add instances in chronological order
						getEvents(dayItem).add(copy);
					}
				}
				else
				{
					Calendar dayInstance = Calendar.getInstance();
					dayInstance.setTime(dayItem.getDate());
					CalendarEvent copy = noEvent.copy();

					copy.setInstanceDay(dayInstance);
					copy.setDayReference(dayItem);
					copy.setWeekReference(weekItem);
					copy.setLocation("");
					copy.setTitle(mNoEventText);
					copy.setPlaceholder(true);
					copy.setShowPlaceholders(mShowPlaceholders);
					getEvents(dayItem).add(copy);
				}
			}
		}
	}

	public void loadCal(Locale locale, List<IWeekItem> lWeeks, List<IDayItem> lDays, List<CalendarEvent> lEvents)
	{
		mWeeks = lWeeks;
		mDays = lDays;
		setEvents(lEvents);
		setLocale(locale);
	}

	// endregion

	// region Private methods

	private List<IDayItem> getDayCells(Calendar startCal)
	{
		Calendar cal = Calendar.getInstance(mLocale);
		cal.setTime(startCal.getTime());
		List<IDayItem> dayItems = new ArrayList<>();

		int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		int offset         = cal.getFirstDayOfWeek() - firstDayOfWeek;
		if(offset > 0)
		{
			offset -= 7;
		}
		cal.add(Calendar.DATE, offset);

//		Log.d(LOG_TAG, String.format("Buiding row week starting at %s", cal.getTime()));
		for(int c = 0; c < 7; c++)
		{
			IDayItem dayItem = new DayItem(cal);
			dayItems.add(dayItem);
			cal.add(Calendar.DATE, 1);
		}

		mDays.addAll(dayItems);
		return dayItems;
	}

	private void setLocale(Locale locale)
	{
		this.mLocale = locale;
		setToday(Calendar.getInstance(mLocale));
		mWeekdayFormatter = new SimpleDateFormat(getContext().getString(R.string.day_name_format), mLocale);
		mMonthHalfNameFormat = new SimpleDateFormat(getContext().getString(R.string.month_half_name_format), locale);
	}

	// endregion
}
