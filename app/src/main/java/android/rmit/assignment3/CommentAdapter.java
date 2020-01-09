package android.rmit.assignment3;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<Comment> comments;
    private Utilities utilities = new Utilities();
    Context mContext;
    SumVote sumVote = new SumVote();
    CommentViewHolder.OnCommentListener onCommentListener;

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment,parent,false);
        return new CommentViewHolder(view,onCommentListener);
    }

    CommentAdapter(ArrayList<Comment> comments, CommentViewHolder.OnCommentListener onCommentListener ,Context context){
        this.comments = comments;
        this.mContext = context;
        this.onCommentListener = onCommentListener;
    }

    @Override
    public void onBindViewHolder(@NonNull final CommentViewHolder holder, final int position) {
        holder.commentContent.setText(comments.get(position).getContent());
        holder.commentVotes.setText(comments.get(position).getUpvote()+"");
        holder.commentAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.itemView.getContext().startActivity(new Intent(holder.itemView.getContext(),ManageUserActivity.class).putExtra("id",comments.get(position).getOwner()));
            }
        });
        holder.commentOwner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.itemView.getContext().startActivity(new Intent(holder.itemView.getContext(),ManageUserActivity.class).putExtra("id",comments.get(position).getOwner()));
            }
        });

        if(mAuth.getUid()!= null && !mAuth.getUid().equals(comments.get(position).getOwner())){
            holder.editComment.setVisibility(View.GONE);
            holder.deleteComment.setVisibility(View.GONE);
            fetchCommentVoteInfo(comments.get(position).getId(),holder,position);
        }
        else if(mAuth.getUid()!=null && mAuth.getUid().equals(comments.get(position).getOwner())){
            holder.commentDownvote.setVisibility(View.GONE);
            holder.commentUpvote.setVisibility(View.GONE);
        }


        fetchCommentOwner(comments.get(position).getOwner(),holder);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder{
        TextView commentContent;
        Button commentUpvote;
        Button commentDownvote;
        TextView commentVotes;
        ImageView commentAvatar;
        TextView commentOwner;
        Button editComment;
        Button deleteComment;


        OnCommentListener onCommentListener;


        CommentViewHolder(View v, final OnCommentListener onCommentListener){
            super(v);
            commentContent = v.findViewById(R.id.comment_content);
            commentUpvote = v.findViewById(R.id.comment_upvote);
            commentDownvote = v.findViewById(R.id.comment_downvote);
            commentVotes = v.findViewById(R.id.comment_votes);
            commentAvatar = v.findViewById(R.id.comment_owner_avatar);
            commentOwner = v.findViewById(R.id.comment_owner_name);
            editComment = v.findViewById(R.id.edit_comment);
            deleteComment = v.findViewById(R.id.delete_comment);


            deleteComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onCommentListener.deleteComment(getAdapterPosition());
                }
            });

            editComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onCommentListener.editComment(getAdapterPosition());
                }
            });

            this.onCommentListener = onCommentListener;
        }



        public interface OnCommentListener{
            void deleteComment(int position);
            void editComment(int position);
        }

    }

    protected void vote(int commentIndex, PostDetailActivity.Vote vote, CommentViewHolder holder){
        switch(vote){
            case UPVOTE:
                holder.commentUpvote.setText("Undo vote");
                holder.commentDownvote.setTextColor(Color.parseColor("#7C020000"));
                comments.get(commentIndex).increaseUpvote();
                fetchupdateSumVotes(comments.get(commentIndex).getOwner(), true);
                break;
            case DOWNVOTE:
                holder.commentDownvote.setText("Undo vote");
                holder.commentUpvote.setTextColor(Color.parseColor("#7C020000"));
                fetchupdateSumVotes(comments.get(commentIndex).getOwner(), false);

                comments.get(commentIndex).decreaseUpvote();
                break;
        }
        updateCommentUpvote(comments.get(commentIndex).getId(),commentIndex,vote,holder);
    }

    protected void undoVote(int commentIndex, PostDetailActivity.Vote vote, CommentViewHolder holder){
        switch(vote){
            case UPVOTE:
                fetchupdateSumVotes(comments.get(commentIndex).getOwner(), false);
                holder.commentUpvote.setText("Up Vote");
                holder.commentDownvote.setTextColor(Color.parseColor("#1A78CA"));
                comments.get(commentIndex).decreaseUpvote();

                break;
            case DOWNVOTE:
                fetchupdateSumVotes(comments.get(commentIndex).getOwner(), true);
                holder.commentDownvote.setText("Down Vote");
                holder.commentUpvote.setTextColor(Color.parseColor("#D13430"));
                comments.get(commentIndex).increaseUpvote();
                break;
        }
        removeCommentVote(comments.get(commentIndex).getId(),holder,commentIndex);
    }

    protected void updateCommentUpvote(String id, final int commentIndex, final PostDetailActivity.Vote vote, final CommentViewHolder holder){
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
            db.collection("Comments").document(id).update("upvote", comments.get(commentIndex).getUpvote())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            db.collection("CommentVotes").document(docId).set(object)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            holder.commentVotes.setText(comments.get(commentIndex).getUpvote()+"");
                                            switch(vote){
                                                case UPVOTE:
                                                    holder.commentDownvote.setClickable(false);
                                                    holder.commentUpvote.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            undoVote(commentIndex, PostDetailActivity.Vote.UPVOTE,holder);
                                                        }
                                                    });
                                                    break;
                                                case DOWNVOTE:
                                                    holder.commentUpvote.setClickable(false);
                                                    holder.commentDownvote.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            undoVote(commentIndex, PostDetailActivity.Vote.DOWNVOTE,holder);
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

    protected void removeCommentVote(final String id, final CommentViewHolder holder,final int commentIndex){
        if(mAuth.getUid()!=null){
            db.collection("Comments").document(id).update("upvote",comments.get(commentIndex).getUpvote())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            db.collection("CommentVotes").document(id.concat(mAuth.getUid())).delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            holder.commentDownvote.setClickable(true);
                                            holder.commentUpvote.setClickable(true);

                                            holder.commentVotes.setText(comments.get(commentIndex).getUpvote()+"");

                                            holder.commentUpvote.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    //Toast.makeText(holder.itemView.getContext(), "upvote clicked", Toast.LENGTH_SHORT).show();
                                                    vote(commentIndex, PostDetailActivity.Vote.UPVOTE,holder);
                                                }
                                            });
                                            holder.commentDownvote.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    vote(commentIndex, PostDetailActivity.Vote.DOWNVOTE,holder);
                                                }
                                            });
                                        }
                                    });
                        }
                    });

        }
    }

    protected void fetchCommentVoteInfo(final String id, final CommentViewHolder holder, final int commentIndex){
        if(mAuth.getUid()!=null) {
            final String docId = id.concat(mAuth.getUid());
            db.collection("CommentVotes").document(docId).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if(documentSnapshot.get("type")!=null){
                                switch(documentSnapshot.get("type").toString()){
                                    case "upvote":
                                        holder.commentUpvote.setText("Undo vote");
                                        holder.commentDownvote.setTextColor(Color.parseColor("#7C020000"));
                                        holder.commentDownvote.setClickable(false);
                                        holder.commentUpvote.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                undoVote(commentIndex, PostDetailActivity.Vote.UPVOTE,holder);
                                            }
                                        });
                                        break;
                                    case "downvote":
                                        holder.commentDownvote.setText("Undo vote");
                                        holder.commentUpvote.setTextColor(Color.parseColor("#7C020000"));
                                        holder.commentUpvote.setClickable(false);
                                        holder.commentDownvote.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                undoVote(commentIndex, PostDetailActivity.Vote.DOWNVOTE,holder);
                                            }
                                        });
                                }
                            }
                            else{
                                holder.commentUpvote.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        vote(commentIndex, PostDetailActivity.Vote.UPVOTE,holder);
                                    }
                                });
                                holder.commentDownvote.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        vote(commentIndex, PostDetailActivity.Vote.DOWNVOTE,holder);
                                    }
                                });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            holder.commentUpvote.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    vote(commentIndex, PostDetailActivity.Vote.UPVOTE,holder);
                                }
                            });
                            holder.commentDownvote.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    vote(commentIndex, PostDetailActivity.Vote.DOWNVOTE,holder);
                                }
                            });
                        }
                    });
        }
    }

    protected void fetchCommentOwner(String ownerId,final CommentViewHolder holder){
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
                                            .into(holder.commentAvatar);
                                }
                                if (user.getFullname() != null) {
                                    holder.commentOwner.setText(user.getFullname());
                                }
                            }
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



}
