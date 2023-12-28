package com.jani.ebookapi;

import com.jani.ebookapi.model.Ebook;
import com.jani.ebookapi.service.EbookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class EbookServiceTests {

    private EbookService ebookService;

    @BeforeEach
    void setUp() {
        ebookService = new EbookService();
    }

    @Test
    void getEbook() {
        // Arrange
        Ebook ebook = new Ebook();
        ebook.setAuthor("testAuthor");
        ebook.setTitle("testTitle");
        ebook.setFormat("testFormat");
        Ebook addedEbook = ebookService.add(ebook);

        // Act
        Ebook retrievedEbook = ebookService.get(addedEbook.getId());

        // Assert
        assertNotNull(retrievedEbook);
        assertEquals(retrievedEbook.getTitle(), addedEbook.getTitle());
        assertEquals(retrievedEbook.getAuthor(), addedEbook.getAuthor());
        assertEquals(retrievedEbook.getFormat(), addedEbook.getFormat());
    }

    @Test
    void addEbook() {
        Ebook ebook = new Ebook();
        ebook.setAuthor("testAuthor");
        ebook.setTitle("testTitle");
        ebook.setFormat("testFormat");
        Ebook addedEbook = ebookService.add(ebook);

        assertNotNull(addedEbook.getId());
        assertEquals(ebook.getTitle(), addedEbook.getTitle());
        assertEquals(ebook.getAuthor(), addedEbook.getAuthor());
        assertEquals(ebook.getFormat(), addedEbook.getFormat());
    }

    @Test
    void updateEbook() {
        // Arrange
        Ebook originalEbook = new Ebook();
        originalEbook.setAuthor("testAuthor");
        originalEbook.setTitle("testTitle");
        originalEbook.setFormat("testFormat");
        Ebook addedEbook = ebookService.add(originalEbook);
        Ebook updatedEbook = new Ebook();
        updatedEbook.setAuthor("testAuthorNew");
        updatedEbook.setTitle("testTitleNew");
        updatedEbook.setFormat("testFormatNew");

        // Act
        ebookService.update(addedEbook.getId(), originalEbook, updatedEbook);
        Ebook retrievedEbook = ebookService.get(addedEbook.getId());

        // Assert
        assertNotNull(retrievedEbook);
        assertEquals(updatedEbook.getTitle(), retrievedEbook.getTitle());
        assertEquals(updatedEbook.getAuthor(), retrievedEbook.getAuthor());
        assertEquals(updatedEbook.getFormat(), retrievedEbook.getFormat());
    }

    @Test
    void removeEbook() {
        // Arrange
        Ebook ebook = new Ebook();
        ebook.setAuthor("testAuthor");
        ebook.setTitle("testTitle");
        ebook.setFormat("testFormat");
        Ebook addedEbook = ebookService.add(ebook);

        // Act
        Ebook removedEbook = ebookService.remove(addedEbook.getId());

        // Assert
        assertNotNull(removedEbook);
        assertEquals(addedEbook, removedEbook);
        assertNull(ebookService.get(addedEbook.getId()));
    }

    @Test
    void getAllEbooks() {
        // Arrange
        Ebook ebook1 = new Ebook();
        ebook1.setAuthor("testAuthor1");
        ebook1.setTitle("testTitle1");
        ebook1.setFormat("testFormat1");
        Ebook ebook2 = new Ebook();
        ebook2.setAuthor("testAuthor2");
        ebook2.setTitle("testTitle2");
        ebook2.setFormat("testFormat2");
        ebookService.add(ebook1);
        ebookService.add(ebook2);

        // Act
        Collection<Ebook> allEbooks = ebookService.getAll();

        // Assert
        assertEquals(2, ebookService.getAll().size());
        assertTrue(allEbooks.contains(ebook1));
        assertTrue(allEbooks.contains(ebook2));
    }
}
