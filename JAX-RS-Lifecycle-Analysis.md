# JAX-RS Resource Class Lifecycle Analysis

## Overview

This document explains the default lifecycle of JAX-RS Resource classes, the impact on data management, and how to prevent race conditions in concurrent environments.

---

## Default Behavior: Per-Request Scope

By default, **JAX-RS instantiates a new Resource class instance for EVERY incoming request**. This is known as the **Per-Request Scope**.

### Example:

```java
@Path("/api/resource")
public class MyResource {
    // New instance created for each request
    public void handleRequest() { }
}
```

## Request Flow:

```
Request 1 (Thread A) → New MyResource() instance
Request 2 (Thread B) → New MyResource() instance
Request 3 (Thread C) → New MyResource() instance
```

Each request gets its own independent instance of the resource class.

---

## Impact on Data Management

### Shared Static Data Structures

Many JAX-RS applications use **static shared data structures** to persist data across requests:

```java
public class DataStore {
    public static Map<String, Entity> entities = new HashMap<>();
    public static List<Item> items = new ArrayList<>();
}
```

### Key Points:

**Because the data is `static`:**
- ✅ All requests share the SAME data structures
- ✅ Data persists across all request instances
- ✅ This is intentional — you want data to survive across requests

**However, this creates a critical problem:**

---

## The Race Condition Problem

### Definition:

A **race condition** occurs when multiple threads access shared data concurrently, and the final result depends on the order of execution.

### Example Scenario:

```
Thread A (Request 1)           Thread B (Request 2)
──────────────────────         ──────────────────────
Start: data = {item1}          Start: data = {item1}
                               get("item1")
                               (reading...)
put("item2", value)             (still iterating)
(writing...)                    
                               ConcurrentModificationException!
```

### Common Issues:

1. **ConcurrentModificationException** — One thread modifies while another iterates
2. **Data Corruption** — Lost updates due to non-atomic operations
3. **Visibility Issues** — One thread doesn't see changes made by another

### Real Example:

```java
@GET
public Collection<Entity> getAll() {
    return DataStore.entities.values();  // Not thread-safe!
}

@POST
public Response create(Entity entity) {
    DataStore.entities.put(entity.getId(), entity);  // Concurrent access!
    return Response.status(Response.Status.CREATED).entity(entity).build();
}
```

**Scenario:**
- Request A calls `getAll()` and starts iterating
- Request B calls `create()` and modifies the map
- Request A crashes with `ConcurrentModificationException`

---

## Solution: Thread-Safe Synchronization

### Option 1: ConcurrentHashMap (Recommended)

```java
public class DataStore {
    public static Map<String, Entity> entities = 
        new ConcurrentHashMap<>();
    
    public static List<Item> items = 
        Collections.synchronizedList(new ArrayList<>());
}
```

**Advantages:**
- Built for concurrent access
- Lock-free reads (faster)
- More efficient than `synchronizedMap`
- Industry standard

### Option 2: Collections.synchronizedMap()

```java
public static Map<String, Entity> entities = 
    Collections.synchronizedMap(new HashMap<>());
```

**Disadvantages:**
- Locks entire map for each operation
- Slower performance
- Not as fine-grained as ConcurrentHashMap

### Option 3: Synchronized Methods

```java
@GET
public synchronized Collection<Entity> getAll() {
    return new ArrayList<>(DataStore.entities.values());
}

@POST
public synchronized Response create(Entity entity) {
    DataStore.entities.put(entity.getId(), entity);
    return Response.status(Response.Status.CREATED).entity(entity).build();
}
```

**Disadvantages:**
- Locks entire method (coarse-grained)
- Only one request can execute at a time
- Poor scalability

---

## Why Per-Request Scope is Used

Even though it creates concurrency challenges, Per-Request Scope is the default because:

| Benefit | Explanation |
|---------|-------------|
| **Stateless Design** | Each request is independent, easier to scale horizontally |
| **Thread Safety for Instance Variables** | Instance variables don't persist across requests |
| **Resource Cleanup** | Old instances are garbage collected after each request |
| **Simplified Testing** | Fresh state for each test case |
| **Distributed Deployment** | Multiple servers can handle requests independently |

---

## Best Practices for Implementing JAX-RS with Shared Data

### Incorrect Implementation (Not Thread-Safe):

```java
public class DataStore {
    public static Map<String, Entity> entities = new HashMap<>();
    // NOT thread-safe!
}
```

### Correct Implementation (Thread-Safe):

```java
public class DataStore {
    // Use ConcurrentHashMap for thread-safe concurrent access
    public static Map<String, Entity> entities = 
        new ConcurrentHashMap<>();
    
    public static List<Item> items = 
        Collections.synchronizedList(new ArrayList<>());
}
```

### Resource Implementation:

```java
@Path("/api/entities")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EntityResource {
    
    @GET
    public Collection<Entity> getAll() {
        // Thread-safe due to ConcurrentHashMap
        return DataStore.entities.values();
    }
    
    @POST
    public Response create(Entity entity) {
        // Thread-safe due to ConcurrentHashMap
        DataStore.entities.put(entity.getId(), entity);
        return Response.status(Response.Status.CREATED)
                .entity(entity)
                .build();
    }
    
    @GET
    @Path("/{entityId}")
    public Response getById(@PathParam("entityId") String entityId) {
        Entity entity = DataStore.entities.get(entityId);
        
        if (entity == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Entity not found\"}")
                    .build();
        }
        
        return Response.ok(entity).build();
    }
}
```

---

## Architectural Decision Summary

| Aspect | Details |
|--------|---------|
| **Resource Lifecycle** | Per-request (new instance per HTTP request) |
| **Data Persistence** | Static shared data structures survive across requests |
| **Storage Pattern** | In-memory HashMap (or thread-safe alternatives) |
| **Concurrency Risk** | Multiple threads access same shared data structures simultaneously |
| **Synchronization Solution** | Use ConcurrentHashMap or synchronizedMap for thread-safe access |
| **Why This Matters** | Prevents data corruption and ConcurrentModificationException |
| **Real-World Impact** | Under high load, unsynchronized access will crash the API |

---

## Conclusion

The per-request lifecycle of JAX-RS Resource classes, combined with static shared data structures, requires careful synchronization to prevent race conditions. Using `ConcurrentHashMap` or `Collections.synchronizedMap()` are industry-standard solutions that provide thread-safe, efficient access without compromising performance or scalability.

This architectural pattern is essential for building robust, production-ready REST APIs that can handle concurrent requests from multiple clients. Understanding the lifecycle and its implications is critical for any developer working with JAX-RS.
