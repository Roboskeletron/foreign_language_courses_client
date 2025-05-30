package ru.vsu.foreign_language_courses_client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        if (args.length < 2) {
            logger.error("Usage: <totalCount> <threads>");
            System.err.println("Usage: <totalCount> <threads>"); // Also to stderr for immediate visibility if logging isn't set up
            System.exit(1);
        }
        int totalCount = 0;
        int threads = 0;
        try {
            totalCount = Integer.parseInt(args[0]);
            threads = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            logger.error("Invalid number format for totalCount or threads. Arguments: totalCount={}, threads={}", args[0], args[1], e);
            System.err.println("Invalid number format for totalCount or threads.");
            System.exit(1);
        }


        logger.info("Application starting with totalCount: {}, threads: {}", totalCount, threads);
        DataGeneratorHttpClient client = new DataGeneratorHttpClient();

        long start = System.currentTimeMillis();
        if (threads <= 1) {
            client.generateSequential(totalCount);
        } else {
            client.generateConcurrent(totalCount, threads);
        }
        long duration = System.currentTimeMillis() - start;
        logger.info("Client operation completed. Requested approximately {} records per main entity type, using {} thread(s) in {} ms (overall time including setup).", totalCount, threads, duration);
    }
}