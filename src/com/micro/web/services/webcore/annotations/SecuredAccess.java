package com.micro.web.services.webcore.annotations;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.TYPE})
public @interface SecuredAccess
{
String checkPost();
String guard();
}