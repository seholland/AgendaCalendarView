package com.github.tibolte.agendacalendarview.agenda;

import com.github.tibolte.agendacalendarview.CalendarManager;
import com.github.tibolte.agendacalendarview.R;
import com.github.tibolte.agendacalendarview.utils.DateHelper;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Header view for the StickyHeaderListView of the agenda view
 */
public class AgendaHeaderView extends LinearLayout
{
	
	public static AgendaHeaderView inflate(ViewGroup parent)
	{
		return (AgendaHeaderView) LayoutInflater.from(parent.getContext()).inflate(R.layout.view_agenda_header, parent, false);
	}
	
	// region Constructors
	
	public AgendaHeaderView(Context context)
	{
		super(context);
	}
	
	public AgendaHeaderView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}
	
	public AgendaHeaderView(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
	}
	
	// endregion
	
	// region Public methods
	
	public void setDay(Calendar day, int currentDayTextColor, int currentDayColor, LayoutStyle style, Drawable wideDateDrawable)
	{
		View     compactLayout = findViewById(R.id.compact_layout);
		View     wideLayout    = findViewById(R.id.wide_layout);
		Calendar today         = CalendarManager.getInstance().getToday();
		
		switch(style)
		{
			case compact:
			{
				compactLayout.setVisibility(VISIBLE);
				wideLayout.setVisibility(GONE);
				
				TextView txtDayOfMonth = findViewById(R.id.view_agenda_day_of_month);
				TextView txtDayOfWeek  = findViewById(R.id.view_agenda_day_of_week);
				View     circleView    = findViewById(R.id.view_day_circle_selected);
				
				SimpleDateFormat dayWeekFormatter = new SimpleDateFormat(getContext().getString(R.string.day_name_format), CalendarManager.getInstance().getLocale());
				txtDayOfMonth.setTextColor(getResources().getColor(R.color.calendar_text_default));
				txtDayOfWeek.setTextColor(getResources().getColor(R.color.calendar_text_default));
				
				if(DateHelper.sameDate(day, today))
				{
					txtDayOfMonth.setTextColor(currentDayTextColor);
					txtDayOfWeek.setTextColor(currentDayTextColor);
					circleView.setVisibility(VISIBLE);
					GradientDrawable drawable = (GradientDrawable) circleView.getBackground();
					drawable.setColor(currentDayColor);
					drawable.setStroke((int) (2 * Resources.getSystem().getDisplayMetrics().density), currentDayColor);
				}
				else
				{
					circleView.setVisibility(INVISIBLE);
				}
				
				txtDayOfMonth.setText(String.valueOf(day.get(Calendar.DAY_OF_MONTH)));
				txtDayOfWeek.setText(dayWeekFormatter.format(day.getTime()));
			}
			break;
			case wide:
			{
				compactLayout.setVisibility(GONE);
				wideLayout.setVisibility(VISIBLE);
				
				SimpleDateFormat dayWeekFormatter = new SimpleDateFormat(getContext().getString(R.string.wide_date_name_format), CalendarManager.getInstance().getLocale());
				
				if(DateHelper.sameDate(day, today))
				{
					dayWeekFormatter = new SimpleDateFormat(getContext().getString(R.string.wide_today_date_name_format), CalendarManager.getInstance().getLocale());
				}
				
				TextView  wideDate           = findViewById(R.id.wide_date);
				ImageView wideDateDecoration = findViewById(R.id.wide_date_decoration);
				
				wideDate.setText(dayWeekFormatter.format(day.getTime()));
				wideDateDecoration.setImageDrawable(wideDateDrawable);
			}
		}
	}
	
	// endregion
	
	public enum LayoutStyle
	{
		compact,
		wide
	}
}
