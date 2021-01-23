# Incidents Service

## Building
From the project root, `./gradlew clean build`

## Running
From the project root, `./gradlew bootRun`

Navigate to http://localhost:8080/swagger-ui.html to explore and invoke the API.

* username: incidents
* password: incidents-password

## Running tests
From the project root, `./gradlew clean test`

## Adding more incidents
1. Drop a new .json file (the extension is important) into src/main/resources/incidents.
2. Rebuild.

Note that integration tests are currently using this same data, so adding new incidents is likely to break
tests.

## Notes
* This probably took about 6.5 hours, plus maybe an hour writing these notes and doing mostly
  cosmetic code cleanup.
* As noted in the code, in the real world, I would have employed a database to store the
  incidents. In this case, I didn't want to spend the time on the setup, including defining
  the schema. Using a schemaless DB might have been a good compromise; however, I usually
  tend to stick to an RDBMS for a new project, unless there's a strong reason otherwise.
  (I feel that usually an RDBMS is going to be a solid, reliable option that's less likely
  to come back to bite you in the long term, compared to many NoSQL options, but that's a
  generalization.)
* For a real, production deployment, I'd spend more time thinking about how we fetch data
  from the weather service. Right now we fetch on demand as incidents are requested. This
  has the benefit of ensuring the weather data is fresh, but it has a number of downsides:
  * Makes us more susceptible to outages from the weather service
  * Making repeat weather queries for the same incident consumes more of our limited requests
    to the weather service (2000 per day, no more than 2 per second).
  * If the weather service doesn't keep historical weather reports indefinitely, we could lose
    access to weather info for older incidents.
    
  A few alternatives to the current implementation:
    * Caching weather info, possibly with a TTL if there are concerns about the weather reports
      becoming stale. The Spring Cache abstraction makes adding caching unobtrusive and makes it
      easier to switch cache implementations with minimal code change.
    * Fetching weather info and storing it when incidents are added. This probably isn't the best
      option if we're concerned about staleness.
    * Periodically batch-fetching and storing weather info. This provides a great deal of flexibility,
      allowing us to control our usage of the weather resource carefully. It also provides the easiest
      way to recover from outages/errors with the service, since presumably those incidents can be
      reprocessed in the next batch. Downside is that weather info is not immediately available for a
      newly added incident.
    * A hybrid approach can mitigate downsides, but adds more complexity to the system.
* Some TODOs:
  * The default OpenAPI docs are barely sufficient. It's possible to annotate the Spring Controller
    to provide more detailed information. This annotation-based approach clutters the code a bit, but
    has the benefit of keeping the documentation in the same place as the code, increasing the likelihood
    that it will actually get updated when the API gets changed.
  * Auth is insufficient for a production system. I'm a fan of products like Keycloak and Auth0.
  * A better package structure would make things more readable. There aren't so many classes yet that the
    single package is unmanageable, but it's close.
  * We'd want CI configuration (e.g. a Jenkinsfile)
  * I'd expect to provide a Dockerfile and likely k8s configuration, docker-compose files for easy local development
  * I usually like to add some forms of static analysis/linting to the build
  * I'm usually more consistent about providing interface classes in the service and persistence layers.
  * Tests are thin. Normally it's pointless to unit test a DAO, since it's typically a database doing all
    the work, but in this case it's totally feasible to unit test the JsonResourceIncidentDao. 
  * Integration tests being stuck using the "production" incident data is a problem. Normally there would be a test
    fixture to reset the data in between tests, and we'd load appropriate data in the setup phase of each test. The
    resource file based approach for storing incident data makes this a hassle to do, and I haven't taken the time
    to mock the `ResourceLoader`.
  * Health checks and diagnostic info are easily added with Spring Actuator
  * The code is light on comments in some places
  * Typically, I like to add HATEOAS links to REST responses. Spring HATEOAS makes this pretty painless.
  
## Alternate approaches
This is a fairly simplistic approach. I didn't go down other paths out of concern for producing a functional
result in a reasonable amount of time. Some other approaches that have occurred to me:
* I believe some API Gateways have capabilities to make fuse and transform responses from multiple upstream
  services. I haven't experimented with these features, but _sounds_ like something that would likely be messy
  to configure in the context of an API Gateway, as opposed to being able to apply all the tools that are
  available from a general-purpose programming language. (Of course, something like Zuul does give you a full,
  general-purpose programming language.)
* Fetching data from multiple APIs and stitching it together is one use case for GraphQL. If I'd picked a GraphQL
  weather API and created a GraphQL API for Incidents, the schemas could potentially be integrated, and clients
  could seamlessly fetch data from both with a single request. I've been curious to experiment with this feature
  of GraphQL, but I decided this wasn't the time to give it a shot for the first time.

## Screenshots
Screenshots showing querying the API through Swagger UI:
![Screenshot 1](Incident%20service%20screen%20shot%201.png)
![Screenshot 2](Incident%20service%20screen%20shot%202.png)
![Screenshot 3](Incident%20service%20screen%20shot%203.png)
![Screenshot 4](Incident%20service%20screen%20shot%204.png)