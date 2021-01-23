package incidentservice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

import java.time.OffsetDateTime;

// TODO: add all other fields
@Value
@Builder(toBuilder = true)
@With
// Normally I'd avoid all these Jackson annotations cluttering the model. See related
// comments in DAO.
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class Incident {
    String id;
    Description description;
    Address address;

    @Value
    @Builder(toBuilder = true)
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Description {
        @JsonProperty("event_opened")
        OffsetDateTime eventOpened;
        @JsonProperty("event_closed")
        OffsetDateTime eventClosed;
        String comments;
    }

    @Value
    @Builder(toBuilder = true)
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Address {
        String geohash;
        Double latitude;
        Double longitude;
        @JsonProperty("address_line1")
        String addressLine1;
        String city;
    }
}
