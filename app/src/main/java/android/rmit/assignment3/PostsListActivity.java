package android.rmit.assignment3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class PostsListActivity extends AppCompatActivity implements PostAdapter.PostViewHolder.OnPostListener {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ArrayList<Post> posts = new ArrayList<>();
    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts_list);

        fetchPosts();
    }

    protected void initRecyclerView(){
        recyclerView=findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new PostAdapter(posts,this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void OnPostClick(int position) {
        Intent intent = new Intent(PostsListActivity.this,PostDetailActivity.class);
        intent.putExtra("id",posts.get(position).getId());
        startActivity(intent);
    }

    protected void fetchPosts(){
        db.collection("Posts").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(QueryDocumentSnapshot documentSnapshot:queryDocumentSnapshots){
                            Post post = documentSnapshot.toObject(Post.class);
                            post.setId(documentSnapshot.getId());
                            posts.add(post);
                            initRecyclerView();
                        }
                    }
                });
    }
}
