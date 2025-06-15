package com.example.namedmemories.models;

public class Event {
    public String id;
    public String title;
    public String date;
    public String time;

    public Event() { }

    public Event(String title, String date, String time) {
        this.title = title;
        this.date = date;
        this.time = time;
    }
}
