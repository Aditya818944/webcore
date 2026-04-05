package com.micro.web.services.webcore.servlets;
import com.micro.web.services.webcore.annotations.*;
import com.micro.web.services.webcore.model.*;
import com.micro.web.services.webcore.pojo.*;
import javax.servlet.http.*;
import java.lang.reflect.*;
import com.google.gson.*;
import javax.servlet.*;
import java.io.*;

public class WebCore extends HttpServlet
{
public static boolean checkForOtherTypes(Parameter parameter,Class clzz)
{
String typeOfParameter=parameter.getType().getName();
if(typeOfParameter.equalsIgnoreCase("java.lang.Long") || typeOfParameter.equalsIgnoreCase("long")){
return false;
}else if(typeOfParameter.equalsIgnoreCase("java.lang.Integer") || typeOfParameter.equalsIgnoreCase("int")){
return false;
}else if(typeOfParameter.equalsIgnoreCase("java.lang.Short") || typeOfParameter.equalsIgnoreCase("short")){
return false;
}else if(typeOfParameter.equalsIgnoreCase("java.lang.Byte") || typeOfParameter.equalsIgnoreCase("byte")){
return false;
}else if(typeOfParameter.equalsIgnoreCase("java.lang.Double") || typeOfParameter.equalsIgnoreCase("double")){
return false;
}else if(typeOfParameter.equalsIgnoreCase("java.lang.Float") || typeOfParameter.equalsIgnoreCase("float")){
return false;
}else if(typeOfParameter.equalsIgnoreCase("java.lang.Character") || typeOfParameter.equalsIgnoreCase("char")){
return false;
}else if(typeOfParameter.equalsIgnoreCase("java.lang.Boolean") || typeOfParameter.equalsIgnoreCase("boolean")){
return false;
}else if(typeOfParameter.equalsIgnoreCase("java.lang.String")){
return false;
}else if(clzz.getName().equalsIgnoreCase(parameter.getType().getName())==false) return false;
return true;
}
public static boolean checkForReturnType(String returnType)
{
if(returnType.equalsIgnoreCase("java.lang.String")) return true;
else if(returnType.equalsIgnoreCase("java.lang.Long") || returnType.equalsIgnoreCase("long")) return true;
else if(returnType.equalsIgnoreCase("java.lang.Integer") || returnType.equalsIgnoreCase("int")) return true;
else if(returnType.equalsIgnoreCase("java.lang.Short") || returnType.equalsIgnoreCase("short")) return true;
else if(returnType.equalsIgnoreCase("java.lang.Byte") || returnType.equalsIgnoreCase("byte")) return true;
else if(returnType.equalsIgnoreCase("java.lang.Double") || returnType.equalsIgnoreCase("double")) return true;
else if(returnType.equalsIgnoreCase("java.lang.Float") || returnType.equalsIgnoreCase("float")) return true;
else if(returnType.equalsIgnoreCase("java.lang.Character") || returnType.equalsIgnoreCase("char")) return true;
else if(returnType.equalsIgnoreCase("java.lang.Boolean") || returnType.equalsIgnoreCase("boolean")) return true;

return false;
}


public static void forwardRequest(String forwardPath,WebCoreModel webCoreModel,HttpServletResponse response,HttpServletRequest request)
{
Gson gson=new Gson();
PrintWriter pw;
String servicePath=forwardPath;
try
{
response.setContentType("text/plain");
pw=response.getWriter();
Service service=webCoreModel.services.get(forwardPath);
if(service==null) {
RequestDispatcher requestDispatcher=request.getRequestDispatcher(forwardPath);
requestDispatcher.forward(request,response);
return ;
}


Class clss=service.getServiceClass();



Method method=null;
Object obj=clss.newInstance();


// We are storing Application ans Session Scope so that we can use them when required 
HttpSession httpSession=request.getSession();
ServletContext servletContext=request.getServletContext();





/******if service is secured part starts here  *************/

Service securityService=null;

if(service.isSecured())
{
//forwardRequest
try
{
securityService=(Service)webCoreModel.services.get(service.getCheckPost());
Class securityClass=securityService.getServiceClass();
Object securityClassObject=securityClass.newInstance();


Class paramTypes[]=new Class[service.getGuardParameters().length];

int l=0;
for(Parameter p : service.getGuardParameters())
{
paramTypes[l]=p.getType();
l++;
}


Method guardMethod=securityClass.getMethod(service.getGuard(),paramTypes);


if(securityService.getInjectApplicationScope())
{
ApplicationScope aps=new ApplicationScope();
aps.setServletContext(request.getServletContext());
try{
method=clss.getMethod("setApplicationScope",ApplicationScope.class);
}catch(NoSuchMethodException nsme)
{
System.out.println("Write setter to inject in camel case notation : ");
return ;
}
method.invoke(securityClassObject,aps);
}else if(securityService.getInjectSessionScope())
{
SessionScope ss=new SessionScope();
ss.setHttpSession(request.getSession());
try{
method=clss.getMethod("setSessionScope",SessionScope.class);
}catch(NoSuchMethodException nsme)
{
System.out.println("Write setter to inject in camel case notation : ");
return ;
}
method.invoke(securityClassObject,ss);
}else if(securityService.getInjectRequestScope())
{
RequestScope rs=new RequestScope();
rs.setHttpServletRequest(request);
try{
method=clss.getMethod("setRequestScope",RequestScope.class);
}catch(NoSuchMethodException nsme)
{
System.out.println("Write setter to inject in camel case notation : ");
return ;
}
method.invoke(securityClassObject,rs);
}

method=null;




Object guardParameters[]=new Object[service.getGuardParameters().length];
int y=0;
for(Parameter p : service.getGuardParameters())
{
String top=p.getType().getName();
if(top.equals("com.thinking.machines.webcore.pojo.RequestScope")){ 
RequestScope rsc=new RequestScope();
rsc.setHttpServletRequest(request);
guardParameters[y]=rsc;
}else if(top.equals("com.thinking.machines.webcore.pojo.SessionScope")){
SessionScope ssc=new SessionScope();
ssc.setHttpSession(httpSession);
guardParameters[y]=ssc;
}else if(top.equals("com.thinking.machines.webcore.pojo.ApplicationScope")){
ApplicationScope asc=new ApplicationScope();
asc.setServletContext(request.getServletContext());
guardParameters[y]=asc;
}
y++;
}




guardMethod.invoke(securityClassObject,guardParameters);

}catch(InvocationTargetException ite)
{
System.out.println("Exception occurs during processing of guard "+ite.getMessage());
response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
}
}


/******if service is secured part end's here  *************/





if(service.getInjectApplicationScope())
{
ApplicationScope applicationScope=new ApplicationScope();
applicationScope.setServletContext(request.getServletContext());
try{
method=clss.getMethod("setApplicationScope",ApplicationScope.class);
}catch(NoSuchMethodException nsme)
{
System.out.println("Write setter to inject in camel case notation : ");
return ;
}
method.invoke(obj,applicationScope);
}else if(service.getInjectSessionScope())
{
SessionScope sessionScope=new SessionScope();
sessionScope.setHttpSession(request.getSession());
try{
method=clss.getMethod("setSessionScope",SessionScope.class);
}catch(NoSuchMethodException nsme)
{
System.out.println("Write setter to inject in camel case notation : ");
return ;
}
method.invoke(obj,sessionScope);
}else if(service.getInjectRequestScope())
{
RequestScope requestScope=new RequestScope();
requestScope.setHttpServletRequest(request);
try{
method=clss.getMethod("setRequestScope",RequestScope.class);
}catch(NoSuchMethodException nsme)
{
System.out.println("Write setter to inject in camel case notation : ");
return ;
}
method.invoke(obj,requestScope);
}




//@AutoWired related code and InjectRequestParameter start's here 
if(service.getAutoWired() || service.getInjectRequestParameter())
{
String methodName;
String value;
Object object=null;
for(Field field : clss.getDeclaredFields())
{
//If of @InjectRequestParameter start's here
if(field.isAnnotationPresent(InjectRequestParameter.class))
{
value=field.getAnnotation(InjectRequestParameter.class).value();
String str=request.getParameter(value);

if(field.getType().getName().equalsIgnoreCase("java.lang.Long") || field.getType().getName().equalsIgnoreCase("long")){
object=Long.parseLong(str);
}else if(field.getType().getName().equalsIgnoreCase("java.lang.Integer")|| field.getType().getName().equalsIgnoreCase("int")){
object=Integer.parseInt(str);
}else if(field.getType().getName().equalsIgnoreCase("java.lang.Short")|| field.getType().getName().equalsIgnoreCase("short")){
object=Short.parseShort(str);
}else if(field.getType().getName().equalsIgnoreCase("java.lang.Byte")|| field.getType().getName().equalsIgnoreCase("byte")){
object=Byte.parseByte(str);
}else if(field.getType().getName().equalsIgnoreCase("java.lang.Double")|| field.getType().getName().equalsIgnoreCase("double")){
object=Double.parseDouble(str);
}else if(field.getType().getName().equalsIgnoreCase("java.lang.Float")|| field.getType().getName().equalsIgnoreCase("float")){
object=Float.parseFloat(str);
}else if(field.getType().getName().equalsIgnoreCase("java.lang.Character")|| field.getType().getName().equalsIgnoreCase("char")){
object=str.charAt(0);
}else if(field.getType().getName().equalsIgnoreCase("java.lang.Boolean")|| field.getType().getName().equalsIgnoreCase("boolean")){
object=Boolean.parseBoolean(str);
}else
{
object=str;
}

if(object!=null && field.getType().isInstance(object)){
methodName="set"+field.getName().substring(0,1).toUpperCase()+field.getName().substring(1);
Method mthd=clss.getMethod(methodName,field.getType());
mthd.invoke(obj,object);
object=null;
}
}
// If of @InjectRequestParameter end's here 






//AutoWired start's form here 
// first we check for @AutoWired annotation applied or not onto the field 
if(field.isAnnotationPresent(AutoWired.class))
{
value=field.getAnnotation(AutoWired.class).value();
object=request.getAttribute(value);
if(object!=null && field.getType().isInstance(object) )
{
System.out.println("Request scope mai mila : ");
methodName="set"+field.getName().substring(0,1).toUpperCase()+field.getName().substring(1);
Method mthd=clss.getMethod(methodName,field.getType());
mthd.invoke(obj,object);
object=null;
}

object=request.getSession().getAttribute(value);

if(object!=null && field.getType().isInstance(object))
{
System.out.println("Session scope mai mila : ");
methodName="set"+field.getName().substring(0,1).toUpperCase()+field.getName().substring(1);
Method mthd=clss.getMethod(methodName,field.getType());
mthd.invoke(obj,object);
object=null;
}

object=request.getServletContext().getAttribute(value);
if(object!=null && field.getType().isInstance(object))
{
System.out.println("Application scope mai mila : ");
methodName="set"+field.getName().substring(0,1).toUpperCase()+field.getName().substring(1);
Method mthd=clss.getMethod(methodName,field.getType());
mthd.invoke(obj,object);
object=null;
}
}
}
}



// here we are checking for parameter of service (method) , and making array of Object class which
// will store each parameter value .
Object parameters[]=new Object[service.getParameters().length];
int x=0;
for(Parameter parameter : service.getParameters())
{
String typeOfParameter=parameter.getType().getName();

if(parameter.isAnnotationPresent(RequestParameter.class))
{
RequestParameter requestParameter=(RequestParameter)parameter.getAnnotation(RequestParameter.class);

if(typeOfParameter.equalsIgnoreCase("java.lang.Long") || typeOfParameter.equalsIgnoreCase("long")){
parameters[x]=Long.parseLong(request.getParameter(requestParameter.value()));
}else if(typeOfParameter.equalsIgnoreCase("java.lang.Integer") || typeOfParameter.equalsIgnoreCase("int")){
parameters[x]=request.getAttribute(requestParameter.value());
}else if(typeOfParameter.equalsIgnoreCase("java.lang.Short") || typeOfParameter.equalsIgnoreCase("short")){
parameters[x]=request.getAttribute(requestParameter.value());
}else if(typeOfParameter.equalsIgnoreCase("java.lang.Byte") || typeOfParameter.equalsIgnoreCase("byte")){
parameters[x]=request.getAttribute(requestParameter.value());
}else if(typeOfParameter.equalsIgnoreCase("java.lang.Double") || typeOfParameter.equalsIgnoreCase("double")){
parameters[x]=request.getAttribute(requestParameter.value());
}else if(typeOfParameter.equalsIgnoreCase("java.lang.Float") || typeOfParameter.equalsIgnoreCase("float")){
parameters[x]=request.getAttribute(requestParameter.value());
}else if(typeOfParameter.equalsIgnoreCase("java.lang.Character") || typeOfParameter.equalsIgnoreCase("char")){
parameters[x]=request.getAttribute(requestParameter.value());
}else if(typeOfParameter.equalsIgnoreCase("java.lang.Boolean") || typeOfParameter.equalsIgnoreCase("boolean")){
parameters[x]=request.getAttribute(requestParameter.value());
}else{
parameters[x]=request.getAttribute(requestParameter.value());
}
}else if(typeOfParameter.equals("com.thinking.machines.webcore.pojo.RequestScope")){ 
RequestScope requestScope=new RequestScope();
requestScope.setHttpServletRequest(request);
parameters[x]=requestScope;
}else if(typeOfParameter.equals("com.thinking.machines.webcore.pojo.SessionScope")){
SessionScope sessionScope=new SessionScope();
sessionScope.setHttpSession(httpSession);
parameters[x]=sessionScope;
}else if(typeOfParameter.equals("com.thinking.machines.webcore.pojo.ApplicationScope")){
ApplicationScope applicationScope=new ApplicationScope();
applicationScope.setServletContext(request.getServletContext());
parameters[x]=applicationScope;
}
x++;
}







method=service.getService();


if(method.getReturnType().getName().equalsIgnoreCase("void")==false){
String returnType=method.getReturnType().getName();
System.out.println("Return type : "+returnType);
if(checkForReturnType(returnType)) pw.print(method.invoke(obj,parameters));
else pw.print(gson.toJson(method.invoke(obj,parameters)));
}else{
method.invoke(obj,parameters);
if(service.getForwardPath().length()!=0 && servicePath.equalsIgnoreCase(service.getForwardPath())==false)forwardRequest(service.getForwardPath(),webCoreModel,response,request);
}

}catch(Exception exception)
{
System.out.println("Exception in forwarding request : "+exception);
}
}


/**************doGet start form there ********************/
public void doGet(HttpServletRequest request,HttpServletResponse response)
{
Gson gson=new Gson();
String dataToReturn="";
PrintWriter pw;
try
{
response.setContentType("text/plain");
pw=response.getWriter();


WebCoreModel webCoreModel=(WebCoreModel)getServletContext().getAttribute("APPLICATION_RELATED_DATASTRUCTURE");


String userContext=getServletConfig().getInitParameter("URL_CONTEXT");
userContext=userContext.substring(0,userContext.indexOf("*")-1);

String contextPath=request.getContextPath()+userContext;
String uri=request.getRequestURI(); 

String pathToService=uri.substring(contextPath.length());



Service service=(Service)webCoreModel.services.get(pathToService);



if(service==null){
response.sendError(HttpServletResponse.SC_NOT_FOUND);
return ;
}







Class clss=service.getServiceClass();

Object obj=clss.newInstance();

Method method=null;

// We are storing Session and Application scope in this variable so that it can be use them where //they required 

HttpSession httpSession=request.getSession();
ServletContext servletContext=request.getServletContext();


/******if service is secured part starts here  *************/

Service securityService=null;


if(service.isSecured())
{
System.out.println("Yes service is secured : ");
//doGet
try
{
securityService=(Service)webCoreModel.services.get(service.getCheckPost());
Class securityClass=securityService.getServiceClass();
Object securityClassObject=securityClass.newInstance();

Class paramTypes[]=new Class[service.getGuardParameters().length];

int l=0;
for(Parameter p : service.getGuardParameters())
{
paramTypes[l]=p.getType();
l++;
}

Method guardMethod=securityClass.getMethod(service.getGuard(),paramTypes);


if(securityService.getInjectApplicationScope())
{
ApplicationScope aps=new ApplicationScope();
aps.setServletContext(request.getServletContext());
try{
method=clss.getMethod("setApplicationScope",ApplicationScope.class);
}catch(NoSuchMethodException nsme)
{
System.out.println("Write setter to inject in camel case notation : ");
return ;
}
method.invoke(securityClassObject,aps);
}else if(securityService.getInjectSessionScope())
{
SessionScope ss=new SessionScope();
ss.setHttpSession(request.getSession());
try{
method=clss.getMethod("setSessionScope",SessionScope.class);
}catch(NoSuchMethodException nsme)
{
System.out.println("Write setter to inject in camel case notation : ");
return ;
}
method.invoke(securityClassObject,ss);
}else if(securityService.getInjectRequestScope())
{
RequestScope rs=new RequestScope();
rs.setHttpServletRequest(request);
try{
method=clss.getMethod("setRequestScope",RequestScope.class);
}catch(NoSuchMethodException nsme)
{
System.out.println("Write setter to inject in camel case notation : ");
return ;
}
method.invoke(securityClassObject,rs);
}

method=null;




Object guardParameters[]=new Object[service.getGuardParameters().length];
int y=0;
for(Parameter p : service.getGuardParameters())
{
String top=p.getType().getName();
if(top.equals("com.thinking.machines.webcore.pojo.RequestScope")){ 
RequestScope rsc=new RequestScope();
rsc.setHttpServletRequest(request);
guardParameters[y]=rsc;
}else if(top.equals("com.thinking.machines.webcore.pojo.SessionScope")){
SessionScope ssc=new SessionScope();
ssc.setHttpSession(httpSession);
guardParameters[y]=ssc;
}else if(top.equals("com.thinking.machines.webcore.pojo.ApplicationScope")){
ApplicationScope asc=new ApplicationScope();
asc.setServletContext(request.getServletContext());
guardParameters[y]=asc;
}
y++;
}




guardMethod.invoke(securityClassObject,guardParameters);

}catch(InvocationTargetException ite)
{
System.out.println("Exception occurs during processing of guard "+ite.getMessage());
response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
}
}

/********if service is secured part end's here *********/

if(service.getInjectApplicationScope())
{
ApplicationScope applicationScope=new ApplicationScope();
applicationScope.setServletContext(request.getServletContext());
try{
method=clss.getMethod("setApplicationScope",ApplicationScope.class);
}catch(NoSuchMethodException nsme)
{
System.out.println("Write setter to inject in camel case notation : ");
return ;
}
method.invoke(obj,applicationScope);
}


if(service.getInjectSessionScope())
{
SessionScope sessionScope=new SessionScope();
sessionScope.setHttpSession(request.getSession());
try{
method=clss.getMethod("setSessionScope",SessionScope.class);
}catch(NoSuchMethodException nsme)
{
System.out.println("Write setter to inject in camel case notation : ");
return ;
}
method.invoke(obj,sessionScope);
}


if(service.getInjectRequestScope())
{
RequestScope requestScope=new RequestScope();
requestScope.setHttpServletRequest(request);
try{
method=clss.getMethod("setRequestScope",RequestScope.class);
}catch(NoSuchMethodException nsme)
{
System.out.println("Write setter to inject in camel case notation : ");
return ;
}
method.invoke(obj,requestScope);
}






//@AutoWired related code and Injecting Request Parameter 
if(service.getAutoWired() || service.getInjectRequestParameter())
{
for(Field field : clss.getDeclaredFields())
{

String methodName=null;
String value=null;
Object object=null;
//If of @InjectRequestParameter start's here
if(field.isAnnotationPresent(InjectRequestParameter.class))
{

value=field.getAnnotation(InjectRequestParameter.class).value();
String str=request.getParameter(value);

if(field.getType().getName().equalsIgnoreCase("java.lang.Long") || field.getType().getName().equalsIgnoreCase("long")){
object=Long.parseLong(str);
}else if(field.getType().getName().equalsIgnoreCase("java.lang.Integer")|| field.getType().getName().equalsIgnoreCase("int")){
object=Integer.parseInt(str);
}else if(field.getType().getName().equalsIgnoreCase("java.lang.Short")|| field.getType().getName().equalsIgnoreCase("short")){
object=Short.parseShort(str);
}else if(field.getType().getName().equalsIgnoreCase("java.lang.Byte")|| field.getType().getName().equalsIgnoreCase("byte")){
object=Byte.parseByte(str);
}else if(field.getType().getName().equalsIgnoreCase("java.lang.Double")|| field.getType().getName().equalsIgnoreCase("double")){
object=Double.parseDouble(str);
}else if(field.getType().getName().equalsIgnoreCase("java.lang.Float")|| field.getType().getName().equalsIgnoreCase("float")){
object=Float.parseFloat(str);
}else if(field.getType().getName().equalsIgnoreCase("java.lang.Character")|| field.getType().getName().equalsIgnoreCase("char")){
object=str.charAt(0);
}else if(field.getType().getName().equalsIgnoreCase("java.lang.Boolean")|| field.getType().getName().equalsIgnoreCase("boolean")){
object=Boolean.parseBoolean(str);
}else 
{
object=str;
}

if(object!=null && field.getType().isInstance(object)){
methodName="set"+field.getName().substring(0,1).toUpperCase()+field.getName().substring(1);
Method mthd=clss.getMethod(methodName,field.getType());
mthd.invoke(obj,object);
object=null;
}
}
// If of @InjectRequestParameter end's here 

// If of @AutoWired start's here 


if(field.isAnnotationPresent(AutoWired.class))
{
value=field.getAnnotation(AutoWired.class).value();



object=request.getAttribute(value);
if(object!=null && field.getType().isInstance(object) )
{
methodName="set"+field.getName().substring(0,1).toUpperCase()+field.getName().substring(1);
Method mthd=clss.getMethod(methodName,field.getType());
mthd.invoke(obj,object);
object=null;
}


// Session Scope
object=request.getSession().getAttribute(value);
if(object!=null && field.getType().isInstance(object))
{
methodName="set"+field.getName().substring(0,1).toUpperCase()+field.getName().substring(1);
Method mthd=clss.getMethod(methodName,field.getType());
mthd.invoke(obj,object);
object=null;
}

//Application Scope
object=request.getServletContext().getAttribute(value);
if(object!=null && field.getType().isInstance(object))
{
methodName="set"+field.getName().substring(0,1).toUpperCase()+field.getName().substring(1);
Method mthd=clss.getMethod(methodName,field.getType());
mthd.invoke(obj,object);
object=null;
}
}
}
}


// here we are checking for parameter of service (method) , and making array of Object class which
// will store each parameter value .
Object parameters[]=new Object[service.getParameters().length];
int x=0;
for(Parameter parameter : service.getParameters())
{
String typeOfParameter=parameter.getType().getName();

if(parameter.isAnnotationPresent(RequestParameter.class))
{
RequestParameter requestParameter=(RequestParameter)parameter.getAnnotation(RequestParameter.class);

if(typeOfParameter.equalsIgnoreCase("java.lang.Long") || typeOfParameter.equalsIgnoreCase("long")){
parameters[x]=Long.parseLong(request.getParameter(requestParameter.value()));
}else if(typeOfParameter.equalsIgnoreCase("java.lang.Integer") || typeOfParameter.equalsIgnoreCase("int")){
parameters[x]=Integer.parseInt(request.getParameter(requestParameter.value()));
}else if(typeOfParameter.equalsIgnoreCase("java.lang.Short") || typeOfParameter.equalsIgnoreCase("short")){
parameters[x]=Short.parseShort(request.getParameter(requestParameter.value()));
}else if(typeOfParameter.equalsIgnoreCase("java.lang.Byte") || typeOfParameter.equalsIgnoreCase("byte")){
parameters[x]=Byte.parseByte(request.getParameter(requestParameter.value()));
}else if(typeOfParameter.equalsIgnoreCase("java.lang.Double") || typeOfParameter.equalsIgnoreCase("double")){
parameters[x]=Double.parseDouble(request.getParameter(requestParameter.value()));
}else if(typeOfParameter.equalsIgnoreCase("java.lang.Float") || typeOfParameter.equalsIgnoreCase("float")){
parameters[x]=Float.parseFloat(request.getParameter(requestParameter.value()));
}else if(typeOfParameter.equalsIgnoreCase("java.lang.Character") || typeOfParameter.equalsIgnoreCase("char")){
parameters[x]=request.getParameter(requestParameter.value()).charAt(0);
}else if(typeOfParameter.equalsIgnoreCase("java.lang.Boolean") || typeOfParameter.equalsIgnoreCase("boolean")){
parameters[x]=Boolean.parseBoolean(request.getParameter(requestParameter.value()));
}else{
parameters[x]=request.getParameter(requestParameter.value());
}
}else if(typeOfParameter.equals("com.thinking.machines.webcore.pojo.RequestScope")){ 
RequestScope requestScope=new RequestScope();
requestScope.setHttpServletRequest(request);
parameters[x]=requestScope;
}else if(typeOfParameter.equals("com.thinking.machines.webcore.pojo.SessionScope")){
SessionScope sessionScope=new SessionScope();
sessionScope.setHttpSession(httpSession);
parameters[x]=sessionScope;
}else if(typeOfParameter.equals("com.thinking.machines.webcore.pojo.ApplicationScope")){
ApplicationScope applicationScope=new ApplicationScope();
applicationScope.setServletContext(request.getServletContext());
parameters[x]=applicationScope;
}
x++;
}






method=service.getService();


if(service.getClassRequestType().equalsIgnoreCase("Get"))
{
// control comes here , it means Get annotation is applied on class 
// now we will check , whether service of this class returning something or not 
if(method.getReturnType().getName().equalsIgnoreCase("void")==false) {
String returnType=method.getReturnType().getName();
System.out.println("Return type : "+returnType);
if(checkForReturnType(returnType)) pw.print(method.invoke(obj,parameters));
else pw.print(gson.toJson(method.invoke(obj,parameters)));
}
else {
method.invoke(obj,parameters);
if(service.getForwardPath().length()!=0 && service.getForwardPath().equalsIgnoreCase(pathToService)==false)forwardRequest(service.getForwardPath(),webCoreModel,response,request);
}
}else if(service.getClassRequestType().equalsIgnoreCase("Post")) 
{
response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
return ;
}else{
// here control comes , it means on class ,request type annotation is not applied , so there is
//probablity of having request type annotation on method itself, we will check for it now
if(service.getServiceRequestType().equalsIgnoreCase("get"))
{
if(method.getReturnType().getName().equalsIgnoreCase("void")==false){

String returnType=method.getReturnType().getName();
System.out.println("Return Type : "+returnType);

if(checkForReturnType(returnType)) pw.print(method.invoke(obj,parameters));
else pw.print(gson.toJson(method.invoke(obj,parameters)));
}else{
method.invoke(obj,parameters);
if(service.getForwardPath().length()!=0 && service.getForwardPath().equalsIgnoreCase(pathToService)==false) forwardRequest(service.getForwardPath(),webCoreModel,response,request);
}
}else if(service.getServiceRequestType().equalsIgnoreCase("post"))
{
response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
return;
}else
{
// control comes here , it means that neither on class and nor on method ,  request type annotation
// is applied , so we can invoke this method
if(method.getReturnType().getName().equalsIgnoreCase("void")==false){

String returnType=method.getReturnType().getName();
System.out.println("Return type : "+returnType);

if(checkForReturnType(returnType)) pw.print(method.invoke(obj,parameters));
else pw.print(gson.toJson(method.invoke(obj,parameters)));
}else
{
method.invoke(obj,parameters);
if(service.getForwardPath().length()!=0 && service.getForwardPath().equalsIgnoreCase(pathToService)==false) forwardRequest(service.getForwardPath(),webCoreModel,response,request);
}
}
}
}catch(Exception exception)
{
System.out.println("Web Core doGet Exception "+exception.getMessage());
}
}



public void doPost(HttpServletRequest request,HttpServletResponse response)
{
Gson gson=new Gson();

String dataToReturn="";
PrintWriter pw;
try
{

WebCoreModel webCoreModel=(WebCoreModel)getServletContext().getAttribute("APPLICATION_RELATED_DATASTRUCTURE");


// Extracting json if any 

boolean jsonPresent=false;
BufferedReader bufferedReader=request.getReader();
StringBuffer stringBuffer=new StringBuffer();
String jsonString;
while(true)
{
jsonString=bufferedReader.readLine();
if(jsonString==null) break;
stringBuffer.append(jsonString);
}

String rawData=stringBuffer.toString();

// Extracting json end's here , if any


if(rawData.length()!=0) jsonPresent=true;

// if json present in request 

Object jsonObject=null;
Class clzz=null;
if(jsonPresent)
{
String typeOfJsonObject=gson.fromJson(rawData,JsonObject.class).get("type").getAsString();
Service serv=webCoreModel.services.get(typeOfJsonObject);
if(serv==null) return ;
clzz=serv.getServiceClass();
jsonObject=gson.fromJson(rawData,clzz);
}






response.setContentType("text/plain");
pw=response.getWriter();



String userContext=getServletConfig().getInitParameter("URL_CONTEXT");
userContext=userContext.substring(0,userContext.indexOf("*")-1);



String contextPath=request.getContextPath()+userContext;    
String uri=request.getRequestURI(); 
String pathToService=uri.substring(contextPath.length());  // done done done

Service service=(Service)webCoreModel.services.get(pathToService);

if(service==null){
response.sendError(HttpServletResponse.SC_NOT_FOUND);
return ;
}





Class clss=service.getServiceClass();

Object obj=clss.newInstance();

Method method=null;



// We are storing Session and Application scope in this variable so that it can be use them where //they required 

HttpSession httpSession=request.getSession();
ServletContext servletContext=request.getServletContext();





/******if service is secured part starts here *************/

Service securityService=null;

if(service.isSecured())
{
//doPost

try
{
securityService=(Service)webCoreModel.services.get(service.getCheckPost());
Class securityClass=securityService.getServiceClass();
Object securityClassObject=securityClass.newInstance();


Class paramTypes[]=new Class[service.getGuardParameters().length];

int l=0;
for(Parameter p : service.getGuardParameters())
{
paramTypes[l]=p.getType();
l++;
}

Method guardMethod=securityClass.getMethod(service.getGuard(),paramTypes);


if(securityService.getInjectApplicationScope())
{
ApplicationScope aps=new ApplicationScope();
aps.setServletContext(request.getServletContext());
try{
method=clss.getMethod("setApplicationScope",ApplicationScope.class);
}catch(NoSuchMethodException nsme)
{
System.out.println("Write setter to inject in camel case notation : ");
return ;
}
method.invoke(securityClassObject,aps);
}else if(securityService.getInjectSessionScope())
{
SessionScope ss=new SessionScope();
ss.setHttpSession(request.getSession());
try{
method=clss.getMethod("setSessionScope",SessionScope.class);
}catch(NoSuchMethodException nsme)
{
System.out.println("Write setter to inject in camel case notation : ");
return ;
}
method.invoke(securityClassObject,ss);
}else if(securityService.getInjectRequestScope())
{
RequestScope rs=new RequestScope();
rs.setHttpServletRequest(request);
try{
method=clss.getMethod("setRequestScope",RequestScope.class);
}catch(NoSuchMethodException nsme)
{
System.out.println("Write setter to inject in camel case notation : ");
return ;
}
method.invoke(securityClassObject,rs);
}

method=null;




Object guardParameters[]=new Object[service.getGuardParameters().length];
int y=0;
for(Parameter p : service.getGuardParameters())
{
String top=p.getType().getName();
if(top.equals("com.thinking.machines.webcore.pojo.RequestScope")){ 
RequestScope rsc=new RequestScope();
rsc.setHttpServletRequest(request);
guardParameters[y]=rsc;
}else if(top.equals("com.thinking.machines.webcore.pojo.SessionScope")){
SessionScope ssc=new SessionScope();
ssc.setHttpSession(httpSession);
guardParameters[y]=ssc;
}else if(top.equals("com.thinking.machines.webcore.pojo.ApplicationScope")){
ApplicationScope asc=new ApplicationScope();
asc.setServletContext(request.getServletContext());
guardParameters[y]=asc;
}
y++;
}




guardMethod.invoke(securityClassObject,guardParameters);

}catch(InvocationTargetException ite)
{
System.out.println("Exception occurs during processing of guard "+ite.getMessage());
response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
}
}

/******if service is secured part starts here *************/





if(service.getInjectApplicationScope())
{
ApplicationScope applicationScope=new ApplicationScope();
applicationScope.setServletContext(request.getServletContext());
try{
method=clss.getMethod("setApplicationScope",ApplicationScope.class);
}catch(NoSuchMethodException nsme)
{
System.out.println("Write setter to inject in camel case notation : ");
return ;
}
method.invoke(obj,applicationScope);
}


if(service.getInjectSessionScope())
{
SessionScope sessionScope=new SessionScope();
sessionScope.setHttpSession(request.getSession());
try{
method=clss.getMethod("setSessionScope",SessionScope.class);
}catch(NoSuchMethodException nsme)
{
System.out.println("Write setter to inject in camel case notation : ");
return ;
}
method.invoke(obj,sessionScope);
}


if(service.getInjectRequestScope())
{
RequestScope requestScope=new RequestScope();
requestScope.setHttpServletRequest(request);
try{
method=clss.getMethod("setRequestScope",RequestScope.class);
}catch(NoSuchMethodException nsme)
{
System.out.println("Write setter to inject in camel case notation : ");
return ;
}
method.invoke(obj,requestScope);
}




//@AutoWired related code and InjectRequestParamter start's here
if(service.getAutoWired() || service.getInjectRequestParameter())
{
for(Field field : clss.getDeclaredFields())
{
String methodName=null;
String value=null;
Object object=null;

if(field.isAnnotationPresent(InjectRequestParameter.class))
{
value=field.getAnnotation(InjectRequestParameter.class).value();
String str=request.getParameter(value);

if(field.getType().getName().equalsIgnoreCase("java.lang.Long") || field.getType().getName().equalsIgnoreCase("long")){
object=Long.parseLong(str);
}else if(field.getType().getName().equalsIgnoreCase("java.lang.Integer")|| field.getType().getName().equalsIgnoreCase("int")){
object=Integer.parseInt(str);
}else if(field.getType().getName().equalsIgnoreCase("java.lang.Short")|| field.getType().getName().equalsIgnoreCase("short")){
object=Short.parseShort(str);
}else if(field.getType().getName().equalsIgnoreCase("java.lang.Byte")|| field.getType().getName().equalsIgnoreCase("byte")){
object=Byte.parseByte(str);
}else if(field.getType().getName().equalsIgnoreCase("java.lang.Double")|| field.getType().getName().equalsIgnoreCase("double")){
object=Double.parseDouble(str);
}else if(field.getType().getName().equalsIgnoreCase("java.lang.Float")|| field.getType().getName().equalsIgnoreCase("float")){
object=Float.parseFloat(str);
}else if(field.getType().getName().equalsIgnoreCase("java.lang.Character")|| field.getType().getName().equalsIgnoreCase("char")){
object=str.charAt(0);
}else if(field.getType().getName().equalsIgnoreCase("java.lang.Boolean")|| field.getType().getName().equalsIgnoreCase("boolean")){
object=Boolean.parseBoolean(str);
}else
{
object=str;
}
//InjectRequestParamter end's here 


if(object!=null && field.getType().isInstance(object)){
methodName="set"+field.getName().substring(0,1).toUpperCase()+field.getName().substring(1);
Method mthd=clss.getMethod(methodName,field.getType());
mthd.invoke(obj,object);
object=null;
}
}
// If of @InjectRequestParameter end's here 




// first we check for @AutoWired annotation applied or not onto the field 
if(field.isAnnotationPresent(AutoWired.class))
{
value=field.getAnnotation(AutoWired.class).value();
object=request.getAttribute(value);
if(object!=null && field.getType().isInstance(object) )
{
System.out.println("Request scope mai mila : ");
methodName="set"+field.getName().substring(0,1).toUpperCase()+field.getName().substring(1);
Method mthd=clss.getMethod(methodName,field.getType());
mthd.invoke(obj,object);
object=null;
}

object=request.getSession().getAttribute(value);

if(object!=null && field.getType().isInstance(object))
{
System.out.println("Session scope mai mila : ");
methodName="set"+field.getName().substring(0,1).toUpperCase()+field.getName().substring(1);
Method mthd=clss.getMethod(methodName,field.getType());
mthd.invoke(obj,object);
object=null;
}

object=request.getServletContext().getAttribute(value);
if(object!=null && field.getType().isInstance(object))
{
System.out.println("Application scope mai mila : ");
methodName="set"+field.getName().substring(0,1).toUpperCase()+field.getName().substring(1);
Method mthd=clss.getMethod(methodName,field.getType());
mthd.invoke(obj,object);
object=null;
}
}
}
}




// here we are checking for parameter of service (method) , and making array of Object class which
// will store each parameter value .
Object parameters[]=new Object[service.getParameters().length];
int x=0;
if(jsonPresent==false)
{
for(Parameter parameter : service.getParameters())
{
String typeOfParameter=parameter.getType().getName();
if(parameter.isAnnotationPresent(RequestParameter.class))
{
RequestParameter requestParameter=(RequestParameter)parameter.getAnnotation(RequestParameter.class);
if(typeOfParameter.equalsIgnoreCase("java.lang.Long") || typeOfParameter.equalsIgnoreCase("long")){
parameters[x]=Long.parseLong(request.getParameter(requestParameter.value()));
}else if(typeOfParameter.equalsIgnoreCase("java.lang.Integer") || typeOfParameter.equalsIgnoreCase("int")){
parameters[x]=Integer.parseInt(request.getParameter(requestParameter.value()));
}else if(typeOfParameter.equalsIgnoreCase("java.lang.Short") || typeOfParameter.equalsIgnoreCase("short")){
parameters[x]=Short.parseShort(request.getParameter(requestParameter.value()));
}else if(typeOfParameter.equalsIgnoreCase("java.lang.Byte") || typeOfParameter.equalsIgnoreCase("byte")){
parameters[x]=Byte.parseByte(request.getParameter(requestParameter.value()));
}else if(typeOfParameter.equalsIgnoreCase("java.lang.Double") || typeOfParameter.equalsIgnoreCase("double")){
parameters[x]=Double.parseDouble(request.getParameter(requestParameter.value()));
}else if(typeOfParameter.equalsIgnoreCase("java.lang.Float") || typeOfParameter.equalsIgnoreCase("float")){
parameters[x]=Float.parseFloat(request.getParameter(requestParameter.value()));
}else if(typeOfParameter.equalsIgnoreCase("java.lang.Character") || typeOfParameter.equalsIgnoreCase("char")){
parameters[x]=request.getParameter(requestParameter.value()).charAt(0);
}else if(typeOfParameter.equalsIgnoreCase("java.lang.Boolean") || typeOfParameter.equalsIgnoreCase("boolean")){
parameters[x]=Boolean.parseBoolean(request.getParameter(requestParameter.value()));
}else{
parameters[x]=request.getParameter(requestParameter.value());
}
}else if(typeOfParameter.equals("com.thinking.machines.webcore.pojo.RequestScope")){ 
RequestScope requestScope=new RequestScope();
requestScope.setHttpServletRequest(request);
parameters[x]=requestScope;
}else if(typeOfParameter.equals("com.thinking.machines.webcore.pojo.SessionScope")){
SessionScope sessionScope=new SessionScope();
sessionScope.setHttpSession(httpSession);
parameters[x]=sessionScope;
}else if(typeOfParameter.equals("com.thinking.machines.webcore.pojo.ApplicationScope")){
ApplicationScope applicationScope=new ApplicationScope();
applicationScope.setServletContext(request.getServletContext());
parameters[x]=applicationScope;
}
x++;
}
}
else if(jsonPresent==true && parameters.length==1)
{
System.out.println("One json and one prameter : ");
parameters[0]=jsonObject;
}else if(jsonPresent==true && parameters.length>1)
{
int count=0;
for(Parameter parameter : service.getParameters())
{
if(parameter.getType().getName().equalsIgnoreCase("com.thinking.machines.webcore.pojo.ApplicationScope")) {
ApplicationScope applicationScope=new ApplicationScope();
applicationScope.setServletContext(request.getServletContext());
parameters[count]=applicationScope;
}else if(parameter.getType().getName().equalsIgnoreCase("com.thinking.machines.webcore.pojo.SessionScope")) {
SessionScope sessionScope=new SessionScope();
sessionScope.setHttpSession(httpSession);
parameters[count]=sessionScope;
}else if(parameter.getType().getName().equalsIgnoreCase("com.thinking.machines.webcore.pojo.RequestScope")) {
RequestScope requestScope=new RequestScope();
requestScope.setHttpServletRequest(request);
parameters[count]=requestScope;
}else if(checkForOtherTypes(parameter,clzz)){ 
// here checkForOtherTypes() method return false if type is not compatible with json object type
parameters[count]=jsonObject;
System.out.println("yes it is json specific ");
}else{
System.out.println("Parameter is not compatible with sent JSON Object ");

 throw new ServiceException("Parameter is not compatible with sent JSON Object ");
}
count++;
}
System.out.println("One json and many parameters ");
}



method=service.getService();
if(service.getClassRequestType().equalsIgnoreCase("Post"))
{
// control comes here , it means Post annotation is applied on class 
// now we will check , whether service of this class returning something or not 
if(method.getReturnType().getName().equalsIgnoreCase("void")==false) {

String returnType=method.getReturnType().getName();
System.out.println("Return type : "+returnType);

if(checkForReturnType(returnType)) pw.print(method.invoke(obj,parameters));
else pw.print(gson.toJson(method.invoke(obj,parameters)));

}
else {
method.invoke(obj,parameters);
if(service.getForwardPath().length()!=0 && service.getForwardPath().equalsIgnoreCase(pathToService)==false)forwardRequest(service.getForwardPath(),webCoreModel,response,request);
}

}else if(service.getClassRequestType().equalsIgnoreCase("Get")) 
{
response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
return ;
}else{
// here control comes , it means on class ,request type annotation is not applied , so there is
//probablity of having request type annotation on method itself, we will check for it now
if(service.getServiceRequestType().equalsIgnoreCase("Post"))
{
if(method.getReturnType().getName().equalsIgnoreCase("void")==false){

String returnType=method.getReturnType().getName();
System.out.println("Return type : "+returnType);

if(checkForReturnType(returnType)) pw.print(method.invoke(obj,parameters));
else pw.print(gson.toJson(method.invoke(obj,parameters)));
}else{
method.invoke(obj,parameters);
if(service.getForwardPath().length()!=0 && service.getForwardPath().equalsIgnoreCase(pathToService)==false) forwardRequest(service.getForwardPath(),webCoreModel,response,request);
}

}else if(service.getServiceRequestType().equalsIgnoreCase("Get"))
{
response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
return;
}else
{
// control comes here , it means that neither on class and nor on method ,  request type annotation
// is applied , so we can invoke this method
if(method.getReturnType().getName().equalsIgnoreCase("void")==false){
String returnType=method.getReturnType().getName();
System.out.println("Return type : "+returnType);

if(checkForReturnType(returnType)) pw.print(method.invoke(obj,parameters));
else pw.print(gson.toJson(method.invoke(obj,parameters)));
}else
{
method.invoke(obj,parameters);
if(service.getForwardPath().length()!=0 && service.getForwardPath().equalsIgnoreCase(pathToService)==false) forwardRequest(service.getForwardPath(),webCoreModel,response,request);
}
}
}
}catch(Exception exception)
{
try{
System.out.println("Exception is occuring : "+exception.getMessage());
PrintWriter printWriter=response.getWriter();
Throwable actualException = exception.getCause();
System.out.println(actualException.getMessage());
printWriter.print(actualException.getMessage());
}catch(Exception ie)
{
// do nothing
}
}
}




}
