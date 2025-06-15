package com.example.namedmemories.activities;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.namedmemories.R;
import com.example.namedmemories.adapters.EventAdapter;
import com.example.namedmemories.models.Event;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class CalendarActivity extends AppCompatActivity implements EventAdapter.OnEventActionListener {

    private RecyclerView eventsRecyclerView;
    private final List<Event> todayEvents = new ArrayList<>();
    private EventAdapter eventAdapter;
    private String selectedDate;
    private FirebaseUser user;
    private DatabaseReference baseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        CalendarView calendarView = findViewById(R.id.calendarView);
        eventsRecyclerView = findViewById(R.id.eventsRecyclerView);
        FloatingActionButton fabAddEvent = findViewById(R.id.fabAddEvent);

        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventAdapter(todayEvents, this);
        eventsRecyclerView.setAdapter(eventAdapter);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null)
            baseRef = FirebaseDatabase.getInstance().getReference("events").child(user.getUid());

        selectedDate = getDateFromMillis(calendarView.getDate());
        loadEventsFromFirebase(selectedDate);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
            loadEventsFromFirebase(selectedDate);
        });

        fabAddEvent.setOnClickListener(v -> showAddEventDialog(null, null));
    }

    private void showAddEventDialog(String existingId, Event existingEvent) {
        AlertDialog.Builder builder = new AlertDialog.Builder(CalendarActivity.this);
        builder.setTitle(existingEvent == null ? "Add Event" : "Edit Event");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_event, null);
        EditText inputTitle = dialogView.findViewById(R.id.inputEventTitle);
        TextView selectedTimeText = dialogView.findViewById(R.id.selectedTimeText);
        Button pickTimeBtn = dialogView.findViewById(R.id.pickTimeButton);

        final String[] selectedTime = {existingEvent != null ? existingEvent.time : "09:00 AM"};
        selectedTimeText.setText("Selected Time: " + selectedTime[0]);

        if (existingEvent != null) inputTitle.setText(existingEvent.title);

        pickTimeBtn.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            new TimePickerDialog(CalendarActivity.this, (view, hourOfDay, minute) -> {
                Calendar c = Calendar.getInstance();
                c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                c.set(Calendar.MINUTE, minute);
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                selectedTime[0] = sdf.format(c.getTime());
                selectedTimeText.setText("Selected Time: " + selectedTime[0]);
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false).show();
        });

        builder.setView(dialogView);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String title = inputTitle.getText().toString().trim();
            if (TextUtils.isEmpty(title)) {
                Toast.makeText(this, "Event title can't be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            Event event = new Event(title, selectedDate, selectedTime[0]);

            if (existingId == null) {
                String key = baseRef.child(selectedDate).push().getKey();
                baseRef.child(selectedDate).child(key).setValue(event);
            } else {
                baseRef.child(selectedDate).child(existingId).setValue(event);
            }

            loadEventsFromFirebase(selectedDate);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void loadEventsFromFirebase(String date) {
        if (user == null) return;
        baseRef.child(date).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                todayEvents.clear();
                for (DataSnapshot eventSnap : snapshot.getChildren()) {
                    Event e = eventSnap.getValue(Event.class);
                    if (e != null) {
                        e.id = eventSnap.getKey(); // Store Firebase key inside the event object
                        todayEvents.add(e);
                    }
                }

                Collections.sort(todayEvents, (a, b) -> {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                        return sdf.parse(a.time).compareTo(sdf.parse(b.time));
                    } catch (Exception e) {
                        return 0;
                    }
                });

                eventAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                todayEvents.clear();
                Toast.makeText(CalendarActivity.this, "Failed to load events", Toast.LENGTH_SHORT).show();
                eventAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onEdit(Event event) {
        showAddEventDialog(event.id, event);
    }

    @Override
    public void onDelete(Event event) {
        if (user == null || event.id == null) return;
        baseRef.child(selectedDate).child(event.id).removeValue();
        loadEventsFromFirebase(selectedDate);
    }

    private String getDateFromMillis(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date(millis));
    }
}
