// File: src/main/java/org/example/model/SerialNumberGenerator.java
package org.example.model;

/**
 * Singleton pattern for generating unique bill serial numbers
 * Thread-safe implementation for future concurrent access
 */
public class SerialNumberGenerator {
    private static SerialNumberGenerator instance;
    private volatile int currentSerial;

    private SerialNumberGenerator() {
        this.currentSerial = 1;
    }

    public static synchronized SerialNumberGenerator getInstance() {
        if (instance == null) {
            instance = new SerialNumberGenerator();
        }
        return instance;
    }

    public synchronized int getNextSerial() {
        return currentSerial++;
    }

    public synchronized void reset() {
        currentSerial = 1;
    }

    public synchronized int getCurrentSerial() {
        return currentSerial;
    }
}