package android.rmit.assignment3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
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
    ImageButton delete;
    ImageButton edit;
    Course_User course_user;
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    //    boolean isSubscribed = false;
    String subscribedCourseid;
    BottomNavigationView bottomNavigationView;
    EditText courseid;
    View notificationBadge;


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
        if (!currentUser.getUid().equals("A1jnuCTWu2QkLygrlUngKRQbfPk2")) {
            openCreateCourse.setVisibility(View.INVISIBLE);

        }
        bottomNavigationView = findViewById(R.id.bottom_nav_course);
        openCreateCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });
        fetchCourse();
        createNavBar();
        bottomNavigationView.getMenu().getItem(0).setChecked(true);

        showNotificationBadge();

    }

    @Override
    protected void onStart() {
        bottomNavigationView.getMenu().getItem(0).setChecked(true);

        super.onStart();
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
                        startActivity(new Intent(CourseActivity.this, ManageUserActivity.class).putExtra("id",mAuth.getUid()));

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

        courseid = createDialog.findViewById(R.id.courseid);
        final EditText coursename = createDialog.findViewById(R.id.coursename);

        final Button createCourse = createDialog.findViewById(R.id.createCourse);

        createCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!courseid.getText().toString().isEmpty() && !coursename.getText().toString().isEmpty()) {
                    course = new Course(courseid.getText().toString(), coursename.getText().toString());
                    db.collection("Courses").document(course.getId()).get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        courseid.setError("Course id is already used. Please choose another one");
                                        courseid.setText("");
                                        courseid.requestFocus();
                                    } else {
                                        // Add a new document with a generated ID
                                        db.collection("Courses").document(course.getId()).set(course).
                                                addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d(TAG, "onSuccess: "+ aVoid);

                                                        courses = new ArrayList<>();
                                                        fetchCourse();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.d(TAG, "onFailure: "+ e);
                                                    }
                                                });
                                        alertDialog.dismiss();

                                    }
                                }
                            });

                }
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
        Intent intent = new Intent(CourseActivity.this,PostsListActivity.class);
        intent.putExtra("id",courses.get(position).getId());
        startActivity(intent);
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

    public void showNotificationBadge(){
        if(mAuth.getUid()!=null){
            db.collection("Notifications").whereEqualTo("user",mAuth.getUid()).whereEqualTo("seen",false).get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()) {

                                if (task.getResult() != null ) {
                                    if (task.getResult().size() >= 1) {
                                        System.out.println("Notification: " + task.getResult().size());
                                        addNotificationBadge(task.getResult().size());
                                    }
                                    else{
                                        removeNotificationBadge();
                                    }
                                }
                                else{
                                    removeNotificationBadge();
                                }
                            }
                            else{
                                removeNotificationBadge();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            removeNotificationBadge();
                        }
                    });
        }
    }

    public void removeNotificationBadge(){
        if(notificationBadge!=null) {
            BottomNavigationMenuView bottomNavigationMenuView = (BottomNavigationMenuView) bottomNavigationView.getChildAt(0);
            View view = bottomNavigationMenuView.getChildAt(2);
            BottomNavigationItemView bottomNavigationItemView = (BottomNavigationItemView) view;
            bottomNavigationItemView.removeView(notificationBadge);

        }
    }

    public void addNotificationBadge(int number){
        BottomNavigationMenuView bottomNavigationMenuView = (BottomNavigationMenuView) bottomNavigationView.getChildAt(0);
        View view = bottomNavigationMenuView.getChildAt(2);
        BottomNavigationItemView bottomNavigationItemView = (BottomNavigationItemView) view;
        notificationBadge = LayoutInflater.from(CourseActivity.this).inflate(R.layout.notification_button, bottomNavigationItemView, false);
        ((TextView) notificationBadge.findViewById(R.id.notif_count)).setText(number + "");
        bottomNavigationItemView.addView(notificationBadge);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNavigationView!=null){
            showNotificationBadge();
        }
    }
}
