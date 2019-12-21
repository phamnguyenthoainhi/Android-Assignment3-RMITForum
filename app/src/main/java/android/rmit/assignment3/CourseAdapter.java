package android.rmit.assignment3;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class CourseAdapter extends ArrayAdapter<Course> {
    private static final String TAG = "CourseAdapter";
    ArrayList<Course> mCourses;

    Context mContext;
    int mResource;

    public CourseAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Course> courses) {
        super(context, resource, courses);
        mContext = context;
        mResource = resource;
        mCourses = courses;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String coursename;
        String courseid;
        courseid = mCourses.get(position).getId();
        coursename = mCourses.get(position).getName();
        Log.d(TAG, "getView: courseid " + mCourses);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);
        TextView courseName = convertView.findViewById(R.id.coursenamedisplay);
        TextView courseId = convertView.findViewById(R.id.courseiddisplay);

        courseName.setText(coursename);
        courseId.setText(courseid);
        return convertView;

    }
}
