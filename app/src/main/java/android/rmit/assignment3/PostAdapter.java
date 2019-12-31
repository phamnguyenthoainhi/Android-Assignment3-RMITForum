package android.rmit.assignment3;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder>  {

    private ArrayList<Post> posts;
    private PostViewHolder.OnPostListener onPostListener;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Utilities utilities = new Utilities();
    private static final String TAG = "PostAdapter";
    Context mContext;

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder (@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post,parent,false);
        return new PostViewHolder(view,onPostListener);
    }

    PostAdapter(ArrayList<Post> posts, PostViewHolder.OnPostListener onPostListener, Context context){
        this.posts = posts;
        this.onPostListener = onPostListener;
        mContext = context;
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position){
        holder.title.setText(posts.get(position).getTitle());
        holder.votes.setText(posts.get(position).getUpvote()+"");
        fetchPostOwner(posts.get(position).getOwner(),holder);
    }

    @Override
    public int getItemCount(){
        return posts.size();
    }

    public void filter(CharSequence text, ArrayList<Post> inputposts) {
        ArrayList<Post> filtered= new ArrayList<>();
        for (Post post: inputposts) {

            if (post.getTitle().toLowerCase().contains(text.toString().toLowerCase())) {
                filtered.add(post);
            }
        }
        posts = filtered;
        notifyDataSetChanged();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView title;
        TextView votes;
        ImageView avatar;
        TextView owner;
        OnPostListener onPostListener;

        PostViewHolder (View v, OnPostListener onPostListener){
            super(v);
            title = v.findViewById(R.id.title_text);
            votes = v.findViewById(R.id.upvotenumber);
            avatar = v.findViewById(R.id.owneravatar);
            owner = v.findViewById(R.id.ownername);
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

    protected void fetchPostOwner(String ownerId,final PostViewHolder holder){
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
                                            .into(holder.avatar);

                                }


                                if (user.getFullname() != null) {
                                    holder.owner.setText(user.getFullname());
                                }
                            }
                        }
                    });
        }
    }

}
