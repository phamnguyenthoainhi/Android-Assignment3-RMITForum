package android.rmit.assignment3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class ReplyDetailActivity extends AppCompatActivity implements CommentAdapter.CommentViewHolder.OnCommentListener {

    String id;
    String TAG="REPLY DETAIL";
    Reply reply = new Reply();
    ArrayList<Comment> comments = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    RecyclerView.Adapter adapter;

    TextView content;

    Button upvoteReply;
    Button downvoteReply;
    Button editReply;
    Button deleteReply;
    TextView votes;
    ImageView avatar;
    TextView owner;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    Utilities utilities = new Utilities();
    SumVote sumVote;

    enum Vote{
        UPVOTE,
        DOWNVOTE
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply_detail);

        sumVote = new SumVote();
        content = findViewById(R.id.content_text);
        upvoteReply = findViewById(R.id.reply_upvote);
        downvoteReply = findViewById(R.id.reply_downvote);
        editReply =findViewById(R.id.edit_reply);
        deleteReply = findViewById(R.id.delete_reply);
        votes = findViewById(R.id.reply_votes);
        avatar =findViewById(R.id.reply_owner_avatar);
        owner = findViewById(R.id.reply_owner_name);

        Button fromdetail = findViewById(R.id.fromreplydetail);
        fromdetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(reply.getPost()!=null && !reply.getPost().equals("")) {
                    startActivity(new Intent(ReplyDetailActivity.this, PostDetailActivity.class).putExtra("id", reply.getPost()));
                }
                finish();
            }
        });
        Button commentButton = findViewById(R.id.comment_button);

        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCommentDialog();
            }
        });

        onNewIntent(getIntent());

        final SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipe_to_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                comments = new ArrayList<>();
                fetchComments(id);
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

        fetchReply((String)bundle.get("id"));
        id =(String) bundle.get("id");

        fetchComments(id);

        Toast.makeText(this, id, Toast.LENGTH_SHORT).show();


    }

    public void initRecyclerView(){
        RecyclerView recyclerView = findViewById(R.id.my_recycler_view);;
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);;

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new CommentAdapter(comments,this,ReplyDetailActivity.this);

        recyclerView.setAdapter(adapter);
    }

    protected void fetchReply (String id){
        db.collection("Replies").document(id).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        reply = documentSnapshot.toObject(Reply.class);
                        if(reply!=null) {
                            reply.setId(documentSnapshot.getId());
                            content.setText(reply.getContent());
                            fetchReplyOwner(reply.getOwner());
                            votes.setText(reply.getUpvote()+"");

                            avatar.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    startActivity(new Intent(ReplyDetailActivity.this,ManageUserActivity.class).putExtra("id",reply.getOwner()));
                                }
                            });

                            owner.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    startActivity(new Intent(ReplyDetailActivity.this,ManageUserActivity.class).putExtra("id",reply.getOwner()));
                                }
                            });

                            if (mAuth.getUid() != null && !mAuth.getUid().equals(reply.getOwner())) {
                                editReply.setVisibility(View.GONE);
                                deleteReply.setVisibility(View.GONE);
                                fetchVoteInfo();
                            } else if (mAuth.getUid() != null && mAuth.getUid().equals(reply.getOwner())) {
                                upvoteReply.setVisibility(View.GONE);
                                downvoteReply.setVisibility(View.GONE);

                                editReply.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        showReplyDialog();
                                    }
                                });

                                deleteReply.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        deleteReply();
                                    }
                                });
                            }
                            fetchVoteInfo();
                        }
                        else{
                            content.setText("Reply does not exist");
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

    protected void vote(Vote vote){
        switch(vote){
            case UPVOTE:
                reply.increaseUpvote();
                upvoteReply.setText("Undo vote");
                downvoteReply.setTextColor(Color.parseColor("#7C020000"));
                if (reply.getOwner() != null ){
                    fetchupdateSumVotes(reply.getOwner(), true);
                }

                break;
            case DOWNVOTE:
                reply.decreaseUpvote();
                downvoteReply.setText("Undo vote");
                upvoteReply.setTextColor(Color.parseColor("#7C020000"));
                if (reply.getOwner() != null) {
                    fetchupdateSumVotes(reply.getOwner(), false);
                }
                break;
        }
        updateUpvote(vote);
    }

    protected void undoVote(Vote vote){
        switch(vote){
            case UPVOTE:
                upvoteReply.setText("Up Vote");
                downvoteReply.setTextColor(Color.parseColor("#1A78CA"));
                reply.decreaseUpvote();
                if (reply.getOwner() != null) {
                    fetchupdateSumVotes(reply.getOwner(), false);
                }
                break;
            case DOWNVOTE:
                upvoteReply.setTextColor(Color.parseColor("#D13430"));
                downvoteReply.setText("Down Vote");
                reply.increaseUpvote();
                if (reply.getOwner() != null ){
                    fetchupdateSumVotes(reply.getOwner(), true);
                }
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
            final String docId = reply.getId().concat(mAuth.getUid());
            db.collection("Replies").document(reply.getId()).update("upvote", reply.getUpvote())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            db.collection("ReplyVotes").document(docId).set(object)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            votes.setText(reply.getUpvote()+"");
                                            switch(vote){
                                                case UPVOTE:
                                                    upvoteReply.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            undoVote(Vote.UPVOTE);
                                                        }
                                                    });
                                                    downvoteReply.setClickable(false);
                                                    break;
                                                case DOWNVOTE:
                                                    downvoteReply.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            undoVote(Vote.DOWNVOTE);
                                                        }
                                                    });
                                                    upvoteReply.setClickable(false);
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
            final String docId = reply.getId().concat(mAuth.getUid());
            db.collection("Replies").document(reply.getId()).update("upvote", reply.getUpvote())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            db.collection("ReplyVotes").document(docId).delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            votes.setText(reply.getUpvote()+"");
                                            upvoteReply.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    vote(Vote.UPVOTE);
                                                }
                                            });

                                            downvoteReply.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    vote(Vote.DOWNVOTE);
                                                }
                                            });

                                            upvoteReply.setClickable(true);
                                            downvoteReply.setClickable(true);
                                        }
                                    });
                        }
                    });
        }
    }

    public void fetchVoteInfo(){
        if(mAuth.getUid()!=null) {
            db.collection("ReplyVotes").document(reply.getId().concat(mAuth.getUid())).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if(documentSnapshot.get("type")!=null){
                                switch(documentSnapshot.get("type").toString()){
                                    //if current user has already upvoted this post
                                    case "upvote":
                                        upvoteReply.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                undoVote(Vote.UPVOTE);
                                            }
                                        });
                                        upvoteReply.setText("Undo vote");
                                        downvoteReply.setTextColor(Color.parseColor("#7C020000"));

                                        downvoteReply.setClickable(false);
                                        break;
                                    case "downvote":
                                        downvoteReply.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                undoVote(Vote.DOWNVOTE);
                                            }
                                        });
                                        downvoteReply.setText("Undo vote");
                                        upvoteReply.setTextColor(Color.parseColor("#7C020000"));
                                        upvoteReply.setClickable(false);
                                }
                            }
                            else{
                                upvoteReply.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        vote(Vote.UPVOTE);
                                    }
                                });

                                downvoteReply.setOnClickListener(new View.OnClickListener() {
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
                            upvoteReply.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    vote(Vote.UPVOTE);
                                }
                            });

                            downvoteReply.setOnClickListener(new View.OnClickListener() {
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
    public void showReplyDialog(){
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final View dialogView = LayoutInflater.from(this).inflate(R.layout.reply_dialog,null);

        final EditText content = dialogView.findViewById(R.id.reply_input_content);
        content.setText(reply.getContent());
        TextView dialogTitle = dialogView.findViewById(R.id.reply_dialog_title);
        dialogTitle.setText("Edit this comment");

        Button replyButton = dialogView.findViewById(R.id.create_reply);

        alert.setView(dialogView);

        final AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(true);


        replyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editReply(reply.getId(),content.getText().toString());
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    public void editReply(String id, String newContent){
        db.collection("Replies").document(id).update("content",newContent)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(ReplyDetailActivity.this, "Successfully updated.", Toast.LENGTH_SHORT).show();
                        fetchReply(reply.getId());
                    }
                });
    }

    public void deleteReply(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Confirmation")
                .setMessage("Do you want to delete this comment?")
                .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        db.collection("Replies").document(reply.getId()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(ReplyDetailActivity.this, "Deleted comment", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                finish();
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

    protected  void fetchComments(String replyId){
        db.collection("Comments").whereEqualTo("reply",replyId).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){
                            Comment comment = documentSnapshot.toObject(Comment.class);
                            comment.setId(documentSnapshot.getId());
                            comments.add(comment);
                            initRecyclerView();

                        }

                    }
                });

    }

    @Override
    public void deleteComment(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Confirmation")
                .setMessage("Do you want to delete this reply?")
                .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        db.collection("Comments").document(comments.get(position).getId()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(ReplyDetailActivity.this, "Deleted the reply.", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                comments=new ArrayList<>();
                                fetchComments(id);
                                if(adapter!=null){
                                    adapter.notifyDataSetChanged();
                                }
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
    public void editComment(final int position) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final View dialogView = LayoutInflater.from(this).inflate(R.layout.comment_dialog,null);

        final EditText content = dialogView.findViewById(R.id.comment_input_content);
        content.setText(comments.get(position).getContent());

        final Button comment = dialogView.findViewById(R.id.create_comment);

        TextView dialogTitle= dialogView.findViewById(R.id.comment_dialog_title);
        dialogTitle.setText("Edit this reply");

        alert.setView(dialogView);


        final AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(true);

        comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("Comments").document(comments.get(position).getId()).update("content",content.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                alertDialog.dismiss();
                                comments=new ArrayList<>();
                                fetchComments(id);
                                if(adapter!=null){
                                    adapter.notifyDataSetChanged();
                                }

                            }
                        });

            }
        });

        alertDialog.show();
    }

    public void showCommentDialog(){
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final View dialogView = LayoutInflater.from(this).inflate(R.layout.comment_dialog,null);


        final EditText newCommentContent = dialogView.findViewById(R.id.comment_input_content);

        final Button comment = dialogView.findViewById(R.id.create_comment);

        alert.setView(dialogView);

        final AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(true);

        comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createComment(new Comment(id,mAuth.getUid(),newCommentContent.getText().toString()));
                alertDialog.dismiss();
            }
        });

        alertDialog.show();

    }

    protected void createComment(final Comment comment){
        db.collection("Comments").add(comment)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(ReplyDetailActivity.this, "Successfully posted reply.", Toast.LENGTH_SHORT).show();
                        if(adapter!=null) {
                            comments=new ArrayList<>();
                            fetchComments(id);
                            adapter.notifyDataSetChanged();
                        }
                        else {
                            comments=new ArrayList<>();
                            fetchComments(id);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ReplyDetailActivity.this, "Failed to post reply. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    protected void fetchReplyOwner(String ownerId){
        db.collection("Users").document(ownerId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user!=null){
                            if(user.getImageuri()!=null && user.getImageuri()!="") {
                                Picasso.with(ReplyDetailActivity.this).load(Uri.parse(user.getImageuri())).fit().centerCrop()
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
}
