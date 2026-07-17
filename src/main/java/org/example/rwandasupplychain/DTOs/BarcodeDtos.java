package org.example.rwandasupplychain.DTOs;

public class BarcodeDtos {
    public record DecodeResponse(
            String decodedContent,
            String matchType,
            Object match
    ) {}
}