package com.example.namedmemories.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.namedmemories.R;
import com.example.namedmemories.adapters.CategoryAdapter;
import com.example.namedmemories.adapters.PersonAdapter;
import com.example.namedmemories.adapters.AttachmentAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddMemoryActivity extends AppCompatActivity {

    private RecyclerView categoryRecyclerView;
    private RecyclerView personRecyclerView;
    private RecyclerView attachmentRecyclerView;
    private LinearLayout personSelectionLayout;
    private LinearLayout memoryDetailsLayout;
    private LinearLayout saveButtonLayout;

    private EditText titleEditText;
    private EditText descriptionEditText;
    private EditText dateEditText;
    private Button saveButton;

    private CategoryAdapter categoryAdapter;
    private PersonAdapter personAdapter;
    private AttachmentAdapter attachmentAdapter;

    private List<Category> categories;
    private Map<String, List<Person>> connections;
    private List<Attachment> attachments;

    private String selectedCategory = "";
    private Person selectedPerson = null;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_memory);

        initViews();
        initData();
        setupRecyclerViews();
        setupClickListeners();
        calendar = Calendar.getInstance();
    }

    private void initViews() {
        categoryRecyclerView = findViewById(R.id.categoryRecyclerView);
        personRecyclerView = findViewById(R.id.personRecyclerView);
        attachmentRecyclerView = findViewById(R.id.attachmentRecyclerView);
        personSelectionLayout = findViewById(R.id.personSelectionLayout);
        memoryDetailsLayout = findViewById(R.id.memoryDetailsLayout);
        saveButtonLayout = findViewById(R.id.saveButtonLayout);

        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        dateEditText = findViewById(R.id.dateEditText);
        saveButton = findViewById(R.id.saveButton);

        // Initially hide person selection and memory details
        personSelectionLayout.setVisibility(View.GONE);
        memoryDetailsLayout.setVisibility(View.GONE);
        saveButtonLayout.setVisibility(View.GONE);
    }

    private void initData() {
        // Initialize categories
        categories = new ArrayList<>();
        categories.add(new Category("friends", "Friends", R.drawable.ic_friends, R.color.blue_500));
        categories.add(new Category("family", "Family", R.drawable.ic_family, R.color.green_500));
        categories.add(new Category("love", "Love", R.drawable.ic_love, R.color.red_500));
        categories.add(new Category("myself", "Myself", R.drawable.ic_person, R.color.purple_500));

        // Initialize connections
        connections = new HashMap<>();
        connections.put("friends", Arrays.asList(
                new Person(1, "John Doe", "👨‍💻"),
                new Person(2, "Sarah Wilson", "👩‍🎨"),
                new Person(3, "Mike Johnson", "👨‍🚀"),
                new Person(4, "Alex Brown", "👨‍🎓"),
                new Person(5, "Emma Davis", "👩‍💼")
        ));
        connections.put("family", Arrays.asList(
                new Person(6, "Mom", "👩‍👧"),
                new Person(7, "Dad", "👨‍👦"),
                new Person(8, "Sister", "👧"),
                new Person(9, "Brother", "👦"),
                new Person(10, "Grandma", "👵"),
                new Person(11, "Grandpa", "👴")
        ));
        connections.put("love", Arrays.asList(
                new Person(12, "Emma", "💕"),
                new Person(13, "Partner", "❤️")
        ));
        connections.put("myself", Arrays.asList(
                new Person(14, "Me", "🙋‍♂️")
        ));

        // Initialize attachments list
        attachments = new ArrayList<>();
    }

    private void setupRecyclerViews() {
        // Category RecyclerView
        categoryAdapter = new CategoryAdapter(categories, this::onCategorySelected);
        categoryRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        categoryRecyclerView.setAdapter(categoryAdapter);

        // Person RecyclerView
        personAdapter = new PersonAdapter(new ArrayList<>(), this::onPersonSelected);
        personRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        personRecyclerView.setAdapter(personAdapter);

        // Attachment RecyclerView
        attachmentAdapter = new AttachmentAdapter(attachments, this::onAttachmentRemoved);
        attachmentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        attachmentRecyclerView.setAdapter(attachmentAdapter);
    }

    private void onPersonSelected(String s) {
    }

    private void onCategorySelected(String s) {
    }

    private void setupClickListeners() {
        saveButton.setOnClickListener(v -> saveMemory());

        findViewById(R.id.photoButton).setOnClickListener(v -> addAttachment("photo"));
        findViewById(R.id.videoButton).setOnClickListener(v -> addAttachment("video"));
        findViewById(R.id.galleryButton).setOnClickListener(v -> addAttachment("gallery"));

        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        // Date picker
        dateEditText.setOnClickListener(v -> showDatePicker());
    }

    private void onCategorySelected(Category category) {
        selectedCategory = category.getId();
        selectedPerson = null;

        // Update category selection UI
        categoryAdapter.setSelectedCategory(selectedCategory);

        // Show person selection
        List<Person> categoryConnections = connections.get(selectedCategory);
        if (categoryConnections != null) {
            personAdapter.updatePersons(categoryConnections);
            personSelectionLayout.setVisibility(View.VISIBLE);
        }

        // Hide memory details until person is selected
        memoryDetailsLayout.setVisibility(View.GONE);
        saveButtonLayout.setVisibility(View.GONE);

        // Reset person selection
        personAdapter.setSelectedPerson(-1);
    }

    private void onPersonSelected(Person person) {
        selectedPerson = person;

        // Update person selection UI
        personAdapter.setSelectedPerson(person.getId());

        // Show memory details
        memoryDetailsLayout.setVisibility(View.VISIBLE);
        saveButtonLayout.setVisibility(View.VISIBLE);
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    dateEditText.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void addAttachment(String type) {
        // In real implementation, open camera/gallery based on type
        // For now, simulate adding an attachment
        Attachment attachment = new Attachment(
                System.currentTimeMillis(),
                type,
                type + "_" + System.currentTimeMillis() + (type.equals("photo") ? ".jpg" : ".mp4"),
                type.equals("photo") ? "📷" : (type.equals("video") ? "🎥" : "🖼️")
        );

        attachments.add(attachment);
        attachmentAdapter.notifyDataSetChanged();

        // Show attachment count
        TextView attachmentCount = findViewById(R.id.attachmentCountText);
        attachmentCount.setText("Attachments (" + attachments.size() + ")");
        attachmentCount.setVisibility(attachments.size() > 0 ? View.VISIBLE : View.GONE);
        attachmentRecyclerView.setVisibility(attachments.size() > 0 ? View.VISIBLE : View.GONE);

        Toast.makeText(this, type.substring(0, 1).toUpperCase() + type.substring(1) + " added", Toast.LENGTH_SHORT).show();
    }

    private void onAttachmentRemoved(long attachmentId) {
        attachments.removeIf(att -> att.getId() == attachmentId);
        attachmentAdapter.notifyDataSetChanged();

        // Update attachment count
        TextView attachmentCount = findViewById(R.id.attachmentCountText);
        if (attachments.size() > 0) {
            attachmentCount.setText("Attachments (" + attachments.size() + ")");
        } else {
            attachmentCount.setVisibility(View.GONE);
            attachmentRecyclerView.setVisibility(View.GONE);
        }
    }

    private void saveMemory() {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String date = dateEditText.getText().toString().trim();

        // Validation
        if (selectedCategory.isEmpty()) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedPerson == null) {
            Toast.makeText(this, "Please select a person", Toast.LENGTH_SHORT).show();
            return;
        }

        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show();
            titleEditText.requestFocus();
            return;
        }

        if (description.isEmpty()) {
            Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show();
            descriptionEditText.requestFocus();
            return;
        }

        if (date.isEmpty()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create memory object
        Memory memory = new Memory(
                System.currentTimeMillis(),
                title,
                description,
                date,
                selectedCategory,
                selectedPerson,
                new ArrayList<>(attachments)
        );

        // In real implementation, save to database
        // For now, just show success message
        Toast.makeText(this, "Memory saved successfully!", Toast.LENGTH_SHORT).show();

        // Return to previous activity
        finish();
    }

    // Data classes
    public static class Category {
        private String id;
        private String name;
        private int iconRes;
        private int colorRes;

        public Category(String id, String name, int iconRes, int colorRes) {
            this.id = id;
            this.name = name;
            this.iconRes = iconRes;
            this.colorRes = colorRes;
        }

        // Getters
        public String getId() { return id; }
        public String getName() { return name; }
        public int getIconRes() { return iconRes; }
        public int getColorRes() { return colorRes; }
    }

    public static class Person {
        private int id;
        private String name;
        private String avatar;

        public Person(int id, String name, String avatar) {
            this.id = id;
            this.name = name;
            this.avatar = avatar;
        }

        // Getters
        public int getId() { return id; }
        public String getName() { return name; }
        public String getAvatar() { return avatar; }
    }

    public static class Attachment {
        private long id;
        private String type;
        private String name;
        private String preview;

        public Attachment(long id, String type, String name, String preview) {
            this.id = id;
            this.type = type;
            this.name = name;
            this.preview = preview;
        }

        // Getters
        public long getId() { return id; }
        public String getType() { return type; }
        public String getName() { return name; }
        public String getPreview() { return preview; }
    }

    public static class Memory {
        private long id;
        private String title;
        private String description;
        private String date;
        private String category;
        private Person person;
        private List<Attachment> attachments;

        public Memory(long id, String title, String description, String date,
                      String category, Person person, List<Attachment> attachments) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.date = date;
            this.category = category;
            this.person = person;
            this.attachments = attachments;
        }

        // Getters
        public long getId() { return id; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getDate() { return date; }
        public String getCategory() { return category; }
        public Person getPerson() { return person; }
        public List<Attachment> getAttachments() { return attachments; }
    }
}