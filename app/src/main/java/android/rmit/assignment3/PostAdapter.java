package android.rmit.assignment3;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder>  {

    private ArrayList<Post> posts;
    private PostViewHolder.OnPostListener onPostListener;

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder (@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post,parent,false);
        return new PostViewHolder(view,onPostListener);
    }

    PostAdapter(ArrayList<Post> posts, PostViewHolder.OnPostListener onPostListener){
        this.posts = posts;
        this.onPostListener = onPostListener;
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position){
        holder.title.setText(posts.get(position).getTitle());
    }

    @Override
    public int getItemCount(){
        return posts.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView title;
        OnPostListener onPostListener;

        PostViewHolder (View v, OnPostListener onPostListener){
            super(v);
            title = v.findViewById(R.id.post_title);
            this.onPostListener = onPostListener;
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onPostListener.OnPostClick(getAdapterPosition());
        }

        public interface OnPostListener{
            void OnPostClick(int position);
        }
    }
}
