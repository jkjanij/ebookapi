package com.jani.ebookapi.model;

public class Ebook {

    private String id;

    private String author;

    private String title;

    private String format;

    public String getId() {
        return id;
    }

    public Ebook(String id, String author, String title, String format) {
        this.id = id;
        this.author = author;
        this.title = title;
        this.format = format;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}
