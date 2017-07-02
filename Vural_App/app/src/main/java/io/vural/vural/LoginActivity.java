package io.vural.vural;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {

    EditText usernameIn;
    EditText phoneNumberIn;
    Button createUserBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameIn = (EditText) findViewById(R.id.usernameIn);
        phoneNumberIn = (EditText) findViewById(R.id.phoneNumberIn);
        createUserBtn = (Button) findViewById(R.id.createUserBtn);

        createUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get phone number from user input
                String phoneNumber = Client.formatPhoneNumber(phoneNumberIn.getText().toString());
                String username = usernameIn.getText().toString();
                // Put phone number and username in extra data
                Client.extraData.add(phoneNumber);
                Client.extraData.add(username);
                // Send message to server
                Client.sendNewUserInit(phoneNumber, username, getApplicationContext());
            }
        });

    }

    @Override
    public void onBackPressed() {
        // make back button not work
    }
}
