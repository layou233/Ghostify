package com.launium.ghostify.client.config;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;

public class ContactsBook extends AbstractConfig {
    static String CONFIG_NAME = "contacts.json";

    @Override
    protected String getConfigName() {
        return CONFIG_NAME;
    }

    @SerializedName("contacts")
    public HashMap<String, Contact> CONTACTS = new HashMap<>(); // <name, contact>

    @Data
    @AllArgsConstructor
    public static class Contact {
        @SerializedName("skin")
        public String skin;

        @SerializedName("starred")
        public boolean starred;
    }
}
