package com.proclos.colibriweb.session.system;

import org.atmosphere.config.service.PathParam;
import org.primefaces.push.annotation.OnMessage;
import org.primefaces.push.annotation.PushEndpoint;
import org.primefaces.push.impl.JSONEncoder;

@PushEndpoint("/{event}/{component}")
public class LogPushResource {
	
	@PathParam("event")
    private String event;
 
    @PathParam("component")
    private String component;
    
    @OnMessage(encoders = {JSONEncoder.class})
    public String onMessage(String message) {
        return message;
    }

}
