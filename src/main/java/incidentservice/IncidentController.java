package incidentservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.Seq;
import org.jooq.lambda.function.Function2;
import org.jooq.lambda.tuple.Tuple;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Past;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(path = "/incidents", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Validated
@Slf4j
public class IncidentController {

    private final IncidentService incidentService;
    private final WeatherService weatherService;
    private final IncidentMapper incidentMapper;

    // TODO: other filter parameters
    // TODO: paging
    // TODO: probably other reasonable input validation, like end > start
    @GetMapping
    public Iterable<IncidentSummaryDto> findIncidents(
            @RequestParam(required = false, name = "geohash") Set<String> geoHashes,
            @Past @RequestParam(required = false) Instant start,
            @Past @RequestParam(required = false) Instant end
    ) {
        Iterable<Incident> incidents = incidentService.findIncidents(geoHashes, start, end);
        Seq<IncidentSummaryDto> incidentDtos = Seq.seq(incidents).map(incidentMapper::toIncidentSummaryDto);

        // TODO: add links via Spring HATEOAS

        return incidentDtos;
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncidentDetailDto> findIncident(@PathVariable String id) {
        // TODO: add links via Spring HATEOAS
        ResponseEntity<IncidentDetailDto> response = incidentService.findIncident(id)
                // fetch weather data and pair with incident
                .map(incident -> Tuple.tuple(incident, fetchWeather(incident)))
                .map(Function2.from(incidentMapper::toIncidentDetailDto)::apply)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        return response;
    }

    private Collection<Weather> fetchWeather(Incident incident) {
        try {
            return weatherService.getWeather(
                    incident.getAddress().getLatitude(),
                    incident.getAddress().getLongitude(),
                    incident.getDescription().getEventOpened().toInstant(),
                    incident.getDescription().getEventClosed().toInstant()
            );
        } catch (Exception e) {
            // Normally I don't favor broadly catching Exception. It's easy for exceptions that you might actually want
            // to handle individually to fall in, and it's likely to go unnoticed. In this instance, I've taken some
            // shortcuts with unchecked exceptions in the weather service/DAO, so this approach is somewhat necessary,
            // as we don't want the whole Incident request failing because of a failure to connect to the weather
            // service. Ultimately, a better approach to consider is declaring one or more checked exceptions that can
            // be handled specifically.
            log.error("Error while fetching weather info for incident " + incident.getId(), e);
            return List.of();
        }
    }
}
