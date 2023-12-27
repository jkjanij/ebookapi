package com.jani.ebookapi;

import com.jani.ebookapi.service.EbookService;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class EbookControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EbookService ebookService;

    @Test
    void shouldReturnHello() throws Exception {

        this.mockMvc.perform(get("/"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Welcome!")));
    }

    //@Test
    //void addEbookWithProperPayload() throws Exception {
    //    given(ebookService.add(ArgumentMatchers.any()))
    //}

    @Test
    void getEbookWithNoStoredEbooks() throws Exception {

        ResultActions response = this.mockMvc.perform(get("/ebooks")
                        .contentType(MediaType.APPLICATION_JSON));

        response.andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath( "$.data", Matchers.empty()));
    }
}
