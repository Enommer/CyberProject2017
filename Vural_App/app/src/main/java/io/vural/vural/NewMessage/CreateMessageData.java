package io.vural.vural.NewMessage;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.gms.maps.model.LatLng;

import io.vural.vural.Client;
import io.vural.vural.Fragments.Map.Message;
import io.vural.vural.R;

public class CreateMessageData extends AppCompatActivity {

    EditText messageIn;
    Button sendBtn;
    int modelId = 0;

    String chatId;
    LatLng location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_message_data);

        chatId = getIntent().getStringExtra("chat_id");
        location = (LatLng) getIntent().getExtras().get("location");

        messageIn = (EditText) findViewById(R.id.messageIn);
        sendBtn = (Button) findViewById(R.id.sendBtn);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get message text from UI
                String messageText = messageIn.getText().toString();
                messageText = messageText.replace('|', (char)174);
                messageText = messageText.replace('/', (char)175);
                // create message
                Message message = new Message(chatId, 0, "Me", location.latitude, location.longitude, messageText, Client.getTimeStamp(), String.valueOf(modelId));

                // send message
                Client.messageSending = message;
                Client.sendMessage(message, getApplicationContext());

                finish();
            }
        });
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_ufo:
                if (checked)
                    modelId = 0;
                    break;
            case R.id.radio_hermes:
                if (checked)
                    modelId = 1;
                    break;
            case R.id.radio_falcon:
                if (checked)
                    modelId = 2;
                break;
        }
    }
}
