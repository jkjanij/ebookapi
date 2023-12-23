package com.jani.ebookapi.web;

import com.jani.ebookapi.model.Ebook;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
public class EbookController {

    private Map<String, Ebook> booksData = new HashMap<>() {{
    }};

    @GetMapping("/")
    public String welcomeToAPI() {
        return "Welcome!";
    }

    @GetMapping("/ebooks")
    public Collection<Ebook> getEbook() {
        return booksData.values();
    }

    @GetMapping("/ebooks/{ebook_id}")
    public Ebook getEbook(@PathVariable String ebook_id) {
        Ebook ebook = booksData.get(ebook_id);
        if (ebook == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return ebook;
    }

    @PostMapping("/ebooks")
    public Ebook addEbook(@RequestBody @Valid Ebook ebook) {
        ebook.setId(UUID.randomUUID().toString());
        booksData.put(ebook.getId(), ebook);
        return ebook;
    }

    @PutMapping("/ebooks/{ebook_id}")
    public Ebook updateEbook(@RequestBody @Valid Ebook updatedEbook, @PathVariable String ebook_id) {
        Ebook existingEbook = booksData.get(ebook_id);
        if (existingEbook == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        updatedEbook.setId(ebook_id);
        booksData.replace(ebook_id, existingEbook, updatedEbook);
        return updatedEbook;
    }

    @DeleteMapping("/ebooks/{ebook_id}")
    public void deleteBook(@PathVariable String ebook_id) {
        Ebook ebook = booksData.remove(ebook_id);
        if (ebook == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

}
