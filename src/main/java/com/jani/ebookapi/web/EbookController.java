package com.jani.ebookapi.web;

import com.jani.ebookapi.model.Ebook;
import com.jani.ebookapi.service.EbookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
public class EbookController {

    private final EbookService ebookService;

    public EbookController(EbookService ebookService) {
        this.ebookService = ebookService;
    }

    @GetMapping("/")
    public String welcomeToAPI() {
        return "Welcome!";
    }

    @GetMapping("/ebooks")
    public ResponseEntity<Object> getEbook() {
        Collection<Ebook> ebooks = ebookService.getAll();
        // format return data
        return new ResponseEntity<>(Map.of("data", ebooks), HttpStatus.OK);
    }

    @GetMapping("/ebooks/{ebook_id}")
    public Ebook getEbook(@PathVariable String ebook_id) {
        Ebook ebook = ebookService.get(ebook_id);
        if (ebook == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        // create new Ebook with null id to discard id
        Ebook returnEbook = new Ebook();
        returnEbook.setFormat(ebook.getFormat());
        returnEbook.setTitle(ebook.getTitle());
        returnEbook.setAuthor(ebook.getAuthor());
        return returnEbook;
    }

    @PostMapping("/ebooks")
    public ResponseEntity<Ebook> addEbook(@RequestBody @Valid Ebook ebook) {
        return new ResponseEntity<>(ebookService.add(ebook), HttpStatus.CREATED);
    }

    @PutMapping("/ebooks/{ebook_id}")
    public Ebook updateEbook(@RequestBody @Valid Ebook updatedEbook, @PathVariable String ebook_id) {
        Ebook existingEbook = ebookService.get(ebook_id);
        if (existingEbook == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        updatedEbook.setId(ebook_id);
        ebookService.update(ebook_id, existingEbook, updatedEbook);
        // create new Ebook with null id to discard id
        Ebook returnEbook = new Ebook();
        returnEbook.setFormat(updatedEbook.getFormat());
        returnEbook.setTitle(updatedEbook.getTitle());
        returnEbook.setAuthor(updatedEbook.getAuthor());
        return returnEbook;
    }

    @DeleteMapping("/ebooks/{ebook_id}")
    public void deleteEbook(@PathVariable String ebook_id) {
        Ebook ebook = ebookService.remove(ebook_id);
        if (ebook == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

}
