package android.rmit.assignment3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class PostsListActivity extends AppCompatActivity implements PostAdapter.PostViewHolder.OnPostListener {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ArrayList<Post> posts = new ArrayList<>();
    RecyclerView recyclerView;
    PostAdapter adapter;
    RecyclerView.LayoutManager layoutManager;
    EditText searchbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts_list);
        searchbar = findViewById(R.id.searchbar);
        adapter = new PostAdapter(posts,this);

        searchbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                posts = new ArrayList<>();
                fetchPosts();


            }
        });

        searchbar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {


            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                adapter.filter(charSequence, posts);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        fetchPosts();
    }

    protected void initRecyclerView(){
        recyclerView=findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
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
