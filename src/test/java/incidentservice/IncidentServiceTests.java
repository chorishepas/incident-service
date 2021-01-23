package incidentservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class IncidentServiceTests {

    @Autowired
    private MockMvc mockMvc;

    private static final String USER = "incidents";
    private static final String PASSWORD = "incidents-password";

    // TODO: moving JSON strings into resource files would declutter these tests and make the JSON, itself,
    // easier to read (no need for escapes)

    private static final String EXPECTED_FIRST_INCIDENT =
            "  {" +
            "    \"id\": \"F01705150050\"," +
            "    \"eventOpened\": \"2017-05-15T17:19:12Z\"," +
            "    \"eventClosed\": \"2017-05-15T18:46:46Z\"," +
            "    \"latitude\": 37.541885," +
            "    \"longitude\": -77.440624," +
            "    \"addressLine1\": \"333 E FRANKLIN ST\"," +
            "    \"city\": \"Richmond\"" +
            "  }";

    private static final String EXPECTED_SECOND_INCIDENT =
            "  {" +
            "    \"id\": \"F01705150090\"," +
            "    \"eventOpened\": \"2017-05-16T00:16:18Z\"," +
            "    \"eventClosed\": \"2017-05-16T00:32:38Z\"," +
            "    \"latitude\": 37.466513," +
            "    \"longitude\": -77.428683," +
            "    \"addressLine1\": \"4301 COMMERCE RD\"," +
            "    \"city\": \"Richmond\"" +
            "  }";

    @Test
    public void shouldListAllIncidents() throws Exception {
        // given a request with no filters
        String url = "/incidents";

        // when
        ResultActions response = this.mockMvc.perform(get(url).with(user(USER).password(PASSWORD)));

        // then both incidents should be returned
        response.andExpect(status().isOk())
                .andExpect(content().json("[" +
                        EXPECTED_FIRST_INCIDENT +
                        "," +
                        EXPECTED_SECOND_INCIDENT +
                        "]"));
    }

    @Test
    public void shouldListIncidentsFilteredByStart() throws Exception {
        // given a start date/time immediately after the end of the first incident
        String url = "/incidents?start=2017-05-15T18:46:47Z";

        // when
        ResultActions response = this.mockMvc.perform(get(url).with(user(USER).password(PASSWORD)));

        // then only the later incident should be returned
        response.andExpect(status().isOk())
                .andExpect(content().json("[" + EXPECTED_SECOND_INCIDENT + "]"));
    }

    @Test
    public void shouldListIncidentsFilteredByEnd() throws Exception {
        // given a end date/time immediately before the beginning of the second incident
        String url = "/incidents?end=2017-05-16T00:16:17Z";

        // when
        ResultActions response = this.mockMvc.perform(get(url).with(user(USER).password(PASSWORD)));

        // then only the earlier incident should be returned
        response.andExpect(status().isOk())
                .andExpect(content().json("[" + EXPECTED_FIRST_INCIDENT + "]"));
    }

    @Test
    public void shouldListIncidentsFilteredByGeohash() throws Exception {
        // given a geohash that includes the first incident, but not the second
        String url = "/incidents?geohash=dq8vt";

        // when
        ResultActions response = this.mockMvc.perform(get(url).with(user(USER).password(PASSWORD)));

        // then only the first incident should be returned
        response.andExpect(status().isOk())
                .andExpect(content().json("[" + EXPECTED_FIRST_INCIDENT + "]"));
    }

    @Test
    public void shouldListIncidentsFilteredByMultipleGeohashes() throws Exception {
        // given two geohashes, each of which includes one of the incidents
        String url = "/incidents?geohash=dq8vt&geohash=dq8vn";

        // when
        ResultActions response = this.mockMvc.perform(get(url).with(user(USER).password(PASSWORD)));

        // then both incidents should be returned
        response.andExpect(status().isOk())
                .andExpect(content().json("[" +
                        EXPECTED_FIRST_INCIDENT +
                        "," +
                        EXPECTED_SECOND_INCIDENT +
                        "]"));
    }

    @Test
    public void shouldReturnEmptyArrayWhenNoIncidentsAreFound() throws Exception {
        // given a geohash that doesn't match any incident
        String url = "/incidents?geohash=x7";

        // when
        ResultActions response = this.mockMvc.perform(get(url).with(user(USER).password(PASSWORD)));

        // then empty results should be returned
        response.andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void shouldFindIncidentDetailsById() throws Exception {
        // given a request for incident details by id
        String url = "/incidents/F01705150050";

        // when
        ResultActions response = this.mockMvc.perform(get(url).with(user(USER).password(PASSWORD)));

        // then details for the appropriate incident should be returned
        response.andExpect(status().isOk())
                .andExpect(content().json("{" +
                        "\"id\":\"F01705150050\"," +
                        "\"eventOpened\":\"2017-05-15T17:19:12Z\"," +
                        "\"eventClosed\":\"2017-05-15T18:46:46Z\"," +
                        "\"latitude\":37.541885," +
                        "\"longitude\":-77.440624," +
                        "\"addressLine1\":\"333 E FRANKLIN ST\"," +
                        "\"city\":\"Richmond\"," +
                        "\"comments\":\"** LOI search completed at 05/15/17 13:19:12 SPECIAL ADDRESS COMMENT: ***RFD: TARGET HAZARD*** ** Case number C201713827 has been assigned to event F01705150050 ** >>>> by: NANCY L. MOREY on terminal: ecc-f1 OLD BOX OF CHEMICALS WANTS IT TO BE CHECKED OUT *****************TAC 3******************* T22/H2/H3 OS - LT FROM T22 HAS CMD\"" +
                        "}"));
    }

    @Test
    void shouldIncludeWeatherDataWithDetails() throws Exception {
        // given a request for incident details by id
        String url = "/incidents/F01705150050";

        // when
        ResultActions response = this.mockMvc.perform(get(url).with(user(USER).password(PASSWORD)));

        // then the details should include weather data
        response.andExpect(status().isOk())
                .andExpect(content().json("{\"weather\":[" +
                        "{\"temperature\":24.1,\"dewPoint\":11.5,\"time\":\"2017-05-15T00:00:00Z\"}," +
                        "{\"temperature\":21.2,\"dewPoint\":10.9,\"time\":\"2017-05-15T01:00:00Z\"}," +
                        "{\"temperature\":21.0,\"dewPoint\":11.1,\"time\":\"2017-05-15T02:00:00Z\"}," +
                        "{\"temperature\":21.1,\"dewPoint\":10.8,\"time\":\"2017-05-15T03:00:00Z\"},{" +
                        "\"temperature\":20.8,\"dewPoint\":10.7,\"time\":\"2017-05-15T04:00:00Z\"}," +
                        "{\"temperature\":19.9,\"dewPoint\":9.0,\"time\":\"2017-05-15T05:00:00Z\"}," +
                        "{\"temperature\":19.3,\"dewPoint\":7.3,\"time\":\"2017-05-15T06:00:00Z\"}," +
                        "{\"temperature\":17.9,\"dewPoint\":4.1,\"time\":\"2017-05-15T07:00:00Z\"}," +
                        "{\"temperature\":17.0,\"dewPoint\":0.5,\"time\":\"2017-05-15T08:00:00Z\"}," +
                        "{\"temperature\":16.1,\"dewPoint\":-0.3,\"time\":\"2017-05-15T09:00:00Z\"}," +
                        "{\"temperature\":15.2,\"dewPoint\":-0.8,\"time\":\"2017-05-15T10:00:00Z\"}," +
                        "{\"temperature\":13.5,\"dewPoint\":3.2,\"time\":\"2017-05-15T11:00:00Z\"}," +
                        "{\"temperature\":15.8,\"dewPoint\":1.8,\"time\":\"2017-05-15T12:00:00Z\"}," +
                        "{\"temperature\":17.2,\"dewPoint\":2.1,\"time\":\"2017-05-15T13:00:00Z\"}," +
                        "{\"temperature\":18.9,\"dewPoint\":0.8,\"time\":\"2017-05-15T14:00:00Z\"}," +
                        "{\"temperature\":20.2,\"dewPoint\":2.0,\"time\":\"2017-05-15T15:00:00Z\"}," +
                        "{\"temperature\":21.1,\"dewPoint\":1.6,\"time\":\"2017-05-15T16:00:00Z\"}," +
                        "{\"temperature\":23.1,\"dewPoint\":2.2,\"time\":\"2017-05-15T17:00:00Z\"}," +
                        "{\"temperature\":24.2,\"dewPoint\":1.4,\"time\":\"2017-05-15T18:00:00Z\"}," +
                        "{\"temperature\":25.1,\"dewPoint\":1.0,\"time\":\"2017-05-15T19:00:00Z\"}," +
                        "{\"temperature\":25.3,\"dewPoint\":0.9,\"time\":\"2017-05-15T20:00:00Z\"}," +
                        "{\"temperature\":25.8,\"dewPoint\":0.8,\"time\":\"2017-05-15T21:00:00Z\"}," +
                        "{\"temperature\":26.1,\"dewPoint\":1.8,\"time\":\"2017-05-15T22:00:00Z\"}," +
                        "{\"temperature\":25.2,\"dewPoint\":4.5,\"time\":\"2017-05-15T23:00:00Z\"}]}"));
    }

    @Test
    public void shouldReturn404WhenNoIncidentDetailsAreFound() throws Exception {
        // given a request for an incident that doesn't exist
        String url = "/incidents/none";

        // when
        ResultActions response = this.mockMvc.perform(get(url).with(user(USER).password(PASSWORD)));

        // then we should get a 404 response status
        response.andExpect(status().isNotFound());
    }

}
