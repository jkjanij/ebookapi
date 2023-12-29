package com.jani.ebookapi;

import com.jani.ebookapi.model.Ebook;
import com.jani.ebookapi.service.EbookService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class EbookIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EbookService ebookService;

    @AfterEach
    void cleanupTestData() {
        ebookService.clearData();
    }

    @Test
    void getAllEbooksWithNoStoredEbooks() throws Exception {
        this.mockMvc.perform(get("/ebooks")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Content-Type", "application/json"))
                .andExpect(jsonPath( "$.data", Matchers.empty()))
                .andExpect(jsonPath("$.*", hasSize(1)));
    }

    @Test
    void addEbookIncorrectPayload() throws Exception {
        this.mockMvc.perform(post("/ebooks")
                .content("{\"author\":\"testAuthor\", \"title\":\"testTitle\"}") // missing field
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addEbookCorrectPayload() throws Exception {
        this.mockMvc.perform(post("/ebooks")
                .content("{\"author\":\"testAuthor\", \"title\":\"testTitle\", \"format\":\"testFormat\"}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void getAllEbooksWithStoredEbooks() throws Exception {
        ebookService.add(new Ebook(null, "testAuthor1", "testTitle1", "testFormat1"));
        ebookService.add(new Ebook(null, "testAuthor2", "testTitle2", "testFormat2"));

        this.mockMvc.perform(get("/ebooks"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/json"))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].id").exists())
                .andExpect(jsonPath("$.data[0].author").value("testAuthor1"))
                .andExpect(jsonPath("$.data[0].title").value("testTitle1"))
                .andExpect(jsonPath("$.data[0].format").value("testFormat1"))
                .andExpect(jsonPath("$.data[0].*", hasSize(4)))
                .andExpect(jsonPath("$.data[1].id").exists())
                .andExpect(jsonPath("$.data[1].author").value("testAuthor2"))
                .andExpect(jsonPath("$.data[1].title").value("testTitle2"))
                .andExpect(jsonPath("$.data[1].format").value("testFormat2"))
                .andExpect(jsonPath("$.data[1].*", hasSize(4)));
    }

    @Test
    void getEbookByIdWithIdMatch() throws Exception {
        Ebook ebook = ebookService.add(new Ebook(null, "testAuthor1", "testTitle1", "testFormat1"));

        this.mockMvc.perform(get("/ebooks/"+ebook.getId()))
                .andExpect(status().isOk());
    }
}
