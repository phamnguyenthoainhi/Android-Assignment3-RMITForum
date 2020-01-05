package android.rmit.assignment3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class PostDetailActivity extends AppCompatActivity implements ReplyAdapter.ReplyViewHolder.OnReplyListener {

    String TAG = "Post Detail";
    String id;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Post post = new Post();
    TextView title;
    TextView content;
    Button upvotePost;
    Button downvotePost;
    Button editPost;
    Button deletePost;
    TextView votes;
    EditText newReplyContent;
    ImageView avatar;
    TextView owner;
    ArrayList<Reply> replies = new ArrayList<>();
    Utilities utilities = new Utilities();
    SumVote sumVote;

    RecyclerView.Adapter adapter;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser;
    User user;

    enum Vote{
        UPVOTE,
        DOWNVOTE
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        sumVote = new SumVote();
        title = findViewById(R.id.title_text);
        content = findViewById(R.id.content_text);
        votes = findViewById(R.id.post_votes);

        upvotePost = findViewById(R.id.post_upvote);
        downvotePost = findViewById(R.id.post_downvote);

        editPost=findViewById(R.id.edit_post);
        deletePost=findViewById(R.id.delete_post);

        avatar = findViewById(R.id.owneravatar);
        owner = findViewById(R.id.ownername);

        Button replyButton = findViewById(R.id.reply_button);

        replyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mAuth.getUid()!=null) {
                    showReplyDialog();
                }
                else{startActivity(new Intent(PostDetailActivity.this,SignInActivity.class));}
            }
        });

        onNewIntent(getIntent());

        currentUser = mAuth.getCurrentUser();
        user = new User();

        final SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipe_to_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                replies = new ArrayList<>();
                fetchReplies(id);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processIntent(intent);
    }

    private void processIntent(Intent intent){

        final Bundle bundle = intent.getExtras();

        fetchPost((String)bundle.get("id"));
        id =(String) bundle.get("id");

        fetchReplies(id);

    }

    public void initRecyclerView(){
        RecyclerView recyclerView = findViewById(R.id.my_recycler_view);;
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);;

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new ReplyAdapter(replies,this, PostDetailActivity.this);

        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onReplyClick(int position) {
        Toast.makeText(this, "clicked", Toast.LENGTH_SHORT).show();
        if(mAuth.getUid()!=null) {
            Intent intent = new Intent(PostDetailActivity.this,ReplyDetailActivity.class);
            intent.putExtra("id",replies.get(position).getId());
            startActivity(intent);
        }
        else{startActivity(new Intent(PostDetailActivity.this,SignInActivity.class));}
    }

    protected void fetchPost (String id){
        db.collection("Posts").document(id).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        post = documentSnapshot.toObject(Post.class);
                        if(post!=null) {
                            post.setId(documentSnapshot.getId());
                            title.setText(post.getTitle());
                            content.setText(post.getContent());
                            votes.setText(post.getUpvote() + "");

                            fetchPostOwner(post.getOwner());

                            avatar.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    startActivity(new Intent(PostDetailActivity.this,ManageUserActivity.class).putExtra("id",post.getOwner()));
                                }
                            });

                            owner.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    startActivity(new Intent(PostDetailActivity.this,ManageUserActivity.class).putExtra("id",post.getOwner()));
                                }
                            });

                            if (mAuth.getUid() != null && !mAuth.getUid().equals(post.getOwner())) {
                                editPost.setVisibility(View.GONE);
                                deletePost.setVisibility(View.GONE);
                                fetchVoteInfo();
                            } else if (mAuth.getUid() != null && mAuth.getUid().equals(post.getOwner())) {
                                upvotePost.setVisibility(View.GONE);
                                downvotePost.setVisibility(View.GONE);

                                editPost.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        showPostDialog();
                                    }
                                });

                                deletePost.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        deletePost();
                                    }
                                });
                            }
                            fetchVoteInfo();
                            fetchPostOwner(post.getOwner());
                        }
                        else{
                            title.setText("Post does not exist.");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG,"Failed to fetch");
                    }
                });
    }

    protected void fetchPostOwner(String ownerId){
        db.collection("Users").document(ownerId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user!=null){
                            if(user.getImageuri()!=null && user.getImageuri()!="") {
                                Picasso.with(PostDetailActivity.this).load(Uri.parse(user.getImageuri())).fit().centerCrop()
                                        .placeholder(R.drawable.grey)
                                        .error(R.drawable.grey)
                                        .into(avatar);
                            }
                            if(user.getFullname()!=null){
                                owner.setText(user.getFullname());
                            }
                        }
                    }
                });
    }

    public void showReplyDialog(){
        final AlertDialog.Builder alert = new AlertDialog.Builder(PostDetailActivity.this);
        final View dialogView = getLayoutInflater().inflate(R.layout.reply_dialog,null);


        newReplyContent = dialogView.findViewById(R.id.reply_input_content);

        Button reply = dialogView.findViewById(R.id.create_reply);

        alert.setView(dialogView);

        final AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(true);

        reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createReply(new Reply(id,mAuth.getUid(),newReplyContent.getText().toString()));
                alertDialog.dismiss();
            }
        });

        alertDialog.show();

    }

    protected void createReply(Reply reply){
        db.collection("Replies").add(reply)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(PostDetailActivity.this, "Successfully posted reply.", Toast.LENGTH_SHORT).show();
                        if(adapter!=null){
                            replies = new ArrayList<>();
                            fetchReplies(id);
                            adapter.notifyDataSetChanged();
                        }
                        else {
                            replies = new ArrayList<>();
                            fetchReplies(id);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PostDetailActivity.this, "Failed to post reply. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    protected  void fetchReplies(String postId){
        db.collection("Replies").whereEqualTo("post",postId).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){
                            Reply reply = documentSnapshot.toObject(Reply.class);
                            reply.setId(documentSnapshot.getId());
                            replies.add(reply);
                            initRecyclerView();


                        }

                    }
                });

    }

    protected void vote(Vote vote){
        switch(vote){
            case UPVOTE:
                post.increaseUpvote();
                if (post.getOwner() != null ){
                    fetchupdateSumVotes(post.getOwner(), true);
                }

                break;
            case DOWNVOTE:
                post.decreaseUpvote();
                if (post.getOwner() != null) {
                    fetchupdateSumVotes(post.getOwner(), false);
                }
                break;
        }
        updateUpvote(vote);
    }

    protected void undoVote(Vote vote){
        switch(vote){
            case UPVOTE:
                post.decreaseUpvote();
                if (post.getOwner() != null) {
                    fetchupdateSumVotes(post.getOwner(), false);
                }
                break;
            case DOWNVOTE:
                post.increaseUpvote();
                if (post.getOwner() != null ){
                    fetchupdateSumVotes(post.getOwner(), true);
                }
                break;
        }
        removeUpvote();
    }

    public void fetchupdateSumVotes(final String ownerid, final boolean plus) {
        db.collection("SumVotes").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot doc: queryDocumentSnapshots.getDocuments()) {
                            if(doc.getId().equals(ownerid)) {
                                if (doc.get("sum") != null) {
                                    if (plus) {
                                        sumVote.setSum((Long) doc.get("sum") + 1);
                                    } else {
                                        if ((long) doc.get("sum") > 0) {
                                            sumVote.setSum((long) doc.get("sum") - 1);
                                        }
                                    }
                                    utilities.updateSumVote(ownerid, sumVote.getSum());
                                }
                            }
                        }
                    }
                });
    }

    protected void updateUpvote(final Vote vote){
        if(mAuth.getUid()!=null) {
            final HashMap<String, String> object = new HashMap<>();
            switch (vote) {
                case UPVOTE:
                    object.put("type", "upvote");
                    break;
                case DOWNVOTE:
                    object.put("type", "downvote");
                    break;
            }
            System.out.println("updating database...");
            final String docId = post.getId().concat(mAuth.getUid());
            db.collection("Posts").document(post.getId()).update("upvote", post.getUpvote())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            db.collection("PostVotes").document(docId).set(object)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            votes.setText(post.getUpvote()+"");
                                            switch(vote){
                                                case UPVOTE:
                                                    upvotePost.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            undoVote(Vote.UPVOTE);
                                                        }
                                                    });
                                                    downvotePost.setClickable(false);
                                                    break;
                                                case DOWNVOTE:
                                                    downvotePost.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            undoVote(Vote.DOWNVOTE);
                                                        }
                                                    });
                                                    upvotePost.setClickable(false);
                                            }
                                        }
                                    });
                        }
                    });
        }
    }



    protected void removeUpvote(){
        if(mAuth.getUid()!=null) {
            System.out.println("updating database...");
            final String docId = post.getId().concat(mAuth.getUid());
            db.collection("Posts").document(post.getId()).update("upvote", post.getUpvote())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            db.collection("PostVotes").document(docId).delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            votes.setText(post.getUpvote()+"");
                                            upvotePost.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    vote(Vote.UPVOTE);
                                                }
                                            });

                                            downvotePost.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    vote(Vote.DOWNVOTE);
                                                }
                                            });

                                            upvotePost.setClickable(true);
                                            downvotePost.setClickable(true);
                                        }
                                    });
                        }
                    });
        }
    }

    protected void fetchVoteInfo(){
        if(mAuth.getUid()!=null) {
            db.collection("PostVotes").document(post.getId().concat(mAuth.getUid())).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if(documentSnapshot.get("type")!=null){
                                switch(documentSnapshot.get("type").toString()){
                                    //if current user has already upvoted this post
                                    case "upvote":
                                        upvotePost.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                undoVote(Vote.UPVOTE);
                                            }
                                        });
                                        downvotePost.setClickable(false);
                                        break;
                                    case "downvote":
                                        downvotePost.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                undoVote(Vote.DOWNVOTE);
                                            }
                                        });
                                        upvotePost.setClickable(false);
                                }
                            }
                            else{
                                upvotePost.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        vote(Vote.UPVOTE);
                                    }
                                });

                                downvotePost.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        vote(Vote.DOWNVOTE);
                                    }
                                });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            upvotePost.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    vote(Vote.UPVOTE);
                                }
                            });

                            downvotePost.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    vote(Vote.DOWNVOTE);
                                }
                            });
                        }
                    })
            ;
        }
    }

    public void showPostDialog(){
        final AlertDialog.Builder alert = new AlertDialog.Builder(PostDetailActivity.this);
        final View dialogView = getLayoutInflater().inflate(R.layout.invite,null);

        final EditText newTitle = dialogView.findViewById(R.id.input_title);
        final EditText newContent = dialogView.findViewById(R.id.input_content);

        newTitle.setText(post.getTitle());
        newContent.setText(post.getContent());

        final Button postButton = dialogView.findViewById(R.id.post);

        alert.setView(dialogView);

        final AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(true);


        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editPost(post.getId(),newTitle.getText().toString(),newContent.getText().toString());
                alertDialog.dismiss();
            }
        });

        alertDialog.show();

    }

    public void editPost(String id, String newTitle, String newContent){

        db.collection("Posts").document(id).update("title",newTitle,"content",newContent)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(PostDetailActivity.this, "Successfully updated.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void deletePost(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Confirmation")
                .setMessage("Do you want to delete this post?")
                .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        db.collection("Posts").document(post.getId()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(PostDetailActivity.this, "Deleted post", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        });

                    }
                })
                .setPositiveButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    @Override
    public void openUser(int position) {
        startActivity(new Intent(PostDetailActivity.this,ManageUserActivity.class).putExtra("id",replies.get(position).getOwner()));
    }
}
