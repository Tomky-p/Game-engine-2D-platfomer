package org.tomek.engine.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * This class represents coordinates with x and y values.
 */
@Getter
@Setter
public class Coords {

    private double x;

    private double y;

    @JsonCreator
    public Coords(@JsonProperty("x")double x,
                  @JsonProperty("y")double y){
        this.x = x;
        this.y = y;
    }
}
