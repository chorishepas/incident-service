package incidentservice;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collection;

@Mapper
public abstract class IncidentMapper {
    @Mapping(source = "description.eventOpened", target = "eventOpened")
    @Mapping(source = "description.eventClosed", target = "eventClosed")
    @Mapping(source = "address.latitude", target = "latitude")
    @Mapping(source = "address.longitude", target = "longitude")
    @Mapping(source = "address.addressLine1", target = "addressLine1")
    @Mapping(source = "address.city", target = "city")
    public abstract IncidentSummaryDto toIncidentSummaryDto(Incident incident);

    @Mapping(source = "incident.description.comments", target = "comments")
    @Mapping(source = "incident", target = "summary")
    // Seems like a bug in MapStruct, but it generates invalid code without this mapping
    @Mapping(source = "weather", target = "weather")
    public abstract IncidentDetailDto toIncidentDetailDto(Incident incident, Collection<Weather> weather);
}
