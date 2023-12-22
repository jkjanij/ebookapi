package com.jani.ebookapi.web;

import com.jani.ebookapi.model.Ebook;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class EbookController {

    private List<Ebook> books = List.of(new Ebook("1", "test", "test", "test"));

    @GetMapping("/")
    public String welcome() {
        return "Welcome!";
    }

    @GetMapping("/ebooks")
    public List<Ebook> get() {
        return books;
    }

}
