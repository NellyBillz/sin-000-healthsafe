package co.wethinkcode.healthsafe;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public final class HospitalServiceClient {

    private final URI wardServiceUrl;
    private final URI alertLevelUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public HospitalServiceClient(URI wardServiceUrl, URI alertLevelServiceUrl) {
        this.wardServiceUrl = wardServiceUrl;
        this.alertLevelUrl = alertLevelServiceUrl.resolve("/alert-level");
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    public boolean wardExists(String wardId) {
        String encodedWardId = URLEncoder.encode(wardId.trim(), StandardCharsets.UTF_8);
        HttpResponse<String> response = get(wardServiceUrl.resolve("/wards/" + encodedWardId));
        if (response.statusCode() == 404) {
            return false;
        }
        ensureSuccess(response, "ward-service");
        return true;
    }

    public int fetchAlertLevel() {
        HttpResponse<String> response = get(alertLevelUrl);
        ensureSuccess(response, "alert-level-service");
        try {
            return objectMapper.readValue(response.body(), AlertLevelResponse.class).level();
        } catch (IOException exception) {
            throw new DownstreamServiceException(
                    "alert-level-service returned an invalid response", exception);
        }
    }

    private HttpResponse<String> get(URI uri) {
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException exception) {
            throw new DownstreamServiceException("Unable to reach a required service", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new DownstreamServiceException("Service request was interrupted", exception);
        }
    }

    private void ensureSuccess(HttpResponse<String> response, String serviceName) {
        if (response.statusCode() != 200) {
            throw new DownstreamServiceException(
                    serviceName + " returned HTTP " + response.statusCode());
        }
    }

    private record AlertLevelResponse(int level) {
    }
}
