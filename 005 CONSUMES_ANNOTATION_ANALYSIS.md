# @Consumes Annotation & Content-Type Mismatch Analysis

## Question
"We explicitly use the @Consumes (MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?"

---

## What is @Consumes?

The `@Consumes` annotation specifies the **media types (content formats) that a resource method can accept**.

```java
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
public class SensorResource {
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)  // ← Restricts POST to JSON only
    public Response createSensor(Sensor sensor) {
        // This method ONLY accepts application/json
    }
}
```

**In plain English**: "This endpoint will ONLY process requests with `Content-Type: application/json` in the HTTP headers."

---

## How JAX-RS Processes Requests

### Flow Diagram

```
1. Client sends HTTP Request
   ↓
2. JAX-RS Container receives request
   ↓
3. Extract Content-Type header from request
   ↓
4. Check against @Consumes annotation
   ↓
   ├─→ MATCH ✅ 
   │   ↓
   │   Deserialize request body
   │   ↓
   │   Invoke method
   │   ↓
   │   Return 200/201 response
   │
   └─→ NO MATCH ❌
       ↓
       Return 415 Unsupported Media Type
       ↓
       Stop (method NEVER called)
```

---

## Scenario 1: Client Sends application/xml

### Request Example

```
POST /api/v1/sensors HTTP/1.1
Host: localhost:8080
Content-Type: application/xml
Content-Length: 145

<?xml version="1.0" encoding="UTF-8"?>
<sensor>
    <id>sensor-005</id>
    <type>temperature</type>
    <roomId>room-001</roomId>
</sensor>
```

### What JAX-RS Does

1. **Receives** the HTTP request
2. **Extracts** `Content-Type: application/xml` from headers
3. **Checks** method-level `@Consumes(APPLICATION_JSON)` on POST
4. **Compares**: `application/xml` ≠ `application/json` ❌
5. **Determines**: Media type mismatch!
6. **Rejects** the request **BEFORE** `createSensor()` is called
7. **Returns** HTTP 415 Unsupported Media Type

### Server Response

```
HTTP/1.1 415 Unsupported Media Type
Content-Type: text/plain
Content-Length: 150

The server is refusing to service the request because the entity of 
the request is in a format not supported by the requested resource 
for this method.
```

### Key Point

❌ **The `createSensor()` method is NEVER called**

The rejection happens in the JAX-RS framework layer, **before** your business logic executes.

---

## Scenario 2: Client Sends text/plain

### Request Example

```
POST /api/v1/sensors HTTP/1.1
Host: localhost:8080
Content-Type: text/plain
Content-Length: 30

sensor-005|temperature|room-001
```

### What JAX-RS Does

1. **Extracts** `Content-Type: text/plain`
2. **Checks** `@Consumes(APPLICATION_JSON)`
3. **Compares**: `text/plain` ≠ `application/json` ❌
4. **Rejects** request immediately
5. **Returns** HTTP 415

### Server Response

```
HTTP/1.1 415 Unsupported Media Type

[Same error message as Scenario 1]
```

### Key Point

❌ **Method NOT called, deserialization NOT attempted**

JAX-RS short-circuits the entire request processing pipeline.

---

## Scenario 3: Client Sends Correct Format (application/json)

### Request Example

```
POST /api/v1/sensors HTTP/1.1
Host: localhost:8080
Content-Type: application/json
Content-Length: 68

{
  "id": "sensor-005",
  "type": "temperature",
  "roomId": "room-001"
}
```

### What JAX-RS Does

1. **Extracts** `Content-Type: application/json`
2. **Checks** `@Consumes(APPLICATION_JSON)`
3. **Compares**: `application/json` = `application/json` ✅
4. **Matches!** Media type is correct
5. **Deserializes** JSON string → `Sensor` object
6. **Calls** `createSensor(Sensor sensor)` method
7. **Executes** business logic normally
8. **Returns** result to client

### Server Response

```
HTTP/1.1 201 Created
Content-Type: application/json
Location: /api/v1/sensors/sensor-005

{
  "id": "sensor-005",
  "type": "temperature",
  "roomId": "room-001"
}
```

### Key Point

✅ **Method IS called, deserialization succeeds, business logic executes**

---

## Technical Consequences Comparison

### Table: Mismatched vs Matched Formats

| Aspect | XML Format | Plain Text | JSON Format |
|--------|-----------|-----------|-----------|
| **Content-Type** | application/xml | text/plain | application/json |
| **@Consumes Match?** | ❌ NO | ❌ NO | ✅ YES |
| **HTTP Status** | 415 | 415 | 201 |
| **Method Called?** | ❌ NO | ❌ NO | ✅ YES |
| **Deserialization** | ❌ Not attempted | ❌ Not attempted | ✅ Performed |
| **Business Logic** | ❌ Not executed | ❌ Not executed | ✅ Executed |
| **Sensor Created?** | ❌ NO | ❌ NO | ✅ YES |
| **Response Time** | ⚡ Very fast | ⚡ Very fast | Normal |
| **Error Handler** | JAX-RS framework | JAX-RS framework | Application code |

---

## What Happens Inside JAX-RS

### The Mismatch Case

```java
// Pseudo-code: How JAX-RS handles the request

public void processRequest(HttpRequest request) {
    
    // Step 1: Get Content-Type from header
    String contentType = request.getHeader("Content-Type");
    // Result: "application/xml"
    
    // Step 2: Get @Consumes from annotation
    MediaType consumesType = getConsumesAnnotation(createSensor);
    // Result: MediaType.APPLICATION_JSON
    
    // Step 3: Compare
    if (!contentType.equals(consumesType.toString())) {
        // MISMATCH!
        // Send 415 and STOP HERE
        return Response.status(415)
            .entity("Unsupported Media Type")
            .build();
        // createSensor() NEVER CALLED!
    }
    
    // Step 4: If we reach here, types match
    // Deserialize and call method
    Sensor sensor = deserializeJson(request.getBody(), Sensor.class);
    createSensor(sensor);
}
```

---

## Common Client Mistakes

### Mistake 1: Hardcoded XML (Wrong)

```python
# Python client (WRONG)
import requests

response = requests.post(
    'http://localhost:8080/api/v1/sensors',
    headers={'Content-Type': 'application/xml'},
    data='<sensor><id>sensor-005</id></sensor>'
)

# Result: 415 Unsupported Media Type
print(f"Status: {response.status_code}")  # 415
```

### Correction

```python
# Python client (CORRECT)
import requests
import json

response = requests.post(
    'http://localhost:8080/api/v1/sensors',
    headers={'Content-Type': 'application/json'},
    data=json.dumps({
        'id': 'sensor-005',
        'type': 'temperature',
        'roomId': 'room-001'
    })
)

# Result: 201 Created
print(f"Status: {response.status_code}")  # 201
```

---

### Mistake 2: JavaScript Fetch with Wrong Content-Type

```javascript
// JavaScript (WRONG)
fetch('http://localhost:8080/api/v1/sensors', {
  method: 'POST',
  headers: {
    'Content-Type': 'text/plain'  // ❌ WRONG!
  },
  body: 'sensor-data'
})
.then(r => r.json())
.catch(e => console.log('Error:', e));

// Response: 415 Unsupported Media Type
```

### Correction

```javascript
// JavaScript (CORRECT)
fetch('http://localhost:8080/api/v1/sensors', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'  // ✅ CORRECT!
  },
  body: JSON.stringify({
    id: 'sensor-005',
    type: 'temperature',
    roomId: 'room-001'
  })
})
.then(r => r.json())
.then(data => console.log('Created:', data));

// Response: 201 Created
```

---

## Why Use @Consumes?

### 1. **API Contract Enforcement**
```
Server to Client: "I only speak JSON!"
```
Prevents clients from sending incompatible formats.

---

### 2. **Early Rejection**
Mismatches caught at framework level, **before** business logic runs:
- ❌ No unnecessary deserialization attempts on POST
- ❌ No wasted CPU cycles
- ✅ Faster rejection (415 status returned immediately)
- Note: GET is not affected since it has no @Consumes

---

### 3. **Prevents Deserialization Errors**
If JAX-RS tried to parse XML as JSON:
```
javax.ws.rs.core.MediaTypeException: 
Cannot deserialize instance of `com.smartcampus.model.Sensor` 
from STRING value ('<?xml version="1.0"?><sensor>...')
```

This would be:
- ❌ 400 Bad Request (confusing - looks like client data error)
- ✅ NOT 415 (clear - wrong format)

---

### 4. **Clear API Documentation**
```java
@POST
@Consumes(MediaType.APPLICATION_JSON)  // Explicit documentation on POST
public Response createSensor(Sensor sensor) { ... }
```

Developers know exactly what format to send. No guessing. It's immediately obvious that POST expects JSON but GET doesn't.

---

### 5. **Framework Flexibility**
JAX-RS automatically:
- Validates Content-Type
- Selects correct deserializer
- Handles errors uniformly
- **No custom code needed**

---

## HTTP Status Codes Explained

### 415 Unsupported Media Type

```
Sent by server when:
- Client's Content-Type header doesn't match @Consumes
- Server cannot process this media type
- Server needs a different format

Example response:
HTTP/1.1 415 Unsupported Media Type
Content-Type: text/plain

The server is refusing to service the request because the entity of 
the request is in a format not supported by the requested resource 
for this method.
```

### 400 Bad Request

```
Different from 415!

400 = Content is in correct format but invalid data
      (e.g., JSON sent, but missing required fields)

415 = Content format itself is wrong
      (e.g., XML sent when JSON expected)
```

---

## Real-World Scenario: Debugging

### Client Experience

```
Client tries to create sensor with XML:
POST /api/v1/sensors
Content-Type: application/xml
<sensor>...</sensor>

↓ (receives error)

HTTP 415 Unsupported Media Type
"The server is refusing to service the request..."

Client thinks: "Oh! The server wants JSON, not XML!"
Client fixes: Changes Content-Type to application/json
Client retries: Works!
```

### Without @Consumes (Hypothetical Bad Design)

```
Client tries to create sensor with XML:
POST /api/v1/sensors
Content-Type: application/xml
<sensor>...</sensor>

↓ (JAX-RS attempts JSON deserialization)

HTTP 400 Bad Request
"JSON parse error: Unexpected character..."

Client thinks: "Is there a typo in my XML?"
Client is confused... "But I sent well-formed XML!"
```

---

## Current Implementation in SensorResource

```java
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
public class SensorResource {

    @GET
    public Collection<Sensor> getAllSensors(@QueryParam("type") String type) {
        // GET has NO @Consumes - doesn't need one (no request body)
        if (type == null || type.trim().isEmpty()) {
            return SmartCampusDataStore.sensors.values();
        }
        return SmartCampusDataStore.sensors.values()
                .stream()
                .filter(sensor -> sensor.getType() != null
                        && sensor.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)  // ← Applied ONLY to POST method
    public Response createSensor(Sensor sensor) {
        // This method explicitly declares JSON as the only accepted format
        // Only application/json is accepted
        
        Room room = SmartCampusDataStore.rooms.get(sensor.getRoomId());
        if (room == null) {
            throw new LinkedResourceNotFoundException(
                "The specified room does not exist.");
        }
        
        SmartCampusDataStore.sensors.put(sensor.getId(), sensor);
        room.getSensorIds().add(sensor.getId());
        SmartCampusDataStore.readingsBySensor.put(
            sensor.getId(), 
            new java.util.ArrayList<>()
        );
        
        return Response.status(Response.Status.CREATED)
                .entity(sensor)
                .build();
    }
}
```

### How It Works

1. **Request arrives** for POST with `Content-Type: application/json`
2. **JAX-RS checks** method-level `@Consumes(APPLICATION_JSON)` ✅
3. **Deserializes** JSON to `Sensor` object
4. **Calls** `createSensor(Sensor sensor)`
5. **Method executes** normally

### Why This Approach is Better

✅ **Cleaner Design**: `@Consumes` only on POST, not on GET (which has no body)
✅ **Explicit**: Clearly shows that POST expects JSON, GET doesn't care about format
✅ **More Accurate**: @Consumes is only relevant for methods with request bodies

---

## Multiple Formats (Advanced)

If you need a POST method to accept multiple formats, you can specify them:

```java
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
public class SensorResource {

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response createSensor(Sensor sensor) {
        // This method accepts BOTH JSON AND XML
        // Client can send either format
    }
    
    @GET
    public Collection<Sensor> getAllSensors() {
        // GET has no @Consumes
        // Appropriate since GET has no request body
    }
}
```

---

## Summary Table

| Aspect | Explanation |
|--------|-------------|
| **What does @Consumes do?** | Restricts accepted request media types |
| **Where is it applied?** | On the POST method only (not class-level) |
| **Who enforces it?** | JAX-RS framework (automatic) |
| **When is it checked?** | Before POST method invocation |
| **What happens on mismatch?** | Returns HTTP 415 Unsupported Media Type |
| **Is method called on mismatch?** | ❌ NO - request rejected early |
| **Is deserialization attempted?** | ❌ NO - skipped for mismatched types |
| **Error handler?** | JAX-RS framework (not application code) |
| **Response time on mismatch?** | ⚡ Fast (no processing needed) |
| **Does GET have @Consumes?** | ❌ NO - GET has no request body |
| **Can it be customized?** | Yes, specify multiple formats: @Consumes({APPLICATION_JSON, APPLICATION_XML}) |

---

## Conclusion

The `@Consumes(MediaType.APPLICATION_JSON)` annotation **applied to the POST method only**:

1. **Enforces** JSON-only input format for POST
2. **Rejects** mismatched formats with **HTTP 415**
3. **Prevents** method execution when format doesn't match
4. **Avoids** expensive deserialization attempts
5. **Provides** clear error messages to clients
6. **Documents** API contract explicitly on the method
7. **Keeps** GET method clean (no unnecessary @Consumes)
8. **Handled automatically** by JAX-RS (no custom code)

This is a **critical design pattern** in REST APIs for validating content types before processing requests. Applying it at the method level (rather than class level) is more precise and follows best practices - only methods that accept request bodies should have @Consumes constraints.

### Current Best Practice in Smart Campus API

✅ **@Consumes on POST only** - Explicit about what formats POST accepts
✅ **@Produces on class level** - All methods return JSON
✅ **GET without @Consumes** - GET has no body to consume

This approach provides clarity and prevents confusion about which methods have content-type requirements.
