package com.uijthof.setu_diff.controller;

import com.uijthof.setu_diff.model.Learning;
import lombok.Data;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data

public class EntitiesResponse implements Serializable {

    List<Learning> learnings = new ArrayList<>();

}