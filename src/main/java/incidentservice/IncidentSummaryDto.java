package incidentservice;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;

@Value
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IncidentSummaryDto {
    String id;
    OffsetDateTime eventOpened;
    OffsetDateTime eventClosed;
    Double latitude;
    Double longitude;
    String addressLine1;
    String city;
}
