package incidentservice;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.Collection;

// TODO: add the rest of the fields
@Value
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IncidentDetailDto {
    @JsonUnwrapped
    IncidentSummaryDto summary;
    String comments;
    Collection<WeatherDto> weather;

    @Value
    @Builder(toBuilder = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class WeatherDto {
        Float temperature;
        Float dewPoint;
        Instant time;
    }
}
