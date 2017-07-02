package io.vural.vural.Fragments.Chats;

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
 * Created by Emanuel on 6/21/2017.
 */

public class ChatArrayAdapter extends ArrayAdapter<String> {

    private Context context;
    private List<String> objects;

    public ChatArrayAdapter(Context context, int resource, List<String> objects){
        super(context, resource, objects);

        this.context = context;
        this.objects = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String chatName = objects.get(position);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.chat_list_item, null);

        TextView chatNameTag = (TextView) view.findViewById(R.id.chatName);
        chatNameTag.setText(chatName);

        return view;
    }
}

