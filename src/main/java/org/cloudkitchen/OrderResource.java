package org.airfactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/order")
public class OrderResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/new")
    public String newOrder() {
        return "hello";
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/new")
    public String disableOrder() {
        return "hello";
    }


    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/new")
    public String completeOrder() {
        return "hello";
    }

}
