package com.andruy.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.andruy.backend.model.CurrencyConversionRates;
import com.andruy.backend.service.CurrencyConversionRatesService;

@RestController
@RequestMapping("/api/currency")
public class CurrencyConversionRatesController {
    private final CurrencyConversionRatesService currencyConversionRatesService;

    public CurrencyConversionRatesController(CurrencyConversionRatesService currencyConversionRatesService) {
        this.currencyConversionRatesService = currencyConversionRatesService;
    }

    @GetMapping
    public ResponseEntity<CurrencyConversionRates> getCurrencyRates() {
        CurrencyConversionRates rates = currencyConversionRatesService.getCurrencyRates();

        return ResponseEntity.ok(rates);
    }
}
