package com.github.tibolte.agendacalendarview.calendar.weekslist;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.tibolte.agendacalendarview.CalendarManager;
import com.github.tibolte.agendacalendarview.R;
import com.github.tibolte.agendacalendarview.models.CalendarEvent;
import com.github.tibolte.agendacalendarview.models.IDayItem;
import com.github.tibolte.agendacalendarview.models.IWeekItem;
import com.github.tibolte.agendacalendarview.utils.BusProvider;
import com.github.tibolte.agendacalendarview.utils.DateHelper;
import com.github.tibolte.agendacalendarview.utils.Events;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class WeeksAdapter extends RecyclerView.Adapter<WeeksAdapter.WeekViewHolder>
{
	
	public static final long FADE_DURATION = 250;
	
	private final Context            mContext;
	private final Calendar           mToday;
	private final List<IWeekItem>    mWeeksList          = new ArrayList<>();
	private       boolean            mDragging;
	private       boolean            mAlphaSet;
	private @ColorInt
	final         int                mDayTextColor;
	private @ColorInt
	final         int                mPastDayTextColor;
	private @ColorInt
	final         int                mCurrentDayColor;
	private @ColorInt
	final         int                mCurrentDayCircleColor;
	private @NonNull
				  HighlightDecorator mHighlightDecorator = new DefaultHighlightDecorator();
	
	// region Constructor
	
	public WeeksAdapter(Context context, Calendar today, int dayTextColor, int currentDayTextColor, int currentDayCircleColor, int pastDayTextColor)
	{
		this.mToday = today;
		this.mContext = context;
		this.mDayTextColor = dayTextColor;
		this.mCurrentDayColor = currentDayTextColor;
		this.mCurrentDayCircleColor = currentDayCircleColor;
		this.mPastDayTextColor = pastDayTextColor;
	}
	
	// endregion
	
	public void updateWeeksItems(List<IWeekItem> weekItems)
	{
		this.mWeeksList.clear();
		this.mWeeksList.addAll(weekItems);
		notifyDataSetChanged();
	}
	
	// region Getters/setters
	
	public List<IWeekItem> getWeeksList()
	{
		return mWeeksList;
	}
	
	public boolean isDragging()
	{
		return mDragging;
	}
	
	public void setDragging(boolean dragging)
	{
		if(dragging != this.mDragging)
		{
			this.mDragging = dragging;
			notifyItemRangeChanged(0, mWeeksList.size());
		}
	}
	
	public boolean isAlphaSet()
	{
		return mAlphaSet;
	}
	
	public void setAlphaSet(boolean alphaSet)
	{
		mAlphaSet = alphaSet;
	}
	
	public void setHighlightDecorator(@NonNull HighlightDecorator highlightDecorator)
	{
		mHighlightDecorator = highlightDecorator;
	}
	
	// endregion
	
	// region RecyclerView.Adapter<WeeksAdapter.WeekViewHolder> methods
	
	@NonNull
	@Override
	public WeekViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
	{
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_week, parent, false);
		return new WeekViewHolder(view);
	}
	
	@Override
	public void onBindViewHolder(@NonNull WeekViewHolder weekViewHolder, int position)
	{
		IWeekItem weekItem = mWeeksList.get(position);
		weekViewHolder.bindWeek(weekItem, mToday);
	}
	
	@Override
	public int getItemCount()
	{
		return mWeeksList.size();
	}
	
	// endregion
	
	// region Class - WeekViewHolder
	
	public class WeekViewHolder extends RecyclerView.ViewHolder
	{
		
		/**
		 * List of layout containers for each day
		 */
		private       List<ConstraintLayout> mCells;
		private final TextView               mTxtMonth;
		private final FrameLayout            mMonthBackground;
		
		public WeekViewHolder(View itemView)
		{
			super(itemView);
			mTxtMonth = itemView.findViewById(R.id.month_label);
			mMonthBackground = itemView.findViewById(R.id.month_background);
			LinearLayout daysContainer = itemView.findViewById(R.id.week_days_container);
			setUpChildren(daysContainer);
		}
		
		public void bindWeek(IWeekItem weekItem, Calendar today)
		{
			setUpMonthOverlay();
			
			List<IDayItem> dayItems = weekItem.getDayItems();
			
			for(int c = 0; c < dayItems.size(); c++)
			{
				final IDayItem   dayItem        = dayItems.get(c);
				ConstraintLayout cellItem       = mCells.get(c);
				TextView         txtDay         = cellItem.findViewById(R.id.view_day_day_label);
				TextView         txtMonth       = cellItem.findViewById(R.id.view_day_month_label);
				View             circleView     = cellItem.findViewById(R.id.view_day_circle_selected);
				View             eventIndicator = cellItem.findViewById(R.id.view_day_events_scheduled);
				cellItem.setOnClickListener(v -> BusProvider.getInstance().send(new Events.DayClickedEvent(dayItem)));
				View timelineAM = cellItem.findViewById(R.id.line_left);
				View timelinePM = cellItem.findViewById(R.id.line_right);
				
				txtMonth.setVisibility(View.GONE);
				txtDay.setTextColor(mDayTextColor);
				txtMonth.setTextColor(mDayTextColor);
				circleView.setVisibility(View.GONE);
				timelineAM.setVisibility(View.GONE);
				timelinePM.setVisibility(View.GONE);
				
				txtDay.setTypeface(null, Typeface.NORMAL);
				txtMonth.setTypeface(null, Typeface.NORMAL);
				
				GradientDrawable eventIndicatorDrawable = (GradientDrawable) eventIndicator.getBackground();
				eventIndicatorDrawable.setColor(mCurrentDayCircleColor);
				
				// Display the day
				txtDay.setText(Integer.toString(dayItem.getDayOfMonth()));
				
				// Highlight first day of the month
				if(dayItem.isFirstDayOfTheMonth() && !dayItem.isSelected())
				{
					txtMonth.setVisibility(View.VISIBLE);
					txtMonth.setText(dayItem.getMonth());
					txtDay.setTypeface(null, Typeface.BOLD);
					txtMonth.setTypeface(null, Typeface.BOLD);
				}
				
				// Check if this day is in the past
				boolean isPast = today.getTime().after(dayItem.getDate()) && !DateHelper.sameDate(today, dayItem.getDate());
				if(isPast)
				{
					txtDay.setTextColor(mPastDayTextColor);
					txtMonth.setTextColor(mPastDayTextColor);
				}
				
				// Show a circle if the day is selected or highlighted
				if(dayItem.isSelected())
				{
					txtDay.setTextColor(mCurrentDayColor);
					circleView.setVisibility(View.VISIBLE);
					GradientDrawable drawable = (GradientDrawable) circleView.getBackground();
					drawable.setColor(mCurrentDayCircleColor);
					eventIndicatorDrawable.setColor(mCurrentDayCircleColor);
				}
				else if(mHighlightDecorator.isHighlighted(dayItem))
				{
					txtDay.setTextColor(mHighlightDecorator.getHighlightTextColor());
					circleView.setVisibility(View.VISIBLE);
					GradientDrawable drawable = (GradientDrawable) circleView.getBackground();
					drawable.setColor(mHighlightDecorator.getHighlightLineColor());
					eventIndicatorDrawable.setColor(mHighlightDecorator.getHighlightLineColor());
				}
				
				//Check to see if there should be a highlight timeline on this cell
				if(mHighlightDecorator.isHighlighted(dayItem))
				{
					if(mHighlightDecorator.highlightMorning(dayItem))
					{
						timelineAM.setVisibility(View.VISIBLE);
						timelineAM.setBackgroundColor(mHighlightDecorator.getHighlightLineColor());
					}
					
					if(mHighlightDecorator.highlightEvening(dayItem))
					{
						timelinePM.setVisibility(View.VISIBLE);
						timelinePM.setBackgroundColor(mHighlightDecorator.getHighlightLineColor());
					}
				}
				
				//Check to see if the event indicator should be shown
				eventIndicator.setVisibility(View.INVISIBLE);
				List<CalendarEvent> events = CalendarManager.getInstance().getEvents(dayItem);
				for(CalendarEvent event : events)
				{
					if(!event.isPlaceholder())
					{
						eventIndicator.setVisibility(View.VISIBLE);
						break;
					}
				}
				
				
				// Check if the month label has to be displayed
				if(dayItem.getDayOfMonth() == 15)
				{
					mTxtMonth.setVisibility(View.VISIBLE);
					SimpleDateFormat monthDateFormat = new SimpleDateFormat(mContext.getResources().getString(R.string.month_name_format), CalendarManager.getInstance().getLocale());
					String           month           = monthDateFormat.format(weekItem.getDate()).toUpperCase();
					if(today.get(Calendar.YEAR) != weekItem.getYear())
					{
						month = month + String.format(" %d", weekItem.getYear());
					}
					mTxtMonth.setText(month);
				}
			}
		}
		
		private void setUpChildren(LinearLayout daysContainer)
		{
			mCells = new ArrayList<>();
			for(int i = 0; i < daysContainer.getChildCount(); i++)
			{
				mCells.add((ConstraintLayout) daysContainer.getChildAt(i));
			}
		}
		
		private void setUpMonthOverlay()
		{
			mTxtMonth.setVisibility(View.GONE);
			
			if(isDragging())
			{
				AnimatorSet animatorSetFadeIn = new AnimatorSet();
				animatorSetFadeIn.setDuration(FADE_DURATION);
				ObjectAnimator animatorTxtAlphaIn        = ObjectAnimator.ofFloat(mTxtMonth, "alpha", mTxtMonth.getAlpha(), 1f);
				ObjectAnimator animatorBackgroundAlphaIn = ObjectAnimator.ofFloat(mMonthBackground, "alpha", mMonthBackground.getAlpha(), 1f);
				animatorSetFadeIn.playTogether(animatorTxtAlphaIn
						//animatorBackgroundAlphaIn
				);
				animatorSetFadeIn.addListener(new Animator.AnimatorListener()
				{
					@Override
					public void onAnimationStart(Animator animation)
					{
					
					}
					
					@Override
					public void onAnimationEnd(Animator animation)
					{
						setAlphaSet(true);
					}
					
					@Override
					public void onAnimationCancel(Animator animation)
					{
					
					}
					
					@Override
					public void onAnimationRepeat(Animator animation)
					{
					
					}
				});
				animatorSetFadeIn.start();
			}
			else
			{
				AnimatorSet animatorSetFadeOut = new AnimatorSet();
				animatorSetFadeOut.setDuration(FADE_DURATION);
				ObjectAnimator animatorTxtAlphaOut        = ObjectAnimator.ofFloat(mTxtMonth, "alpha", mTxtMonth.getAlpha(), 0f);
				ObjectAnimator animatorBackgroundAlphaOut = ObjectAnimator.ofFloat(mMonthBackground, "alpha", mMonthBackground.getAlpha(), 0f);
				animatorSetFadeOut.playTogether(animatorTxtAlphaOut
						//animatorBackgroundAlphaOut
				);
				animatorSetFadeOut.addListener(new Animator.AnimatorListener()
				{
					@Override
					public void onAnimationStart(Animator animation)
					{
					
					}
					
					@Override
					public void onAnimationEnd(Animator animation)
					{
						setAlphaSet(false);
					}
					
					@Override
					public void onAnimationCancel(Animator animation)
					{
					
					}
					
					@Override
					public void onAnimationRepeat(Animator animation)
					{
					
					}
				});
				animatorSetFadeOut.start();
			}
			
			if(isAlphaSet())
			{
				//mMonthBackground.setAlpha(1f);
				mTxtMonth.setAlpha(1f);
			}
			else
			{
				//mMonthBackground.setAlpha(0f);
				mTxtMonth.setAlpha(0f);
			}
		}
	}
	
	// endregion
}
