# smart-campus-api

## Overview of my API:
For this coursework I have implemented a RESTful Smart Campus API with the JAX-RS framework
as required by the specifications. 


Functionally, This API was designed to manage rooms  for a building in the Campus, each room could have one or more Sensors and those sensor will produce readings, historical data is requested to be kept for a sensor, a sensor has a current value.
User will be able to use the following services:

1. API Discovery Service
Purpose:
Allows a client to discover the API entry point and available top‑level resources.
Typical endpoint:

GET /api/v1

What it provides:

Confirms the API is running
Lists or links to key resources (rooms, sensors)
Acts as a HATEOAS‑style root endpoint


2. Room Management Service
Purpose:
Manages physical rooms in the campus (labs, lecture halls, offices, etc.).
Core capabilities:

Create rooms
Retrieve room details
List all rooms
Delete rooms (with safety checks)

Typical endpoints:

POST /api/v1/rooms → create a room
GET /api/v1/rooms → list all rooms
GET /api/v1/rooms/{roomId} → get a specific room
DELETE /api/v1/rooms/{roomId} → remove a room

Deletion is blocked if sensors are still assigned


3. Sensor Management Service
Purpose:
Manages IoT sensors deployed in rooms (temperature, CO₂, occupancy, etc.).
Core capabilities:

Register sensors
Retrieve sensor details
List all sensors
Filter sensors by type
Validate room–sensor relationships

Typical endpoints:

POST /api/v1/sensors → register a new sensor
GET /api/v1/sensors → list all sensors
GET /api/v1/sensors?type={type} → filter by sensor type
GET /api/v1/sensors/{sensorId} → get sensor details


4. Sensor Reading Service (Sub‑resource)
Purpose:
Handles historical and real‑time readings produced by sensors.
This is a nested (sub‑resource) service, meaning it only exists in the context of a sensor.
Core capabilities:

Add new sensor readings
Retrieve reading history for a sensor
Automatically update a sensor’s current value

Typical endpoints:

GET /api/v1/sensors/{sensorId}/readings → get reading history
POST /api/v1/sensors/{sensorId}/readings → add a new reading


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

I could Open in a browser: http://localhost:8080/api/v1

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
*List all rooms
curl -s http://localhost:8080/api/v1/rooms
*Create a room
curl -X POST http://localhost:8080/api/v1/rooms ^
 -H "Content-Type: application/json" ^
 -d "{\"id\":\"ROOM-01\",\"name\":\"Main Room\",\"capacity\":50}"
*Register a Sensor
curl -X POST "http://localhost:8080/api/v1/sensors" -H "Content-Type: application/json" -d "{\"id\":\"S-001\",\"type\":\"CO2\",\"status\":\"ACTIVE\",\"roomId\":\"ROOM-01\",\"currentValue\":0}"
*Delete a Room
curl -X DELETE "http://localhost:8080/api/v1/rooms/ROOM-01"


## Answers to CW questions:

### Part 1:

Q1)
In your report, explain the default lifecycle of a JAX‑RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in‑memory data structures (maps/lists) to prevent data loss or race conditions.


In JAX‑RS, the default lifecycle of a Resource class is per‑request. This means the framework creates a new instance of the resource every time a request comes in. It is not treated as a singleton unless we explicitly configure it that way.
Because each request gets its own instance, the fields inside the resource class are not shared, so we don’t need to worry about synchronizing them.
However, the moment we use shared in‑memory data structures — like a static Map, a shared List, or any object stored outside the resource instance — things change. Those structures are shared across multiple requests, which means multiple threads can modify them at the same time. That’s where race conditions and data loss can happen.
To avoid these problems, we need to use thread‑safe collections (like ConcurrentHashMap) or add proper synchronization when updating shared data.



Q2)
Why is the provision of “Hypermedia” (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?

In our Smart‑Campus API, we expose several resources such as RoomResource, SensorResource, SensorReadingResource, and DiscoveryResource. When we talk about Hypermedia in REST (HATEOAS), the idea is that these resources shouldn’t just return raw JSON. They should also include links that guide the client toward the next valid actions.
For example, when the API returns a Room resource, a hypermedia‑driven response could include links like:
- a link to view the sensors installed in that room
- a link to get the latest sensor readings
- a link to update the room’s information
- a link to navigate back to the discovery endpoint
Similarly, a SensorResource response could include links to:
- fetch the sensor’s current status
- retrieve its historical readings
- navigate to the room the sensor belongs to
This makes the API self‑describing. Instead of relying only on static documentation, client developers can discover available operations directly from the responses. This is especially useful in our project because the Smart‑Campus system has interconnected resources (rooms → sensors → readings). Hypermedia helps clients move through these relationships naturally, without memorizing URLs or guessing endpoints.
As the API evolves, the server can update the links it provides, and clients automatically stay aligned with the current behavior. This reduces breakage and makes integrations more robust.


### Part 2:

Q1)
When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client‑side processing.

When our API returns a list of rooms, we can choose to return only the room IDs or the full room objects, and each option has different implications.
If we return only the IDs, the response is much smaller. This reduces network bandwidth, which is useful when the client only needs to know which rooms exist. The client can then request full details only for the rooms it actually cares about. This approach is efficient but requires the client to make additional requests if it needs more information.
On the other hand, returning the full room objects gives the client everything in one response. This avoids extra API calls, but it increases the payload size, especially if each room contains nested data like sensors or readings. Larger responses use more bandwidth and require more client‑side processing, which can slow things down on low‑power devices.
In short:
- IDs only → lighter, faster, but requires more requests
- Full objects → fewer requests, but heavier and more expensive to send and process


Q2)
Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.

Yes — in our Smart‑Campus API, the DELETE operation is idempotent. Idempotent means that performing the same operation multiple times has the same final effect as performing it once.
In our implementation, when a client sends a DELETE request for a room, the room is removed from the data store. If the client accidentally sends the exact same DELETE request again, nothing breaks. The room is already gone, so the API simply returns a response indicating that the resource no longer exists or that there is nothing left to delete. Importantly, the second (or third) DELETE does not cause additional changes or errors — the system’s state stays the same.
This behavior is what makes DELETE idempotent:
- First DELETE: removes the room
- Next DELETEs: do nothing but also do not cause inconsistent state
The final outcome is identical no matter how many times the DELETE request is repeated.
Example:

1st DELETE /rooms/room-001 → 200 OK (deleted room)
2nd DELETE /rooms/room-001 → 200 OK (already deleted)
3rd DELETE /rooms/room-001 → 200 OK (already deleted)

### Part 3:

Q1)
We explicitly use the  annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as  or . How does JAX‑RS handle this mismatch.

When we annotate a POST method with , we are telling JAX‑RS that this endpoint only accepts JSON input. This means the server expects the request body to be sent with the header .
If a client sends the data in a different format — for example  or  — JAX‑RS cannot find a matching MessageBodyReader capable of converting that format into the Java object the method expects. Because of this mismatch, the framework rejects the request before it even reaches our method.

Q2)
You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/v1/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?

Using  for filtering (like ) is better than putting the filter in the URL path (like ) because query parameters are the standard way to filter or search through a collection.
With query parameters, the main URL  still represents the whole list of sensors, and the filters are just optional details. This keeps the API clean and makes it easy to add more filters later, such as .
If we put the filter in the path, it starts to look like a completely different resource, even though it’s not. It also becomes harder to combine multiple filters.
So in short:
• 	Query parameters are flexible, clean, and made for filtering.
• 	Path filters make the API messy and harder to extend.


### Part 4:

Q1)
Discuss the architectural benefits of the Sub‑Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., ) in one massive controller class?

In our Smart‑Campus API, the Sub‑Resource Locator pattern helps keep the code clean and easier to manage. Instead of putting every nested path inside one huge controller class, we split the logic into smaller classes. For example, sensor‑related paths go into one class, and sensor‑reading paths go into another.
This makes the API easier to understand because each class focuses on one part of the system. It also keeps the code from becoming a giant file full of mixed responsibilities. As the API grows, this structure helps us avoid confusion and makes it easier to maintain, test, and extend the project.
So basically:
- Each resource gets its own class.
- The code stays organized and readable.
- The API is easier to scale as it gets bigger.


### Part 5:

Q1)

Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?

In this situation, the client sends a valid JSON body, but the problem is that the roomId inside it points to a room that doesn’t exist. The JSON itself is fine — the issue is with the meaning of the data.
That’s why HTTP 422 Unprocessable Entity is more accurate than 404.
A 404 means “the URL you requested does not exist,” but here the URL is correct — it’s the data inside the request that contains an invalid reference.
Using 422 tells the client:
- “Your JSON is valid.”
- “But the server cannot process it because one of the referenced resources doesn’t exist.”
This makes the error clearer and more helpful for the client, especially when validating relationships between resources.


Q2)
From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace.

When an API accidentally exposes a full Java stack trace, it gives attackers way too much information about how the system works internally. A stack trace can reveal things like the exact class names, package structure, file paths, and even the specific line of code where the error happened.
From a cybersecurity point of view, this is dangerous because an attacker can use that information to guess what technologies you’re using, what libraries might be vulnerable, and where weak points in the code might exist. It basically gives them a roadmap of your backend.
That’s why we return a generic 500 Internal Server Error instead — it hides all those internal details and keeps the system safer.

Q3)
Why is it advantageous to use JAX‑RS filters for cross‑cutting concerns like logging, rather than manually inserting  statements inside every single resource method.


Using JAX‑RS filters for logging is better because it lets us handle all the logging in one place, instead of repeating Logger.info() in every resource method. This keeps our code much cleaner. Each controller can focus only on the actual business logic, while the filter automatically logs every request and response for the whole API.
It also makes the project easier to maintain. If we ever want to change how logging works, we only update the filter instead of editing every single class. This gives us consistent logging across the entire API with much less effort.

If you want, I can help you write the explanation for the filter implementation too.




















