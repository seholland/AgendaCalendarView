package com.github.tibolte.agendacalendarview.models;

import java.util.Calendar;
import java.util.Date;

public interface IDayItem {

    // region Getters/Setters

    Date getDate();

    void setDate(Date date);

    int getDayOfMonth();

    boolean isToday();

    boolean isSelected();

    void setSelected(boolean selected);

    boolean isFirstDayOfTheMonth();

    String getMonth();
    
    Calendar getCalendar();
    
    void setCalendar(Calendar calendar);

    // endregion

    String toString();

    IDayItem copy();

}
