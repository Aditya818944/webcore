package com.micro.web.services.webcore.pojo;
import javax.servlet.http.*;
public class SessionScope
{
private HttpSession httpSession;
public void setAttribute(String attribute,Object value)
{
this.httpSession.setAttribute(attribute,value);
}
public Object getAttribute(String attribute)
{
return this.httpSession.getAttribute(attribute);
}

public void setHttpSession(HttpSession httpSession)
{
this.httpSession=httpSession;
}
public HttpSession getHttpSession()
{
return this.httpSession;
}
}