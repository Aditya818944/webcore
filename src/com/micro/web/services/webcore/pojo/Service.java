package com.micro.web.services.webcore.pojo;
import java.lang.reflect.*;
public class Service 
{
private boolean secured;
private String checkPost;
private String guard;
private Parameter guardParameters[];
private Parameter parameters[];
private boolean autoWired;
private boolean injectRequestParameter;
private boolean injectApplicationDirectory;
private boolean injectApplicationScope;
private boolean injectSessionScope;
private boolean injectRequestScope;
private boolean runOnStartup;
private int priority;
private String forwardPath;
private boolean requestType;
private String classRequestType;
private String serviceRequestType;
private Class serviceClass;
private String path;
private Method service;

public boolean isSecured()
{
return this.secured;
}
public void setSecured(boolean secured)
{
this.secured=secured;
}
public void setCheckPost(String checkPost)
{
this.checkPost=checkPost;
}
public String getCheckPost()
{
return this.checkPost;
}
public void setGuard(String guard)
{
this.guard=guard;
}
public String getGuard()
{
return this.guard;
}
public void setGuardParameter(Parameter guardParameters[])
{
this.guardParameters=guardParameters;
}
public Parameter[] getGuardParameters()
{
return this.guardParameters;
}
public void setParameter(Parameter parameters[])
{
this.parameters=parameters;
}
public Parameter [] getParameters()
{
return this.parameters;
}
public void setAutoWired(boolean autoWired)
{
this.autoWired=autoWired;
}
public boolean getAutoWired()
{
return this.autoWired;
}

public void setInjectRequestParameter(boolean injectRequestParameter)
{
this.injectRequestParameter=injectRequestParameter;
}
public boolean getInjectRequestParameter()
{
return this.injectRequestParameter;
}

public void setInjectApplicationDirectory(boolean injectApplicationDirectory)
{
this.injectApplicationDirectory=injectApplicationDirectory;
}
public boolean getInjectApplicationDirectory()
{
return this.injectApplicationDirectory;
}
public void setInjectApplicationScope(boolean injectApplicationScope)
{
this.injectApplicationScope=injectApplicationScope;
}
public boolean getInjectApplicationScope()
{
return this.injectApplicationScope;
}
public void setInjectSessionScope(boolean injectSessionScope)
{
this.injectSessionScope=injectSessionScope;
}
public boolean getInjectSessionScope()
{
return this.injectSessionScope;
}
public void setInjectRequestScope(boolean injectRequestScope)
{
this.injectRequestScope=injectRequestScope;
}
public boolean getInjectRequestScope()
{
return this.injectRequestScope;
}
public void setRunOnStartup(boolean runOnStartup)
{
this.runOnStartup=runOnStartup;
}
public boolean getRunOnStartup()
{
return this.runOnStartup;
}
public void setPriority(int priority)
{
this.priority=priority;
}
public int getPriority()
{
return this.priority;
}
public void setForwardPath(String forwardPath)
{
this.forwardPath=forwardPath;
}
public String getForwardPath()
{
return this.forwardPath;
}
public void setRequestType(boolean requestType)
{
this.requestType=requestType;
}
public boolean getRequestType()
{
return this.requestType;
}
public void setClassRequestType(String classRequestType)
{
this.classRequestType=classRequestType;
}
public String getClassRequestType()
{
return this.classRequestType;
}
public void setServiceRequestType(String serviceRequestType)
{
this.serviceRequestType=serviceRequestType;
}
public String getServiceRequestType()
{
return this.serviceRequestType;
}
public void setServiceClass(Class serviceClass)
{
this.serviceClass=serviceClass;
}
public Class getServiceClass()
{
return this.serviceClass;
}
public void setPath(String path)
{
this.path=path;
}
public String getPath()
{
return this.path;
}
public void setService(Method service)
{
this.service=service;
}
public Method getService()
{
return this.service;
}
}