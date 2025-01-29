package com.example.cryptosim.service;

import com.example.cryptosim.model.CryptoCurrency;
import com.example.cryptosim.repository.CryptoCurrencyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Use Mockito Extension for unit tests
public class CryptoCurrencyServiceTest {

    @Mock
    private CryptoCurrencyRepository cryptoCurrencyRepository; // Mock repository

    @InjectMocks
    private CryptoCurrencyService cryptoCurrencyService; // Inject mocks into the service

    @Test
    public void testGetAllCryptos() {
        // Given
        CryptoCurrency crypto1 = new CryptoCurrency();
        crypto1.setId(1L);
        crypto1.setName("Bitcoin");
        crypto1.setSymbol("BTC");
        crypto1.setPair("BTC/USD");

        CryptoCurrency crypto2 = new CryptoCurrency();
        crypto2.setId(2L);
        crypto2.setName("Ethereum");
        crypto2.setSymbol("ETH");
        crypto2.setPair("ETH/USD");

        List<CryptoCurrency> expectedCryptos = Arrays.asList(crypto1, crypto2);

        // When
        when(cryptoCurrencyRepository.findAll()).thenReturn(expectedCryptos); // Mock repository method

        // Call the service method
        List<CryptoCurrency> actualCryptos = cryptoCurrencyService.getAllCryptos();

        // Then
        assertNotNull(actualCryptos);
        assertEquals(2, actualCryptos.size());
        assertEquals("Bitcoin", actualCryptos.get(0).getName());
        assertEquals("ETH", actualCryptos.get(1).getSymbol());

        // Verify that the repository method was called exactly once
        verify(cryptoCurrencyRepository, times(1)).findAll();
    }
}
