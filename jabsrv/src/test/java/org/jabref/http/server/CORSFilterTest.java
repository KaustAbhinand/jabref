package org.jabref.http.server;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CORSFilterTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "http://localhost",
            "http://localhost:1234",
            "https://localhost:3000",
            "http://127.0.0.1:23119",
            "http://[::1]:23119",
            "chrome-extension://abcdefghijklmnopqrstuvwxyzabcdef",
            "moz-extension://d0e0d0e0-0000-0000-0000-000000000000"
    })
    void allowsLocalCallers(String origin) {
        assertTrue(CORSFilter.isAllowedOrigin(origin));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "null",
            "https://example.com",
            "http://localhost.example.com",
            "http://127.0.0.1.example.com",
            "http://evil-localhost",
            "file://",
            "not an origin"
    })
    void rejectsEverythingElse(String origin) {
        assertFalse(CORSFilter.isAllowedOrigin(origin));
    }
}
