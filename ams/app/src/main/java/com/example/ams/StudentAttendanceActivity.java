package com.example.ams;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;

public class StudentAttendanceActivity extends AppCompatActivity {

    private static final String TAG = "StudentAttendanceActivity";
    private ListView studentListView;
    private Button recordAttendanceButton;
    private ArrayList<String> studentList;
    private DatabaseReference studentsRef;
    private DatabaseReference attendanceRef;
    private String selectedSubject;
    private String teacherUID;
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_attendance);

        studentListView = findViewById(R.id.studentListView);
        recordAttendanceButton = findViewById(R.id.recordAttendanceButton);
        studentList = new ArrayList<>();

        // Get the subject, teacher UID, and date from the intent
        selectedSubject = getIntent().getStringExtra("selectedSubject");
        teacherUID = getIntent().getStringExtra("teacherUID");
        selectedDate = getIntent().getStringExtra("selectedDate");

        if (selectedSubject == null || teacherUID == null || selectedDate == null) {
            Log.e(TAG, "Missing subject, teacher UID, or date!");
            Toast.makeText(this, "Error: Missing information.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        studentsRef = FirebaseDatabase.getInstance().getReference("teachers")
                .child(teacherUID).child("subjects").child(selectedSubject).child("students");
        attendanceRef = FirebaseDatabase.getInstance().getReference("teachers")
                .child(teacherUID).child("subjects").child(selectedSubject).child("attendance").child(selectedDate);

        fetchStudents();

        recordAttendanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordAttendance();
            }
        });
    }

    private void fetchStudents() {
        studentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                studentList.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String studentName = snapshot.getValue(String.class);
                        if (studentName != null) {
                            studentList.add(studentName);
                        }
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(StudentAttendanceActivity.this,
                            android.R.layout.simple_list_item_multiple_choice, studentList);
                    studentListView.setAdapter(adapter);
                    studentListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                } else {
                    Log.d(TAG, "No students found.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                Toast.makeText(StudentAttendanceActivity.this, "Failed to load students.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void recordAttendance() {
        for (int i = 0; i < studentListView.getCount(); i++) {
            if (studentListView.isItemChecked(i)) {
                String studentName = studentList.get(i);
                String studentId = attendanceRef.push().getKey();
                if (studentId != null) {
                    HashMap<String, Object> attendanceRecord = new HashMap<>();
                    attendanceRecord.put("studentName", studentName);
                    attendanceRef.child(studentId).setValue(attendanceRecord);
                }
            }
        }
        Toast.makeText(this, "Attendance recorded!", Toast.LENGTH_SHORT).show();
        navigateToDashboard();
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(StudentAttendanceActivity.this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
