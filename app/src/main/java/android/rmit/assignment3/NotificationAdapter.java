package android.rmit.assignment3;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private ArrayList<Notification> notifications;
    private NotificationViewHolder.OnNotificationListener onNotificationListener;

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent,int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification,parent,false);
        return new NotificationViewHolder(view, onNotificationListener);
    }

    NotificationAdapter(ArrayList<Notification> notifications,NotificationViewHolder.OnNotificationListener onNotificationListener){
        this.notifications = notifications;
        this.onNotificationListener = onNotificationListener;
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        holder.title.setText(notifications.get(position).getTitle());
        holder.content.setText(notifications.get(position).getContent());
        if(!notifications.get(position).isSeen()){
            holder.itemView.findViewById(R.id.notification_card).setBackgroundColor(Color.parseColor("#ffe6e6"));
        }
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView title;
        TextView content;
        OnNotificationListener onNotificationListener;

        NotificationViewHolder (View v, OnNotificationListener onNotificationListener){
            super(v);
            title = v.findViewById(R.id.notification_title);
            content = v.findViewById(R.id.notification_content);
            this.onNotificationListener = onNotificationListener;
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onNotificationListener.onNotificationClick(getAdapterPosition());
        }

        public interface OnNotificationListener{
            void onNotificationClick(int position);
        }
    }
}
