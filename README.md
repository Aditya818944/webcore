# WebCore Framework
A lightweight, annotation-based Java backend framework built on Servlets, designed to simplify web service development with features like routing, dependency injection, scope management, security, and automatic documentation generation

# Overview

WebCore is a custom-built Java backend framework that abstracts low-level Servlet APIs and provides a clean, structured way to build web services using annotations.

It includes:

* Annotation-based routing
* Scope management
* JSON handling
* Security system
* Automatic API documentation generation (a tool that create API documentation )


 #  Tech Stack
* Java 21
* Apache Tomcat 9
* jQuery (for frontend requests) it is the dependency

# Architecture
The framework is based on two core servlets:

1) WebCoreStartup
* Executes on server startup
* Scans all classes using SERVICE_PACKAGE_PREFIX
* Prepares metadata (services, annotations, mappings)
* Executes @Onstartup methods

2) WebCore
*  Acts as the main dispatcher
* Handles all incoming requests
* Resolves service methods
* Injects dependencies
* Executes services and returns response


# Configuration (web.xml)
Key Configuration that must be done in web.xml file of framework
* `SERVICE_PACKAGE_PREFIX` → Defines base package for scanning , value against it must be written by framework user
* `URL_CONTEXT` → Defines routing prefix (by default url = `/testing/*`) , this is also decided by framework user 
* `JSFILE_NAME` → Name of generated JavaScript file , name of java script file is also written by framework user (by default name=abcd.js)
* `JavaScriptSender`  → Write url pattern against JavaScriptSender servlet (default url = "/get-java-script")

# Features 

#### Annotations

1) `@Path` Defines the URL mapping for a class or method.
   
     a) Can be applied on class and method
     b) Mandatory for both class and method to be considered during scanning
   
  
2) `@Get` Specifies that the service handles HTTP GET requests
   
    a) Can be applied on:
       * Class → applies to all methods
       * Method → applies only to that method

   
3) `@Post` Specifies that the service handles HTTP POST requests
   
    a) Can be applied on:
       * Class → applies to all methods
       * Method → applies only to that method

   IMPORTANT NOTES FOR (@Get,@Post and @Path)
    * Only classes with @Path are scanned by the framework
    * Classes without @Path are ignored
    * Methods must also have @Path to be accessible
    * Methods without @Path are ignored
    CASE 1 -@Get / @Post on Class -> Applies to all methods inside the class
    CASE 2 -Not on Class and Not on Method -> Framework check method level annotation , applies accordingly
    CASE 3 -Not on Class and Not on Method -> Method supports both GET and POST
       
   EXAMPLE :
       import com.micro.web.services.webcore.annotations.*;
       @Path("/student")
       @Get
       public class Student
       {
       @Path("/add-student")
       public String add()
       {
         return "Student is added";
       }
       }

  Request url : http://localhost:8080/app-name/testing/student/add-student


  4) `@Forward` The @Forward annotation is used to forward a request from one service method to another service method within the framework.
     
     a) Applied on Method , Specifies the target service path to forward the request

     IMPORTANT NOTES FOR @Forward()
       * Forwarding is server-side (internal)
       * Browser URL does not change
       * The original method (add()) does not return response
       * The forwarded method controls the final response


     EXAMPLE :
     @Path("/student")
     @Get
     public class Student
     {
       @Path("/add-student")
       @Forward("/student/send-sms")
       public void add()
       {
         // Add student logic
       }
     
     @Path("/send-sms")
     public void sendSMS()
     {
       // Send SMS logic
     }
   }

Request url : http://localhost:8080/app-name/testing/student/add-student


5) `@Onstartup` The @Onstartup annotation is used to execute specific methods automatically during server startup.
It is mainly used for initialization tasks such as loading data, preparing caches, or setting up required data structures.

   a) Applied on method
   b) Method must have void return type
   c) Method must have void return type
   d) Each @Onstartup method must have a unique priority
   e) No two methods can share the same priority
   f) Priority determines execution order

IMPORTANT NOTES FOR @Onstartup
 * Method must return void
 * @Forward cannot be used with @Onstartup
 * Method should not depend on runtime request data

EXAMPLE : 
@Path("/student") 
public class Student 
{ 
@Onstartup(1) 
public void populateStudentRelatedDataStructure() 
{ 
// Initialize required data 
}
}

Note : no url it is applied on startup method which is used to initializing purposes


6) `@InjectApplicationScope` and `@InjectSessionScope` and `@InjectRequestScope`  These annotations allow automatic injection of scope objects into service classes.

   a) Applied on class
   b) Requires:Private member variable and Proper getter and setter methods

   IMPORTANT NOTES FOR ABOVE ANNOTATIONS
   * Must apply annotation on class:
    @InjectApplicationScope
    @InjectSessionScope
    @InjectRequestScope
  * Must define:
    Private variable
    Setter method (mandatory)
    Naming must follow camelCase convention

    EXAMPLE :
    @Get @Path("/candidate")
    @InjectApplicationScope
    @InjectSessionScope
     @InjectRequestScope
    public class Candidate
    {
     private ApplicationScope applicationScope;
     private SessionScope sessionScope;
    private RequestScope requestScope;
    public void setApplicationScope(ApplicationScope applicationScope)
    {
    this.applicationScope = applicationScope;
     }
    public ApplicationScope getApplicationScope()
    {
     return this.applicationScope;
     }
    public void setSessionScope(SessionScope sessionScope)
    {
    this.sessionScope = sessionScope;
    }
     public SessionScope getSessionScope()
     {
     return this.sessionScope;
    }
     public void setRequestScope(RequestScope requestScope)
    {
     this.requestScope = requestScope;
    }
    public RequestScope getRequestScope()
    {
     return this.requestScope;
    }
     @Path("/add")
    public void add()
    {
     sessionScope.setAttribute("AttributeThree","attribute-three-value");
     System.out.println("Candidate detail is added");
    }
    }



7) `@AutoWired()`  The @AutoWired annotation is used to automatically inject an object into a class property by searching for it across different scopes.
It eliminates the need for manually accessing Application, Session, or Request scopes.

   a) Applied on class fields (properties)
   b) Requires: A corresponding setter method
   c) Accepts a key as parameter

   IMPORTANT NOTES FOR @AutoWired()
   
    * Setter method is mandatory
    * Key must exist in at least one scope
    * If key is not found → value will remain null (or can be handled by framework)
  
   EXAMPLE :
   @Path("/bulb-vendor")
   @Get
   public class BulbVendor
   {
   @AutoWired("xyz")
   private Bulb bulb;
   public void setBulb(Bulb bulb)
   {
   this.bulb = bulb;
   }
   @Path("/get-bulb")
   public String getBulb()
   {
   return "Bulb with wattage : " + bulb.getWattage();
   }
   }



   8) `@RequestParameter`  The @RequestParameter annotation is used to bind HTTP request parameters (query parameters) to method arguments.
  
       a) Applied on method parameters
       b) Accepts a key name corresponding to the request parameter

      IMPORTANT RULES FOR @RequestParameter
        * Annotation must be applied to each parameter
        * Parameter name must match key in request
        * Framework performs type compatibility check
        * If conversion fails → error should be handled (framework responsibility)

      EXAMPLE :
      @Path("/student")
      @Get
      public class Student
      {
      @Path("/add-student")
      public String addStudent( @RequestParameter("age") int age, @RequestParameter("name") String name, @RequestParameter("roll_number") int rollNumber )
      { // student adding code return "Student added";
      }
      }




  9)  `@InjectRequestParameter` The @InjectRequestParameter annotation is used to inject HTTP request parameters (query parameters) directly into class-level
      properties. It avoids passing the same parameter repeatedly to multiple methods.

        a) Applied on class fields (properties)
        b) Requires:A corresponding setter method
        c) Accepts a key name from request parameters

       IMPORTANT NOTES FOR @InjectRequestParameter
          * Setter method is mandatory
          * Annotation must be applied on class field
          * Key must match query parameter name
          * Type conversion must be valid
          * Injection happens before any service method execution
     
       EXAMPLES :
       @Path("/student")
       @Get
       public class Student
       {
       @InjectRequestParameter("school_code") private int schoolCode;
       public void setSchoolCode(int schoolCode)
       {
       this.schoolCode = schoolCode;
       }
       public int getSchoolCode()
       {
       return this.schoolCode;
       }
       @Path("/add-student")
       public void addStudent()
       {
        // use schoolCode
       }
       @Path("/delete-student")
       public void deleteStudent()
       {
       // use schoolCode
       }
       }


10) Method Parameter Injection : The framework allows automatic injection of scope objects and request parameters directly into method arguments.
This eliminates the need for class-level injection or manual setup.

     a) The following types can be directly used as method parameters:
         * ApplicationScope
         * SessionScope
         * RequestScope

 IMPORTANT NOTES 
   * Scope injection works based on parameter type
   * Request parameters require @RequestParameter
   * Type conversion is handled by framework
   * Injection happens automatically before method execution

EXAMPLE : 
@Path("/student")
@Get
public class Student
{
    @Path("/do-something")
    public void doSomething(
        ApplicationScope as,
        SessionScope ss,
        RequestScope rs,
        @RequestParameter("age") int age
    )
    {
        // use scopes and age directly
    }
}



11) JSON Request Handling : The framework supports automatic conversion of JSON request body into Java objects for service methods.

    Parameter Rules

    Let n = number of parameters in service method:

        🔹 Case 1: n = 0
        Method is invoked normally
        JSON body (if present) is ignored
    
        🔹 Case 2: n = 1
        Parameter is treated as JSON object
        Framework converts JSON → Java object
    
        🔹 Case 3: n > 1
        Only one parameter can represent JSON object
        Remaining parameters must be one of:
        ApplicationScope
        SessionScope
        RequestScope

    IMPORTANT NOTES
     * Only one JSON object parameter allowed
     * JSON structure must match Java class
     * Type compatibility must be maintained
     * JSON parsing errors should be handled by framework
     * Multiple custom objects are not allowed
          -> public String method(Participant p, Bulb b) X NOT ALLOWED

    EXAMPLES :
    
    @Path("/something")
    @Post
    public class Something
    {
    @Path("/take-json-object")
    public String takeJsonObject(Participant p)
    {
    return "Received: " + p.getName();
    }
    }

    or (JSON+SCOPES)
    @Path("/something")
     @Post
     public class Something
    {
    @Path("/take-json-object")
     public String takeJsonObject(Participant p)
    {
    return "Received: " + p.getName();
    }
    }



12 @SecuredAccess (Authentication System ) : The @SecuredAccess annotation is used to secure service classes by enforcing authentication checks before                 executing any service method.


      a) Applied on class
      b) Defines a security guard that validates access before method execution


    IMPORTANT NOTES :
       * Guard method must:
            Accept required scopes (e.g., SessionScope)
            Throw exception on failure
       * Guard class must be accessible via class path
       * Authentication state must be maintained in scope (SessionScope recommended)
   
    EXAMPLE :
    
            @SecuredAccess(checkPost="bobby.web.application.working.directories.SecurityGuard",guard="guard")
            @Path("/employee")
            @InjectApplicationScope
        public class Employee
        {
            private ApplicationScope as;
        
            public void setApplicationScope(ApplicationScope as)
            {
                this.as = as;
            }
        
            @Onstartup(3)
            public void setLoginCredentials()
            {
                as.setAttribute("password","adityasinghchouhan");
            }
        
            @Path("/add")
            public String add()
            {
                return "Employee added";
            }
        }
          🔑 Login Service Example
          @Path("/Login")
          @Get
          public class Login
          {
              @Path("/login")
              public void login(
                  @RequestParameter("password") String password,
                  ApplicationScope as,
                  SessionScope ss
              )
              {
                  String asPassword = (String) as.getAttribute("password");
          
                  if(password.equals(asPassword)) {
                      ss.setAttribute("credentials","correct");
                  } else {
                      ss.setAttribute("credentials","incorrect");
                  }
              }
          }
            🛡️ Security Guard
            public class SecurityGuard
            {
                public void guard(SessionScope ss) throws ServiceException
                {
                    String credentials = (String) ss.getAttribute("credentials");
            
                    if(credentials.equals("incorrect")) {
                        throw new ServiceException("Incorrect credentials");
                    }
                }
            }
       

    

        

  
   
