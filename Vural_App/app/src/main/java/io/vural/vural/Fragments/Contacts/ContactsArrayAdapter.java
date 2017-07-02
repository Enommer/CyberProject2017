package io.vural.vural.Fragments.Contacts;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import io.vural.vural.R;

/**
 * Created by Emanuel on 6/17/2017.
 */

public class ContactsArrayAdapter extends ArrayAdapter<Contact> {

    private Context context;
    private List<Contact> objects;

    public ContactsArrayAdapter(Context context, int resource, List<Contact> objects){
        super(context, resource, objects);

        this.context = context;
        this.objects = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Contact contact = objects.get(position);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.contact_list_item, null);

        // TODO replace with chat with contact
        TextView contactName = (TextView) view.findViewById(R.id.contactName);
        TextView contactPhoneNumber = (TextView) view.findViewById(R.id.contactPhoneNumber);
        contactName.setText(contact.getName());
        // show phone number with + in front
        contactPhoneNumber.setText(contact.getPhoneNumber());


        return view;
    }
}
