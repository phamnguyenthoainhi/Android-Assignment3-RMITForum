package android.rmit.assignment3;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SubscribedCourseAdapter extends RecyclerView.Adapter<SubscribedCourseAdapter.SubscribedCourseViewHolder> {
    ArrayList<Course> msubscribedcourses;
    Context mContext;
    SubscribedCourseViewHolder.OnCourseListener onCourseListener;

    public SubscribedCourseAdapter(ArrayList<Course> subscribedcourses, SubscribedCourseViewHolder.OnCourseListener onCourseListener, Context context) {
        this.msubscribedcourses = subscribedcourses;
        this.mContext = context;
        this.onCourseListener = onCourseListener;

    }

    @NonNull
    @Override
    public SubscribedCourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_course, null);


        return new SubscribedCourseViewHolder(view,onCourseListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SubscribedCourseViewHolder holder, int position) {
        holder.coursename.setText(msubscribedcourses.get(position).getName());
        holder.subscribecourseid.setText(msubscribedcourses.get(position).getId());


    }

    @Override
    public int getItemCount() {
        return msubscribedcourses.size();
    }




    public static class SubscribedCourseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView coursename;
        TextView subscribecourseid;
        OnCourseListener onCourseListener;
        public SubscribedCourseViewHolder(@NonNull View itemView, OnCourseListener onCourseListener) {
            super(itemView);
            coursename = itemView.findViewById(R.id.subsribecoursename);
            subscribecourseid = itemView.findViewById(R.id.subscribecourseid);
            this.onCourseListener = onCourseListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onCourseListener.onCourseClick(getAdapterPosition());
        }

        public interface OnCourseListener{
            void onCourseClick(int position);
        }
    }
}
