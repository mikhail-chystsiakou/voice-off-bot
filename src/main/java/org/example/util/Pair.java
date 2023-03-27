package org.example.util;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class Pair<K, V> implements Serializable {

    private K key;
    private V value;

    @Override
    public String toString() {
        return key + "=" + value;
    }
}