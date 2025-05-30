package ru.vsu.foreign_language_courses_client;

import com.github.javafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DataGeneratorHttpClient {

    private static final Logger logger = LoggerFactory.getLogger(DataGeneratorHttpClient.class);
    private static final String BASE_URL = "http://localhost:8080/api";
    private final RestTemplate restTemplate;

    public DataGeneratorHttpClient() {
        this.restTemplate = new RestTemplate();
    }

    public List<UUID> createCourses(int count, Faker faker) {
        if (count <= 0) return Collections.emptyList();
        List<UUID> createdIds = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Map<String, Object> body = new HashMap<>();
            body.put("name", faker.educator().course());
            body.put("description", faker.lorem().sentence());
            body.put("createdAt", Instant.now().toString());

            HttpEntity<Map<String, Object>> request = buildRequest(body);
            try {
                ResponseEntity<Map> response = restTemplate.postForEntity(
                        BASE_URL + "/courses", request, Map.class);
                if (response.getBody() != null && response.getBody().get("id") != null) {
                    UUID id = UUID.fromString((String) response.getBody().get("id"));
                    createdIds.add(id);
                    logger.debug("Created Course: {}", id);
                } else {
                    logger.warn("Course creation API call returned success but no ID or empty body. Body: {}", body);
                }
            } catch (RestClientException e) {
                logger.error("Error creating course. Body: {}, Error: {}", body, e.getMessage());
            }  catch (Exception e) {
                logger.error("Unexpected error creating course. Body: {}", body, e);
            }
        }
        return createdIds;
    }

    public UUID createGroup(UUID courseId, Faker faker) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", faker.lorem().word() + " Group");
        body.put("courseId", courseId.toString());
        body.put("schedule", List.of("Mon-Wed-Fri " + faker.number().numberBetween(9, 17) + ":00",
                "Tue-Thu " + faker.number().numberBetween(9, 17) + ":00"));

        HttpEntity<Map<String, Object>> request = buildRequest(body);
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    BASE_URL + "/groups", request, Map.class);
            if (response.getBody() != null && response.getBody().get("id") != null) {
                UUID groupId = UUID.fromString((String) response.getBody().get("id"));
                logger.debug("Created Group: {}", groupId);
                return groupId;
            } else {
                logger.warn("Group creation API call for course {} returned success but no ID or empty body. Body: {}", courseId, body);
            }
        } catch (RestClientException e) {
            logger.error("Error creating group for course {}. Body: {}, Error: {}", courseId, body, e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error creating group for course {}. Body: {}", courseId, body, e);
        }
        return null;
    }

    public List<UUID> createStudents(int count, Faker faker) {
        if (count <= 0) return Collections.emptyList();
        List<UUID> createdIds = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Map<String, Object> body = new HashMap<>();
            body.put("firstName", faker.name().firstName());
            body.put("lastName", faker.name().lastName());
            body.put("email", faker.internet().emailAddress());
            body.put("registeredAt", Instant.now().toString());

            HttpEntity<Map<String, Object>> request = buildRequest(body);
            try {
                ResponseEntity<Map> response = restTemplate.postForEntity(
                        BASE_URL + "/students", request, Map.class);
                if (response.getBody() != null && response.getBody().get("id") != null) {
                    UUID id = UUID.fromString((String) response.getBody().get("id"));
                    createdIds.add(id);
                    logger.debug("Created Student: {}", id);
                } else {
                    logger.warn("Student creation API call returned success but no ID or empty body. Body: {}", body);
                }
            } catch (RestClientException e) {
                logger.error("Error creating student. Body: {}, Error: {}", body, e.getMessage());
            } catch (Exception e) {
                logger.error("Unexpected error creating student. Body: {}", body, e);
            }
        }
        return createdIds;
    }

    public boolean createEducation(UUID studentId, UUID groupId) {
        Map<String, Object> body = new HashMap<>();
        body.put("studentId", studentId.toString());
        body.put("groupId", groupId.toString());
        body.put("enrolledAt", Instant.now().toString());

        HttpEntity<Map<String, Object>> request = buildRequest(body);
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    BASE_URL + "/educations", request, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().get("id") != null) {
                logger.debug("Created Education: {}", response.getBody().get("id"));
                return true;
            } else {
                logger.warn("Education creation for student {} and group {} API call returned {} or no ID. Body: {}", studentId, groupId, response.getStatusCode(), body);
            }
        } catch (RestClientException e) {
            logger.error("Error creating education for student {} and group {}. Body: {}, Error: {}", studentId, groupId, body, e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error creating education for student {} and group {}. Body: {}", studentId, groupId, body, e);
        }
        return false;
    }

    private HttpEntity<Map<String, Object>> buildRequest(Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    public void generateSequential(int totalCount) {
        logger.info("Starting sequential generation of {} records for each primary type...", totalCount);
        Faker localFaker = new Faker();
        long startOverall = System.currentTimeMillis();

        logger.info("Creating courses...");
        List<UUID> courseIds = createCourses(totalCount, localFaker);
        logger.info("Finished creating courses. Target: {}, Actual: {}. Time: {}ms", totalCount, courseIds.size(), (System.currentTimeMillis() - startOverall));
        if (courseIds.isEmpty() && totalCount > 0) {
            logger.error("No courses created. Aborting further sequential generation.");
            return;
        }

        long startStudents = System.currentTimeMillis();
        logger.info("Creating students...");
        List<UUID> studentIds = createStudents(totalCount, localFaker);
        logger.info("Finished creating students. Target: {}, Actual: {}. Time: {}ms", totalCount, studentIds.size(), (System.currentTimeMillis() - startStudents));
        if (studentIds.isEmpty() && totalCount > 0) {
            logger.error("No students created. Aborting further sequential generation.");
            return;
        }

        long startGroups = System.currentTimeMillis();
        logger.info("Creating groups...");
        List<UUID> groupIds = new ArrayList<>();
        for (int i = 0; i < totalCount; i++) {
            if (courseIds.isEmpty()) break;
            UUID courseId = courseIds.get(i % courseIds.size());
            UUID groupId = createGroup(courseId, localFaker);
            if (groupId != null) {
                groupIds.add(groupId);
            }
            if ((i + 1) % (Math.max(1, totalCount / 10)) == 0) {
                logger.debug("  Sequentially created {}/{} groups...", (i + 1), totalCount);
            }
        }
        logger.info("Finished creating groups. Target: {}, Actual: {}. Time: {}ms", totalCount, groupIds.size(), (System.currentTimeMillis() - startGroups));
        if (groupIds.isEmpty() && totalCount > 0) {
            logger.error("No groups created. Aborting further sequential generation.");
            return;
        }

        long startEducations = System.currentTimeMillis();
        logger.info("Creating educations...");
        AtomicInteger educationsCreatedCount = new AtomicInteger(0);
        for (int i = 0; i < totalCount; i++) {
            if (studentIds.isEmpty() || groupIds.isEmpty()) break;
            UUID studentId = studentIds.get(i % studentIds.size());
            UUID groupId = groupIds.get(i % groupIds.size());
            if (createEducation(studentId, groupId)) {
                educationsCreatedCount.incrementAndGet();
            }
            if ((i + 1) % (Math.max(1, totalCount / 10)) == 0 ) {
                logger.debug("  Sequentially created {}/{} educations...", (i + 1), totalCount);
            }
        }
        logger.info("Finished creating educations. Target: {}, Actual: {}. Time: {}ms", totalCount, educationsCreatedCount.get(), (System.currentTimeMillis() - startEducations));
        logger.info("Finished sequential generation in {}ms", (System.currentTimeMillis() - startOverall));
    }

    public void generateConcurrent(int totalCount, int numThreads) {
        logger.info("Starting concurrent generation for {} sets of records across {} threads.", totalCount, numThreads);
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        List<Future<?>> futures = new ArrayList<>();

        int countPerThread = totalCount / numThreads;
        int remainder = totalCount % numThreads;

        AtomicInteger totalCoursesCreated = new AtomicInteger(0);
        AtomicInteger totalStudentsCreated = new AtomicInteger(0);
        AtomicInteger totalGroupsCreated = new AtomicInteger(0);
        AtomicInteger totalEducationsCreated = new AtomicInteger(0);
        long startOverall = System.currentTimeMillis();

        for (int i = 0; i < numThreads; i++) {
            final int itemsToProcess = countPerThread + (i < remainder ? 1 : 0);
            if (itemsToProcess == 0) continue;

            final int threadNum = i + 1;
            Callable<Void> task = () -> {
                Faker threadLocalFaker = new Faker();
                logger.info("Thread {} starting, to process {} sets of entities.", threadNum, itemsToProcess);
                long threadStartTime = System.currentTimeMillis();

                List<UUID> threadCourseIds = createCourses(itemsToProcess, threadLocalFaker);
                totalCoursesCreated.addAndGet(threadCourseIds.size());
                logger.debug("Thread {}: created {}/{} courses.", threadNum, threadCourseIds.size(), itemsToProcess);

                List<UUID> threadStudentIds = createStudents(itemsToProcess, threadLocalFaker);
                totalStudentsCreated.addAndGet(threadStudentIds.size());
                logger.debug("Thread {}: created {}/{} students.", threadNum, threadStudentIds.size(), itemsToProcess);

                List<UUID> threadGroupIds = new ArrayList<>();
                if (!threadCourseIds.isEmpty()) {
                    for (int j=0; j < itemsToProcess; j++) {
                        if (threadCourseIds.isEmpty()) break; // Should not happen if itemsToProcess > 0 initially
                        UUID courseId = threadCourseIds.get(j % threadCourseIds.size());
                        UUID groupId = createGroup(courseId, threadLocalFaker);
                        if (groupId != null) {
                            threadGroupIds.add(groupId);
                        }
                    }
                    totalGroupsCreated.addAndGet(threadGroupIds.size());
                    logger.debug("Thread {}: created {} groups (target {}).", threadNum, threadGroupIds.size(), itemsToProcess);
                } else if (itemsToProcess > 0){
                    logger.warn("Thread {}: No courses created by this thread, cannot create groups.", threadNum);
                }

                int educationsInThread = 0;
                if (!threadStudentIds.isEmpty() && !threadGroupIds.isEmpty()) {
                    for (int j = 0; j < itemsToProcess; j++) {
                        if (threadStudentIds.isEmpty() || threadGroupIds.isEmpty()) break;
                        UUID studentId = threadStudentIds.get(j % threadStudentIds.size());
                        UUID groupId = threadGroupIds.get(j % threadGroupIds.size());
                        if (createEducation(studentId, groupId)) {
                            educationsInThread++;
                        }
                    }
                    totalEducationsCreated.addAndGet(educationsInThread);
                    logger.debug("Thread {}: created {} educations (target {}).", threadNum, educationsInThread, itemsToProcess);
                } else if (itemsToProcess > 0) {
                    logger.warn("Thread {}: No students or groups created by this thread, cannot create educations.", threadNum);
                }
                logger.info("Thread {} finished in {}ms. Processed targets: Courses({}), Students({}), Groups({}), Educations({}). Actual Created: {}C, {}S, {}G, {}E.",
                        threadNum, (System.currentTimeMillis() - threadStartTime),
                        itemsToProcess,itemsToProcess,itemsToProcess,itemsToProcess, /* Targets */
                        threadCourseIds.size(), threadStudentIds.size(), threadGroupIds.size(), educationsInThread /* Actuals */);
                return null;
            };
            futures.add(executorService.submit(task));
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("A thread was interrupted during execution.", e);
            } catch (ExecutionException e) {
                logger.error("Error encountered in a worker thread execution.", e.getCause());
            }
        }

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.MINUTES)) {
                logger.warn("Executor did not terminate in 60 minutes. Forcing shutdown.");
                List<Runnable> droppedTasks = executorService.shutdownNow();
                logger.warn("Executor was abruptly shut down. {} tasks were pending.", droppedTasks.size());
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
            logger.warn("Executor termination was interrupted.");
        }

        long duration = System.currentTimeMillis() - startOverall;
        logger.info("Finished all concurrent generation tasks in {}ms.", duration);
        logger.info("Summary: Total Courses Attempted: {}, Actual: {}", totalCount, totalCoursesCreated.get());
        logger.info("Summary: Total Students Attempted: {}, Actual: {}", totalCount, totalStudentsCreated.get());
        logger.info("Summary: Total Groups Attempted: {}, Actual: {}", totalCount, totalGroupsCreated.get());
        logger.info("Summary: Total Educations Attempted: {}, Actual: {}", totalCount, totalEducationsCreated.get());
    }
}