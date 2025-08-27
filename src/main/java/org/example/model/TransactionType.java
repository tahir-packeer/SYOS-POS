// File: src/main/java/org/example/model/TransactionType.java
package org.example.model;

public enum TransactionType {
    IN_STORE("IN_STORE"),
    ONLINE("ONLINE");

    private final String value;

    TransactionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static TransactionType fromString(String value) {
        for (TransactionType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown transaction type: " + value);
    }
}