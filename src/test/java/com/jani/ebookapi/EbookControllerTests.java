package com.jani.ebookapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jani.ebookapi.model.Ebook;
import com.jani.ebookapi.service.EbookService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.*;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
class EbookControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EbookService ebookService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnHello() throws Exception {

        this.mockMvc.perform(get("/"))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Welcome!")));
    }

    @Test
    void addEbookWithProperPayload() throws Exception {

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
                        //.andDo(MockMvcResultHandlers.print())
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
    void addEbookWithImproperPayload(String improperPayload) throws Exception {

        // Arrange
        // Act
        ResultActions response = this.mockMvc.perform(post("/ebooks")
                .content(improperPayload)
                .contentType(MediaType.APPLICATION_JSON));

        // Assert
        verifyNoInteractions(ebookService);
        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().string(""));
                //.andDo(print());
    }

    @Test
    void getEbooksWithNoStoredEbooks() throws Exception {

        // Arrange
        Map<String, Ebook> noEbooks = new HashMap<>();
        when(ebookService.getAll()).thenReturn(noEbooks.values());

        // Act
        ResultActions response = this.mockMvc.perform(get("/ebooks")
                        .contentType(MediaType.APPLICATION_JSON));

        // Assert
        response.andExpect(MockMvcResultMatchers.status().isOk())
                //.andDo(print())
                .andExpect(header().string("Content-Type", "application/json"))
                .andExpect(jsonPath( "$.data", Matchers.empty()))
                .andExpect(jsonPath("$.*", hasSize(1)));
    }

    @Test
    void getEbooksWithStoredEbook() throws Exception {

        // Arrange
        Map<String, Ebook> oneEbook = new HashMap<>()
                {{
                    String id = UUID.randomUUID().toString();
                    put(id, new Ebook(id, "testAuthor", "testTitle", "testFormat"));
                }};
        when(ebookService.getAll()).thenReturn(oneEbook.values());

        // Act
        ResultActions response = this.mockMvc.perform(get("/ebooks")
                .contentType(MediaType.APPLICATION_JSON));

        // Assert
        response.andExpect(MockMvcResultMatchers.status().isOk())
                //.andDo(print())
                .andExpect(header().string("Content-Type", "application/json"))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").exists())
                //.andExpect(jsonPath("$.data[0].id", isValidUUID())) // need some method to check for valid UUID
                .andExpect(jsonPath("$.data[0].author").value("testAuthor"))
                .andExpect(jsonPath("$.data[0].title").value("testTitle"))
                .andExpect(jsonPath("$.data[0].format").value("testFormat"))
                .andExpect(jsonPath("$.data[0].*", hasSize(4)));
    }

    @Test
    void getEbookByIdWithMatch() throws Exception {

        // Arrange
        String id = UUID.randomUUID().toString();
        Ebook returnEbook = new Ebook();
        returnEbook.setFormat("testFormat");
        returnEbook.setTitle("testTitle");
        returnEbook.setAuthor("testAuthor");
        when(ebookService.get(id)).thenReturn(returnEbook);

        // Act
        ResultActions response = this.mockMvc.perform(get("/ebooks/"+id)
                .contentType(MediaType.APPLICATION_JSON));

        // Assert
        response.andExpect(MockMvcResultMatchers.status().isOk())
                //.andDo(print())
                .andExpect(header().string("Content-Type", "application/json"))
                .andExpect(jsonPath("$.author").value("testAuthor"))
                .andExpect(jsonPath("$.title").value("testTitle"))
                .andExpect(jsonPath("$.format").value("testFormat"))
                .andExpect(jsonPath("$.*", hasSize(3)));
    }

    @Test
    void getEbookByIdWithoutMatch() throws Exception {

        // Arrange
        String id = UUID.randomUUID().toString();
        when(ebookService.get(id)).thenReturn(null);

        // Act
        ResultActions response = this.mockMvc.perform(get("/ebooks/"+id)
                .contentType(MediaType.APPLICATION_JSON));

        // Assert
        response.andExpect(MockMvcResultMatchers.status().isNotFound())
                //.andDo(print())
                .andExpect(content().string(""));
    }
}
