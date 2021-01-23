package incidentservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Seq;
import org.jooq.lambda.Unchecked;
import org.jooq.lambda.tuple.Tuple;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * An IncidentDao that reads incidents from JSON files on the classpath. This is done for simplicity. In the real world
 * this data would be fetched from a database so we could benefit from indexes. Presumably we'll need to add incidents
 * incidents at some point too, and that's not practical with this approach.
 */
@Repository
@RequiredArgsConstructor
public class JsonResourceIncidentDao implements IncidentDao {
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;

    private static final String INCIDENTS_LOCATION = "classpath:incidents/";

    /**
     * Finds incidents in a given location and within a given time range.
     * @param geoHashes incidents with the given geohash prefix will be returned
     * @param start incidents ending after the given time will be returned
     * @param end incidents beginning before the given time will be returned
     * @return incidents matching all filter parameters
     */
    @Override
    public Iterable<Incident> findIncidents(Collection<String> geoHashes, Instant start, Instant end) {
        try {
            // Note: all kinds of performance implications here from reading and parsing each file on each request.
            // See note above about optimally fetching this data from a DB. In lieu of that, alternatives would be
            // preloading the data (at the expense of memory), caching the parsed Incidents, and possibly memoizing.
            Resource[] incidentResources = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
                    .getResources(INCIDENTS_LOCATION + "*.json");
            return Seq.of(incidentResources)
                    .map(Unchecked.function(
                            resource -> Tuple.tuple(
                                    // Note: normally I'd apply some sort of mapper here (maybe a Jackson mixin class or a MapStruct
                                    // mapper) so that we're not stuck having our model mirror the JSON exactly. This is just for
                                    // expediency.
                                    objectMapper.readValue(resource.getInputStream(), Incident.class),
                                    getIdFromFilename(resource.getFilename())
                            )
                    ))
                    .filter(incidentWithId -> filterIncident(incidentWithId.v1, geoHashes, start, end))
                    // Apply the id (derived from the filename) to the Incident
                    .map(incidentWithId -> incidentWithId.v1.withId(incidentWithId.v2))
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Retrieves one incident by id
     * @param id the incident id
     * @return an Optional incident
     */
    @Override
    public Optional<Incident> findIncident(String id) {
        Resource incidentResource = resourceLoader.getResource(INCIDENTS_LOCATION + id + ".json");
        if (!incidentResource.exists()) {
            return Optional.empty();
        }

        try {
            Incident incident = objectMapper.readValue(incidentResource.getInputStream(), Incident.class);
            return Optional.of(incident.withId(id));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static boolean filterIncident(Incident incident, Collection<String> geoHashes, Instant start, Instant end) {
        Instant eventClosed = incident.getDescription().getEventClosed().toInstant();
        Instant eventOpened = incident.getDescription().getEventOpened().toInstant();
        String addressGeoHash = incident.getAddress().getGeohash();
        Set<String> defaultedGeoHashes = Seq.seq(geoHashes)
                .map(geoHash -> StringUtils.defaultString(geoHash, ""))
                .toSet();
        Instant defaultedStart = Objects.requireNonNullElse(start, Instant.MIN);
        Instant defaultedEnd = Objects.requireNonNullElse(end, Instant.MAX);

        // Look for any overlap between the event time window and the requested time window. Also check overlap with
        // the requested geoHash.
        // Note that we do !instant.isBefore(...) as opposed to instant.isAfter(...) to get an inclusive check.
        boolean isIncluded = !eventClosed.isBefore(defaultedStart) &&
                !eventOpened.isAfter(defaultedEnd) &&
                (defaultedGeoHashes.isEmpty() || Seq.seq(defaultedGeoHashes).anyMatch(addressGeoHash::startsWith));
        return isIncluded;
    }

    private static String getIdFromFilename(String filename) {
        // Strip the ".json". Wouldn't be the worst to double check that there is a ".json", but we should be safe.
        return filename.substring(0, filename.length() - 5);
    }

}
