package android.rmit.assignment3;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ReplyAdapter extends RecyclerView.Adapter<ReplyAdapter.ReplyViewHolder> implements CommentAdapter.CommentViewHolder.OnCommentListener {

    private ArrayList<Reply> replies;
    private ReplyViewHolder.OnReplyListener onReplyListener;



    private ArrayList<Comment> comments = new ArrayList<>();

    @NonNull
    @Override
    public ReplyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reply,parent,false);
        return new ReplyViewHolder(view, onReplyListener);
    }

    ReplyAdapter(ArrayList<Reply> replies,ReplyViewHolder.OnReplyListener onReplyListener){
        this.replies = replies;
        this.onReplyListener = onReplyListener;
    }

    @Override
    public void onBindViewHolder(@NonNull final ReplyViewHolder holder, int position){
        holder.replyContent.setText(replies.get(position).getContent());
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        db.collection("Comments").whereEqualTo("reply",replies.get(position).getId()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        comments = new ArrayList<>();
                        for(QueryDocumentSnapshot documentSnapshot:queryDocumentSnapshots){
                            Comment comment = documentSnapshot.toObject(Comment.class);
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
         OnReplyListener onReplyListener;

         ReplyViewHolder(View v, OnReplyListener onReplyListener){
             super(v);
             replyContent = v.findViewById(R.id.reply_content);
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
             return false;
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
}
