package com.example.athletesync;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {
    private final List<Message> messageList;

    public MessageAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder h, int i) {
        Message m = messageList.get(i);
        if (m.getSentBy().equals(Message.SENT_BY_ME)) {
            h.leftChatView.setVisibility(View.GONE);
            h.rightChatView.setVisibility(View.VISIBLE);
            h.rightTextView.setText(m.getMessage());
        } else {
            h.rightChatView.setVisibility(View.GONE);
            h.leftChatView.setVisibility(View.VISIBLE);
            h.leftTextView.setText(m.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView leftChatView, rightChatView;
        TextView leftTextView, rightTextView;

        MyViewHolder(@NonNull View v) {
            super(v);
            leftChatView = v.findViewById(R.id.left_chat_view);
            rightChatView = v.findViewById(R.id.right_chat_view);
            leftTextView = v.findViewById(R.id.left_chat_text_view);
            rightTextView = v.findViewById(R.id.right_chat_text_view);
        }
    }
}