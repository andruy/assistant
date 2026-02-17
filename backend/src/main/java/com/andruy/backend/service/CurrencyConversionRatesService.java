package com.andruy.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.andruy.backend.model.CurrencyConversionRates;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CurrencyConversionRatesService {
    @Value("${my.currency.key}")
    private String apiKey;
    @Autowired
    private ObjectMapper objectMapper;
    private Logger logger = LoggerFactory.getLogger(CurrencyConversionRatesService.class);

    public CurrencyConversionRates getCurrencyRates() {
        String url = "https://v6.exchangerate-api.com/v6/" + apiKey + "/latest/USD";

        String json = new RestTemplate().getForObject(url, String.class);

        try {
            return objectMapper.readValue(json, CurrencyConversionRates.class);
        } catch (Exception e) {
            logger.error("Error while reading currency conversion rates from JSON: {}", e.getMessage());
            return null;
        }
    }
}
