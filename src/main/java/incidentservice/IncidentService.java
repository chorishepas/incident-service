package incidentservice;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

/**
 * Service layer for Incidents. Typically there would be some sort of business logic here, but as
 * this service is all CRUD (well, just "R") at the moment, it's just delegating to the DAO.
 */
@Service
@RequiredArgsConstructor
public class IncidentService {
    private final IncidentDao incidentDao;

    // TODO: other filter parameters
    public Iterable<Incident> findIncidents(Collection<String> geoHashes, Instant start, Instant end) {
        return incidentDao.findIncidents(geoHashes, start, end);
    }

    public Optional<Incident> findIncident(String id) {
        return incidentDao.findIncident(id);
    }
}
