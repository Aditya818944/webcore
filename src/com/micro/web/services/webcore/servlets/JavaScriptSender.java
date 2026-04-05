package com.micro.web.services.webcore.servlets;
import javax.servlet.http.*;
import javax.servlet.*;
import java.io.*;
public class JavaScriptSender extends HttpServlet
{
public void doGet(HttpServletRequest request,HttpServletResponse response)
{
try
{
PrintWriter pw=response.getWriter();
response.setContentType("application/javascript");

File file=new File(getServletContext().getRealPath("/WEB-INF/JSFILES/")+request.getParameter("name"));
if(file.exists()==false) pw.println("// no java script available ");
else{
RandomAccessFile randomAccessFile=new RandomAccessFile(file,"r");
while(randomAccessFile.getFilePointer()<randomAccessFile.length()) pw.print(randomAccessFile.readLine());
randomAccessFile.close();
}
}catch(Exception exception)
{

}
}
public void doPost(HttpServletRequest request,HttpServletResponse response)
{
doGet(request,response);
}
}
