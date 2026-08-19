package com.hotel.controller;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named="HMS_IT",matches="1")
class ManagerWebSmokeTest {
    private static Tomcat tomcat;private static HttpClient client;private static String base;
    @BeforeAll static void start()throws Exception{
        Path baseDir=Path.of("target","tomcat-manager-it").toAbsolutePath();Files.createDirectories(baseDir);
        tomcat=new Tomcat();tomcat.setBaseDir(baseDir.toString());tomcat.setPort(0);tomcat.getConnector();
        Path webapp=Path.of("src","main","webapp").toAbsolutePath();Context context=tomcat.addWebapp("/HotelManagement",webapp.toString());
        StandardRoot resources=new StandardRoot(context);resources.addPreResources(new DirResourceSet(resources,"/WEB-INF/classes",Path.of("target","classes").toAbsolutePath().toString(),"/"));context.setResources(resources);context.setParentClassLoader(Thread.currentThread().getContextClassLoader());
        tomcat.start();base="http://127.0.0.1:"+tomcat.getConnector().getLocalPort()+"/HotelManagement";CookieManager cookies=new CookieManager(null,CookiePolicy.ACCEPT_ALL);client=HttpClient.newBuilder().cookieHandler(cookies).followRedirects(HttpClient.Redirect.NEVER).build();
    }
    @AfterAll static void stop()throws Exception{if(tomcat!=null){tomcat.stop();tomcat.destroy();}}

    @Test void managerLoginNavigationRbacAndLogoutWorkEndToEnd()throws Exception{
        HttpResponse<String> login=get("/login");assertEquals(200,login.statusCode());assertTrue(login.body().contains("Đăng nhập"));
        String form="email="+encode("manager.demo@hotel.vn")+"&password="+encode("Manager@123");HttpRequest request=HttpRequest.newBuilder(URI.create(base+"/login")).header("Content-Type","application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(form)).build();HttpResponse<String> response=client.send(request,HttpResponse.BodyHandlers.ofString());assertEquals(302,response.statusCode());assertTrue(response.headers().firstValue("location").orElse("").endsWith("/manager/dashboard"));
        for(String route:new String[]{"/manager/dashboard","/manager/rooms","/manager/room-types","/manager/pricing","/manager/housekeeping","/manager/maintenance","/manager/reports","/profile"})assertEquals(200,get(route).statusCode(),route);
        HttpResponse<String> dashboard=get("/manager/dashboard");assertTrue(dashboard.body().contains("Manager Dashboard"));
        HttpResponse<String> pricing=get("/manager/pricing");assertTrue(pricing.body().contains("Bảng giá loại phòng"));assertTrue(pricing.body().contains("Manager Demo Standard"));assertFalse(pricing.body().contains("Manager Demo Suite (Inactive)"));
        HttpResponse<String> search=get("/rooms");assertEquals(200,search.statusCode());assertTrue(search.body().contains("Kết quả ("));assertTrue(search.body().contains("Tối đa 25 dòng/trang"));
        HttpResponse<String> forbidden=get("/reception/checkin");assertEquals(403,forbidden.statusCode());assertTrue(forbidden.body().contains("You do not have permission"));
        HttpResponse<String> logout=get("/logout");assertEquals(302,logout.statusCode());assertTrue(logout.headers().firstValue("location").orElse("").endsWith("/login"));assertEquals(302,get("/manager/dashboard").statusCode());
        if("1".equals(System.getenv("HMS_BROWSER_IT")))Thread.sleep(180_000);
    }
    private static HttpResponse<String> get(String path)throws Exception{return client.send(HttpRequest.newBuilder(URI.create(base+path)).GET().build(),HttpResponse.BodyHandlers.ofString());}
    private static String encode(String value){return URLEncoder.encode(value,StandardCharsets.UTF_8);}
}
