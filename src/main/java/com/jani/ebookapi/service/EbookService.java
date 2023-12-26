package com.jani.ebookapi.service;

import com.jani.ebookapi.model.Ebook;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class EbookService {

    private Map<String, Ebook> booksData = new HashMap<>() {{
    }};

    public Collection<Ebook> getAll() {
        return booksData.values();
    }

    public Ebook get(String ebookId) {
        return booksData.get(ebookId);
    }

    public Ebook add(Ebook ebook) {
        ebook.setId(UUID.randomUUID().toString());
        // check that random UUID is unique
        while (true) {
            if (booksData.get(ebook.getId()) != null) {
                ebook.setId(UUID.randomUUID().toString());
            } else { break; }
        }
        booksData.put(ebook.getId(), ebook);
        return ebook;
    }

    public void update(String ebookId, Ebook existingEbook, Ebook updatedEbook) {
        booksData.replace(ebookId, existingEbook, updatedEbook);
    }

    public Ebook remove(String ebookId) {
        return booksData.remove(ebookId);
    }
}
