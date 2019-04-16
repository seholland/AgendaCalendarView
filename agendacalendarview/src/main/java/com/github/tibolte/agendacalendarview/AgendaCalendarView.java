package com.github.tibolte.agendacalendarview;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;

import com.github.tibolte.agendacalendarview.agenda.AgendaAdapter;
import com.github.tibolte.agendacalendarview.agenda.AgendaView;
import com.github.tibolte.agendacalendarview.calendar.CalendarView;
import com.github.tibolte.agendacalendarview.calendar.weekslist.HighlightDecorator;
import com.github.tibolte.agendacalendarview.models.BaseCalendarEvent;
import com.github.tibolte.agendacalendarview.models.CalendarEvent;
import com.github.tibolte.agendacalendarview.models.IDayItem;
import com.github.tibolte.agendacalendarview.models.IWeekItem;
import com.github.tibolte.agendacalendarview.models.WeekItem;
import com.github.tibolte.agendacalendarview.render.DefaultEventRenderer;
import com.github.tibolte.agendacalendarview.render.EventRenderer;
import com.github.tibolte.agendacalendarview.utils.BusProvider;
import com.github.tibolte.agendacalendarview.utils.Events;
import com.github.tibolte.agendacalendarview.utils.ListViewScrollTracker;
import com.github.tibolte.agendacalendarview.widgets.FloatingActionButton;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * View holding the agenda and calendar view together.
 */
public class AgendaCalendarView extends FrameLayout implements StickyListHeadersListView.OnStickyHeaderChangedListener
{

	private static final String LOG_TAG = AgendaCalendarView.class.getSimpleName();

	private CalendarView         mCalendarView;
	private AgendaView           mAgendaView;
	private FloatingActionButton mFloatingActionButton;

	private int mAgendaCurrentDayTextColor, mAgendaCurrentDayColor, mCalendarHeaderColor, mCalendarBackgroundColor, mCalendarDayTextColor, mCalendarPastDayTextColor, mCalendarCurrentDayColor, mCalendarCurrentDayCircleColor,
			mFabColor;
	private String mNoEventText;
	private CalendarPickerController mCalendarPickerController;

	private ListViewScrollTracker mAgendaListViewScrollTracker;
	private final AbsListView.OnScrollListener mAgendaScrollListener = new AbsListView.OnScrollListener()
	{
		int mCurrentAngle;
		final int mMaxAngle = 85;

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState)
		{

		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
		{
			int scrollY = mAgendaListViewScrollTracker.calculateScrollY(firstVisibleItem, visibleItemCount);
			if(scrollY != 0)
			{
				mFloatingActionButton.show();
			}
			//            Log.d(LOG_TAG, String.format("Agenda listView scrollY: %d", scrollY));
			int toAngle = scrollY / 100;
			if(toAngle > mMaxAngle)
			{
				toAngle = mMaxAngle;
			}
			else if(toAngle < -mMaxAngle)
			{
				toAngle = -mMaxAngle;
			}
			RotateAnimation rotate = new RotateAnimation(mCurrentAngle, toAngle, mFloatingActionButton.getWidth() / 2, mFloatingActionButton.getHeight() / 2);
			rotate.setFillAfter(true);
			mCurrentAngle = toAngle;
			mFloatingActionButton.startAnimation(rotate);
		}
	};

	// region Constructors

	public AgendaCalendarView(Context context)
	{
		super(context);
	}

	public AgendaCalendarView(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AgendaCalendarView, 0, 0);
		mAgendaCurrentDayTextColor = a.getColor(R.styleable.AgendaCalendarView_agendaCurrentDayTextColor, getResources().getColor(R.color.theme_light_primary));
		mAgendaCurrentDayColor = a.getColor(R.styleable.AgendaCalendarView_agendaCurrentDayColor, getResources().getColor(R.color.theme_primary));
		mCalendarHeaderColor = a.getColor(R.styleable.AgendaCalendarView_calendarHeaderColor, getResources().getColor(R.color.theme_primary_dark));
		mCalendarBackgroundColor = a.getColor(R.styleable.AgendaCalendarView_calendarColor, getResources().getColor(R.color.theme_primary));
		mCalendarDayTextColor = a.getColor(R.styleable.AgendaCalendarView_calendarDayTextColor, getResources().getColor(R.color.theme_text_icons));
		mCalendarCurrentDayColor = a.getColor(R.styleable.AgendaCalendarView_calendarCurrentDayTextColor, getResources().getColor(R.color.calendar_text_current_day));
		mCalendarCurrentDayCircleColor = a.getColor(R.styleable.AgendaCalendarView_calendarCurrentDayCircleColor, getResources().getColor(R.color.theme_primary));
		mCalendarPastDayTextColor = a.getColor(R.styleable.AgendaCalendarView_calendarPastDayTextColor, getResources().getColor(R.color.theme_light_primary));
		mFabColor = a.getColor(R.styleable.AgendaCalendarView_fabColor, getResources().getColor(R.color.theme_accent));
		mNoEventText = a.getString(R.styleable.AgendaCalendarView_calendarNoEventText);
		a.recycle();
		if(mNoEventText == null)
		{
			mNoEventText = context.getResources().getString(R.string.agenda_event_no_events);
		}

		//        Log.i(LOG_TAG, "Selected circle color: " + String.format("#%06X", (0xFFFFFF & mCalendarCurrentDayCircleColor)));

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		Objects.requireNonNull(inflater).inflate(R.layout.view_agendacalendar, this, true);

		setAlpha(0f);
	}

	// endregion

	// region Class - View

	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		mCalendarView = findViewById(R.id.calendar_view);
		mAgendaView = findViewById(R.id.agenda_view);
		mFloatingActionButton = findViewById(R.id.floating_action_button);
		ColorStateList csl = new ColorStateList(new int[][]{new int[0]}, new int[]{mFabColor});
		mFloatingActionButton.setBackgroundTintList(csl);

		mCalendarView.findViewById(R.id.cal_day_names).setBackgroundColor(mCalendarHeaderColor);
		mCalendarView.findViewById(R.id.list_week).setBackgroundColor(mCalendarBackgroundColor);

		mAgendaView.getAgendaListView().setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> mCalendarPickerController.onEventSelected(CalendarManager.getInstance().getEvents().get(position)));

		BusProvider.getInstance().toObserverable().subscribe(event -> {
			if(event instanceof Events.DayClickedEvent)
			{
				mCalendarPickerController.onDaySelected(((Events.DayClickedEvent) event).getDay());
			}
			else if(event instanceof Events.EventsFetched)
			{
				ObjectAnimator alphaAnimation = new ObjectAnimator().ofFloat(this, "alpha", getAlpha(), 1f).setDuration(500);
				alphaAnimation.addListener(new Animator.AnimatorListener()
				{
					@Override
					public void onAnimationStart(Animator animation)
					{

					}

					@Override
					public void onAnimationEnd(Animator animation)
					{
						long fabAnimationDelay = 500;
						// Just after setting the alpha from this view to 1, we hide the fab.
						// It will reappear as soon as the user is scrolling the Agenda view.
						new Handler().postDelayed(() -> {
							mFloatingActionButton.hide();
							mAgendaListViewScrollTracker = new ListViewScrollTracker(mAgendaView.getAgendaListView());
							mAgendaView.getAgendaListView().setOnScrollListener(mAgendaScrollListener);
							mFloatingActionButton.setOnClickListener((v) -> {
								mAgendaView.translateList(0);
								mAgendaView.getAgendaListView().scrollToCurrentDate(CalendarManager.getInstance().getToday());
								new Handler().postDelayed(() -> mFloatingActionButton.hide(), fabAnimationDelay);
							});
						}, fabAnimationDelay);
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
				alphaAnimation.start();
			}
		});
	}

	// endregion

	// region Interface - StickyListHeadersListView.OnStickyHeaderChangedListener

	@Override
	public void onStickyHeaderChanged(StickyListHeadersListView stickyListHeadersListView, View header, int position, long headerId)
	{
		Log.d(LOG_TAG, String.format("onStickyHeaderChanged, position = %d, headerId = %d", position, headerId));

		if(CalendarManager.getInstance().getEvents().size() > 0)
		{
			CalendarEvent event = CalendarManager.getInstance().getEvents().get(position);
			if(event != null)
			{
				mCalendarView.scrollToDate(event);
				mCalendarPickerController.onScrollToDate(event.getInstanceDay());
			}
		}
	}

	// endregion

	// region Public methods

	public void init(List<CalendarEvent> eventList, Calendar minDate, Calendar maxDate, Locale locale, CalendarPickerController calendarPickerController)
	{
		mCalendarPickerController = calendarPickerController;

		CalendarManager.getInstance(getContext()).buildCal(minDate, maxDate, locale, new WeekItem(), mNoEventText);

		// Feed our views with weeks list and events
		mCalendarView.init(CalendarManager.getInstance(getContext()), mCalendarDayTextColor, mCalendarCurrentDayColor, mCalendarCurrentDayCircleColor, mCalendarPastDayTextColor);

		// Load agenda events and scroll to current day
		AgendaAdapter agendaAdapter = new AgendaAdapter(mAgendaCurrentDayTextColor, mAgendaCurrentDayColor);
		mAgendaView.getAgendaListView().setAdapter(agendaAdapter);
		mAgendaView.getAgendaListView().setOnStickyHeaderChangedListener(this);

		CalendarManager.getInstance().loadEvents(eventList, new BaseCalendarEvent());
		BusProvider.getInstance().send(new Events.EventsFetched());
//		Log.d(LOG_TAG, "CalendarEventTask finished");

		// add default event renderer
		addEventRenderer(new DefaultEventRenderer());
	}

	public void init(Locale locale, List<IWeekItem> lWeeks, List<IDayItem> lDays, List<CalendarEvent> lEvents, CalendarPickerController calendarPickerController)
	{
		mCalendarPickerController = calendarPickerController;

		CalendarManager.getInstance(getContext()).loadCal(locale, lWeeks, lDays, lEvents);

		// Feed our views with weeks list and events
		mCalendarView.init(CalendarManager.getInstance(getContext()), mCalendarDayTextColor, mCalendarCurrentDayColor, mCalendarCurrentDayCircleColor, mCalendarPastDayTextColor);

		// Load agenda events and scroll to current day
		AgendaAdapter agendaAdapter = new AgendaAdapter(mAgendaCurrentDayTextColor, mAgendaCurrentDayColor);
		mAgendaView.getAgendaListView().setAdapter(agendaAdapter);
		mAgendaView.getAgendaListView().setOnStickyHeaderChangedListener(this);

		// notify that actually everything is loaded
		BusProvider.getInstance().send(new Events.EventsFetched());
		Log.d(LOG_TAG, "CalendarEventTask finished");

		// add default event renderer
		addEventRenderer(new DefaultEventRenderer());
	}

	public void addEventRenderer(@NonNull final EventRenderer<?> renderer)
	{
		AgendaAdapter adapter = (AgendaAdapter) mAgendaView.getAgendaListView().getAdapter();
		adapter.addEventRenderer(renderer);
	}

	public void enableCalenderView(boolean enable)
	{
		mAgendaView.enablePlaceholderForCalendar(enable);
		mCalendarView.setVisibility(enable ? VISIBLE : GONE);
		mAgendaView.findViewById(R.id.view_shadow).setVisibility(enable ? VISIBLE : GONE);
	}

	public void enableFloatingIndicator(boolean enable)
	{
		mFloatingActionButton.setVisibility(enable ? VISIBLE : GONE);
	}

	public IDayItem getSelectedDay()
	{
		return mCalendarView.getSelectedDay();
	}

	public void setSelectedDay(long timestamp)
	{
		if(timestamp == 0)
			return;

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timestamp);
		mAgendaView.getAgendaListView().scrollToCurrentDate(calendar);
	}

	public void updateEventList(List<CalendarEvent> eventList)
	{
		((AgendaAdapter) mAgendaView.getAgendaListView().getAdapter()).updateEvents(eventList);
	}

	public void setCalendarHighlightDecorator(@NonNull HighlightDecorator highlightDecorator)
	{
		mCalendarView.setHighlightDecorator(highlightDecorator);
	}
	// endregion
}
