package org.devdynam0507.proxy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class CustomHeaders implements Iterable<Entry<String, String>> {
    private final List<Pair<String, String>> headers;

    private CustomHeaders() {
        this.headers = new ArrayList<>();
    }

    public void addHeader(String name, String value) {
        headers.add(new Pair<>(name, value));
    }

    public int size() {
        return headers.size();
    }

    public boolean isEmpty() {
        return headers.isEmpty();
    }

    public Iterator<Entry<String, String>> toEntry() {
        return headers.stream()
            .map(pair -> Map.entry(pair.key(), pair.value()))
            .toList()
            .iterator();
    }

    @Override
    public Iterator<Entry<String, String>> iterator() {
        return toEntry();
    }


    public static CustomHeaders of() {
        return new CustomHeaders();
    }
}
