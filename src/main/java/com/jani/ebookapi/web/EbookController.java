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
    public ResponseEntity<Ebook> getEbook(@PathVariable String ebook_id) {
        Ebook ebook = ebookService.get(ebook_id);
        if (ebook == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        // create new Ebook with null id to discard id
        Ebook returnEbook = new Ebook();
        returnEbook.setFormat(ebook.getFormat());
        returnEbook.setTitle(ebook.getTitle());
        returnEbook.setAuthor(ebook.getAuthor());
        return new ResponseEntity<>(returnEbook, HttpStatus.OK);
    }

    @PostMapping("/ebooks")
    public ResponseEntity<Ebook> addEbook(@RequestBody @Valid Ebook ebook) {
        return new ResponseEntity<>(ebookService.add(ebook), HttpStatus.CREATED);
    }

    @PutMapping("/ebooks/{ebook_id}")
    public ResponseEntity<Ebook> updateEbook(@RequestBody @Valid Ebook updateForEbook, @PathVariable String ebook_id) {
        Ebook existingEbook = ebookService.get(ebook_id);
        if (existingEbook == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        updateForEbook.setId(ebook_id);
        ebookService.update(ebook_id, existingEbook, updateForEbook);
        // create new Ebook with null id to discard id
        Ebook returnEbook = new Ebook();
        returnEbook.setFormat(updateForEbook.getFormat());
        returnEbook.setTitle(updateForEbook.getTitle());
        returnEbook.setAuthor(updateForEbook.getAuthor());
        return new ResponseEntity<>(returnEbook, HttpStatus.OK);
    }

    @DeleteMapping("/ebooks/{ebook_id}")
    public void deleteEbook(@PathVariable String ebook_id) {
        Ebook ebook = ebookService.remove(ebook_id);
        if (ebook == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

}
