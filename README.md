# WebCore Framework

A lightweight, annotation-based Java backend framework built on Servlets, designed to simplify web service development with features like routing, dependency injection, scope management, security, and automatic documentation generation.

#  Overview

WebCore is a custom-built Java backend framework that abstracts low-level Servlet APIs and provides a clean, structured way to build web services using annotations.

#  Key Features
Annotation-based routing
Scope management (Application, Session, Request)
JSON request handling
Built-in security system
Automatic API documentation generation
🛠 Tech Stack
Java 21
Apache Tomcat 9
jQuery (for frontend requests)

# Architecture

The framework is based on two core servlets:

1. WebCoreStartup
Executes on server startup
Scans all classes using SERVICE_PACKAGE_PREFIX
Prepares metadata (services, annotations, mappings)
Executes @OnStartup methods
2. WebCore
Acts as the main dispatcher
Handles all incoming requests
Resolves service methods
Injects dependencies
Executes services and returns responses


 # Configuration (web.xml)

The following parameters must be configured:

Parameter	Description
SERVICE_PACKAGE_PREFIX	Base package for scanning (defined by user)
URL_CONTEXT	Routing prefix (default: /testing/*)
JSFILE_NAME	Name of generated JavaScript file (default: abcd.js)
JavaScriptSender	URL pattern for JavaScript servlet (default: /get-java-script)
*  Features
*  Annotations
1. @Path

Defines the URL mapping for a class or method.

Can be applied on class and method
Mandatory for scanning
2. @Get

Handles HTTP GET requests.

Can be applied on:
Class → applies to all methods
Method → applies to specific method
3. @Post

Handles HTTP POST requests.

Can be applied on:
Class → applies to all methods
Method → applies to specific method
## Important Rules
Only classes with @Path are scanned
Methods must also have @Path
If no @Get/@Post → method supports both
## Example
@Path("/student")
@Get
public class Student {

    @Path("/add-student")
    public String add() {
        return "Student is added";
    }
}

## Request URL:

http://localhost:8080/app-name/testing/student/add-student
4. @Forward

Used for internal request forwarding.

Key Points
Server-side forwarding
URL does NOT change
Target method controls response
Example
@Path("/student")
@Get
public class Student {

    @Path("/add-student")
    @Forward("/student/send-sms")
    public void add() {
        // Add student logic
    }

    @Path("/send-sms")
    public void sendSMS() {
        // Send SMS logic
    }
}
5. @OnStartup

Executes methods during server startup.

Rules
Must return void
Unique priority required
Cannot use @Forward
Example
@Path("/student")
public class Student {

    @OnStartup(1)
    public void init() {
        // Initialization logic
    }
}
6. Scope Injection
Annotations:
@InjectApplicationScope
@InjectSessionScope
@InjectRequestScope
Rules
Applied on class
Requires:
Private variables
Setter methods
7. @AutoWired

Automatically injects objects from scopes.

Rules
Applied on fields
Setter method required
Key must exist in scope
8. @RequestParameter

Binds query parameters to method arguments.

Example
@Path("/student")
@Get
public class Student {

    @Path("/add-student")
    public String addStudent(
        @RequestParameter("age") int age,
        @RequestParameter("name") String name
    ) {
        return "Student added";
    }
}
9. @InjectRequestParameter

Injects request parameters into class fields.

Rules
Applied on fields
Setter required
Injected before method execution
10. Method Parameter Injection

Supports direct injection of:

ApplicationScope
SessionScope
RequestScope
Example
@Path("/student")
@Get
public class Student {

    @Path("/do-something")
    public void doSomething(
        ApplicationScope as,
        SessionScope ss,
        RequestScope rs,
        @RequestParameter("age") int age
    ) {
        // logic
    }
}
11. JSON Request Handling
Rules
Case	Behavior
n = 0	JSON ignored
n = 1	Treated as JSON object
n > 1	Only one JSON object allowed
Example
@Path("/something")
@Post
public class Something {

    @Path("/take-json-object")
    public String takeJsonObject(Participant p) {
        return "Received: " + p.getName();
    }
}
12. @SecuredAccess (Authentication System)

Used to secure service classes.

Rules
Applied on class
Uses guard method for validation
Throws exception if unauthorized

## Login Example
@Path("/Login")
@Get
public class Login {

    @Path("/login")
    public void login(
        @RequestParameter("password") String password,
        ApplicationScope as,
        SessionScope ss
    ) {
        String stored = (String) as.getAttribute("password");

        if(password.equals(stored)) {
            ss.setAttribute("credentials","correct");
        } else {
            ss.setAttribute("credentials","incorrect");
        }
    }
}
## Security Guard
public class SecurityGuard {

    public void guard(SessionScope ss) throws ServiceException {
        String credentials = (String) ss.getAttribute("credentials");

        if(credentials.equals("incorrect")) {
            throw new ServiceException("Incorrect credentials");
        }
    }
}
#### Final Notes
WebCore simplifies Servlet-based backend development
Fully annotation-driven architecture
Designed for learning + lightweight production use
