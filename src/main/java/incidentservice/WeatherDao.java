package incidentservice;

import java.time.Instant;
import java.util.Collection;

public interface WeatherDao {
    Collection<Weather> getWeather(double latitude, double longitude, Instant start, Instant end);
}
