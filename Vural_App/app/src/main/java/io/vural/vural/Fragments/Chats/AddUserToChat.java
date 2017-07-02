package io.vural.vural.Fragments.Chats;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

import io.vural.vural.Client;
import io.vural.vural.Fragments.Contacts.Contact;
import io.vural.vural.Fragments.Contacts.ContactsFragment;
import io.vural.vural.R;


public class AddUserToChat extends AppCompatActivity {

    Button doneBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user_to_chat);
        doneBtn = (Button) findViewById(R.id.doneBtn);

        ContactsFragment.addingContacts = true;

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Contact> addToChatContacts = ContactsFragment.addToChatContacts;
                if(addToChatContacts.size() > 0) {
                    ContactsFragment.addToChatContacts = null;

                    String chatId = getIntent().getStringExtra("chat_id");
                    String userPhoneNumberList = "|user_list=";

                    // add users to user phone number list
                    for(int i = 0; i < addToChatContacts.size(); i++) {
                        String phoneNumber = Client.formatPhoneNumber(addToChatContacts.get(i).getPhoneNumber());

                        userPhoneNumberList += phoneNumber;

                        // if not last in list add comma
                        if(i < addToChatContacts.size() - 1){
                            userPhoneNumberList += ",";
                        }
                    }
                    Client.sendAddUserListToChat(chatId, userPhoneNumberList, getApplicationContext());
                }

                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ContactsFragment.addingContacts = false;
    }
}
