package com.jani.ebookapi.web;

import com.jani.ebookapi.model.Ebook;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
public class EbookController {

    private Map<String, Ebook> booksData = new HashMap<>() {{
        put("1", new Ebook("1", "test", "test", "test"));
    }};
    //private List<Ebook> books = List.of(new Ebook("1", "test", "test", "test"));

    @GetMapping("/")
    public String welcomeToAPI() {
        return "Welcome!";
    }

    @GetMapping("/ebooks")
    public Collection<Ebook> getEbook() {
        return booksData.values();
    }

    @GetMapping("/ebooks/{id}")
    public Ebook getEbook(@PathVariable String id) {
        Ebook ebook = booksData.get(id);
        if (ebook == null) throw new ResponseStatusException((HttpStatus.NOT_FOUND));
        return ebook;
    }

    @PostMapping("/ebooks")
    public Ebook addEbook(@RequestBody Ebook ebook) {
        ebook.setId(UUID.randomUUID().toString());
        booksData.put(ebook.getId(), ebook);
        return ebook;
    }

}
