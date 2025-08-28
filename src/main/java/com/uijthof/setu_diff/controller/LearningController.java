package com.uijthof.setu_diff.controller;

import com.uijthof.setu_diff.service.MongoService;
import com.uijthof.setu_diff.model.Learning;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@AllArgsConstructor
public class LearningController {

    private MongoService mongoService;

    @GetMapping("/hello")
    public String sayHello() {
        return "Hallo, dit is mijn eerste Spring Boot Controller!";
    }

    @GetMapping(
            value = "/get-all-entities",
            produces = "application/json")
    public ResponseEntity<EntitiesResponse> getAllEntities() {
        var data = mongoService.findAllLearnings();
        var response = new EntitiesResponse();
        response.setLearnings(data);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/add-learning")
    public ResponseEntity<String> addLearning(@RequestParam String title, @RequestParam String discription) {
        mongoService.saveLearning(title, discription);
        return new ResponseEntity<>("Learning entity succesvol aangemaakt", HttpStatus.CREATED);
    }

    @GetMapping("/search-by-title")
    public ResponseEntity<EntitiesResponse> searchByTitle(@RequestParam String title) {
        List<Learning> data = mongoService.searchByTitle(title);
        EntitiesResponse response = new EntitiesResponse();
        response.setLearnings(data);
        return ResponseEntity.ok(response);
    }

    @PostMapping(
            value ="add-learning-json",
            consumes = "multipart/form-data",
            produces = "application/json")
    public ResponseEntity<String> addLearningWithJsonFile(@RequestParam("title") String title, @RequestParam("description") String description, @RequestParam MultipartFile file) {
        try {
            mongoService.saveFromJsonFile(title, description, file);
            return new ResponseEntity<>("File uploaded successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("File read failed" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/search-by-id")
    public ResponseEntity<EntitiesResponse> searchById(@RequestParam String id) {
        List<Learning> data = mongoService.searchById(id);
        EntitiesResponse response = new EntitiesResponse();
        response.setLearnings(data);
        return ResponseEntity.ok(response);
    }

}