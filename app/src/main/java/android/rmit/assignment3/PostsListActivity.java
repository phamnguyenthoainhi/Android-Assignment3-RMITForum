package android.rmit.assignment3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;


import android.widget.Button;

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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class PostsListActivity extends AppCompatActivity implements PostAdapter.PostViewHolder.OnPostListener {

    EditText title;
    EditText content;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ArrayList<Post> posts = new ArrayList<>();
    RecyclerView recyclerView;
    PostAdapter adapter;
    RecyclerView.LayoutManager layoutManager;
    String courseId;

    EditText searchbar;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    BottomNavigationView bottomNavigationView;
    View notificationBadge;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts_list);
        searchbar = findViewById(R.id.searchbar);

        searchbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                posts = new ArrayList<>();
                if(courseId!=null) {
                    fetchPosts(courseId);
                }
            }
        });

        bottomNavigationView = findViewById(R.id.bottom_nav_post);
        createNavBar();
        searchbar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {


            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (adapter != null) {
                    adapter.filter(charSequence, posts);
                    adapter.notifyDataSetChanged();
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        onNewIntent(getIntent());

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

        showNotificationBadge();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processIntent(intent);
    }

    private void processIntent(Intent intent){

        final Bundle bundle = intent.getExtras();

        fetchPosts((String)bundle.get("id"));
        courseId =(String) bundle.get("id");

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNavigationView!=null){
            showNotificationBadge();
        }
        if(adapter!=null){
            posts= new ArrayList<>();
            fetchPosts(courseId);
            adapter.notifyDataSetChanged();
        }

    }

    public ArrayList<Post> sort(ArrayList<Post> postArrayList) {
        Collections.sort(postArrayList, new Comparator<Post>() {
            @Override
            public int compare(Post p1, Post p2) {
                return (int) (p2.getUpvote() - p1.getUpvote());
            }
        } );
        return postArrayList;
    }

    protected void initRecyclerView(ArrayList<Post> arrayList){
        recyclerView = findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        adapter = new PostAdapter(arrayList,this, PostsListActivity.this);
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

    protected void fetchPosts(String courseId){

        db.collection("Posts").whereEqualTo("course",courseId).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(QueryDocumentSnapshot documentSnapshot:queryDocumentSnapshots){
                            Post post = documentSnapshot.toObject(Post.class);
                            post.setId(documentSnapshot.getId());
                            posts.add(post);
                        }
                        initRecyclerView(sort(posts));

                    }
                });
    }

    @Override
    protected void onStart() {
        bottomNavigationView.getMenu().getItem(0).setChecked(false);
        super.onStart();
    }

    public void createNavBar() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.navigation_home:
                        startActivity(new Intent(PostsListActivity.this, CourseActivity.class));

                        break;
                    case R.id.navigation_user:
                        startActivity(new Intent(PostsListActivity.this, ManageUserActivity.class).putExtra("id",mAuth.getUid()));
                        break;
                    case R.id.navigation_notifications:
                        startActivity(new Intent(PostsListActivity.this,NotificationsListActivity.class));
                        //Toast.makeText(MainActivity.this, "Switch to Notification", Toast.LENGTH_SHORT).show();
                        break;
                }
                return true;
            }
        });
    }

    public void showPostDialog(){
        final AlertDialog.Builder alert = new AlertDialog.Builder(PostsListActivity.this);
        final View dialogView = getLayoutInflater().inflate(R.layout.post_dialog,null);

        title = dialogView.findViewById(R.id.input_title);
        content = dialogView.findViewById(R.id.input_content);

        Button post = dialogView.findViewById(R.id.post);

        alert.setView(dialogView);

        final AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(true);


        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPost(new Post(mAuth.getUid(),title.getText().toString(),content.getText().toString(),courseId));
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
        notificationBadge = LayoutInflater.from(PostsListActivity.this).inflate(R.layout.notification_button, bottomNavigationItemView, false);
        ((TextView) notificationBadge.findViewById(R.id.notif_count)).setText(number + "");
        bottomNavigationItemView.addView(notificationBadge);
    }
}
