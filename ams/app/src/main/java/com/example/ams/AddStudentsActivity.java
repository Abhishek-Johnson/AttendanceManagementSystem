package com.example.ams;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;

public class AddStudentsActivity extends AppCompatActivity {

    private TextView subjectTextView;
    private EditText studentNameEditText, studentRollNoEditText;
    private Button addStudentButton;
    private ListView studentListView;

    private ArrayList<String> students;
    private ArrayAdapter<String> adapter;
    private DatabaseReference studentsRef;
    private String teacherUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_students);

        subjectTextView = findViewById(R.id.subjectTextView);
        studentNameEditText = findViewById(R.id.studentNameEditText);
        studentRollNoEditText = findViewById(R.id.studentRollNoEditText);
        addStudentButton = findViewById(R.id.addStudentButton);
        studentListView = findViewById(R.id.studentListView);

        String subjectName = getIntent().getStringExtra("SUBJECT_NAME");
        subjectTextView.setText(subjectName);

        teacherUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        studentsRef = FirebaseDatabase.getInstance().getReference("teachers").child(teacherUID).child("subjects").child(subjectName).child("students");

        students = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, students);
        studentListView.setAdapter(adapter);

        loadStudents();

        addStudentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String studentName = studentNameEditText.getText().toString().trim();
                String rollNo = studentRollNoEditText.getText().toString().trim();
                if (!studentName.isEmpty() && !rollNo.isEmpty()) {
                    String studentInfo = rollNo + ": " + studentName;
                    addStudent(studentInfo);
                    studentNameEditText.setText("");
                    studentRollNoEditText.setText("");
                }
            }
        });
    }

    private void loadStudents() {
        studentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                students.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String student = snapshot.getValue(String.class);
                    students.add(student);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void addStudent(String studentInfo) {
        studentsRef.push().setValue(studentInfo);
    }
}
