package android.rmit.assignment3;

import android.content.DialogInterface;
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

import java.util.ArrayList;
import java.util.HashMap;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<Comment> comments;
    private Utilities utilities = new Utilities();

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment,parent,false);
        return new CommentViewHolder(view);
    }

    CommentAdapter(ArrayList<Comment> comments){
        this.comments = comments;
    }

    @Override
    public void onBindViewHolder(@NonNull final CommentViewHolder holder, final int position) {
        holder.commentContent.setText(comments.get(position).getContent());
        holder.commentVotes.setText(comments.get(position).getUpvote()+"");

        if(mAuth.getUid()!= null && !mAuth.getUid().equals(comments.get(position).getOwner())){
            holder.editComment.setVisibility(View.GONE);
            holder.deleteComment.setVisibility(View.GONE);
            fetchCommentVoteInfo(comments.get(position).getId(),holder,position);
        }
        else if(mAuth.getUid()!=null && mAuth.getUid().equals(comments.get(position).getOwner())){
            holder.commentDownvote.setVisibility(View.GONE);
            holder.commentUpvote.setVisibility(View.GONE);
            holder.editComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editComment(comments.get(position).getId(),holder,position);
                }
            });
            holder.deleteComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteComment(comments.get(position).getId(),holder);
                }
            });
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

        CommentViewHolder(View v){
            super(v);
            commentContent = v.findViewById(R.id.comment_content);
            commentUpvote = v.findViewById(R.id.comment_upvote);
            commentDownvote = v.findViewById(R.id.comment_downvote);
            commentVotes = v.findViewById(R.id.comment_votes);
            commentAvatar = v.findViewById(R.id.comment_onwer_avatar);
            commentOwner = v.findViewById(R.id.comment_onwer_name);
            editComment = v.findViewById(R.id.edit_comment);
            deleteComment = v.findViewById(R.id.delete_comment);

        }
    }

    protected void vote(int commentIndex, PostDetailActivity.Vote vote, CommentViewHolder holder){
        switch(vote){
            case UPVOTE:
                comments.get(commentIndex).increaseUpvote();
                break;
            case DOWNVOTE:
                comments.get(commentIndex).decreaseUpvote();
                break;
        }
        updateCommentUpvote(comments.get(commentIndex).getId(),commentIndex,vote,holder);
    }

    protected void undoVote(int commentIndex, PostDetailActivity.Vote vote, CommentViewHolder holder){
        switch(vote){
            case UPVOTE:
                comments.get(commentIndex).decreaseUpvote();
                break;
            case DOWNVOTE:
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
                                        holder.commentDownvote.setClickable(false);
                                        holder.commentUpvote.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                undoVote(commentIndex, PostDetailActivity.Vote.UPVOTE,holder);
                                            }
                                        });
                                        break;
                                    case "downvote":
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
                                if (user.getImageuri() != null && user.getImageuri() != "") {
                                    holder.commentAvatar.setImageURI(utilities.convertUri(user.getImageuri()));
                                }
                                if (user.getFullname() != null) {
                                    holder.commentOwner.setText(user.getFullname());
                                }
                            }
                        }
                    });
        }
    }

    protected void editComment (final String id, CommentViewHolder holder, int commentIndex){

        final AlertDialog.Builder alert = new AlertDialog.Builder(holder.itemView.getContext());
        final View dialogView = LayoutInflater.from(holder.itemView.getContext()).inflate(R.layout.comment_dialog,null);

        final EditText content = dialogView.findViewById(R.id.comment_input_content);
        content.setText(comments.get(commentIndex).getContent());

        Button comment = dialogView.findViewById(R.id.create_comment);

        alert.setView(dialogView);

        final AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(true);

        comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("Comments").document(id).update("content",content.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                alertDialog.dismiss();
                            }
                        });

            }
        });

        alertDialog.show();
    }

    protected void deleteComment(final String id, final CommentViewHolder holder){
        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext())
                .setTitle("Confirmation")
                .setMessage("Do you want to delete this comment?")
                .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        db.collection("Comments").document(id).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(holder.itemView.getContext(), "Deleted the comment.", Toast.LENGTH_SHORT).show();
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
}
