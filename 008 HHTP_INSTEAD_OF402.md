### Why HTTP 422 is more semantically accurate than 404 for missing references

HTTP **422 Unprocessable Entity** is often more appropriate than **404 Not Found** when a request contains a valid JSON payload, but includes a reference to a resource that does not exist (e.g., a `roomId` that is not in the system).

The key reason is that **422 focuses on the validity of the request content**, whereas **404 refers to the requested endpoint or resource itself**.

In this scenario, the client is sending a request such as:

```http
POST /api/v1/sensors
```

with a JSON body that includes a `roomId`. The endpoint `/api/v1/sensors` clearly exists and is reachable, so returning a 404 would be misleading. A 404 implies that the requested resource or URI could not be found, which is not the case here.

Instead, the issue lies within the **payload of the request**. The JSON structure is syntactically correct, but semantically invalid because it references a room that does not exist. This is exactly what HTTP 422 is designed to represent: the server understands the request and its format, but **cannot process it due to logical or semantic errors in the data**.

Using 422 therefore provides a more precise and meaningful response to the client. It communicates that:

* the request was well-formed,
* the endpoint is valid,
* but the data inside the request violates business rules or referential integrity.

From a RESTful design perspective, this improves clarity and helps client developers debug issues more effectively. Instead of assuming the endpoint is incorrect (as a 404 might suggest), they can immediately understand that the problem lies with the data they provided.

In summary, **HTTP 422 is semantically more accurate than 404 in this case because it distinguishes between a missing endpoint and an invalid reference within an otherwise valid request payload**, leading to clearer communication and better API design.
