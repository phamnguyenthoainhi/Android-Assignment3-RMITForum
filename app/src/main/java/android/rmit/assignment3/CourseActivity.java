package android.rmit.assignment3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class CourseActivity extends AppCompatActivity implements CourseAdapter.CourseViewHolder.OnCourseListener {
    ImageButton openCreateCourse;
    FirebaseFirestore db;
    Course course;
    private static final String TAG = "CourseActivity";
    RecyclerView recyclerView;
    CourseAdapter courseAdapter;
    ArrayList<Course> courses;
    ImageButton edit;
    ImageButton delete;
    Course_User course_user;
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
//    boolean isSubscribed = false;
    String subscribedCourseid;
    BottomNavigationView bottomNavigationView;
    


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);
        db = FirebaseFirestore.getInstance();
        openCreateCourse = findViewById(R.id.opencreatecourse);
        mAuth = FirebaseAuth.getInstance();
        View course = getLayoutInflater().inflate(R.layout.course, null);
        edit = course.findViewById(R.id.editcourse);
        delete = course.findViewById(R.id.deletecourse);
        currentUser = mAuth.getCurrentUser();
        courses = new ArrayList<>();
        bottomNavigationView = findViewById(R.id.bottom_nav_course);
        openCreateCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });
        fetchCourse();
        createNavBar();
    }


    public void initListView() {
        Log.d(TAG, "initListView: " + courses);

        recyclerView = findViewById(R.id.recycleviewcourse);
        courseAdapter = new CourseAdapter(courses, this);
        recyclerView.setAdapter(courseAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    public void createNavBar() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.navigation_home:
                        startActivity(new Intent(CourseActivity.this, CourseActivity.class));

                        break;
                    case R.id.navigation_user:
                        startActivity(new Intent(CourseActivity.this, ManageUserActivity.class));
                        break;
                    case R.id.navigation_notifications:
                        startActivity(new Intent(CourseActivity.this,NotificationsListActivity.class));
                        break;
                }
                return true;
            }
        });
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

    public void showEditDialog(final int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(CourseActivity.this);

        final View createDialog = getLayoutInflater().inflate(R.layout.edit_course, null);
        builder.setView(createDialog);
        final AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(true);

        final EditText courseidedit = createDialog.findViewById(R.id.courseidedit);
        final EditText coursenameedit = createDialog.findViewById(R.id.coursenameedit);
        if (courses.size() > 0 ) {
            courseidedit.setText(courses.get(position).getId());
            coursenameedit.setText(courses.get(position).getName());
        }
        final Button editCourse = createDialog.findViewById(R.id.editCoursebtn);
        editCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (courseidedit.getText().toString().isEmpty()) {
                    courseidedit.setError("Course id can not be empty");
                } else if (coursenameedit.getText().toString().isEmpty()) {
                    coursenameedit.setError("Course name can not be empty");
                } else {
                    if (courses.size() > 0) {
                        AlertDialog.Builder buider1 = new AlertDialog.Builder(view.getContext()).setTitle("Confirmation").setMessage("Do you want to save these changes?")
                                .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        final Course editedcourse = new Course();
                                        editedcourse.setDocid(courses.get(position).getDocid());
                                        editedcourse.setName(coursenameedit.getText().toString());
                                        editedcourse.setId(courseidedit.getText().toString());
                                        updateCourse(editedcourse);
                                        courses = new ArrayList<>();
                                        fetchCourse();
                                    }
                                })
                                .setPositiveButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                });
                        buider1.create().show();
                    }
                    alertDialog.dismiss();

                }
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
                        courses = new ArrayList<>();
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
                                course.setDocid(document.getId());
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


    //    Update course with new values
    private void updateCourse(final Course course){
        db.collection("Courses").document(course.getDocid())
                .set(course)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(CourseActivity.this, "Successfully updated course info", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CourseActivity.this, "Failed to update course", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteCourse(String docid) {
        db.collection("Courses").document(docid)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });
    }
    @Override
    public void onCourseClick(int position) {

    }

    @Override
    public void editButtonClick(View v, int posision) {
        Toast.makeText(this, "Edit clicked "+ posision, Toast.LENGTH_SHORT).show();
        showEditDialog(posision);

    }

    @Override
    public void deleteButtonClick(View v, final int position) {
        Toast.makeText(this, "Delete clicked "+ position, Toast.LENGTH_SHORT).show();
        AlertDialog.Builder buider = new AlertDialog.Builder(v.getContext()).setTitle("Confirmation").setMessage("Do you want to delete the selected course?")
                .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (courses.size() > 0) {
                            deleteCourse(courses.get(position).getDocid());
                        }
                        courses = new ArrayList<>();
                        fetchCourse();
                    }
                })
                .setPositiveButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        buider.create().show();
    }

    @Override
    public void subscribe(View v, int position) {
        if (courses.size() > 0 && currentUser!= null) {
            course_user = new Course_User(courses.get(position).getDocid(),currentUser.getUid());
            Utilities utilities = new Utilities();
            utilities.subscribe(course_user, CourseActivity.this);

        }
    }

    @Override
    public void unsubscribe(View v, int position) {
        if (courses.size() > 0 && currentUser!= null) {
            Utilities utilities = new Utilities();
            utilities.unsubscribe(courses.get(position).getDocid().concat(mAuth.getUid()), CourseActivity.this);

        }
    }

}
