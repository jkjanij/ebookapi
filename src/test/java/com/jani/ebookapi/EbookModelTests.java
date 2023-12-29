package com.jani.ebookapi;

import com.jani.ebookapi.model.Ebook;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EbookModelTests {

    @Test
    void shouldCreateEbookWithConstructor() {
        Ebook ebook = new Ebook("1", "testAuthor", "testTitle", "testFormat");

        assertThat(ebook.getId()).isEqualTo("1");
        assertThat(ebook.getAuthor()).isEqualTo("testAuthor");
        assertThat(ebook.getTitle()).isEqualTo("testTitle");
        assertThat(ebook.getFormat()).isEqualTo("testFormat");
    }

    @Test
    void shouldSetAndGetId() {
        Ebook ebook = new Ebook();
        ebook.setId("1");

        assertThat(ebook.getId()).isEqualTo("1");
    }

    @Test
    void shouldSetAndGetAuthor() {
        Ebook ebook = new Ebook();
        ebook.setAuthor("testAuthor");

        assertThat(ebook.getAuthor()).isEqualTo("testAuthor");
    }

    @Test
    void shouldSetAndGetTitle() {
        Ebook ebook = new Ebook();
        ebook.setTitle("testTitle");

        assertThat(ebook.getTitle()).isEqualTo("testTitle");
    }

    @Test
    void shouldSetAndGetFormat() {
        Ebook ebook = new Ebook();
        ebook.setFormat("testFormat");

        assertThat(ebook.getFormat()).isEqualTo("testFormat");
    }
}
