package io.vural.vural.Fragments.Contacts;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import io.vural.vural.Client;
import io.vural.vural.Database.DatabaseHandler;
import io.vural.vural.Fragments.Chats.CreateChat;
import io.vural.vural.Fragments.Map.FullScreenMap;
import io.vural.vural.R;


/**
 * Created by Emanuel on 6/17/2017.
 */

public class ContactsFragment extends ListFragment {

    static List<Contact> contacts;
    ContactsArrayAdapter adapter;
    public static boolean addingContacts = false;
    public static ArrayList<Contact> addToChatContacts;

    public ContactsFragment(){

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 2) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                if(contacts != null) {
                    getContacts();
                }
            }
        }
    }

    private void getContacts() {
        List<Contact> contactList = new ArrayList<>();
        // Get the ContentResolver
        ContentResolver contentResolver = getActivity().getContentResolver();
        // Get the Cursor of all the contacts
        Cursor cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,null);

        cursor.moveToFirst();
        for(int i = 0; i < cursor.getCount(); i++){
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            // Get the contacts phone number
            String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            // check that phone is mobile
            int phoneType = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
            if (phoneType != ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE){
                cursor.moveToNext();
                continue;
            }

            // add contact to list
            contactList.add(new Contact(name, phoneNumber));
            cursor.moveToNext();
        }

        // Close the cursor
        cursor.close();

        contacts = contactList;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        contacts = new ArrayList<>();

        // If device is running SDK < 23 (Marshmallow)
        if (Build.VERSION.SDK_INT < 23) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 2);
        } else {
            if (getActivity().checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                // ask for permission
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS}, 2);
            } else {
                if(contacts != null) {
                    getContacts();
                }
            }
        }

        ContactsArrayAdapter adapter = new ContactsArrayAdapter(getActivity(), R.layout.contact_list_item, contacts);
        setListAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        addToChatContacts = new ArrayList<>();
        return inflater.inflate(R.layout.fragment_contacts, container, false);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if(addingContacts) {

            ImageView marker = (ImageView) v.findViewById(R.id.marker);
            if(marker.getVisibility() == View.VISIBLE){
                marker.setVisibility(View.INVISIBLE);
                addToChatContacts.remove(contacts.get(position));
            }else {
                marker.setVisibility(View.VISIBLE);
                addToChatContacts.add(contacts.get(position));
            }

        }
    }

}
