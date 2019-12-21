package android.rmit.assignment3;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ReplyAdapter extends RecyclerView.Adapter<ReplyAdapter.ReplyViewHolder> {

    private ArrayList<Reply> replies;
    private ReplyViewHolder.OnReplyListener onReplyListener;

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
    public void onBindViewHolder(@NonNull ReplyViewHolder holder,int position){
        holder.replyContent.setText(replies.get(position).getContent());
    }

    @Override
    public int getItemCount(){
        return replies.size();
    }

    public static class ReplyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
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

         public interface OnReplyListener{
             void onReplyClick(int position);

         }
    }
}
