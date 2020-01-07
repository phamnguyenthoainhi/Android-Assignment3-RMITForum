package android.rmit.assignment3;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RankAdapter extends RecyclerView.Adapter<RankAdapter.RankViewHolder>{


    ArrayList<UserSumVote> userSumVotes;
    private static final String TAG = "RankAdapter";
    Context mcontext;



    public RankAdapter(ArrayList<UserSumVote> userSumVotes, Context context) {
        this.userSumVotes = userSumVotes;
        mcontext = context;
    }

    @NonNull
    @Override
    public RankViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rank,parent,false);
        return new RankViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull RankViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: " + userSumVotes.get(position).getImageuri());
        if (userSumVotes.get(position).getVote() == null) {
            userSumVotes.get(position).setVote((long) 0);
        }


        if (userSumVotes.get(position).getImageuri() == null) {

            Picasso.with(mcontext).load(R.drawable.grey).fit().centerCrop()
                    .placeholder(R.drawable.grey)
                    .error(R.drawable.grey)
                    .into(holder.rankavatar);
        } else {
                    Picasso.with(mcontext).load(Uri.parse(userSumVotes.get(position).getImageuri())).fit().centerCrop()
                .placeholder(R.drawable.grey)
                .error(R.drawable.grey)
                .into(holder.rankavatar);
        }

        holder.ranksumvote.setText(userSumVotes.get(position).getVote().toString());
        holder.rankusername.setText(userSumVotes.get(position).getUsername());



    }

    @Override
    public int getItemCount() {
        return userSumVotes.size();
    }

    public static class RankViewHolder extends RecyclerView.ViewHolder {

        TextView rankusername;
        ImageView rankavatar;
        TextView ranksumvote;
        public RankViewHolder(@NonNull View itemView) {
            super(itemView);
            rankavatar = itemView.findViewById(R.id.rankavatar);
            rankusername = itemView.findViewById(R.id.rankusername);
            ranksumvote = itemView.findViewById(R.id.ranksumvote);


        }


    }
}
