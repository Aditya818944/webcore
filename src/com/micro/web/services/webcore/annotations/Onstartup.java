package com.micro.web.services.webcore.annotations;
import java.lang.annotation.*;
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Onstartup
{
int value();
}