package android.rmit.assignment3;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
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

    private ArrayList<Comment> comments = new ArrayList<>();
    Context mContext;

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
        holder.replyContent.setText(replies.get(position).getContent());
        holder.replyVotes.setText(replies.get(position).getUpvote()+"");

        fetchReplyVoteInfo(replies.get(position).getId(),holder,position);
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
                            initRecyclerView(holder.itemView);
                        }
                    }
                });
    }

    private void initRecyclerView(View v){
        RecyclerView recyclerView=v.findViewById(R.id.my_recycler_view);
        RecyclerView.Adapter adapter= new CommentAdapter(comments,this);
        RecyclerView.LayoutManager layoutManager= new LinearLayoutManager(v.getContext());

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }



    @Override
    public int getItemCount(){
        return replies.size();
    }

    public static class ReplyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
         TextView replyContent;
         Button replyUpvote;
         Button replyDownvote;
         TextView replyVotes;
         ImageView replyAvatar;
         TextView replyOwner;
         OnReplyListener onReplyListener;

         ReplyViewHolder(View v, OnReplyListener onReplyListener){
             super(v);
             replyContent = v.findViewById(R.id.reply_content);
             replyUpvote = v.findViewById(R.id.reply_upvote);
             replyDownvote = v.findViewById(R.id.reply_downvote);
             replyVotes=v.findViewById(R.id.reply_votes);
             replyAvatar = v.findViewById(R.id.reply_owner_avatar);
             replyOwner = v.findViewById(R.id.reply_owner_name);
             this.onReplyListener = onReplyListener;
             v.setOnClickListener(this);
         }

         @Override
         public void onClick(View v){
             onReplyListener.onReplyClick(getAdapterPosition());
         }

        @Override
        public boolean onLongClick(View v) {
             onReplyListener.onReplyLongClick(getAdapterPosition());
             return true;
        }

        public interface OnReplyListener{
             void onReplyClick(int position);
             boolean onReplyLongClick(int position);
         }
    }

    @Override
    public void onCommentClick(int position) {
        System.out.println("comment clicked");
    }

    protected void vote(int replyIndex, PostDetailActivity.Vote vote, ReplyViewHolder holder){
        switch(vote){
            case UPVOTE:
                replies.get(replyIndex).increaseUpvote();
                break;
            case DOWNVOTE:
                replies.get(replyIndex).decreaseUpvote();
                break;
        }
        updateReplyUpvote(replies.get(replyIndex).getId(),replyIndex,vote,holder);
    }

    protected void undoVote(int replyIndex, PostDetailActivity.Vote vote, ReplyViewHolder holder){
        switch(vote){
            case UPVOTE:
                replies.get(replyIndex).decreaseUpvote();
                break;
            case DOWNVOTE:
                replies.get(replyIndex).increaseUpvote();
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
}
