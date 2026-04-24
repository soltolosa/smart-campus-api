# smart-campus-api

## Overview of my API:
For this coursework I have implemented a RESTful Smart Campus API with the JAX-RS framework as required by the specifications. This API was designed to manage rooms and the variety of sensors they may contain while maintaining a historical record of sensor readings, updating the currentValue field in the Sensor object when a new reading is recorded, ensuring consistency across the system.

The API also implements a root Discovery endpoint at GET/api/v1, this returns a JSON response that returns basic information and some HATEOAS links as per the requirements. These include rooms, sensors and sensor readings.

The client will be able to use the following services:

API Discovery Service
Purpose:
This allows a client to discover the API entry point and the available resources.

The endpoint:

GET /api/v1

This confirms that the API is running lists or links to the main resources such as rooms and sensons

Room Management
Purpose:
This allows for room management within the smart campus.

Main functions:

Create rooms
Retrieve rooms
List all rooms
Retrieve rooms by their unique ID
Delete rooms

endpoints:

POST /api/v1/rooms 
GET /api/v1/rooms 
GET /api/v1/rooms/{roomId} 
DELETE /api/v1/rooms/{roomId} 


Sensor Management
Purpose:
This allows for sensor management such as temperature, CO2 ect..

Main functions:

Register sensors to a valid room
Retrieve sensor
List all sensors
Filter sensors by type

endpoints:

POST /api/v1/sensors 
GET /api/v1/sensors 
GET /api/v1/sensors?type={type}
GET /api/v1/sensors/{sensorId} 


Sub‑resource
Purpose:
This handles historical and current readings assigned to sensors.

Main functions:

Add new sensor readings
Retrieve reading history for a specific sensor
Update a sensor’s current value

endpoints:

GET /api/v1/sensors/{sensorId}/readings 
POST /api/v1/sensors/{sensorId}/readings

## Steps to build and launch the server:

Below instructions are for a clean installation from scratch:

Firstly I had to install Maven, downloading the binary package from (https://maven.apache.org/download.cgi)
I used the latest version available: 3.9.15, I followed the instructions to install in a windows machine (unzip the file, add MAVEN_HOME to users Env)

with maven avaiable (I already had Java JDK installed) I enter into a Terminal and donwload my code from github (Location of the code is up to you) 
git clone https://github.com/soltolosa/smart-campus-api.git
cd smart-campus-api
git checkout main
mvn clean install
mvn exec:java 

To Verify

open in a browser: http://localhost:8080/api/v1

{"apiName":"Smart Campus API","contact":"w2081584@westminster.ac.uk","description":"A RESTful API for managing smart campus resources.","resources":{"rooms":"/api/v1/rooms","sensors":"/api/v1/sensors","sensorReadings":"/api/v1/sensors/{sensorId}/readings"},"version":"v1"}

or in terminal use a curl instruction: curl http://localhost:8080/api/v1

StatusCode        : 200
StatusDescription : OK
Content           : {"apiName":"Smart Campus API","contact":"w2081584@westminster.ac.uk","description":"A RESTful API for
                    managing smart campus resources.","resources":{"rooms":"/api/v1/rooms","sensors":"/api/v1/sensors"...
RawContent        : HTTP/1.1 200 OK
                    Content-Length: 272
                    Content-Type: application/json

{"apiName":"Smart Campus API","contact":"w2081584@westminster.ac.uk","description":"A RESTful API for
                    managing smart campus reso...
Forms             : {}
Headers           : {[Content-Length, 272], [Content-Type, application/json]}
Images            : {}
InputFields       : {}
Links             : {}
ParsedHtml        : mshtml.HTMLDocumentClass
RawContentLength  : 272


## Five sample curl commands
Used in CMD
*Discovery:

curl -s http://localhost:8080/api/v1/

{"apiName":"Smart Campus API","contact":"w2081584@westminster.ac.uk","description":"A RESTful API for managing smart campus resources.","resources":{"rooms":"/api/v1/rooms","sensors":"/api/v1/sensors","sensorReadings":"/api/v1/sensors/{sensorId}/readings"},"version":"v1"}


*List all rooms

curl -s http://localhost:8080/api/v1/rooms

[{"id":"ROOM-01","name":"Main Room","capacity":50,"sensorIds":["S-001"]}]


*Create a room

curl -X POST http://localhost:8080/api/v1/rooms ^
 -H "Content-Type: application/json" ^
 -d "{\"id\":\"ROOM-02\",\"name\":\"Main Room\",\"capacity\":150}"

{"id":"ROOM-02","name":"Main Room","capacity":150,"sensorIds":[]}


*Register a Sensor

curl -X POST "http://localhost:8080/api/v1/sensors" -H "Content-Type: application/json" -d "{\"id\":\"S-002\",\"type\":\"CO2\",\"status\":\"ACTIVE\",\"roomId\":\"ROOM-01\",\"currentValue\":0}"

{"id":"S-002","type":"CO2","status":"ACTIVE","currentValue":0.0,"roomId":"ROOM-01"}


*Delete a Room

curl -X DELETE "http://localhost:8080/api/v1/rooms/ROOM-01"

{"error":"Room cannot be deleted because it still has sensors assigned."}


## Answers to CW questions:

### Part 1:

Q1)
In your report, explain the default lifecycle of a JAX‑RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in‑memory data structures (maps/lists) to prevent data loss or race conditions.


The default lifecycle of a JAX-RS Resource class is per‑request which means the a new instance is instantiated for every incoming request, so its not treated as a singleton.
Since each request has its own instance, the fields inside the resource class are not shared and any data stored will be lost after the request is finished. When using in‑memory data structures instead of a databse, data structures such as lists and maps are shared across meaning that multiple threads can modify them and iterate them simultaneously which creates a risk of race condition and data loss, to minimise this risk it is important to use thread‑safe synchrinization such as Collections.synchronizedList.


Q2)
Why is the provision of “Hypermedia” (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?

It is considered a hallmark as users with no prior knowledge of the API's structure  can still navigate its resources easily. Instead of relying on external documentation, client developers can discover available functions directly from the responses via hypermedia links in the responses. This allows clients to navigate the application without needing to memorize URLs or endpoints. The server can also update the said links as the API changes, whis reduces the risk of outdated and incorrect external documentation.


### Part 2:

Q1)
When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client‑side processing.

When returning only IDs, the response is much smaller which reduces network bandwidth. This can be efficient if the client only wants the room IDs, however, it also requires the client to make additional requests if they need more information such as the name and capacity which as a result would increase client-side-processing.
In contrast, returning the full room objects returns all the information on the room at once (much more comprehensive). This means that the client will not have to make additional requests, however, this also increases the network bandwith which can significantly slow down load times especially when theres a large amount of rooms. 


Q2)
Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.

The DELETE operation is idempotent in my implementation. Idempotent means that performing the same operation multiple times has the same final state as performing it once.
In my implementation, when a client sends a DELETE request for a room, the room is removed from the data store and a Room deleted successfuly response is returned. If a client mistakenly sends the same DELETE request, since the room has already been removed, a response stating that the resource no longer exists (404 Not Found) "Room not found" is returned. A third DELETE will also send out the same respone as the room has already been deleted. This is idempotent as the state will remain the same no matter how many DELETE requests are sent out (the room is not found as it has been already deleted after the first DELETE).

Example from my implementation(tested on postman):
1st DELETE: "message": "Room deleted successfully"  (200 OK)
2nd DELETE: "error": "Room not found"  (404 NOT FOUND)
3rd DELETE: "error": "Room not found"  (404 NOT FOUND)
4th DELETE: "error": "Room not found"  (404 NOT FOUND)

### Part 3:

Q1)
We explicitly use the @Consumes (MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX‑RS handle this mismatch?

If a client sends the data in a different format such as text/plain or application/xml JAX‑RS will not find a MessageBodyReader capable of converting that format into the expected Java object. This is because when annotating POST method with @Consumes (MediaType.APPLICATION_JSON), the endpoint will only accept requests with a JSON payload. As a result the request ends up being rejected before even reaching the source method. JAX-RS handles this mismatch by returning a HTTP 415 Unsupported Media Type response to the client. 

Q2)
You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/v1/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?

Using @QueryParam for filtering is superior than having type as part of the URL path because query parameters are the standard way to filter through a collection. Using query params allows the URL to still represent the relevant resource, while the filtering is just additional information. This keeps the API clean and makes it easy to add more filters later if needed. Putting the filter in the path can make it look like a different resource, even if thats not the case and can also make it much harder to combine filters if more complex fileting is required.


### Part 4:

Q1)
Discuss the architectural benefits of the Sub‑Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g.,sensors/{id}/readings/{rid}) in one massive controller class?

Some benefits of the Sub‑Resource Locator pattern are that it helps keep the code clean and is much easier to manage complexity in large APIs. Instead of defining every nested path in a big controller class, the logic is split into smaller classes. This way the API easier to follow since each class focuses on one part of the system, for example, sensor‑reading paths will go in a related class such as SensorReadingResource. This is especially helpful in larger APIs as it is easier to maintain compared to a giant controller class with very mixed responsabilities which is also more prone to erros and harder to modify.


### Part 5:

Q1)

Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?

HTTP 422 is more semantically accurate than a standard 404 in this case since the request is still correct, just incomplete. 404 NOT FOUND means that the resource was not found, this can be very misleading as even if the request was correct but there is a missing reference it will just say that the resource was not found. It is much more transparent to use HTTP 422 as it shows that the request was valid however there is missing/invalid data inside. This way the user can identify what reference was missing or invalid in their request instead of trying to guess the issue with the request.

Q2)
From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?

When an API exposes internal Java stack traces, possible attackers could get important information of the API such as class names, method names, package structures, file paths as well as the exact lines of code where the error happened. This is a very big risk as an attacker can use that information to identify vulnerabilities ine API, and weak points in the code that might exist making it easier for them to attack again and exploit these. For this reason its very important to return 500 Internal Server Error(through a global exception handler) as it censors that information and protects the system.

Q3)
Why is it advantageous to use JAX‑RS filters for cross‑cutting concerns like logging, rather than manually inserting  statements inside every single resource method?


It is better to use  JAX‑RS filters for logging since all the logging can be handled in one place instead of repeating Logger.info() in all the resource methods. This is cleaner as the filter automatically logs all the requests and responses for the API.
If there is a need to change how the logging works, then only the LoggingFilter class will need to be updated instead of changing resource classes. This is much easier to maintain and also minimises the risk of errors. This is also much more consistent and less hassle than manually inserting statements in each resource method.