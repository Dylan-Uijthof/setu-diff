package com.uijthof.setu_diff.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "learning")
public class Learning {

    @Id
    private String id;

    public String title;

    public String  discription;

    private org.bson.Document document;
}
