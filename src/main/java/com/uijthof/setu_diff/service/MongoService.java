package com.uijthof.setu_diff.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonDiff;
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
        // Stap 1 & 2: Oude en nieuwe JSON ophalen
        Learning existingLearning = mongoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Learning with id " + id + " not found"));
        Document oldDocument = existingLearning.getDocument();
        if (oldDocument == null) {
            throw new IllegalStateException("The existing document with id " + id + " does not contain JSON data.");
        }
        if (newFile.isEmpty()) {
            throw new IllegalArgumentException("New json file is empty");
        }

        // Stap 3: Converteer de BSON Document en het nieuwe bestand naar JsonNode objecten.
        // Dit is de native manier voor de zjsonpatch bibliotheek.
        JsonNode oldJsonNode = objectMapper.readTree(oldDocument.toJson());
        JsonNode newJsonNode = objectMapper.readTree(newFile.getInputStream());

        // Stap 4: Genereer de "JSON Patch". Dit is de diepe vergelijking.
        JsonNode patch = JsonDiff.asJson(oldJsonNode, newJsonNode);

        // Stap 5: Vertaal de patch naar het door u gewenste, leesbare formaat
        return convertPatchToCustomFormat(patch, oldJsonNode);
    }

    /**
     * Hulp-methode om een standaard JSON Patch om te zetten naar een lijst
     * met 'from' en 'to' waardes.
     */
    private List<Map<String, Object>> convertPatchToCustomFormat(JsonNode patch, JsonNode oldJsonNode) {
        List<Map<String, Object>> customDiffOutput = new ArrayList<>();
        for (JsonNode operation : patch) {
            Map<String, Object> diffDetail = new LinkedHashMap<>();
            String op = operation.get("op").asText();
            String path = operation.get("path").asText();

            diffDetail.put("operation", op);
            diffDetail.put("path", path);

            switch (op) {
                case "replace":
                    // De patch bevat de nieuwe waarde ('value'). We zoeken de oude waarde op in het originele document.
                    JsonNode oldValueNode = oldJsonNode.at(path);
                    diffDetail.put("from", objectMapper.convertValue(oldValueNode, Object.class));
                    diffDetail.put("to", objectMapper.convertValue(operation.get("value"), Object.class));
                    break;
                case "add":
                    diffDetail.put("from", null);
                    diffDetail.put("to", objectMapper.convertValue(operation.get("value"), Object.class));
                    break;
                case "remove":
                    // De patch bevat geen waarde. We zoeken de verwijderde waarde op in het originele document.
                    JsonNode removedValueNode = oldJsonNode.at(path);
                    diffDetail.put("from", objectMapper.convertValue(removedValueNode, Object.class));
                    diffDetail.put("to", null);
                    break;
            }
            if (!diffDetail.isEmpty()) {
                customDiffOutput.add(diffDetail);
            }
        }
        return customDiffOutput;
    }
}
