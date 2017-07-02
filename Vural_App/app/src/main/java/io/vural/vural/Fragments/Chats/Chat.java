package io.vural.vural.Fragments.Chats;

/**
 * Created by Emanuel on 6/21/2017.
 */

public class Chat {

    String chatName;
    String chatId;
    String dateCreated;

    public Chat(String chatName, String chatId, String dateCreated){
        this.chatName = chatName;
        this.chatId = chatId;
        this.dateCreated = dateCreated;
    }

    public String getChatName(){
        return chatName;
    }

    public String getChatId(){
        return chatId;
    }

    public String getDateCreated(){
        return dateCreated;
    }

}
