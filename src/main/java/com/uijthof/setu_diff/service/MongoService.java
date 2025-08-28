package com.uijthof.setu_diff.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uijthof.setu_diff.model.Learning;
import com.uijthof.setu_diff.repo.MongoRepo;
import lombok.AllArgsConstructor;
import org.bson.Document;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@AllArgsConstructor
public class MongoService {

    private final MongoRepo mongoRepo;

    private final ObjectMapper objectMapper;

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
}
