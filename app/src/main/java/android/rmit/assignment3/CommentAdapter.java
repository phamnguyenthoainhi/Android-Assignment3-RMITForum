package android.rmit.assignment3;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {


    private ArrayList<Comment> comments;
    private CommentViewHolder.OnCommentListener onCommentListener;

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment,parent,false);
        return new CommentViewHolder(view,onCommentListener);
    }

    CommentAdapter(ArrayList<Comment> comments,CommentViewHolder.OnCommentListener onCommentListener){
        this.comments = comments;
        this.onCommentListener = onCommentListener;
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        holder.commentContent.setText(comments.get(position).getContent());
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView commentContent;

        OnCommentListener onCommentListener;

        CommentViewHolder(View v, OnCommentListener onCommentListener){
            super(v);
            commentContent = v.findViewById(R.id.comment_content);
            this.onCommentListener = onCommentListener;
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v){
            onCommentListener.onCommentClick(getAdapterPosition());
        }

        public interface OnCommentListener{
            void onCommentClick(int position);
        }
    }
}
