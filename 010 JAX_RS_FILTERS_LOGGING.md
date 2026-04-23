### Why JAX-RS Filters Are Better for Cross-Cutting Concerns Like Logging

Using JAX-RS filters for cross-cutting concerns such as logging is advantageous because it provides a **centralised, consistent, and reusable** way to handle behaviour that applies to many or all API endpoints.

If `Logger.info()` statements are manually inserted into every resource method, the same logging logic has to be repeated many times throughout the codebase. This leads to duplication, makes the code harder to read, and increases the chance of inconsistency. For example, one method may log the URI and status code, while another may forget to log anything at all.

JAX-RS filters solve this problem by allowing logging to be handled in one place. A `ContainerRequestFilter` can log details of every incoming request, and a `ContainerResponseFilter` can log the status of every outgoing response. This means all endpoints are covered automatically without needing to modify each individual resource method.

This approach has several benefits:

* **Separation of concerns**: Resource classes stay focused on business logic, while logging is handled separately.
* **Consistency**: Every request and response is logged in the same format.
* **Maintainability**: If the logging behaviour needs to change, it only needs to be updated in one class.
* **Scalability**: As the API grows and more resource methods are added, the filter continues to apply automatically.
* **Cleaner code**: Resource methods remain shorter and easier to understand because they are not cluttered with repeated logging statements.

From a software engineering perspective, logging is a classic cross-cutting concern because it affects many parts of the application but is not part of the core functionality of any single endpoint. JAX-RS filters are designed specifically to handle this kind of requirement in a cleaner and more professional way.

In summary, using JAX-RS filters for logging is better than adding manual `Logger.info()` calls in every resource method because it reduces duplication, improves consistency, keeps the code cleaner, and makes the application easier to maintain.
