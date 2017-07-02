package io.vural.vural.Fragments.Map;

/**
 * Created by Emanuel on 6/21/2017.
 */

public class Message {

    private String chatId;
    private int messageId;
    private String srcPhoneNumber;
    private double lat;
    private double lng;
    private String messageData;
    private String timeStamp;
    private String modelId;

    public Message(String chatId, int messageId, String srcPhoneNumber, double lat, double lng, String messageData, String timeStamp, String modelId) {
        this.chatId = chatId;
        this.messageId = messageId;
        this.srcPhoneNumber = srcPhoneNumber;
        this.lat = lat;
        this.lng = lng;
        this.messageData = messageData;
        this.timeStamp = timeStamp;
        this.modelId = modelId;
    }

    public String getChatId() {
        return chatId;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public String getSrcPhoneNumber() {
        return srcPhoneNumber;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public String getMessageData() {
        return messageData;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getModelId() {
        return modelId;
    }
}
