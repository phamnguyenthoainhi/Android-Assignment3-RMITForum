package android.rmit.assignment3;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class ReplyAdapter extends RecyclerView.Adapter<ReplyAdapter.ReplyViewHolder> implements CommentAdapter.CommentViewHolder.OnCommentListener {

    private ArrayList<Reply> replies;
    private ReplyViewHolder.OnReplyListener onReplyListener;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private Utilities utilities = new Utilities();
    SumVote sumVote = new SumVote();
    ReplyViewHolder holder;

    private ArrayList<Comment> comments = new ArrayList<>();
    Context mContext;
    RecyclerView.Adapter adapter;

    @NonNull
    @Override
    public ReplyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reply,parent,false);
        return new ReplyViewHolder(view, onReplyListener);
    }

    ReplyAdapter(ArrayList<Reply> replies,ReplyViewHolder.OnReplyListener onReplyListener, Context context){
        this.replies = replies;
        this.onReplyListener = onReplyListener;
        mContext = context;
    }

    @Override
    public void onBindViewHolder(@NonNull final ReplyViewHolder holder, final int position){
        this.holder = holder;
        holder.replyContent.setText(replies.get(position).getContent());
        holder.replyVotes.setText(replies.get(position).getUpvote()+"");
        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCommentDialog(position,holder);
            }
        });

        if(mAuth.getUid()!=null && !mAuth.getUid().equals(replies.get(position).getOwner())){
            holder.deleteReply.setVisibility(View.GONE);
            holder.editReply.setVisibility(View.GONE);
            fetchReplyVoteInfo(replies.get(position).getId(),holder,position);
        }
        else if(mAuth.getUid()!=null && mAuth.getUid().equals(replies.get(position).getOwner())){
            holder.replyUpvote.setVisibility(View.GONE);
            holder.replyDownvote.setVisibility(View.GONE);

        }


        fetchReplyOwner(replies.get(position).getOwner(),holder);

        db.collection("Comments").whereEqualTo("reply",replies.get(position).getId()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        comments = new ArrayList<>();
                        for(QueryDocumentSnapshot documentSnapshot:queryDocumentSnapshots){
                            Comment comment = documentSnapshot.toObject(Comment.class);
                            comment.setId(documentSnapshot.getId());
                            comments.add(comment);

                        }
                        initRecyclerView(holder.itemView);
                    }
                });
    }

    private void initRecyclerView(View v){
        RecyclerView recyclerView=v.findViewById(R.id.my_recycler_view);
        adapter= new CommentAdapter(comments,this,v.getContext());
        RecyclerView.LayoutManager layoutManager= new LinearLayoutManager(v.getContext());

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public int getItemCount(){
        return replies.size();
    }

    public static class ReplyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
         TextView replyContent;
         Button replyUpvote;
         Button replyDownvote;
         TextView replyVotes;
         ImageView replyAvatar;
         TextView replyOwner;
         Button editReply;
         Button deleteReply;
         Button comment;

         OnReplyListener onReplyListener;

         ReplyViewHolder(View v, final OnReplyListener onReplyListener){
             super(v);
             replyContent = v.findViewById(R.id.reply_content);
             replyUpvote = v.findViewById(R.id.reply_upvote);
             replyDownvote = v.findViewById(R.id.reply_downvote);
             replyVotes=v.findViewById(R.id.reply_votes);
             replyAvatar = v.findViewById(R.id.reply_owner_avatar);
             replyOwner = v.findViewById(R.id.reply_owner_name);
             editReply = v.findViewById(R.id.edit_reply);
             deleteReply = v.findViewById(R.id.delete_reply);
             comment=v.findViewById(R.id.reply_comment);

             replyOwner.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     onReplyListener.openUser(getAdapterPosition());
                 }
             });

             replyAvatar.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     onReplyListener.openUser(getAdapterPosition());
                 }
             });

             deleteReply.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     onReplyListener.deleteReply(getAdapterPosition());
                 }
             });

             editReply.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     onReplyListener.editReply(getAdapterPosition());
                 }
             });

             this.onReplyListener = onReplyListener;
             v.setOnClickListener(this);
         }

         @Override
         public void onClick(View v){
             onReplyListener.onReplyClick(getAdapterPosition());
         }


        public interface OnReplyListener{
             void onReplyClick(int position);
             void openUser(int position);
             void deleteReply(int position);
             void editReply(int position);
         }
    }

    protected void vote(int replyIndex, PostDetailActivity.Vote vote, ReplyViewHolder holder){
        switch(vote){
            case UPVOTE:
                replies.get(replyIndex).increaseUpvote();
                holder.replyUpvote.setTextColor(Color.parseColor("#7C020000"));
                fetchupdateSumVotes(replies.get(replyIndex).getOwner(), true);
                break;
            case DOWNVOTE:
                fetchupdateSumVotes(replies.get(replyIndex).getOwner(), false);
                holder.replyDownvote.setTextColor(Color.parseColor("#7C020000"));
                replies.get(replyIndex).decreaseUpvote();
                break;
        }
        updateReplyUpvote(replies.get(replyIndex).getId(),replyIndex,vote,holder);
    }

    protected void undoVote(int replyIndex, PostDetailActivity.Vote vote, ReplyViewHolder holder){
        switch(vote){
            case UPVOTE:
                replies.get(replyIndex).decreaseUpvote();
                holder.replyUpvote.setTextColor(Color.parseColor("#D13430"));


                fetchupdateSumVotes(replies.get(replyIndex).getOwner(), false);
                break;
            case DOWNVOTE:
                holder.replyDownvote.setTextColor(Color.parseColor("#1A78CA"));

                replies.get(replyIndex).increaseUpvote();
                fetchupdateSumVotes(replies.get(replyIndex).getOwner(), true);
                break;
        }
        removeReplyVote(replies.get(replyIndex).getId(),holder,replyIndex);
    }

    protected void updateReplyUpvote(String id, final int replyIndex, final PostDetailActivity.Vote vote, final ReplyViewHolder holder){
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
            final String docId = id.concat(mAuth.getUid());
            db.collection("Replies").document(id).update("upvote", replies.get(replyIndex).getUpvote())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            db.collection("ReplyVotes").document(docId).set(object)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            holder.replyVotes.setText(replies.get(replyIndex).getUpvote()+"");
                                            switch(vote){
                                                case UPVOTE:
                                                    holder.replyDownvote.setClickable(false);
                                                    holder.replyUpvote.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            undoVote(replyIndex, PostDetailActivity.Vote.UPVOTE,holder);
                                                        }
                                                    });
                                                    break;
                                                case DOWNVOTE:
                                                    holder.replyUpvote.setClickable(false);
                                                    holder.replyDownvote.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            undoVote(replyIndex, PostDetailActivity.Vote.DOWNVOTE,holder);
                                                        }
                                                    });
                                                    break;
                                            }
                                        }
                                    });
                        }
                    });
        }
    }

    protected void removeReplyVote(final String id, final ReplyViewHolder holder,final int replyIndex){
        if(mAuth.getUid()!=null){
            db.collection("Replies").document(id).update("upvote",replies.get(replyIndex).getUpvote())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            db.collection("ReplyVotes").document(id.concat(mAuth.getUid())).delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            holder.replyDownvote.setClickable(true);
                                            holder.replyUpvote.setClickable(true);

                                            holder.replyVotes.setText(replies.get(replyIndex).getUpvote()+"");

                                            holder.replyUpvote.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    //Toast.makeText(holder.itemView.getContext(), "upvote clicked", Toast.LENGTH_SHORT).show();
                                                    vote(replyIndex, PostDetailActivity.Vote.UPVOTE,holder);
                                                }
                                            });
                                            holder.replyDownvote.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    vote(replyIndex, PostDetailActivity.Vote.DOWNVOTE,holder);
                                                }
                                            });
                                        }
                                    });
                        }
                    });

        }
    }

    protected void fetchReplyVoteInfo(final String id, final ReplyViewHolder holder, final int replyIndex){
        if(mAuth.getUid()!=null) {
            final String docId = id.concat(mAuth.getUid());
            db.collection("ReplyVotes").document(docId).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if(documentSnapshot.get("type")!=null){
                                switch(documentSnapshot.get("type").toString()){
                                    case "upvote":
                                        holder.replyDownvote.setClickable(false);
                                        holder.replyUpvote.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                undoVote(replyIndex, PostDetailActivity.Vote.UPVOTE,holder);
                                            }
                                        });
                                        break;
                                    case "downvote":
                                        holder.replyUpvote.setClickable(false);
                                        holder.replyDownvote.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                undoVote(replyIndex, PostDetailActivity.Vote.DOWNVOTE,holder);
                                            }
                                        });
                                }
                            }
                            else{
                                holder.replyUpvote.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        vote(replyIndex, PostDetailActivity.Vote.UPVOTE,holder);
                                    }
                                });
                                holder.replyDownvote.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        vote(replyIndex, PostDetailActivity.Vote.DOWNVOTE,holder);
                                    }
                                });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            holder.replyUpvote.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    vote(replyIndex, PostDetailActivity.Vote.UPVOTE,holder);
                                }
                            });
                            holder.replyDownvote.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    vote(replyIndex, PostDetailActivity.Vote.DOWNVOTE,holder);
                                }
                            });
                        }
                    });
        }
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


    protected void fetchReplyOwner(String ownerId,final ReplyViewHolder holder){
        if(ownerId!=null) {
            db.collection("Users").document(ownerId).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null) {
                                if (user.getImageuri() != null && !user.getImageuri().equals("")) {
                                    Picasso.with(mContext).load(Uri.parse(user.getImageuri())).fit().centerCrop()
                                            .placeholder(R.drawable.grey)
                                            .error(R.drawable.grey)
                                            .into(holder.replyAvatar);
//                                    holder.replyAvatar.setImageURI(utilities.convertUri(user.getImageuri()));
                                }
                                if (user.getFullname() != null) {
                                    holder.replyOwner.setText(user.getFullname());
                                }
                            }
                        }
                    });
        }
    }


    public void showCommentDialog(final int position, final ReplyViewHolder holder){
        final AlertDialog.Builder alert = new AlertDialog.Builder(holder.itemView.getContext());
        final View dialogView = LayoutInflater.from(holder.itemView.getContext()).inflate(R.layout.comment_dialog,null);


        final EditText newCommentContent = dialogView.findViewById(R.id.comment_input_content);

        final Button comment = dialogView.findViewById(R.id.create_comment);

        alert.setView(dialogView);

        final AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(true);

        comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createComment(new Comment(replies.get(position).getId(),mAuth.getUid(),newCommentContent.getText().toString()),holder);
                alertDialog.dismiss();
            }
        });

        alertDialog.show();

    }


    protected void createComment(final Comment comment, final ReplyViewHolder holder){
        db.collection("Comments").add(comment)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(holder.itemView.getContext(), "Successfully posted comment.", Toast.LENGTH_SHORT).show();
                        if(adapter!=null) {
                            db.collection("Comments").whereEqualTo("reply", comment.getReply()).get()
                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            comments = new ArrayList<>();
                                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                                Comment comment = documentSnapshot.toObject(Comment.class);
                                                comment.setId(documentSnapshot.getId());
                                                comments.add(comment);
                                                initRecyclerView(holder.itemView);
                                            }
                                        }
                                    });
                        }
                        else {
                            db.collection("Comments").whereEqualTo("reply", comment.getReply()).get()
                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            comments = new ArrayList<>();
                                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                                Comment comment = documentSnapshot.toObject(Comment.class);
                                                comment.setId(documentSnapshot.getId());
                                                comments.add(comment);
                                                initRecyclerView(holder.itemView);
                                            }
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(holder.itemView.getContext(), "Failed to post comment. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void deleteComment(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext())
                .setTitle("Confirmation")
                .setMessage("Do you want to delete this comment?")
                .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        db.collection("Comments").document(comments.get(position).getId()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(holder.itemView.getContext(), "Deleted the comment.", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                db.collection("Comments").whereEqualTo("reply",comments.get(position).getReply()).get()
                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                comments = new ArrayList<>();
                                                for(QueryDocumentSnapshot documentSnapshot:queryDocumentSnapshots){
                                                    Comment comment = documentSnapshot.toObject(Comment.class);
                                                    comment.setId(documentSnapshot.getId());
                                                    comments.add(comment);
                                                    initRecyclerView(holder.itemView);
                                                }
                                                if(adapter!=null){
                                                    adapter.notifyDataSetChanged();
                                                }
                                            }
                                        });
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
        final AlertDialog.Builder alert = new AlertDialog.Builder(holder.itemView.getContext());
        final View dialogView = LayoutInflater.from(holder.itemView.getContext()).inflate(R.layout.comment_dialog,null);

        final EditText content = dialogView.findViewById(R.id.comment_input_content);
        content.setText(comments.get(position).getContent());

        final Button comment = dialogView.findViewById(R.id.create_comment);

        TextView dialogTitle= dialogView.findViewById(R.id.comment_dialog_title);
        dialogTitle.setText("Edit this comment");

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
                                db.collection("Comments").whereEqualTo("reply",comments.get(position).getReply()).get()
                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                comments = new ArrayList<>();
                                                for(QueryDocumentSnapshot documentSnapshot:queryDocumentSnapshots){
                                                    Comment comment = documentSnapshot.toObject(Comment.class);
                                                    comment.setId(documentSnapshot.getId());
                                                    comments.add(comment);
                                                    initRecyclerView(holder.itemView);
                                                }
                                                if(adapter!=null){
                                                    adapter.notifyDataSetChanged();
                                                }
                                            }
                                        });
                            }
                        });

            }
        });

        alertDialog.show();
    }
}
