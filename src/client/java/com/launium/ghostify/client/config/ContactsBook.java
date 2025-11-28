package com.launium.ghostify.client.config;

import com.google.gson.annotations.SerializedName;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.AllArgsConstructor;
import lombok.Data;

public class ContactsBook extends AbstractConfig {
    static String CONFIG_NAME = "contacts.json";

    @Override
    protected String getConfigName() {
        return CONFIG_NAME;
    }

    @SerializedName("contacts")
    public Object2ObjectOpenHashMap<String, Contact> CONTACTS = new Object2ObjectOpenHashMap<>(); // <name, contact>

    @Data
    @AllArgsConstructor
    public static class Contact {
        @SerializedName("skin")
        public String skin;

        @SerializedName("starred")
        public boolean starred;
    }
}
