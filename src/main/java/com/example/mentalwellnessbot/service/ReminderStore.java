package com.example.mentalwellnessbot.service;

import com.example.mentalwellnessbot.model.Subscription;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public class ReminderStore {
    private final ObjectMapper mapper = new ObjectMapper();
    private final File file = new File("subscriptions.json");

    public synchronized List<Subscription> load() {
        try {
            if (!file.exists()) return new ArrayList<>();
            return mapper.readValue(file, new TypeReference<>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public synchronized void save(List<Subscription> subs) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, subs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
