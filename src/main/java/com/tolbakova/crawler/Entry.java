package com.tolbakova.crawler;

import java.util.List;
import java.util.stream.Collectors;

class Entry {
    private String url;
    private List<Entry> entries;

    public Entry(String url) {
        this.url = url;
    }


    public String getUrl() {
        return url;
    }

    public void setEntries(List<Entry> entries1) {
        this.entries = entries1;
    }

    @Override
    public String toString() {
        return entries == null ? url : entries.stream().map(e -> url + e.toString()).collect(Collectors.joining("\n"));
    }
}