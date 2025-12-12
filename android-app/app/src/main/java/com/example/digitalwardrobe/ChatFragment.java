package com.example.digitalwardrobe;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {

    RecyclerView recyclerView;
    ChatAdapter adapter;
    List<ChatMessage> chatList = new ArrayList<>();

    EditText input;
    TextView send;

    ChatBrain brain;

    public ChatFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerView = view.findViewById(R.id.chat_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        input = view.findViewById(R.id.chat_input);
        send = view.findViewById(R.id.chat_send);

        adapter = new ChatAdapter(requireContext(), chatList);
        recyclerView.setAdapter(adapter);

        brain = new ChatBrain(requireContext());

        send.setOnClickListener(v -> {
            String text = input.getText().toString().trim();
            if (text.isEmpty()) return;

            addMessage(text, ChatMessage.TYPE_USER);
            input.setText("");

            respondAI(text);
        });

        return view;
    }

    private void addMessage(String msg, int type) {
        chatList.add(new ChatMessage(msg, type));
        adapter.notifyItemInserted(chatList.size() - 1);
        recyclerView.scrollToPosition(chatList.size() - 1);
    }

    private void respondAI(String userMessage) {

        new Thread(() -> {

            // Generate outfit-based answer using Groq + wardrobe data
            String reply = brain.handleMessage(userMessage, true);

            requireActivity().runOnUiThread(() -> {
                addMessage(reply, ChatMessage.TYPE_AI);
            });

        }).start();
    }
}
