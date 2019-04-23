package com.github.tibolte.agendacalendarview.render;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.tibolte.agendacalendarview.R;
import com.github.tibolte.agendacalendarview.models.BaseCalendarEvent;

/**
 * Class helping to inflate our default layout in the AgendaAdapter
 */
public class DefaultEventRenderer extends EventRenderer<BaseCalendarEvent> {

    // region class - EventRenderer

    @Override
    public void render(@NonNull View view, @NonNull BaseCalendarEvent event) {
        TextView txtTitle = view.findViewById(R.id.view_agenda_event_title);
        TextView txtLocation = view.findViewById(R.id.view_agenda_event_location);
        LinearLayout descriptionContainer = view.findViewById(R.id.view_agenda_event_description_container);
        LinearLayout locationContainer = view.findViewById(R.id.view_agenda_event_location_container);

        if(event.isPlaceholder() && !event.showPlaceholders())
        {
            view.setVisibility(View.GONE);
            return;
        }
        
        view.setVisibility(View.VISIBLE);
        descriptionContainer.setVisibility(View.VISIBLE);
        txtTitle.setTextColor(view.getResources().getColor(android.R.color.black));

        txtTitle.setText(event.getTitle());
        txtLocation.setText(event.getLocation());
        if (event.getLocation().length() > 0) {
            locationContainer.setVisibility(View.VISIBLE);
            txtLocation.setText(event.getLocation());
        } else {
            locationContainer.setVisibility(View.GONE);
        }

        if (event.isPlaceholder()) {
            txtTitle.setTextColor(view.getResources().getColor(android.R.color.darker_gray));
        } else {
            txtTitle.setTextColor(view.getResources().getColor(R.color.theme_text_icons));
        }
        descriptionContainer.setBackgroundColor(event.getColor());
        txtLocation.setTextColor(view.getResources().getColor(R.color.theme_text_icons));
    }

    @Override
    public int getEventLayout() {
        return R.layout.view_agenda_event;
    }

    // endregion
}
