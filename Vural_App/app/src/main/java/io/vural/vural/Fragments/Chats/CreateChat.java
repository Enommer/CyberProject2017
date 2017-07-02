package io.vural.vural.Fragments.Chats;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import io.vural.vural.Client;
import io.vural.vural.Database.DatabaseHandler;
import io.vural.vural.R;

public class CreateChat extends AppCompatActivity{

    TextView messageOut;
    EditText chatNameIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_chat);

        messageOut = (TextView) findViewById(R.id.messageOut);
        chatNameIn = (EditText) findViewById(R.id.chatName);

        Button createBtn = (Button) findViewById(R.id.createBtn);
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(chatNameIn.getText().toString().equals("")){
                    // if chat name empty show error
                    messageOut.setText("Chat Must Have Name");
                    messageOut.setTextColor(Color.RED);
                }else{
                    // get chat name from UI
                    String chatName = chatNameIn.getText().toString();

                    if(DatabaseHandler.doesChatNameExist(chatName)){
                        // if chat name exists show error
                        messageOut.setText("Chat Name Exists");
                        messageOut.setTextColor(Color.RED);
                    }else {
                        Client.extraData.add(chatName);

                        // if chat name does not exist create chat
                        Client.sendCreateChat(chatName, getApplicationContext());
                        finish();
                    }
                }
            }
        });
    }
}
