package com.uijthof.setu_diff.repo;

import com.uijthof.setu_diff.model.Learning;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MongoRepo extends MongoRepository<Learning, String> {

    List<Learning> findAllByTitleLikeIgnoreCase(String title);
    List<Learning> findAllById(String id);

}