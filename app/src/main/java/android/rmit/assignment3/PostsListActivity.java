package android.rmit.assignment3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class PostsListActivity extends AppCompatActivity implements PostAdapter.PostViewHolder.OnPostListener {

    EditText title;
    EditText content;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ArrayList<Post> posts = new ArrayList<>();
    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts_list);

        fetchPosts();

        Button createPost = findViewById(R.id.create_post);

        createPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mAuth.getCurrentUser()!=null) {
                    showPostDialog();
                }
                else{
                    startActivity(new Intent(PostsListActivity.this, SignInActivity.class));
                }
            }
        });
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

    public void showPostDialog(){
        final AlertDialog.Builder alert = new AlertDialog.Builder(PostsListActivity.this);
        final View dialogView = getLayoutInflater().inflate(R.layout.invite,null);

        title = dialogView.findViewById(R.id.input_title);
        content = dialogView.findViewById(R.id.input_content);

        Button post = dialogView.findViewById(R.id.post);

        alert.setView(dialogView);

        final AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(true);

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPost(new Post(mAuth.getUid(),title.getText().toString(),content.getText().toString()));
                alertDialog.dismiss();
            }
        });

        alertDialog.show();

    }

    public void createPost(Post post){
        db.collection("Posts").add(post)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(PostsListActivity.this, documentReference.getId(), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(PostsListActivity.this,PostDetailActivity.class);
                        intent.putExtra("id",documentReference.getId());
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PostsListActivity.this, "Failed to upload post", Toast.LENGTH_SHORT).show();
                    }
                });


    }
}
