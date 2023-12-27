package com.jani.ebookapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jani.ebookapi.model.Ebook;
import com.jani.ebookapi.service.EbookService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
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
                .andDo(print())
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

        // Act and Assert
        ResultActions response = this.mockMvc.perform(post("/ebooks")
                        .content(objectMapper.writeValueAsString(inputEbook))
                        .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isCreated())
                        .andDo(MockMvcResultHandlers.print())
                        .andExpect(jsonPath("$.author").value("testAuthor"))
                        .andExpect(jsonPath("$.title").value("testTitle"))
                        .andExpect(jsonPath("$.format").value("testFormat"))
                        .andExpect(jsonPath("$.id").exists())
                        .andExpect(jsonPath("$.id").isString());
    }

    @Test
    void getEbookWithNoStoredEbooks() throws Exception {

        // Act and Assert
        ResultActions response = this.mockMvc.perform(get("/ebooks")
                        .contentType(MediaType.APPLICATION_JSON));

        response.andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath( "$.data", Matchers.empty()));
    }
}
