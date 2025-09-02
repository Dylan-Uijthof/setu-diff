package com.uijthof.setu_diff.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.uijthof.setu_diff.model.Learning;
import com.uijthof.setu_diff.repo.MongoRepo;
import lombok.AllArgsConstructor;
import org.bson.Document;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
@Service
@AllArgsConstructor
public class MongoService {

    private final MongoRepo mongoRepo;
    private final ObjectMapper objectMapper;
    private final Gson gson;

    public List<Learning> findAllLearnings() {
        return mongoRepo.findAll();
    }

    public List<Learning> searchByTitle(String title) {
        return mongoRepo.findAllByTitleLikeIgnoreCase(title);
    }

    public List<Learning> searchById(String id) {
        return mongoRepo.findAllById(id);
    }

    public void saveLearning(String title, String discription) {
        var learning = new Learning();
        learning.setTitle(title);
        learning.setDiscription(discription);
        mongoRepo.save(learning);
    }
    public void saveFromJsonFile(String title, String description, MultipartFile file) {
        if  (file.isEmpty()) {
            throw new IllegalArgumentException("cannot upload empty file");
        }
        try (InputStream inputStream = file.getInputStream()) {
            String json = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            var document = Document.parse(json);
            var learning = new Learning();
            learning.setTitle(title);
            learning.setDiscription(description);
            learning.setDocument(document);

            mongoRepo.save(learning);
        } catch (IOException e)  {
            throw new RuntimeException("failed to read json data: " + e);
        }
    }

    public List<Map<String, Object>> diffJsonFiles(String id, MultipartFile newFile) throws IOException {
        // Stap 1: Haal het oude document op
        Learning existingLearning = mongoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Learning with id " + id + " not found"));
        Document oldDocument = existingLearning.getDocument();
        if (oldDocument == null) {
            throw new IllegalStateException("The existing document with id " + id + " does not contain JSON data.");
        }

        // Stap 2: Lees het nieuwe JSON-bestand in
        if (newFile.isEmpty()) {
            throw new IllegalArgumentException("New json file is empty");
        }
        String oldJsonContent = oldDocument.toJson();
        String newJsonContent = new String(newFile.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        // Stap 3: Converteer beide JSON-strings naar standaard Java Maps met Gson
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> oldMap = gson.fromJson(oldJsonContent, type);
        Map<String, Object> newMap = gson.fromJson(newJsonContent, type);

        // Stap 4: Gebruik Guava's Maps.difference om de verschillen te vinden
        MapDifference<String, Object> diff = Maps.difference(oldMap, newMap);

        // Stap 5: Vertaal de verschillen naar een leesbaar formaat
        List<Map<String, Object>> customDiffOutput = new ArrayList<>();

        // Toegevoegde velden (alleen in de nieuwe map)
        diff.entriesOnlyOnRight().forEach((key, value) -> {
            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("operation", "add");
            detail.put("path", "/" + key);
            detail.put("from", null);
            detail.put("to", value);
            customDiffOutput.add(detail);
        });

        // Verwijderde velden (alleen in de oude map)
        diff.entriesOnlyOnLeft().forEach((key, value) -> {
            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("operation", "remove");
            detail.put("path", "/" + key);
            detail.put("from", value);
            detail.put("to", null);
            customDiffOutput.add(detail);
        });

        // Gewijzigde velden
        diff.entriesDiffering().forEach((key, valueDifference) -> {
            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("operation", "replace");
            detail.put("path", "/" + key);
            detail.put("from", valueDifference.leftValue());
            detail.put("to", valueDifference.rightValue());
            customDiffOutput.add(detail);
        });

        return customDiffOutput;
    }
}
