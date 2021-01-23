package incidentservice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UncheckedIOException;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

@Service
public class MeteoStatWeatherDao implements WeatherDao {

    private final String apiKey;
    private final String url;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public MeteoStatWeatherDao(
            @Value("${meteostat-api.api-key}") String apiKey,
            @Value("${meteostat-api.url}") String url,
            RestTemplateBuilder restTemplateBuilder,
            ObjectMapper objectMapper
    ) {
        this.apiKey = apiKey;
        this.url = url;
        this.restTemplate = restTemplateBuilder.build();
        this.objectMapper = objectMapper;
    }

    @Override
    public Collection<Weather> getWeather(double latitude, double longitude, Instant start, Instant end) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneId.from(ZoneOffset.UTC));
        URI uri = UriComponentsBuilder.fromUriString(url)
                .queryParam("lat", Double.toString(latitude))
                .queryParam("lon", Double.toString(longitude))
                .queryParam("start", dateTimeFormatter.format(start))
                .queryParam("end", dateTimeFormatter.format(end))
                .build()
                .toUri();

        String weatherJson;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-api-key", apiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            weatherJson = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class).getBody();
        } catch (RestClientException e) {
            // TODO: proper error handling
            throw e;
        }

        try {
            MeteoStatResponseDto response = objectMapper.readValue(weatherJson, MeteoStatResponseDto.class);
            // Note that the weather service doesn't seem to return partial-day data, so we'll tend to get weather info
            // before and after the requested window. This might actually be useful information, so I'm leaving that
            // extra data in. It could easily be filtered here, though.
            return response.data;
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    @lombok.Value
    @Builder(toBuilder = true)
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class MeteoStatResponseDto {
        Collection<Weather> data;
    }
}
