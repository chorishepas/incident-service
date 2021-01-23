package incidentservice;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

public interface IncidentDao {
    // TODO: other filter parameters
    Iterable<Incident> findIncidents(Collection<String> geoHashes, Instant start, Instant end);
    Optional<Incident> findIncident(String id);
}
