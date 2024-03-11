package com.CollabCraft.WebSocket;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;


public class WebSocketInterceptor extends HttpSessionHandshakeInterceptor {

    private final static Logger log=Logger.getLogger(WebSocketInterceptor.class);

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler handler, Exception exceptions) {
        log.info("================= afterHandshake ：handler: "+handler+"exceptions: "+exceptions);
        super.afterHandshake(request, response, handler, exceptions);
    }


    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler handler,
                                   Map<String, Object> map) throws Exception {
        log.info("================== beforeHandshake ：handler: "+handler+"map: "+map.values());
        HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
        String id = servletRequest.getSession().getId();
        System.out.println("beforeHandshake: \n"+id);
        String username = servletRequest.getParameter("username");
        map.put("username",username);
        String docName = servletRequest.getParameter("docName");
        map.put("docName",docName);
        String docOwner = servletRequest.getParameter("docOwner");
        map.put("docOwner",docOwner);

        return super.beforeHandshake(request, response, handler, map);
    }

}