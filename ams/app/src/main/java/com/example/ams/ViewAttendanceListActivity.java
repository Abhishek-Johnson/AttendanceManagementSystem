package com.example.ams;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
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

public class ViewAttendanceListActivity extends AppCompatActivity {

    private static final String TAG = "ViewAttendanceListActivity";
    private ListView attendanceListView;
    private ArrayList<String> attendanceList;
    private DatabaseReference attendanceRef;
    private String selectedSubject;
    private String teacherUID;
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_attendance_list);

        attendanceListView = findViewById(R.id.attendanceListView);
        attendanceList = new ArrayList<>();

        selectedSubject = getIntent().getStringExtra("selectedSubject");
        teacherUID = getIntent().getStringExtra("teacherUID");
        selectedDate = getIntent().getStringExtra("selectedDate");

        if (selectedSubject == null || teacherUID == null || selectedDate == null) {
            Log.e(TAG, "Missing subject, teacher UID, or date!");
            Toast.makeText(this, "Error: Missing information.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        attendanceRef = FirebaseDatabase.getInstance().getReference("teachers")
                .child(teacherUID).child("subjects").child(selectedSubject).child("attendance").child(selectedDate);

        fetchAttendance();
    }

    private void fetchAttendance() {
        attendanceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                attendanceList.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String studentName = snapshot.child("studentName").getValue(String.class);
                        if (studentName != null) {
                            attendanceList.add(studentName);
                        }
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(ViewAttendanceListActivity.this,
                            android.R.layout.simple_list_item_1, attendanceList);
                    attendanceListView.setAdapter(adapter);
                } else {
                    Log.d(TAG, "No attendance records found for the selected date.");
                    Toast.makeText(ViewAttendanceListActivity.this, "No records found for the selected date.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                Toast.makeText(ViewAttendanceListActivity.this, "Failed to load attendance records.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
