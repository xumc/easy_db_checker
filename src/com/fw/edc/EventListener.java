package com.fw.edc;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.Event;
import com.github.shyiko.mysql.binlog.event.EventType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mxu2 on 6/1/15.
 */
public class EventListener implements BinaryLogClient.EventListener {
    private UI ui;
    private List<Event> events;

    public EventListener(UI ui) {
        this.ui = ui;
        events = new ArrayList<Event>();
    }

    public void onEvent(Event event) {
        try {
            System.out.println(event);
            events.add(event);
            EventType type = event.getHeader().getEventType();
            if (EventType.isDelete(type) || EventType.isUpdate(type) || EventType.isWrite(type)) {
                List<PackagedEvent> packagedEvents = new ArrayList<PackagedEvent>();
                PackagedEvent packagedEvent = new PackagedEvent(events);
                if (!packagedEvent.filterOutBy(EventFilter.getAllFilters())) {
                    packagedEvents.add(packagedEvent);
                }

                ui.displayEvent(packagedEvents);
                events.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
