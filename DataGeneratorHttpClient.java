import com.github.javafaker.Faker;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.*;
import java.util.stream.IntStream;
import java.util.UUID;

public class DataGeneratorHttpClient {

    private static final String BASE_URL = "http://localhost:8080/api";
    private final RestTemplate restTemplate;
    private final Faker faker;

    public DataGeneratorHttpClient() {
        this.restTemplate = new RestTemplate();
        this.faker = new Faker();
    }

    public static void main(String[] args) {
        DataGeneratorHttpClient client = new DataGeneratorHttpClient();

        // Generate sample courses
        List<UUID> courseIds = client.createCourses(5);

        // Generate sample groups for each course
        Map<UUID, UUID> groupIds = new HashMap<>();
        courseIds.forEach(courseId -> {
            UUID groupId = client.createGroup(courseId);
            groupIds.put(courseId, groupId);
        });

        // Generate sample students
        List<UUID> studentIds = client.createStudents(10);

        // Enroll each student in a random group
        studentIds.forEach(studentId -> {
            UUID groupId = new ArrayList<>(groupIds.values())
                    .get(faker.random().nextInt(groupIds.size()));
            client.createEducation(studentId, groupId);
        });
    }

    public List<UUID> createCourses(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> {
                    UUID id = UUID.randomUUID();
                    Map<String, Object> body = new HashMap<>();
                    body.put("name", faker.educator().course());
                    body.put("description", faker.lorem().sentence());
                    body.put("createdAt", Instant.now().toString());

                    HttpEntity<Map<String, Object>> request = buildRequest(body);
                    ResponseEntity<Map> response = restTemplate.postForEntity(
                            BASE_URL + "/courses", request, Map.class);
                    System.out.println("Created Course: " + response.getBody());
                    return UUID.fromString((String) response.getBody().get("id"));
                })
                .toList();
    }

    public UUID createGroup(UUID courseId) {
        UUID id = UUID.randomUUID();
        Map<String, Object> body = new HashMap<>();
        body.put("name", faker.lorem().word() + " Group");
        body.put("courseId", courseId.toString());
        body.put("schedule", List.of("Mon-Wed-Fri", "Tue-Thu"));

        HttpEntity<Map<String, Object>> request = buildRequest(body);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                BASE_URL + "/groups", request, Map.class);
        System.out.println("Created Group: " + response.getBody());
        return UUID.fromString((String) response.getBody().get("id"));
    }

    public List<UUID> createStudents(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> {
                    UUID id = UUID.randomUUID();
                    Map<String, Object> body = new HashMap<>();
                    body.put("firstName", faker.name().firstName());
                    body.put("lastName", faker.name().lastName());
                    body.put("email", faker.internet().emailAddress());
                    body.put("registeredAt", Instant.now().toString());

                    HttpEntity<Map<String, Object>> request = buildRequest(body);
                    ResponseEntity<Map> response = restTemplate.postForEntity(
                            BASE_URL + "/students", request, Map.class);
                    System.out.println("Created Student: " + response.getBody());
                    return UUID.fromString((String) response.getBody().get("id"));
                })
                .toList();
    }

    public void createEducation(UUID studentId, UUID groupId) {
        Map<String, Object> body = new HashMap<>();
        body.put("studentId", studentId.toString());
        body.put("groupId", groupId.toString());
        body.put("enrolledAt", Instant.now().toString());

        HttpEntity<Map<String, Object>> request = buildRequest(body);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                BASE_URL + "/educations", request, Map.class);
        System.out.println("Created Education: " + response.getBody());
    }

    private HttpEntity<Map<String, Object>> buildRequest(Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }
}