package io.vural.vural;

/**
 * Created by Emanuel on 6/21/2017.
 */

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.vural.vural.Database.DatabaseHandler;
import io.vural.vural.Fragments.Chats.Chat;
import io.vural.vural.Fragments.Map.Message;


public class Client extends AsyncTask<String, String, String> {

    static String userId;
    private Context context; // context for sending errors as toasts
    public static ArrayList<String> extraData = new ArrayList<>(); // list for extra data to act on when sending is done
    public static Message messageSending;
    public static ArrayList<String> newChatList = new ArrayList<>();

    private static String ERROR_MESSAGE_TYPE = "-1";
    private static String NEW_USER_INIT_MESSAGE_TYPE = "0";
    private static String NEW_USER_FINAL_MESSAGE_TYPE = "1";
    private static String CREATE_CHAT_MESSAGE_TYPE = "2";
    private static String DELETE_CHAT_MESSAGE_TYPE = "3";
    private static String RENAME_CHAT_MESSAGE_TYPE = "4";
    private static String ADD_USER_TO_CHAT_MESSAGE_TYPE = "5";
    private static String REMOVE_USER_FROM_CHAT_MESSAGE_TYPE = "6";
    private static String SEND_MESSAGE_TYPE = "7";
    private static String GET_MESSAGE_TYPE = "8";
    private static String MARK_AS_READ_MESSAGE_TYPE = "9";
    private static String ADD_USER_LIST_TO_CHAT_MESSAGE_TYPE = "10";
    private static String CREATE_PRIVATE_CHAT_MESSAGE_TYPE = "11";
    private static String GET_CHAT_NAME_MESSAGE_TYPE = "12";

    public static void sendText(String message, Context context){
        // add user id to message
        if(userId != null){
            message += "|user_id=" + userId;
        }

        // start do in background
        Client client = new Client(context);
        client.execute(message);
    }

    private Client(Context context){
        this.context = context;
    }

    @Override
    protected String doInBackground(String... params) {

        Socket socket;
        InputStreamReader inputStreamReader;
        BufferedReader bufferedReader;
        PrintWriter printWriter;
        String ip = "34.224.223.107";
        int port = 3626;
        String messageType = null;

        try {
            socket = new Socket(ip, port);

            String messageToServer = params[0];

            // Send message to server
            printWriter = new PrintWriter(socket.getOutputStream());
            printWriter.write(messageToServer);
            printWriter.flush();

            // Receive from server
            inputStreamReader = new InputStreamReader(socket.getInputStream());
            bufferedReader = new BufferedReader(inputStreamReader);
            String messageFromServer = bufferedReader.readLine();

            socket.close();

            // Handle message
            messageType = getField("message_type", messageFromServer);
            handleMessageByType(messageType, messageFromServer);

            Log.e("SERVER MESSAGE", messageFromServer);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return messageType;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);

        // Show toast message
        Toast.makeText(context, values[0], Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onPostExecute(String messageType) {
        super.onPostExecute(messageType);

        if(messageType.equals(NEW_USER_INIT_MESSAGE_TYPE)) {
            Intent newUserFinal = new Intent(context, NewUserFinal.class);

            String phoneNumber = extraData.get(0);
            String username = extraData.get(1);

            newUserFinal.putExtra("phone_number", phoneNumber);
            newUserFinal.putExtra("username", username);

            newUserFinal.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(newUserFinal);
        }else if(messageType.equals(NEW_USER_FINAL_MESSAGE_TYPE)){
            String username = extraData.get(0);
            String userId = extraData.get(1);

            DatabaseHandler.insertUserDetails(userId, username);
            Intent startApp = new Intent(context, MainActivity.class);

            startApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(startApp);
        }

        // wipe extra data
        extraData.clear();
    }

    public static void sendNewUserInit(String phoneNumber, String username, Context context){
        sendText(String.format("|message_type=%s|phone_number=%s|username=%s", NEW_USER_INIT_MESSAGE_TYPE, phoneNumber, username), context);
    }

    public static void sendNewUserFinal(String phoneNumber, String verificationCode, Context context){
        sendText(String.format("|message_type=%s|phone_number=%s|verification_code=%s", NEW_USER_FINAL_MESSAGE_TYPE, phoneNumber, verificationCode), context);
    }

    public static void sendCreateChat(String chatName, Context context){
        sendText(String.format("|message_type=%s|chat_name=%s", CREATE_CHAT_MESSAGE_TYPE, chatName), context);
    }

    public static void sendAddUserListToChat(String chatId, String userPhoneNumberList, Context context){
        sendText(String.format("|message_type=%s|chat_id=%s|user_list=%s", ADD_USER_LIST_TO_CHAT_MESSAGE_TYPE, chatId, userPhoneNumberList), context);
    }

    public static void sendMessage(Message m, Context context){
        sendText(String.format("|message_type=%s|chat_id=%s|lat=%f|lng=%f|message_data=%s//%s", SEND_MESSAGE_TYPE, m.getChatId(), m.getLat(), m.getLng(), m.getMessageData(), m.getModelId()), context);
    }

    public static void sendGet(Context context){
        sendText(String.format("|message_type=%s", GET_MESSAGE_TYPE), context);
    }

    public static void sendGetChatName(String chat_id, Context context){
//        sendText(String.format("|message_type=%s|chat_id=%s", GET_CHAT_NAME_MESSAGE_TYPE, chat_id), context);
    }

    public void handleMessageByType(String messageType, String messageFromServer) {

        String message = messageFromServer;

        // Error message
        if(messageType.equals(ERROR_MESSAGE_TYPE)){

            if(message == null) {
                message = "no response from server";
            }

            publishProgress(message);
            return;

        }
        // New user init
        else if(messageType.equals(NEW_USER_INIT_MESSAGE_TYPE)) {
            return;

        }
        // New user final
        else if(messageType.equals(NEW_USER_FINAL_MESSAGE_TYPE)) {
            String userId = getField("user_id", message);

            extraData.add(userId);
        }
        // Get chat name
        else if (messageType.equals(GET_CHAT_NAME_MESSAGE_TYPE)){
            String chatName = getField("chat_name", message);
            String chatId = newChatList.remove(0);

            DatabaseHandler.insertChat(new Chat(chatName, chatId, getDate()));

        }else if(messageType.equals(GET_MESSAGE_TYPE)){


            String[] server_chat_name_list = null;
            List<String> chat_name_list = DatabaseHandler.getChatNameList();

            try{
                boolean chatExists;
                server_chat_name_list = getField("added_chats", message).split(",");

                // check if user was removed from chat
                for(String chat_name: chat_name_list){
                    chatExists = false;
                    for(String server_chat_name: server_chat_name_list){
                        if(chat_name.equals(server_chat_name)){
                            chatExists = true;
                            break;
                        }
                    }
                    // if chat is not in server list
                    if(!chatExists){
                        DatabaseHandler.removeChat(chat_name);
                    }
                }


            }catch (Exception ignored){

            }

            String[] message_list = getField("unread_messages", message).split("|||");
            Log.d("debuger", getField("unread_messages", message));

            // if there are no unread messages
            if(getField("unread_messages", message).equals("")){
                return;
            }



            for(int i = 0; i < message_list.length; i++){
                String unread_message = message_list[i];

                String[] messageData = unread_message.split("|");
                String chatId = messageData[0];

                if(!DatabaseHandler.doesChatIdExist(chatId)){
                    newChatList.add(chatId);
                    sendGetChatName(chatId, context);
                }

                String messageId = messageData[1];
                String srcPhoneNumber = messageData[2];
                String lat = messageData[4];
                String lng = messageData[5];
                String messageDataField = messageData[6];
                String messageText = messageDataField.split("//")[0];

                messageText = messageText.replace((char)174, '|');
                messageText = messageText.replace((char)175, '/');

                String modelId = messageDataField.split("//")[1];

                DatabaseHandler.insertMessage(new Message(chatId, Integer.valueOf(messageId), srcPhoneNumber, Double.valueOf(lat), Double.valueOf(lng), messageText, Client.getTimeStamp(), modelId));
            }
        }

        // from now on only messages that need chat id
        // extract chat id from message
        String chatId = getField("chat_id", message);

        // Create chat
        if(messageType.equals(CREATE_CHAT_MESSAGE_TYPE)){

            String chatName = extraData.get(0);

            // Add chat to database
            DatabaseHandler.insertChat(new Chat(chatName, chatId, getDate()));

        }else if(messageType.equals(SEND_MESSAGE_TYPE)){

            // extract message id from message
            String messageId = getField("message_id", message);
            // set message id of message
            messageSending.setMessageId(Integer.valueOf(messageId));
            // add message to database
            DatabaseHandler.insertMessage(messageSending);

        }


    }

    public static String getField(String fieldName, String messageContent){
        try{
            String pattern = "\\|" + fieldName + "=([^|]+)";
            Pattern regex = Pattern.compile(pattern);
            Matcher matcher = regex.matcher(messageContent);
            matcher.find();
            return matcher.group(1);
        }catch (Exception e){
            // If message type field missing return -1 message type
            return "-1";
        }
    }

    public static String getDate(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy");
        return simpleDateFormat.format(new Date());
    }

    public static String getTimeStamp(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
        return simpleDateFormat.format(new Date());
    }

    public static String formatPhoneNumber(String phoneNumber){
        String formattedPhoneNumber = "";

        for(char c: phoneNumber.toCharArray()){
            if(Character.isDigit(c)){
                formattedPhoneNumber += c;
            }
        }
        if(formattedPhoneNumber.toCharArray()[0] == '0'){
            formattedPhoneNumber = "972" + formattedPhoneNumber.substring(1);
        }

        return formattedPhoneNumber;
    }
}