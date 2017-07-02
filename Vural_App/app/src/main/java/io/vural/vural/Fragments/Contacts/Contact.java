package io.vural.vural.Fragments.Contacts;

/**
 * Created by Emanuel on 6/17/2017.
 */

public class Contact {

    String name;
    String phoneNumber;

    public Contact(String name, String phoneNumber){
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
