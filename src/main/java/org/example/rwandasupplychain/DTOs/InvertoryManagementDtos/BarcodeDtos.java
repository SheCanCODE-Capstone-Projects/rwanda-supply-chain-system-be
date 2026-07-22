package org.example.rwandasupplychain.DTOs.InvertoryManagementDtos;

public class BarcodeDtos {
    public record DecodeResponse(
            String decodedContent,
            String matchType,
            Object match
    ) {}
}