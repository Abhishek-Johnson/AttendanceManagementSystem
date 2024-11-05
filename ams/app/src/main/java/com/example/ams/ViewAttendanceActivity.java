package com.example.ams;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;

public class ViewAttendanceActivity extends AppCompatActivity {

    private Spinner subjectSpinner;
    private CalendarView calendarView;
    private Button proceedButton;

    private ArrayList<String> subjects;
    private DatabaseReference subjectsRef;
    private String teacherUID;
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_attendance);

        subjectSpinner = findViewById(R.id.subjectSpinner);
        calendarView = findViewById(R.id.calendarView);
        proceedButton = findViewById(R.id.proceedButton);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        teacherUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        subjectsRef = FirebaseDatabase.getInstance().getReference("teachers").child(teacherUID).child("subjects");

        subjects = new ArrayList<>();
        loadSubjects();

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
        });

        proceedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedSubject = (String) subjectSpinner.getSelectedItem();
                if (selectedSubject != null && !selectedSubject.isEmpty() && selectedDate != null) {
                    Intent intent = new Intent(ViewAttendanceActivity.this, ViewAttendanceListActivity.class);
                    intent.putExtra("selectedSubject", selectedSubject);
                    intent.putExtra("teacherUID", teacherUID);
                    intent.putExtra("selectedDate", selectedDate);
                    startActivity(intent);
                    Log.d("ViewAttendanceActivity", "Proceeding with subject: " + selectedSubject + " and date: " + selectedDate);
                } else {
                    Toast.makeText(ViewAttendanceActivity.this, "Please select a subject and date", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadSubjects() {
        subjectsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                subjects.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    subjects.add(snapshot.getKey());
                }
                populateSpinner();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ViewAttendanceActivity", "Database error: " + databaseError.getMessage());
                Toast.makeText(ViewAttendanceActivity.this, "Failed to load subjects.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, subjects);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subjectSpinner.setAdapter(adapter);
    }
}
