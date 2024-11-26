package com.example.identityService.Util;

public class TimeConverter {
    public static int convertToMilliseconds(String input) {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Input cannot be null or empty");
        }

        String numericPart = input.replaceAll("[^0-9]", "");
        String unitPart = input.replaceAll("[^a-zA-Z]", "");

        if (numericPart.isEmpty() || unitPart.isEmpty()) {
            throw new IllegalArgumentException("Invalid input format: " + input);
        }

        int value = Integer.parseInt(numericPart);

        return switch (unitPart.toLowerCase()) {
            case "ms" -> // Milliseconds
                    value;
            case "s" -> // Seconds
                    value * 1000;
            case "m" -> // Minutes
                    value * 60 * 1000;
            case "h" -> // Hours
                    value * 60 * 60 * 1000;
            case "d" -> // Days
                    value * 24 * 60 * 60 * 1000;
            default -> throw new IllegalArgumentException("Unsupported time unit: " + unitPart);
        };
    }
}
