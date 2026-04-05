package com.micro.web.services.webcore.servlets;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;
import javax.servlet.*;
import javax.servlet.http.*;
import com.micro.web.services.webcore.annotations.*;
import com.micro.web.services.webcore.model.*;
import com.micro.web.services.webcore.pojo.*;
public class WebCoreStartup extends HttpServlet
{

// Startup service excecuter 
public static void startupServicesExecuter(Map<Integer,Service> startupServices,ServletContext servletContext)
{
try
{
Class clss;
Method method;
List<Service> list=new ArrayList<>();

startupServices.forEach((k,service)->{
list.add(service);
});


for(Service service : list)
{
clss=service.getServiceClass();
Object obj=clss.newInstance();


if(service.getInjectApplicationScope())
{
ApplicationScope applicationScope=new ApplicationScope();
applicationScope.setServletContext(servletContext);
try{
method=clss.getMethod("setApplicationScope",ApplicationScope.class);
}catch(NoSuchMethodException nsme){
System.out.println("Write setter to inject , in camel case notation : ");
return;
}

method.invoke(obj,applicationScope);
}



//@AutoWired related code
if(service.getAutoWired())
{
for(Field field : clss.getDeclaredFields())
{
// first we check for @AutoWired annotation applied or not onto the field 
if(field.isAnnotationPresent(AutoWired.class))
{
String methodName;
String value=field.getAnnotation(AutoWired.class).value();
Object object=servletContext.getAttribute(value);
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





method=service.getService();
if(method.getReturnType().getName().equalsIgnoreCase("void")==false) {
System.out.println("Service return type should be [void] ");
break;
}else if(method.getParameterTypes().length!=0){
System.out.println("Service should not have any parameters :");
break;
}else method.invoke(obj);
}
System.out.println("All startups methods are executed , without any error :");
}catch(Exception exception)
{
System.out.println("Exception in startupServiceExecuter "+exception.getMessage());
}
}


// POJO checker 
public boolean isPojo(Class clss)
{

if(clss.isAnnotationPresent(InjectApplicationDirectory.class) || clss.isAnnotationPresent(InjectApplicationScope.class ) || clss.isAnnotationPresent(InjectRequestScope.class) || clss.isAnnotationPresent(InjectSessionScope.class) || clss.isAnnotationPresent(Post.class) || clss.isAnnotationPresent(Get.class) ) return false;

Field fields[]=clss.getFields();
for(Field f : fields) if(f.isAnnotationPresent(AutoWired.class) || f.isAnnotationPresent(InjectRequestParameter.class)) return false;


Method methods[]=clss.getDeclaredMethods();
for(Method m : methods) if(m.isAnnotationPresent(Path.class) || m.isAnnotationPresent(Get.class) || m.isAnnotationPresent(Post.class)) return false;

return true;
}

// javascript creater 
void createJavaScript(Class clss,String mainStringPath,String userContext)
{
int index=mainStringPath.indexOf("classes");
mainStringPath=mainStringPath.substring(0,index);
System.out.println(mainStringPath+"88888");
boolean flag=false;
String doubleQuote="\"";
String parts[]=clss.getName().split("\\.");
Path classPath=null;
boolean jsonInRequest=false;
boolean getType=false;
boolean postType=false;
if(clss.isAnnotationPresent(Path.class)) classPath=(Path)clss.getAnnotation(Path.class);
try
{
File fileOne=new File(mainStringPath+"JSFILES"+File.separator+"JSFILE1.js");
fileOne.createNewFile();

File fileTwo=new File(mainStringPath+"JSFILES"+File.separator+"JSFILE2.js");
fileTwo.createNewFile();

if(isPojo(clss)) 
{
// control comes here means it is pojo class (properties and setter/ getter )
Field properties[]=clss.getDeclaredFields();


/*File file=new File(mainStringPath+"JSFILES"+File.separator+"JSFILE1.js");
file.createNewFile();
*/
RandomAccessFile randomAccessFile=new RandomAccessFile(fileOne,"rw");


while(randomAccessFile.getFilePointer()<randomAccessFile.length()) randomAccessFile.readLine();
randomAccessFile.writeBytes("class "+parts[parts.length-1]+"\n");
randomAccessFile.writeBytes("{\n");
randomAccessFile.writeBytes("type;\n");
for(Field f : properties) randomAccessFile.writeBytes(f.getName()+";\n");

for(Field f : properties ) {
randomAccessFile.writeBytes("set"+f.getName().substring(0,1).toUpperCase()+f.getName().substring(1)+"("+f.getName()+")\n");
randomAccessFile.writeBytes("{\n");
randomAccessFile.writeBytes("this."+f.getName()+"="+f.getName()+";\n");
randomAccessFile.writeBytes("}\n");

randomAccessFile.writeBytes("get"+f.getName().substring(0,1).toUpperCase()+f.getName().substring(1)+"()\n");
randomAccessFile.writeBytes("{\n");
randomAccessFile.writeBytes("return this."+f.getName()+";\n");
randomAccessFile.writeBytes("}\n");
}

Path path=(Path)clss.getAnnotation(Path.class);
randomAccessFile.writeBytes("setType(type)\n");
randomAccessFile.writeBytes("{\n");
randomAccessFile.writeBytes("this.type=type;\n");
randomAccessFile.writeBytes("}\n");


randomAccessFile.writeBytes("getType()\n");
randomAccessFile.writeBytes("{\n");
randomAccessFile.writeBytes("return this.type; \n");
randomAccessFile.writeBytes("}\n");

randomAccessFile.writeBytes("}\n");
randomAccessFile.close();
}else
{ 
// control comes here means class is not a pojo it is service for which request may come from client side 
Method methods[]=clss.getDeclaredMethods();
/*
File file=new File(mainStringPath+"JSFILES"+File.separator+"JSFILE2.js");
file.createNewFile();
*/
RandomAccessFile randomAccessFile=new RandomAccessFile(fileTwo,"rw");

while(randomAccessFile.getFilePointer()<randomAccessFile.length()) randomAccessFile.readLine();

randomAccessFile.writeBytes("class "+parts[parts.length-1]+"\n");
randomAccessFile.writeBytes("{\n");

for(Method m : methods)
{
if(m.isAnnotationPresent(Path.class)==false) continue;
jsonInRequest=false;
Parameter parameters[]=m.getParameters();

flag=false;

randomAccessFile.writeBytes("static "+m.getName()+"(");
if(parameters.length!=0) 
{
for(int i=0;i<parameters.length;i++) {
if(parameters[i].isAnnotationPresent(RequestParameter.class))
{
if(i>0) randomAccessFile.writeBytes(",");
randomAccessFile.writeBytes(parameters[i].getName());
}else
{

if((parameters[i].getType().getName().equals("com.thinking.machines.webcore.pojo.ApplicationScope") || parameters[i].getType().getName().equals("com.thinking.machines.webcore.pojo.SessionScope") || parameters[i].getType().getName().equals("com.thinking.machines.webcore.pojo.RequestScope"))==true ) {
System.out.println("yaha aya "+parameters[i].getType().getName());
continue;
}
else
{
jsonInRequest=true;
System.out.println("8888 "+parameters[i].getType().getName());
randomAccessFile.writeBytes(parameters[i].getName());
}

} ///big else
}
} 
randomAccessFile.writeBytes(")\n");

randomAccessFile.writeBytes("{\n");
randomAccessFile.writeBytes("var promise=new Promise(function(resolve,reject){\n");
randomAccessFile.writeBytes("$.ajax({\n");
randomAccessFile.writeBytes("type :  ");
if(m.isAnnotationPresent(Get.class)){ 
randomAccessFile.writeBytes(doubleQuote+"GET"+doubleQuote+" ,\n");
getType=true;
}else if(m.isAnnotationPresent(Post.class)) {
postType=true;
randomAccessFile.writeBytes(doubleQuote+"POST"+doubleQuote+" , \n");
}else if(clss.isAnnotationPresent(Get.class)) {
getType=true;
randomAccessFile.writeBytes(doubleQuote+"GET"+doubleQuote+" , \n");
}else if(clss.isAnnotationPresent(Post.class)){ 
postType=true;
randomAccessFile.writeBytes(doubleQuote+"POST"+doubleQuote+" ,\n");
}else{
getType=true;
postType=true;
 randomAccessFile.writeBytes(doubleQuote+"GET"+doubleQuote+" , \n");
}


randomAccessFile.writeBytes("url : "+doubleQuote+"/"+userContext+classPath.value()+m.getAnnotation(Path.class).value());

if(jsonInRequest==false)
{
randomAccessFile.writeBytes("?");
if(parameters.length!=0)
{
for(int i=0;i<parameters.length;i++)
{
if(i>0 && parameters[i].isAnnotationPresent(RequestParameter.class)) randomAccessFile.writeBytes("+"+doubleQuote+"&");
if(parameters[i].isAnnotationPresent(RequestParameter.class)){
flag=true;
randomAccessFile.writeBytes(parameters[i].getAnnotation(RequestParameter.class).value()+"="+doubleQuote+"+"+parameters[i].getName());
}
}
}
}else
{
for(Parameter p : parameters) {
if((p.getType().getName().equals("com.thinking.machines.webcore.pojo.ApplicationScope") || p.getType().getName().equals("com.thinking.machines.webcore.pojo.SessionScope") || p.getType().getName().equals("com.thinking.machines.webcore.pojo.RequestScope"))==true ) continue;
else{
randomAccessFile.writeBytes(doubleQuote+" , \n");
randomAccessFile.writeBytes("data : JSON.stringify("+p.getName()+ ") ,\n");
randomAccessFile.writeBytes("contentType : "+doubleQuote+"application/json");
break;
}
}
}

if(flag==false) randomAccessFile.writeBytes(doubleQuote+" , \n");
else randomAccessFile.writeBytes(",\n");

String returnType=m.getReturnType().getName();

if(!(returnType.equals("java.lang.String") || returnType.equals("java.lang.Integer") || returnType.equals("java.lang.Short") || returnType.equals("java.lang.Byte") || returnType.equals("java.lang.Character") || returnType.equals("java.lang.Boolean") ||returnType.equals("long") || returnType.equals("int") ||returnType.equals("short") ||returnType.equals("byte") ||returnType.equals("double") ||returnType.equals("float")  ||returnType.equals("char")  ||returnType.equals("boolean") || returnType.equals("void")))
{
randomAccessFile.writeBytes("dataType : "+doubleQuote+"json"+doubleQuote+" , \n");
}


randomAccessFile.writeBytes("success : function(response){\n");
randomAccessFile.writeBytes("resolve(response);\n");
randomAccessFile.writeBytes("},\n");


randomAccessFile.writeBytes("error : function(error){\n");
randomAccessFile.writeBytes("reject(error);\n");
randomAccessFile.writeBytes("},\n");


randomAccessFile.writeBytes("});\n");

randomAccessFile.writeBytes("});\n");

randomAccessFile.writeBytes("return promise;\n");

randomAccessFile.writeBytes("}\n");
}

randomAccessFile.writeBytes("}\n");

randomAccessFile.close();
/**************
class StudentService
{
static add(student)
{
var promise=new Promise(function(resolve,reject){
$.ajax({
url:"/webcore/testing/student_service/add",
type:"POST",
data:JSON.stringify(student),
contentType:"application/json",
success:function(response){
resolve(response);
},
error:function(error){
reject(error);
}
});
});
return promise;
}
}
********/
}
}catch(Exception excep)
{
System.out.println(excep.getMessage());
}

}

void mergeJavaScript(String mainStringPath,String jsFileName)
{
try
{
int index=mainStringPath.indexOf("classes");
mainStringPath=mainStringPath.substring(0,index);
File jsFile=new File(mainStringPath+File.separator+"JSFILES"+File.separator+jsFileName);
jsFile.createNewFile();

RandomAccessFile randomAccessFileJsFile=new RandomAccessFile(jsFile,"rw");

File fileOne=new File(mainStringPath+"JSFILES"+File.separator+"JSFILE1.js");
RandomAccessFile randomAccessFileOne=new RandomAccessFile(fileOne,"r");


// now reading from JSFILE1.js and writing into jsFile
while(randomAccessFileOne.getFilePointer()<randomAccessFileOne.length()) randomAccessFileJsFile.writeBytes(randomAccessFileOne.readLine()+"\n");
randomAccessFileOne.close();



File fileTwo=new File(mainStringPath+"JSFILES"+File.separator+"JSFILE2.js");
RandomAccessFile randomAccessFileTwo=new RandomAccessFile(fileTwo,"r");

// now reading from JSFILE2.JS  and writing into jsFile
while(randomAccessFileTwo.getFilePointer()<randomAccessFileTwo.length()) randomAccessFileJsFile.writeBytes(randomAccessFileTwo.readLine()+"\n");
randomAccessFileTwo.close();


fileOne.delete();
fileTwo.delete();
randomAccessFileJsFile.close();
}catch(Exception exception)
{
System.out.println("File exception : "+exception.getMessage());
}
}


public void init()
{
System.out.println("HEllo i am in ");
String userContext=getServletConfig().getInitParameter("URL_CONTEXT");
userContext=userContext.substring(0,userContext.indexOf("*")-1);

String jsFileName=getServletConfig().getInitParameter("JSFILE_NAME");
/***********Data Structure to store services ***********/
WebCoreModel webCoreModel=new WebCoreModel();
/*****************************************************/


Map<Integer,Service> treeMap=new TreeMap<>();

String realPath[]=getServletContext().getRealPath("").split("\\\\");  

String mainPathString;
mainPathString=getServletContext().getRealPath("/WEB-INF/classes/");

String servicePackagePrefix=getServletConfig().getInitParameter("SERVICE_PACKAGE_PREFIX");
try
{



File servicePackagePrefixDirectory=new File(mainPathString+servicePackagePrefix);

if(servicePackagePrefixDirectory.isFile()==false && servicePackagePrefixDirectory.isDirectory()==false) System.out.println("Given service package prefix : ["+servicePackagePrefix+"] is invalid ");
Stack<File> directories=new Stack<>();
directories.push(servicePackagePrefixDirectory);

boolean secured=false;



while(directories.isEmpty()==false)
{
File file=directories.pop();
System.out.println("\n\n- - - - - - - - - \n");
System.out.println(file.getName());
for(File directory : file.listFiles())
{
if(directory.isDirectory()) directories.push(directory);
else{
String str=directory.getPath().substring(directory.getPath().indexOf(servicePackagePrefix));
if(str.endsWith(".class")){
str=str.substring(0,str.indexOf(".class")).replace(File.separator,".");
Class clss=Class.forName(str);




if(!clss.isAnnotationPresent(Path.class)) continue;



/*********creating java script ************/
createJavaScript(clss,mainPathString,realPath[realPath.length-1]+userContext);
/*********java script is created***********/

Path classPath=(Path)clss.getAnnotation(Path.class);





String requestType="";







if(clss.isAnnotationPresent(Get.class)) requestType="Get";
if(clss.isAnnotationPresent(Post.class)) requestType="Post";



boolean autoWired=false;
boolean injectRequestParameter=false;
Service service=null;


/*****this three lines is for just storing class path (not method ) **************************/
service=new Service();
service.setServiceClass(clss);
webCoreModel.services.put(classPath.value(),service);
/******************************************************************************/

// checking for AutoWired annotation
for(Field field : clss.getDeclaredFields())
{
if(field.isAnnotationPresent(AutoWired.class))
{
autoWired=true;
break;
}
}

// checking for InjectRequestParameter annotation
for(Field field : clss.getDeclaredFields())
{
if(field.isAnnotationPresent(InjectRequestParameter.class))
{
injectRequestParameter=true;
break;
}
}


service=null;
for(Method method : clss.getMethods())
{


if(!method.isAnnotationPresent(Path.class)) continue; 
Path methodPath=method.getAnnotation(Path.class);

String methodRequestType=""; 



if(method.isAnnotationPresent(Get.class)) methodRequestType="Get";
else  if(method.isAnnotationPresent(Post.class)) methodRequestType="Post";


service=new Service();

Service securedService=null;
SecuredAccess as=null;

Class clz=null;
if(clss.isAnnotationPresent(SecuredAccess.class)!=false)
{ 
as=(SecuredAccess)clss.getAnnotation(SecuredAccess.class);
clz=Class.forName(as.checkPost());

service.setSecured(true);
service.setCheckPost(as.checkPost());
service.setGuard(as.guard());
for(Method m : clz.getMethods())
{
if(m.getName().equals(as.guard())) {
service.setGuardParameter(m.getParameters());
break;
}
}
}else if(method.isAnnotationPresent(SecuredAccess.class)!=false)
{
as=(SecuredAccess)method.getAnnotation(SecuredAccess.class);
clz=Class.forName(as.checkPost());

service.setSecured(true);
service.setCheckPost(as.checkPost());
service.setGuard(as.guard());
for(Method m : clz.getMethods())
{
if(m.getName().equals(as.guard())) {
service.setGuardParameter(m.getParameters());
break;
}
}
}else
{
service.setSecured(false);
service.setCheckPost(null);
service.setGuard(null);
}

service.setServiceClass(clss);
service.setPath(classPath.value()+methodPath.value());


service.setAutoWired(autoWired);
service.setInjectRequestParameter(injectRequestParameter);                                        
service.setInjectApplicationDirectory(clss.isAnnotationPresent(InjectApplicationDirectory.class));
service.setInjectApplicationScope(clss.isAnnotationPresent(InjectApplicationScope.class));
service.setInjectSessionScope(clss.isAnnotationPresent(InjectSessionScope.class));
service.setInjectRequestScope(clss.isAnnotationPresent(InjectRequestScope.class));


if(method.isAnnotationPresent(Forward.class)) service.setForwardPath(((Forward)method.getAnnotation(Forward.class)).value());
else service.setForwardPath("");

if(method.isAnnotationPresent(Onstartup.class)){ 
service.setRunOnStartup(true);
service.setPriority(((Onstartup)method.getAnnotation(Onstartup.class)).value());
if(treeMap.containsKey(service.getPriority())) treeMap.put(service.getPriority()+1,service);
else treeMap.put(service.getPriority(),service);
}
else {
service.setRunOnStartup(false);
service.setPriority(-1);
}

Parameter parameters[]=method.getParameters();

service.setParameter(parameters);
service.setService(method);
service.setClassRequestType(requestType);
service.setServiceRequestType(methodRequestType);

//as!=null means service is secured
if(as!=null)
{
securedService=new Service();
Class clzz=Class.forName(as.checkPost());
for(Method m : clzz.getMethods()){
if(m.getName().equals(as.guard())) {
securedService.setParameter(m.getParameters());
securedService.setService(m);
}
}
securedService.setInjectApplicationScope(clzz.isAnnotationPresent(InjectApplicationScope.class));
securedService.setInjectSessionScope(clzz.isAnnotationPresent(InjectSessionScope.class));
securedService.setInjectRequestScope(clzz.isAnnotationPresent(InjectRequestScope.class));
securedService.setServiceClass(clzz);
}


webCoreModel.services.put(service.getPath(),service);
webCoreModel.services.put(service.getCheckPost(),securedService);

}
}
}
}
/***********Now putting , our webCoreModel into application scope *****************/
getServletContext().setAttribute("APPLICATION_RELATED_DATASTRUCTURE",webCoreModel);
/**********************************************************************************/
System.out.println("- - - - - - - - - \n");
}
System.out.println("Total startup services  : "+treeMap.size());
startupServicesExecuter(treeMap,getServletContext());
/*** below method merge both javascript i.e. one java script file JSFILE1 only contain pojo and another java script file JSFILE2 only contain services , so we have to merge both of them into one js file , so that it can be served to client on request ******/
mergeJavaScript(mainPathString,jsFileName);
}
catch(Exception exception)
{
System.out.println(exception);
}
System.out.println("********************************************\n");
System.out.println("All is well bro , everything is executed without error ");
System.out.println("********************************************\n");
}
public void doGet(HttpServletRequest request,HttpServletResponse response)
{
System.out.println("doGet of WebCoreStartup not yet implemented : ");
}
public void doPost(HttpServletRequest request,HttpServletResponse response)
{
System.out.println("doPost of WebCoreStartup not yet implemented : ");
}
}