package io.vural.vural;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class NewUserFinal extends AppCompatActivity {

    EditText codeIn;
    Button enter;
    TextView errorOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user_final);

        codeIn = (EditText) findViewById(R.id.codeIn);
        enter = (Button) findViewById(R.id.enterBtn);
        errorOut = (TextView) findViewById(R.id.errorOut);
        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get verification code from UI
                String code = codeIn.getText().toString();

                // Get phone number and username from intent
                String phoneNumber = (String) getIntent().getExtras().get("phone_number");
                String username = (String) getIntent().getExtras().get("username");

                Client.extraData.add(username);

                Client.sendNewUserFinal(phoneNumber, code, getApplicationContext());
            }
        });
    }

    @Override
    public void onBackPressed() {
        // makes back button not work
    }
}
