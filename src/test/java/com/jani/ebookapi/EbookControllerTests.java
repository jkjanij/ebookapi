package com.jani.ebookapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jani.ebookapi.model.Ebook;
import com.jani.ebookapi.service.EbookService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.*;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest
@AutoConfigureMockMvc
class EbookControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EbookService ebookService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnWelcomeMessage() throws Exception {

        this.mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Welcome!")));
    }

    @Test
    void shouldAddEbookWithCorrectPayload() throws Exception {

        // Arrange
        Ebook inputEbook = new Ebook();
        inputEbook.setAuthor("testAuthor");
        inputEbook.setTitle("testTitle");
        inputEbook.setFormat("testFormat");

        Ebook outputEbook = new Ebook();
        outputEbook.setId(UUID.randomUUID().toString());
        outputEbook.setAuthor("testAuthor");
        outputEbook.setTitle("testTitle");
        outputEbook.setFormat("testFormat");

        when(ebookService.add(any(Ebook.class))).thenReturn(outputEbook);

        // Act & Assert
        this.mockMvc.perform(post("/ebooks")
                .content(objectMapper.writeValueAsString(inputEbook))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Content-Type", "application/json"))
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
    void shouldRejectAddEbookWithIncorrectPayload(String improperPayload) throws Exception {

        // Act & Assert
        this.mockMvc.perform(post("/ebooks")
                .content(improperPayload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
        verifyNoInteractions(ebookService);
    }

    @Test
    void shouldGetAllEbooksWithNoStoredEbooks() throws Exception {

        // Arrange
        when(ebookService.getAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        this.mockMvc.perform(get("/ebooks")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/json"))
                .andExpect(jsonPath( "$.data", Matchers.empty()))
                .andExpect(jsonPath("$.*", hasSize(1)));
    }

    @Test
    void shouldGetAllEbooksWithStoredEbook() throws Exception {

        // Arrange
        String id = UUID.randomUUID().toString();
        Ebook ebook = new Ebook(id, "testAuthor", "testTitle", "testFormat");
        when(ebookService.getAll()).thenReturn(Arrays.asList(ebook));

        // Act & Assert
        this.mockMvc.perform(get("/ebooks")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/json"))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").exists())
                .andExpect(jsonPath("$.data[0].author").value("testAuthor"))
                .andExpect(jsonPath("$.data[0].title").value("testTitle"))
                .andExpect(jsonPath("$.data[0].format").value("testFormat"))
                .andExpect(jsonPath("$.data[0].*", hasSize(4)));
    }

    @Test
    void shouldGetEbookByIdWithMatchingId() throws Exception {

        // Arrange
        String id = UUID.randomUUID().toString();
        Ebook returnEbook = new Ebook();
        returnEbook.setFormat("testFormat");
        returnEbook.setTitle("testTitle");
        returnEbook.setAuthor("testAuthor");
        when(ebookService.get(id)).thenReturn(returnEbook);

        // Act & Assert
        this.mockMvc.perform(get("/ebooks/"+id))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/json"))
                .andExpect(jsonPath("$.author").value("testAuthor"))
                .andExpect(jsonPath("$.title").value("testTitle"))
                .andExpect(jsonPath("$.format").value("testFormat"))
                .andExpect(jsonPath("$.*", hasSize(3)));
    }

    @Test
    void shouldNotGetEbookByIdWithoutMatchingId() throws Exception {

        // Arrange
        String id = UUID.randomUUID().toString();
        when(ebookService.get(id)).thenReturn(null);

        // Act & Assert
        this.mockMvc.perform(get("/ebooks/"+id))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
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
        when(ebookService.get(id)).thenReturn(null);

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
    void shouldRejectUpdateEbookWithIncorrectPayload(String improperPayload) throws Exception {

        // Arrange
        String id = UUID.randomUUID().toString();

        // Act & Assert
        this.mockMvc.perform(put("/ebooks/"+id)
                .content(objectMapper.writeValueAsString(improperPayload))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest())
                .andExpect(content().string(""));
        verifyNoInteractions(ebookService);
    }

    @Test
    void shouldUpdateEbookWithMatchingId() throws Exception {

        // Arrange
        String id = UUID.randomUUID().toString();
        Ebook existingEbook = new Ebook();
        existingEbook.setId(id);
        existingEbook.setFormat("testFormat");
        existingEbook.setTitle("testTitle");
        existingEbook.setAuthor("testAuthor");

        Ebook updateForEbook = new Ebook();
        updateForEbook.setFormat("testFormatNew");
        updateForEbook.setTitle("testTitleNew");
        updateForEbook.setAuthor("testAuthorNew");

        when(ebookService.get(id)).thenReturn(existingEbook);
        // no need to mock void ebookService.update

        // Act & Assert
        this.mockMvc.perform(put("/ebooks/"+id)
                .content(objectMapper.writeValueAsString(updateForEbook))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/json"))
                .andExpect(jsonPath("$.author").value("testAuthorNew"))
                .andExpect(jsonPath("$.title").value("testTitleNew"))
                .andExpect(jsonPath("$.format").value("testFormatNew"))
                .andExpect(jsonPath("$.*", hasSize(3)));
    }

    @Test
    void shouldNotDeleteEbookByIdWithoutMatchingId() throws Exception {

        // Arrange
        String id = UUID.randomUUID().toString();
        when(ebookService.remove(id)).thenReturn(null);

        // Act & Assert
        this.mockMvc.perform(delete("/ebooks/"+id))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }

    @Test
    void shouldDeleteEbookByIdWithMatchingId() throws Exception {

        // Arrange
        String id = UUID.randomUUID().toString();
        Ebook removedEbook = new Ebook();

        when(ebookService.remove(id)).thenReturn(removedEbook);

        // Act & Assert
        this.mockMvc.perform(delete("/ebooks/"+id))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }
}
