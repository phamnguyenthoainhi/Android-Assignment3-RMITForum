package android.rmit.assignment3;

import android.rmit.assignment3.Course;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.ArrayList;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    ArrayList<Course> myCourseList ;

    CourseViewHolder.OnCourseListener myOnCourseListener;
    private static final String TAG = "CourseAdapter";





    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.course, parent, false);


        return(new CourseViewHolder(view, myOnCourseListener));
    }


    CourseAdapter(ArrayList<Course> courseList, CourseViewHolder.OnCourseListener onCourseListener) {
        this.myCourseList = courseList;
        this.myOnCourseListener = onCourseListener;
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {

        holder.courseid.setText(myCourseList.get(position).getId());
        holder.coursename.setText(myCourseList.get(position).getName());

        fetchSubscribeInfo(myCourseList.get(position).getDocid(),holder);
    }

    @Override
    public int getItemCount() {
        return myCourseList.size();
    }

    public static class CourseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        TextView coursename;
        TextView courseid;
        ImageButton edit;
        ImageButton delete;
        Button subscribebtn;
        Button unsubscribebtn;
        private FirebaseAuth mAuth;
        OnCourseListener onCourseListener;
        CourseViewHolder(View v, final OnCourseListener onCourseListener) {
            super(v);
            courseid = v.findViewById(R.id.courseiddisplay);
            coursename = v.findViewById(R.id.coursenamedisplay);
            edit = v.findViewById(R.id.editcourse);
            delete = v.findViewById(R.id.deletecourse);
            subscribebtn = v.findViewById(R.id.subscribebtn);
            unsubscribebtn = v.findViewById(R.id.unsubscribebtn);
            mAuth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = mAuth.getCurrentUser();

            if (!currentUser.getUid().equals("A1jnuCTWu2QkLygrlUngKRQbfPk2")) {
                edit.setVisibility(View.INVISIBLE);
                delete.setVisibility(View.INVISIBLE);


            } else {
                edit.setVisibility(View.VISIBLE);
                delete.setVisibility(View.VISIBLE);

            }

            this.onCourseListener = onCourseListener;
            v.setOnClickListener(this);

            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onCourseListener.editButtonClick(view, getAdapterPosition());
                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onCourseListener.deleteButtonClick(view, getAdapterPosition());
                }
            });

            subscribebtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    subscribebtn.setVisibility(View.GONE);
                    unsubscribebtn.setVisibility(View.VISIBLE);
                    onCourseListener.subscribe(view, getAdapterPosition());
                }
            });

            unsubscribebtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    unsubscribebtn.setVisibility(View.GONE);
                    subscribebtn.setVisibility(View.VISIBLE);
                    onCourseListener.unsubscribe(view, getAdapterPosition());
                }
            });
        }

        @Override
        public void onClick(View v) {
            onCourseListener.onCourseClick(getAdapterPosition());
        }

        public interface OnCourseListener{
            void onCourseClick(int position);
            void editButtonClick(View v, int posision);
            void deleteButtonClick(View v, int position);
            void subscribe(View v, int position);
            void unsubscribe(View v, int position);
        }
    }

    private void fetchSubscribeInfo(String idCourse, final CourseViewHolder holder){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();

        db.collection("CourseUsers").document(idCourse.concat(mAuth.getUid())).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if(documentSnapshot.exists()){
                                if (mAuth.getCurrentUser().getUid().equals("A1jnuCTWu2QkLygrlUngKRQbfPk2")) {
                                    holder.subscribebtn.setVisibility(View.GONE);
                                    holder.unsubscribebtn.setVisibility(View.GONE);
                                } else {
                                    holder.unsubscribebtn.setVisibility(View.VISIBLE);
                                    holder.subscribebtn.setVisibility(View.GONE);
                                }

                            }
                            else{
                                if (mAuth.getCurrentUser().getUid().equals("A1jnuCTWu2QkLygrlUngKRQbfPk2")) {
                                    holder.subscribebtn.setVisibility(View.GONE);
                                    holder.unsubscribebtn.setVisibility(View.GONE);
                                } else {
                                    holder.subscribebtn.setVisibility(View.VISIBLE);
                                    holder.unsubscribebtn.setVisibility(View.GONE);
                                }


                            }
                        }
                    }
                });
    }


}
