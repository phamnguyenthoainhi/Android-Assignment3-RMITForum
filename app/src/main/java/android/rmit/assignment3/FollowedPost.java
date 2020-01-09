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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FollowedPost extends Fragment {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private static final String TAG = "SubscribedCourseUser";
    ArrayList<Post> followedPost;
    Post post;
    FirebaseUser currentUser;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    PostAdapter adapter;

    protected void initRecyclerView(View view, final Context context, final ArrayList<Post> arrayList){
        recyclerView = view.findViewById(R.id.postrecyclerview);
        recyclerView.setHasFixedSize(true);
        adapter = new PostAdapter(arrayList, new PostAdapter.PostViewHolder.OnPostListener() {
            @Override
            public void OnPostClick(int position) {

                Intent intent = new Intent(getActivity(),PostDetailActivity.class);
                intent.putExtra("id", arrayList.get(position).getId());
                startActivity(intent);
            }
        }, context);
        layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }


    public void fetchPostsbyUser(final String userid, final View view, final Context context) {
        db.collection("PostUser")
                .whereEqualTo("user",userid)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(QueryDocumentSnapshot documentSnapshot:queryDocumentSnapshots){

                            fetchPostbyId(documentSnapshot.get("post").toString(),view,context);
                        }
                    }
                });
    }


    public void fetchPostbyId(final String id, final View view, final Context context) {
        followedPost = new ArrayList<>();
        db.collection("Posts").document(id)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot doc) {
                        post = new Post();
                        post.setContent(doc.get("content").toString());
                        post.setCourse(doc.get("course").toString());
                        post.setOwner(doc.get("owner").toString());
                        post.setDateTime((Long) doc.get("dateTime"));
                        post.setTitle(doc.get("title").toString());
                        post.setUpvote((Long) doc.get("upvote"));
                        post.setId(doc.getId());
                        followedPost.add(post);
                        initRecyclerView(view, context,followedPost);

                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.post_tab, container, false);
        db = FirebaseFirestore.getInstance();
        followedPost = new ArrayList<>();
        ManageUserActivity manageUserActivity = (ManageUserActivity)getActivity();
        String userId = manageUserActivity.userId;
        if (!userId.equals("pDc0OYA6wKT8P6oUoTMk53muN242")) {
            fetchPostsbyUser(userId, view, getContext());
        } else {
            view.setVisibility(View.INVISIBLE);
        }

        return view;
    }
}
