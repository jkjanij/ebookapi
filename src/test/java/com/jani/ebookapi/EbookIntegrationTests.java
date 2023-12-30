package com.jani.ebookapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jani.ebookapi.model.Ebook;
import com.jani.ebookapi.service.EbookService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest(classes = EbookApplication.class)
@AutoConfigureMockMvc
public class EbookIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EbookService ebookService;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void cleanupTestData() {
        ebookService.clearData();
    }

    @Test
    void shouldAddEbookWithCorrectPayload() throws Exception {
        // Arrange
        Ebook update = new Ebook();
        update.setFormat("testFormat");
        update.setTitle("testTitle");
        update.setAuthor("testAuthor");

        // Act & Assert
        this.mockMvc.perform(post("/ebooks")
                .content(objectMapper.writeValueAsString(update))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.author").value("testAuthor"))
                .andExpect(jsonPath("$.title").value("testTitle"))
                .andExpect(jsonPath("$.format").value("testFormat"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.*", hasSize(4)));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "test",                             // non-JSON payload
            "{ }",                              // empty JSON payload
            "{ \"author\": \"test\" }",         // missing required fields
            "{ \"author\": \"testAuthor\"," +   // extra field
              "\"title\": \"testTitle\"," +
              "\"format\": \"testFormat\"," +
              "\"testField\": \"testValue\" }",
            "{ \"author\": \"\"," +             // empty required field values
              "\"title\": \"\"," +
              "\"format\": \"\" }",
    })
    void shouldNotAddEbookWithIncorrectPayload(String incorrectPayload) throws Exception {

        // Arrange
        // Act
        this.mockMvc.perform(post("/ebooks")
                .content(incorrectPayload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    void shouldGetAllEbooksWithNoStoredEbooks() throws Exception {
        // Act & Assert
        this.mockMvc.perform(get("/ebooks")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Content-Type", "application/json"))
                .andExpect(jsonPath( "$.data", Matchers.empty()))
                .andExpect(jsonPath("$.*", hasSize(1)));
    }

    @Test
    void shouldGetAllEbooksWithStoredEbooks() throws Exception {
        // Arrange
        ebookService.add(new Ebook(null, "testAuthor1", "testTitle1", "testFormat1"));
        ebookService.add(new Ebook(null, "testAuthor2", "testTitle2", "testFormat2"));

        // Act & Assert
        this.mockMvc.perform(get("/ebooks"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/json"))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data", hasItem(
                        allOf(
                                hasEntry("author", "testAuthor1"),
                                hasEntry("title", "testTitle1"),
                                hasEntry("format", "testFormat1")
                        )
                )))
                .andExpect(jsonPath("$.data", hasItem(
                        allOf(
                                hasEntry("author", "testAuthor2"),
                                hasEntry("title", "testTitle2"),
                                hasEntry("format", "testFormat2")
                        )
                )));
    }

    @Test
    void shouldGetEbookWithMatchingId() throws Exception {
        // Arrange
        Ebook ebook = ebookService.add(new Ebook(null, "testAuthor", "testTitle", "testFormat"));

        // Act & Assert
        this.mockMvc.perform(get("/ebooks/"+ebook.getId()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/json"))
                .andExpect(jsonPath("$.author").value("testAuthor"))
                .andExpect(jsonPath("$.title").value("testTitle"))
                .andExpect(jsonPath("$.format").value("testFormat"))
                .andExpect(jsonPath("$.*", hasSize(3)));
    }

    @Test
    void shouldNotGetEbookWithoutMatchingId() throws Exception {
        // Arrange
        String id = UUID.randomUUID().toString();

        // Act & Assert
        this.mockMvc.perform(get("/ebooks/"+id))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }

    @Test
    void shouldNotUpdateEbookWithoutMatchingId() throws Exception {
        // Arrange
        String id = UUID.randomUUID().toString();
        Ebook update = new Ebook();
        update.setFormat("testFormat");
        update.setTitle("testTitle");
        update.setAuthor("testAuthor");

        // Act & Assert
        this.mockMvc.perform(put("/ebooks/"+id)
                .content(objectMapper.writeValueAsString(update))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "test",                             // non-JSON payload
            "{ }",                              // empty JSON payload
            "{ \"author\": \"test\" }",         // missing required fields
            "{ \"author\": \"testAuthor\"," +   // extra field
              "\"title\": \"testTitle\"," +
              "\"format\": \"testFormat\"," +
              "\"testField\": \"testValue\" }",
            "{ \"author\": \"\"," +             // empty required field values
              "\"title\": \"\"," +
              "\"format\": \"\" }",
    })
    void shouldNotUpdateEbookWithIncorrectPayload(String improperPayload) throws Exception {
        // Arrange
        String id = UUID.randomUUID().toString();

        // Act & Assert
        this.mockMvc.perform(put("/ebooks/"+id)
                .content(objectMapper.writeValueAsString(improperPayload))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    void shouldUpdateEbookWithMatchingId() throws Exception {
        // Arrange
        Ebook ebook = ebookService.add(new Ebook(null, "testAuthor", "testTitle", "testFormat"));
        Ebook updateEbook = new Ebook();
        updateEbook.setFormat("testFormatNew");
        updateEbook.setTitle("testTitleNew");
        updateEbook.setAuthor("testAuthorNew");

        // Act & Assert
        this.mockMvc.perform(put("/ebooks/"+ebook.getId())
                .content(objectMapper.writeValueAsString(updateEbook))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/json"))
                .andExpect(jsonPath("$.author").value("testAuthorNew"))
                .andExpect(jsonPath("$.title").value("testTitleNew"))
                .andExpect(jsonPath("$.format").value("testFormatNew"))
                .andExpect(jsonPath("$.*", hasSize(3)));
    }

    @Test
    void shouldNotDeleteEbookWithoutMatchingId() throws Exception {
        // Arrange
        String id = UUID.randomUUID().toString();

        // Act & Assert
        this.mockMvc.perform(delete("/ebooks/"+id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }

    @Test
    void shouldDeleteEbookWithMatchingId() throws Exception {
        // Arrange
        Ebook ebook = ebookService.add(new Ebook(null, "testAuthor", "testTitle", "testFormat"));

        // Act & Assert
        this.mockMvc.perform(delete("/ebooks/"+ebook.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().string(""));
    }
}
