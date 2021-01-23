package incidentservice;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;

/**
 * Service layer for Weather. Typically there would be some sort of business logic here, but as
 * this service is only fetching weather reports at the moment, it's just delegating to the DAO.
 */
@Service
@RequiredArgsConstructor
public class WeatherService {
    private final WeatherDao weatherDao;

    Collection<Weather> getWeather(double latitude, double longitude, Instant start, Instant end) {
        return weatherDao.getWeather(latitude, longitude, start, end);
    }
}
