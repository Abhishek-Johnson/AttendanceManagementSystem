package com.example.ams;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;

public class DashboardActivity extends AppCompatActivity {

    private EditText subjectEditText;
    private Button addSubjectButton;
    private Button markAttendanceButton;
    private Button viewAttendanceButton;
    private ListView subjectListView;

    private ArrayList<String> subjects;
    private SubjectAdapter adapter;
    private DatabaseReference subjectsRef;
    private String teacherUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        subjectEditText = findViewById(R.id.subjectEditText);
        addSubjectButton = findViewById(R.id.addSubjectButton);
        markAttendanceButton = findViewById(R.id.markAttendanceButton);
        subjectListView = findViewById(R.id.subjectListView);
        viewAttendanceButton = findViewById(R.id.viewAttendanceButton);

        teacherUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        subjectsRef = FirebaseDatabase.getInstance().getReference("teachers").child(teacherUID).child("subjects");

        subjects = new ArrayList<>();
        adapter = new SubjectAdapter(subjects);
        subjectListView.setAdapter(adapter);

        loadSubjects();

        addSubjectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String subject = subjectEditText.getText().toString().trim();
                if (!subject.isEmpty()) {
                    subjectsRef.child(subject).child("students");
                    subjects.add(subject);
                    adapter.notifyDataSetChanged();
                    subjectEditText.setText("");
                }
            }
        });

        markAttendanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, MarkAttendanceActivity.class);
                startActivity(intent);
            }
        });

        viewAttendanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, ViewAttendanceActivity.class);
                startActivity(intent);
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
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private class SubjectAdapter extends ArrayAdapter<String> {
        SubjectAdapter(ArrayList<String> subjects) {
            super(DashboardActivity.this, 0, subjects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_subject, parent, false);
            }

            String subject = getItem(position);
            TextView subjectNameTextView = convertView.findViewById(R.id.subjectNameTextView);
            Button removeSubjectButton = convertView.findViewById(R.id.removeSubjectButton);

            subjectNameTextView.setText(subject);
            subjectNameTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(DashboardActivity.this, AddStudentsActivity.class);
                    intent.putExtra("SUBJECT_NAME", subject);
                    startActivity(intent);
                }
            });

            removeSubjectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    subjects.remove(position);
                    subjectsRef.child(subject).removeValue();
                    notifyDataSetChanged();
                }
            });

            return convertView;
        }
    }
}
