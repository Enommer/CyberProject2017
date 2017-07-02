package io.vural.vural.Fragments.Map;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import io.vural.vural.Fragments.Chats.AddUserToChat;
import io.vural.vural.R;

public class FullScreenMap extends AppCompatActivity {

    String chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_map);

        chatId = getIntent().getStringExtra("chat_id");
        Log.d("debug", chatId);

        FloatingActionButton addContactsBtn = (FloatingActionButton) findViewById(R.id.addContactsBtn);

        // check if is private chat
        if(getIntent().hasExtra("private")){
            addContactsBtn.setVisibility(View.INVISIBLE);
        }else{
            addContactsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent addContacts = new Intent(getApplicationContext(), AddUserToChat.class);
                    addContacts.putExtra("chat_id", chatId);
                    startActivity(addContacts);
                }
            });
        }
    }
}
