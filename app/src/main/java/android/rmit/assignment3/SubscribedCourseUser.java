package android.rmit.assignment3;

import android.content.Context;
import android.content.Intent;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SubscribedCourseUser extends Fragment {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private static final String TAG = "SubscribedCourseUser";
    ArrayList<Course> subscribedCourses;
    Course fetchedCourse;
    FirebaseUser currentUser;
    RecyclerView recyclerView;
    GridLayoutManager gridLayoutManager;
    SubscribedCourseAdapter subscribedCourseAdapter;

    public void fetchCoursesbyUser(final String userid, final View view, final Context context) {
        db.collection("CourseUsers")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.get("userid").equals(userid)) {
                                    Log.d(TAG, "onComplete: fetchcoursebyuser "+ document.get("courseid"));
                                    fetchCoursebyId(document.get("courseid").toString(), view, context);
                                }
                            }
                        }

                    }
                });
    }


    public void fetchCoursebyId(final String doccourseid, final View view, final Context context) {
        subscribedCourses = new ArrayList<>();
        Log.d(TAG, "fetchCoursebyId: hello");
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
                        initRecyclerView(view, context);

                    }
                });
    }

        public void initRecyclerView(View view, Context context) {
        recyclerView = view.findViewById(R.id.subscribecourserecyclerview);
        gridLayoutManager = new GridLayoutManager(context, 1);
        recyclerView.setLayoutManager(gridLayoutManager);
        subscribedCourseAdapter = new SubscribedCourseAdapter(subscribedCourses, new SubscribedCourseAdapter.SubscribedCourseViewHolder.OnCourseListener() {
            @Override
            public void onCourseClick(int position) {
                startActivity(new Intent(getActivity(),PostsListActivity.class).putExtra("id",subscribedCourses.get(position).getId()));
            }
        },context);
        recyclerView.setAdapter(subscribedCourseAdapter);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.courses_tab, container, false);
        mAuth = FirebaseAuth.getInstance();

        ManageUserActivity manageUserActivity = (ManageUserActivity)getActivity();

        subscribedCourses = new ArrayList<>();
        String userId = manageUserActivity.userId;

        if (!userId.equals("pDc0OYA6wKT8P6oUoTMk53muN242")) {
            fetchCoursesbyUser(userId, view, getContext());
        } else  {
            view.setVisibility(View.INVISIBLE);
        }

        return view;
    }
}
