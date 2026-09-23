package co.wethinkcode.healthsafe;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public final class IngestionClient {

    private final URI wardsUri;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public IngestionClient(URI ingestionServiceUrl) {
        this.wardsUri = ingestionServiceUrl.resolve("/wards");
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public List<WardRecord> fetchWards() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(wardsUri)
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Ingestion service returned HTTP " + response.statusCode());
        }

        return objectMapper.readValue(response.body(), new TypeReference<>() { });
    }
}
