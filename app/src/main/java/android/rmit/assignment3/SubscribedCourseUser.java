package android.rmit.assignment3;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SubscribedCourseUser extends Fragment {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private static final String TAG = "SubscribedCourseUser";
    ArrayList<Course> subscribedCourses;
    Course fetchedCourse;
    FirebaseUser currentUser;




    public void fetchCoursesbyUser(final String userid) {

        db.collection("CourseUsers")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                if (document.get("userid").equals(userid)) {
                                    Log.d(TAG, "onComplete: fetchcoursebyuser "+ document.get("courseid"));
                                    fetchCoursebyId(document.get("courseid").toString());
                                }
                            }
                        }
                    }
                });
    }

    public void fetchCoursebyId(final String doccourseid) {
        subscribedCourses = new ArrayList<>();
        db.collection("Courses").document(doccourseid)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        fetchedCourse = new Course();
                        fetchedCourse.setId(snapshot.get("id").toString());
                        fetchedCourse.setName(snapshot.get("name").toString());
                        fetchedCourse.setDocid(doccourseid);
                        subscribedCourses.add(fetchedCourse);
//                        initRecyclerView();
                    }
                });
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.courses_tab, container, false);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        fetchCoursesbyUser(currentUser.getUid());


        return view;
    }
}
