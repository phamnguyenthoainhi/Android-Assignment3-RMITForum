package android.rmit.assignment3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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
    TextView votes;
    EditText newReplyContent;
    EditText newCommentContent;
    ArrayList<Reply> replies = new ArrayList<>();

    RecyclerView.Adapter adapter;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    enum Vote{
        UPVOTE,
        DOWNVOTE
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        title = findViewById(R.id.title_text);
        content = findViewById(R.id.content_text);
        votes = findViewById(R.id.post_votes);

        upvotePost = findViewById(R.id.post_upvote);
        downvotePost = findViewById(R.id.post_downvote);

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

        Toast.makeText(this, id, Toast.LENGTH_SHORT).show();


    }

    public void initRecyclerView(){
        RecyclerView recyclerView = findViewById(R.id.my_recycler_view);;
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);;

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new ReplyAdapter(replies,this);

        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onReplyClick(int position) {
        Toast.makeText(this, "clicked", Toast.LENGTH_SHORT).show();
        if(mAuth.getUid()!=null) {
            showCommentDialog(position);
        }
        else{startActivity(new Intent(PostDetailActivity.this,SignInActivity.class));}
    }

    @Override
    public boolean onReplyLongClick(int position) {
        if(mAuth.getUid()!=null) {
            showCommentDialog(position);
        }
        else{startActivity(new Intent(PostDetailActivity.this,SignInActivity.class));}
        return true;
    }

    protected void fetchPost (String id){
        db.collection("Posts").document(id).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        post = documentSnapshot.toObject(Post.class);
                        post.setId(documentSnapshot.getId());
                        title.setText(post.getTitle());
                        content.setText(post.getContent());
                        votes.setText(post.getUpvote()+"");

                        fetchVoteInfo();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG,"Failed to fetch");
                    }
                });
    }

    public void showCommentDialog(final int position){
        final AlertDialog.Builder alert = new AlertDialog.Builder(PostDetailActivity.this);
        final View dialogView = getLayoutInflater().inflate(R.layout.comment_dialog,null);


        newCommentContent = dialogView.findViewById(R.id.comment_input_content);

        final Button comment = dialogView.findViewById(R.id.create_comment);

        alert.setView(dialogView);

        final AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(true);

        comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createComment(new Comment(replies.get(position).getId(),mAuth.getUid(),newCommentContent.getText().toString()));
                alertDialog.dismiss();
            }
        });

        alertDialog.show();

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

    protected void createComment(Comment comment){
        db.collection("Comments").add(comment)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(PostDetailActivity.this, "Successfully posted comment.", Toast.LENGTH_SHORT).show();
//                        replies = new ArrayList<>();
//                        fetchReplies(id);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PostDetailActivity.this, "Failed to post comment. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    protected void createReply(Reply reply){
        db.collection("Replies").add(reply)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(PostDetailActivity.this, "Successfully posted reply_dialog.", Toast.LENGTH_SHORT).show();
//                        replies = new ArrayList<>();
//                        fetchReplies(id);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PostDetailActivity.this, "Failed to post reply_dialog. Please try again.", Toast.LENGTH_SHORT).show();
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
                            //Toast.makeText(PostDetailActivity.this, reply.getContent(), Toast.LENGTH_SHORT).show();
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
                break;
            case DOWNVOTE:
                post.decreaseUpvote();
                break;
        }

        updateUpvote(vote);
    }

    protected void undoVote(Vote vote){
        switch(vote){
            case UPVOTE:
                post.decreaseUpvote();
                break;
            case DOWNVOTE:
                post.increaseUpvote();
                break;
        }
        removeUpvote();
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




}
