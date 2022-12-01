package io.ruv.counters.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ruv.counters.service.*;
import io.ruv.counters.service.impl.CountersServiceImpl;
import io.ruv.counters.web.dto.CounterDto;
import io.ruv.counters.web.dto.CounterNamesDto;
import io.ruv.counters.web.dto.CounterSumDto;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("SameParameterValue")
@WebMvcTest
@AutoConfigureMockMvc
@RequiredArgsConstructor
public class CountersControllerTest {

    @MockBean
    private CountersService service;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private final String oneName = "one-name";
    private final long oneValue = 1;

    private final CounterDto oneDto = new CounterDto() {{

        setName(oneName);
        setValue(oneValue);
    }};

    private final String baseUrl = "/api/v1/counters";

    private MockHttpServletRequestBuilder create() {

        return MockMvcRequestBuilders.post(baseUrl).contentType(MediaType.APPLICATION_JSON);
    }

    private MockHttpServletRequestBuilder insert(String name) {

        return MockMvcRequestBuilders.put(String.format("%s/%s", baseUrl, name)).contentType(MediaType.APPLICATION_JSON);
    }

    private MockHttpServletRequestBuilder increment(String name) {

        return MockMvcRequestBuilders.post(String.format("%s/%s", baseUrl, name));
    }

    private MockHttpServletRequestBuilder get(String name) {

        return MockMvcRequestBuilders.get(String.format("%s/%s", baseUrl, name));
    }

    private MockHttpServletRequestBuilder delete(String name) {

        return MockMvcRequestBuilders.delete(String.format("%s/%s", baseUrl, name));
    }

    private MockHttpServletRequestBuilder sum() {

        return MockMvcRequestBuilders.get(String.format("%s%s", baseUrl, "/extension/values-sum"));
    }

    private MockHttpServletRequestBuilder names() {

        return MockMvcRequestBuilders.get(String.format("%s%s", baseUrl, "/extension/names-list"));
    }

    private byte[] json(Object o) throws JsonProcessingException {

        return objectMapper.writeValueAsBytes(o);
    }

    @AfterEach
    public void reset() {

        Mockito.reset(service);
    }

    @Test
    public void createNonExistingReturnsOkDto() throws Exception {

        Mockito.when(service.create(Mockito.any()))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        //act
        mockMvc.perform(create().content(json(oneDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value(oneName))
                .andExpect(jsonPath("value").value(oneValue));
    }

    @Test
    public void createWithoutNameAssignsNameReturnsOkDto() throws Exception {

        Mockito.when(service.create(Mockito.any()))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        val unnamedDto = new CounterDto();
        unnamedDto.setValue(oneValue);

        //act
        mockMvc.perform(create().content(json(unnamedDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value(Matchers.not(Matchers.blankOrNullString())))
                .andExpect(jsonPath("value").value(oneValue));
    }

    @Test
    public void createDuplicateNameReturnsBadRequest() throws Exception {

        Mockito.when(service.create(Mockito.any()))
                .thenThrow(DuplicateNameException.of(oneName));

        //act
        mockMvc.perform(create().content(json(oneDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].message").value(containsString(oneName)));
    }

    @Test
    public void createEmptyNameReturnsBadRequest() throws Exception {

        Mockito.when(service.create(Mockito.any()))
                .thenThrow(IllegalNameException.emptyName());

        val emptyNameDto = new CounterDto();
        emptyNameDto.setName("");
        emptyNameDto.setValue(oneValue);

        //act
        mockMvc.perform(create().content(json(emptyNameDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].message").value(IllegalNameException.emptyName().getMessage()));
    }

    @Test
    public void createTooLongNameReturnsBadRequest() throws Exception {

        val maxLength = CountersServiceImpl.MAX_NAME_LENGTH;

        val longName = String.join("", Collections.nCopies(maxLength + 1, "x"));

        Mockito.when(service.create(Mockito.any()))
                .thenThrow(IllegalNameException.tooLongName(longName, maxLength));

        val longNameDto = new CounterDto();
        longNameDto.setName(longName);
        longNameDto.setValue(oneValue);

        //act
        mockMvc.perform(create().content(json(longNameDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].message").value(containsString(longNameDto.getName())))
                .andExpect(jsonPath("errors[0].message").value(containsString(String.valueOf(maxLength))));
    }

    @Test
    public void insertNonExistingReturnsOkDto() throws Exception {

        Mockito.when(service.create(Mockito.any()))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        //act
        mockMvc.perform(insert(oneName).content(json(oneDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value(oneName))
                .andExpect(jsonPath("value").value(oneValue));
    }

    @Test
    public void insertNonExistingRewritesNameFromPath() throws Exception {

        Mockito.when(service.create(Mockito.any()))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        val anotherNameDto = new CounterDto();
        anotherNameDto.setName("anotherName");
        anotherNameDto.setValue(oneValue);

        //act
        mockMvc.perform(insert(oneName).content(json(anotherNameDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value(oneName))
                .andExpect(jsonPath("value").value(oneValue));
    }

    @Test
    public void insertDuplicateNameReturnsBadRequest() throws Exception {

        Mockito.when(service.create(Mockito.any()))
                .thenThrow(DuplicateNameException.of(oneName));

        //act
        mockMvc.perform(insert(oneName).content(json(oneDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].message").value(containsString(oneName)));
    }

    @Test
    public void insertNullNameReturnsBadRequest() throws Exception {

        Mockito.when(service.create(Mockito.any()))
                .thenThrow(IllegalNameException.emptyName());

        val emptyNameDto = new CounterDto();
        emptyNameDto.setName(null);
        emptyNameDto.setValue(oneValue);

        //act
        mockMvc.perform(insert(null).content(json(emptyNameDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].message")
                        .value(IllegalNameException.emptyName().getMessage()));
    }

    @Test
    public void insertTooLongNameReturnsBadRequest() throws Exception {

        val maxLength = CountersServiceImpl.MAX_NAME_LENGTH;

        val longName = String.join("", Collections.nCopies(maxLength + 1, "x"));

        Mockito.when(service.create(Mockito.any()))
                .thenThrow(IllegalNameException.tooLongName(longName, maxLength));

        val longNameDto = new CounterDto();
        longNameDto.setName(longName);
        longNameDto.setValue(oneValue);

        //act
        mockMvc.perform(insert(longName).content(json(longNameDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].message").value(containsString(longNameDto.getName())))
                .andExpect(jsonPath("errors[0].message").value(containsString(String.valueOf(maxLength))));
    }

    @Test
    public void incrementExistingReturnsOkDto() throws Exception {

        Mockito.when(service.incrementByName(Mockito.any()))
                .thenReturn(oneDto);

        //act
        mockMvc.perform(increment(oneName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value(oneName))
                .andExpect(jsonPath("value").value(oneValue));
    }

    @Test
    public void incrementNonExistingReturnsNotFound() throws Exception {

        Mockito.when(service.incrementByName(Mockito.any()))
                .thenThrow(NotFoundException.of(oneName));

        //act
        mockMvc.perform(increment(oneName))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("errors[0].message").value(containsString(oneName)));
    }

    @Test
    public void incrementOverflowingReturnsInsufficientStorage() throws Exception {

        Mockito.when(service.incrementByName(Mockito.any()))
                .thenThrow(OverflowException.of(oneName, new ArithmeticException()));

        //act
        mockMvc.perform(increment(oneName))
                .andExpect(status().isInsufficientStorage())
                .andExpect(jsonPath("errors[0].message").value(containsString(oneName)));
    }

    @Test
    public void getExistingReturnsOkDto() throws Exception {

        Mockito.when(service.getByName(Mockito.any()))
                .thenReturn(oneDto);

        //act
        mockMvc.perform(get(oneName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value(oneName))
                .andExpect(jsonPath("value").value(oneValue));
    }

    @Test
    public void getNonExistingReturnsNotFound() throws Exception {

        Mockito.when(service.getByName(Mockito.any()))
                .thenThrow(NotFoundException.of(oneName));

        //act
        mockMvc.perform(get(oneName))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("errors[0].message").value(Matchers.containsString(oneName)));
    }

    @Test
    public void deleteExistingReturnsOkDto() throws Exception {

        Mockito.when(service.deleteByName(Mockito.any()))
                .thenReturn(oneDto);

        //act
        mockMvc.perform(delete(oneName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value(oneName))
                .andExpect(jsonPath("value").value(oneValue));
    }

    @Test
    public void deleteNonExistingReturnsNotFound() throws Exception {

        Mockito.when(service.deleteByName(Mockito.any()))
                .thenThrow(NotFoundException.of(oneName));

        //act
        mockMvc.perform(delete(oneName))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("errors[0].message").value(Matchers.containsString(oneName)));
    }

    @Test
    public void countersSumExistingReturnsOkSum() throws Exception {

        Mockito.when(service.getCounterSum())
                .thenReturn(new CounterSumDto(BigInteger.valueOf(oneValue)));

        //act
        mockMvc.perform(sum())
                .andExpect(status().isOk())
                .andExpect(jsonPath("sum").value(BigInteger.valueOf(oneValue)));
    }

    @Test
    public void countersSumEmptyReturnsOkZero() throws Exception {

        Mockito.when(service.getCounterSum())
                .thenReturn(new CounterSumDto(BigInteger.ZERO));

        //act
        mockMvc.perform(sum())
                .andExpect(status().isOk())
                .andExpect(jsonPath("sum").value(BigInteger.ZERO));
    }

    @Test
    public void countersSumBigReturnsOkSum() throws Exception {

        val longer = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE);

        Mockito.when(service.getCounterSum())
                .thenReturn(new CounterSumDto(longer));

        //act
        mockMvc.perform(sum())
                .andExpect(status().isOk())
                .andExpect(jsonPath("sum").value(longer));
    }

    @Test
    public void counterNamesExistingReturnsOkNames() throws Exception {

        Mockito.when(service.getCounterNames())
                .thenReturn(new CounterNamesDto(List.of(oneName)));

        //act
        mockMvc.perform(names())
                .andExpect(status().isOk())
                .andExpect(jsonPath("names").isArray())
                .andExpect(jsonPath("names").value(Matchers.hasSize(1)))
                .andExpect(jsonPath("names").value(hasItem(oneName)));
    }

    @Test
    public void counterNamesExistingReturnsOkEmpty() throws Exception {

        Mockito.when(service.getCounterNames())
                .thenReturn(new CounterNamesDto(List.of()));

        //act
        mockMvc.perform(names())
                .andExpect(status().isOk())
                .andExpect(jsonPath("names").isArray())
                .andExpect(jsonPath("names").value(Matchers.empty()));
    }
}
