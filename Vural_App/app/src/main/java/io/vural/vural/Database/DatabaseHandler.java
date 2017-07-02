package io.vural.vural.Database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.vural.vural.Fragments.Chats.Chat;
import io.vural.vural.Fragments.Contacts.Contact;
import io.vural.vural.Fragments.Map.Message;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Emanuel on 6/21/2017.
 */

public class DatabaseHandler {

    public static SQLiteDatabase database;
    public static Cursor c;

    public static void init(Context context) {
        try {
            //create database
            database = context.openOrCreateDatabase("vural_database.db", MODE_PRIVATE, null);

            // drop table
//            database.execSQL("DROP TABLE contacts");
//            database.execSQL("DROP TABLE chats");
//            database.execSQL("DROP TABLE messages");


            // create all tables
            database.execSQL("CREATE TABLE IF NOT EXISTS chats (chat_id TEXT, chat_name TEXT, date_created TEXT)");
            database.execSQL("CREATE TABLE IF NOT EXISTS messages (chat_id TEXT, message_id INT, src_phone_number TEXT, lat REAL, lng REAL, message_data TEXT, timestamp TEXT, modelId TEXT)");
            database.execSQL("CREATE TABLE IF NOT EXISTS user_details (user_id TEXT, username TEXT)");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void close(){
        database.close();
    }

    public static void insertUserDetails(String userId, String username){
        database.execSQL(String.format("INSERT INTO user_details (user_id, username) VALUES ('%s', '%s')", userId, username));
    }

    public static String getUserId(){
        c = database.rawQuery("SELECT * FROM user_details", null);
        c.moveToFirst();
        return c.getString(0);
    }

    public static void insertChat(Chat chat) {
        database.execSQL("INSERT INTO chats (chat_id, chat_name, date_created) VALUES ('" + chat.getChatId() + "', '" + chat.getChatName() + "', '" + chat.getDateCreated() + "')");
    }

    public static void insertMessage(Message message) {
        database.execSQL(String.format("INSERT INTO messages (chat_id, message_id, src_phone_number, lat, lng, message_data, timestamp, modelId) VALUES ('%s', %d, '%s', %g, %g, '%s', '%s', '%s')", message.getChatId(), message.getMessageId(), message.getSrcPhoneNumber(), message.getLat(), message.getLng(), message.getMessageData(), message.getTimeStamp(), message.getModelId()));
    }

    public static void removeChat(String chatName){
        database.execSQL(String.format("DELETE FROM chats WHERE chat_name='%s'", chatName));
    }

    public static Contact getContactByPhoneNumber(String phoneNumber) {
        c = database.rawQuery("SELECT * FROM contacts WHERE phone_number='" + phoneNumber + "'", null);
        c.moveToFirst();
        Log.d("debug", c.getString(0) + c.getString(1) + c.getString(2));
        return new Contact(c.getString(0), c.getString(1));
    }


    public static ArrayList<Message> getMessagesForSQL(String condition) {
        ArrayList<Message> messages = new ArrayList<>();

        c = database.rawQuery("SELECT * FROM messages" + condition, null);
        c.moveToFirst();
//        Log.d("count", "SELECT * FROM messages" + condition + " | " +String.valueOf(c.getCount()));

        for (int i = 0; i < c.getCount(); i++) {
            Message message = new Message(c.getString(0), c.getInt(1), c.getString(2), c.getDouble(3), c.getDouble(4), c.getString(5), c.getString(6), c.getString(7));
            messages.add(message);
            c.moveToNext();
        }

        return messages;
    }

    public static ArrayList<Message> getMessagesForChatId(String chatId) {
        return getMessagesForSQL(String.format(" WHERE chat_id='%s'", chatId));
    }

    public static ArrayList<Message> getAllMessages() {
        return getMessagesForSQL("");
    }

    public static List<String> getChatNameList() {
        List<String> chatNames = new ArrayList<>();

        c = database.rawQuery("SELECT chat_name FROM chats", null);
        c.moveToFirst();
        for (int i = 0; i < c.getCount(); i++) {
            chatNames.add(c.getString(0));
            c.moveToNext();
        }

        return chatNames;
    }

    public static String getChatIdForChatName(String chatName) {
        c = database.rawQuery(String.format("SELECT chat_id FROM chats WHERE chat_name='%s'", chatName), null);
        c.moveToFirst();
        return c.getString(0);
    }

    public static boolean doesChatNameExist(String chatName){
        c = database.rawQuery(String.format("SELECT chat_id FROM chats WHERE chat_name='%s'", chatName), null);

        if(c.getCount() == 0){
            return false;
        }

        return true;
    }

    public static boolean doesChatIdExist(String chatId){
        c = database.rawQuery(String.format("SELECT chat_id FROM chats WHERE chat_id='%s'", chatId), null);

        if(c.getCount() == 0){
            return false;
        }

        return true;
    }


}
