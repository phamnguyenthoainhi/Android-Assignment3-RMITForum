package android.rmit.assignment3;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class CourseActivity extends AppCompatActivity {
    Button openCreateCourse;
    FirebaseFirestore db;
    Course course;
    private static final String TAG = "CourseActivity";
    ListView listView;
    CourseAdapter courseAdapter;
    ArrayList<Course> courses;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);
        db = FirebaseFirestore.getInstance();
        openCreateCourse = findViewById(R.id.opencreatecourse);
        courses = new ArrayList<>();

        openCreateCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });

        fetchCourse();
    }

    public void initListView() {
        Log.d(TAG, "initListView: " + courses);
        listView = findViewById(R.id.listviewcourse);
        courseAdapter = new CourseAdapter(this, R.layout.course, courses);
        listView.setAdapter(courseAdapter);
    }

    public void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CourseActivity.this);

        final View createDialog = getLayoutInflater().inflate(R.layout.create_course, null);
        builder.setView(createDialog);
        final AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(true);

        final EditText courseid = createDialog.findViewById(R.id.courseid);
        final EditText coursename = createDialog.findViewById(R.id.coursename);

        final Button createCourse = createDialog.findViewById(R.id.createCourse);

        createCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!courseid.getText().toString().isEmpty() && !coursename.getText().toString().isEmpty()) {
                    course = new Course(courseid.getText().toString(), coursename.getText().toString());
                    createCourse(course);
                }
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    public void createCourse(Course course)
    {
        // Add a new document with a generated ID
        db.collection("Courses")
                .add(course)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                        fetchCourse();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    public void fetchCourse() {
        db.collection("Courses")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Course course = new Course();
                                course.setId(document.get("id").toString());
                                course.setName(document.get("name").toString());
                                courses.add(course);
                            }
                            Log.d(TAG, "onComplete: courses " + courses);

                            initListView();
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

}
