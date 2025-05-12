package com.example.traveljurnalapp;

import java.util.HashMap;
import java.util.Map;

public class NotesStorage {
    private static final Map<String, String> notesMap = new HashMap<>();

    public static void saveNote(String tripId, String noteText) {
        notesMap.put(tripId, noteText);
    }

    public static String getNote(String tripId) {
        return notesMap.getOrDefault(tripId, "");
    }
}
