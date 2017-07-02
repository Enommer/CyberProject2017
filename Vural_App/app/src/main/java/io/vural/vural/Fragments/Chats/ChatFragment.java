package io.vural.vural.Fragments.Chats;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import io.vural.vural.Database.DatabaseHandler;
import io.vural.vural.Fragments.Contacts.ContactsFragment;
import io.vural.vural.Fragments.Map.FullScreenMap;
import io.vural.vural.NewMessage.CreateMessageData;
import io.vural.vural.R;

/**
 * Created by Emanuel on 6/17/2017.
 */

public class ChatFragment extends ListFragment {

    List<String> chatNames;

    public ChatFragment(){
        chatNames = DatabaseHandler.getChatNameList();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ChatArrayAdapter adapter = new ChatArrayAdapter(getActivity(), R.layout.chat_list_item, chatNames);
        setListAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FloatingActionButton newChatBtn = (FloatingActionButton) view.findViewById(R.id.newChatBtn);
        newChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createChat = new Intent(getContext(), CreateChat.class);
                startActivity(createChat);
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Intent nextStep;

        // check if location is selected
        if(getActivity().getIntent().hasExtra("location")){
            // Go to create message data
            nextStep = new Intent(getContext(), CreateMessageData.class);
            // Attach location
            nextStep.putExtra("location", (LatLng) getActivity().getIntent().getExtras().get("location"));
        }else{
            // Go to choose location
            nextStep = new Intent(getContext(), FullScreenMap.class);
        }

        // Attach chat id
        nextStep.putExtra("chat_id", DatabaseHandler.getChatIdForChatName(chatNames.get(position)));

        startActivity(nextStep);

    }



}
