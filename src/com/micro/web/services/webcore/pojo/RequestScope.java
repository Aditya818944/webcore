package com.micro.web.services.webcore.pojo;
import javax.servlet.http.*;
public class RequestScope
{
private HttpServletRequest httpServletRequest;
public void setAttribute(String attribute,Object value)
{
this.httpServletRequest.setAttribute(attribute,value);
}
public Object getAttribute(String attribute)
{
return this.httpServletRequest.getAttribute(attribute);
}


public void setHttpServletRequest(HttpServletRequest httpServletRequest)
{
this.httpServletRequest=httpServletRequest;
}
public HttpServletRequest getHttpServletRequest()
{
return this.httpServletRequest;
}
}