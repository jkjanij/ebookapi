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
import org.springframework.test.web.servlet.ResultActions;
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

        when(ebookService.add(any(Ebook.class))).thenAnswer(invocation -> {
            Ebook outputEbook = invocation.getArgument(0);
            outputEbook.setId(UUID.randomUUID().toString()); // Mocking the generated ID
            return outputEbook;
        });

        // Act
        ResultActions response = this.mockMvc.perform(post("/ebooks")
                        .content(objectMapper.writeValueAsString(inputEbook))
                        .contentType(MediaType.APPLICATION_JSON));

        // Assert
        response.andExpect(MockMvcResultMatchers.status().isCreated())
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
          "\"testField\": \"testValue\" }"
    })
    void shouldRejectAddEbookWithIncorrectPayload(String improperPayload) throws Exception {

        // Arrange
        // Act
        ResultActions response = this.mockMvc.perform(post("/ebooks")
                .content(improperPayload)
                .contentType(MediaType.APPLICATION_JSON));

        // Assert
        verifyNoInteractions(ebookService);
        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    void shouldGetAllEbooksWithNoStoredEbooks() throws Exception {

        // Arrange
        Map<String, Ebook> noEbooks = new HashMap<>();
        when(ebookService.getAll()).thenReturn(noEbooks.values());

        // Act
        ResultActions response = this.mockMvc.perform(get("/ebooks")
                        .contentType(MediaType.APPLICATION_JSON));

        // Assert
        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(header().string("Content-Type", "application/json"))
                .andExpect(jsonPath( "$.data", Matchers.empty()))
                .andExpect(jsonPath("$.*", hasSize(1)));
    }

    @Test
    void shouldGetAllEbooksWithStoredEbook() throws Exception {

        // Arrange
        String id = UUID.randomUUID().toString();
        Map<String, Ebook> oneEbook = new HashMap<>()
                {{
                    put(id, new Ebook(id, "testAuthor", "testTitle", "testFormat"));
                }};
        when(ebookService.getAll()).thenReturn(oneEbook.values());

        // Act
        ResultActions response = this.mockMvc.perform(get("/ebooks")
                .contentType(MediaType.APPLICATION_JSON));

        // Assert
        response.andExpect(MockMvcResultMatchers.status().isOk())
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

        // Act
        ResultActions response = this.mockMvc.perform(get("/ebooks/"+id));

        // Assert
        response.andExpect(MockMvcResultMatchers.status().isOk())
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

        // Act
        ResultActions response = this.mockMvc.perform(get("/ebooks/"+id));

        // Assert
        response.andExpect(MockMvcResultMatchers.status().isNotFound())
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

        // Act
        ResultActions response = this.mockMvc.perform(put("/ebooks/"+id)
                .content(objectMapper.writeValueAsString(update))
                .contentType(MediaType.APPLICATION_JSON));

        // Assert
        response.andExpect(MockMvcResultMatchers.status().isNotFound())
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
                    "\"testField\": \"testValue\" }"
    })
    void shouldRejectUpdateEbookWithIncorrectPayload(String improperPayload) throws Exception {

        // Arrange
        String id = UUID.randomUUID().toString();

        // Act
        ResultActions response = this.mockMvc.perform(put("/ebooks/"+id)
                .content(objectMapper.writeValueAsString(improperPayload))
                .contentType(MediaType.APPLICATION_JSON));

        // Assert
        verifyNoInteractions(ebookService);
        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    void shouldUpdateEbookWithMatchingId() throws Exception {

        // Arrange
        String id = UUID.randomUUID().toString();
        Map<String, Ebook> oneEbook = new HashMap<>()
        {{
            put(id, new Ebook(id, "testAuthor", "testTitle", "testFormat"));
        }};
        Ebook updateEbook = new Ebook();
        updateEbook.setFormat("testFormatNew");
        updateEbook.setTitle("testTitleNew");
        updateEbook.setAuthor("testAuthorNew");
        when(ebookService.get(id)).thenReturn(oneEbook.get(id));
        // no need to mock void ebookService.update

        // Act
        ResultActions response = this.mockMvc.perform(put("/ebooks/"+id)
                .content(objectMapper.writeValueAsString(updateEbook))
                .contentType(MediaType.APPLICATION_JSON));

        // Assert
        response.andExpect(MockMvcResultMatchers.status().isOk())
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

        // Act
        ResultActions response = this.mockMvc.perform(delete("/ebooks/"+id));

        // Assert
        response.andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(content().string(""));
    }

    @Test
    void shouldDeleteEbookByIdWithMatchingId() throws Exception {

        // Arrange
        String id = UUID.randomUUID().toString();
        Ebook removedEbook = new Ebook();

        when(ebookService.remove(id)).thenReturn(removedEbook);

        // Act
        ResultActions response = this.mockMvc.perform(delete("/ebooks/"+id));

        // Assert
        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().string(""));
    }
}
