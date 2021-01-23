package incidentservice;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

@Value
@Builder(toBuilder = true)
// Normally wouldn't want these Jackson annotations cluttering the model, plus the @JsonProperty
// annotations below wouldn't be correct for another implementation of WeatherService using a
// different weather API. This is purely a shortcut to save time.
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class Weather {
    // TODO: add whatever additional fields are of interest
    @JsonProperty("temp")
    Float temperature;

    @JsonProperty("dwpt")
    Float dewPoint;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    Instant time;
}
