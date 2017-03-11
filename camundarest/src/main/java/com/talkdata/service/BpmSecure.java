package com.talkdata.service;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.RunAs;
import javax.ejb.Stateless;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.security.auth.login.LoginContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.camunda.bpm.engine.variable.VariableMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;



/**
 * Set of secure ReST examples for interacting with the Camunda engine 
 * 
 * @author GaryS
 *
 */
@RequestScoped
@Path("bpmsecure")
@DeclareRoles({"myusergroup" }) 
public class BpmSecure {

	/**
	 * Logging 
	 */
	private final Logger LOGGER = Logger.getLogger(Bpm.class.getName());
	
	
	/**
	 * Access to the Camunda Process API
	 */
	@Inject
	private RuntimeService runtimeService;
	
	
	/**
	 * 
	 * 
	 * 
	 * @param hello
	 * @return
	 */
	@GET
	@Path("echoget/{hello}")
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public String echoGet(@PathParam("hello") String hello) {

		LOGGER.info("*** echoget - hello: " + hello);

		// assemble a basic JSON reply and return
		String echoReply = "{\"echoback\": \"" + hello + "\"}";

		return echoReply;
	}
	
	/**
	 * 
	 */
	@GET
	@Path("secureechoget/{hello}")
	@RolesAllowed("myusergroup") 
	@Produces(MediaType.APPLICATION_JSON)
	public String secureEchoGet(@PathParam("hello") String hello) {

		LOGGER.info("*** echoget - hello: " + hello);
		
		
		//LoginContext loginContext = new LoginContext
		
		

		// assemble a basic JSON reply and return
		String echoReply = "{\"echoback\": \"" + hello + "\"}";

		return echoReply;
	}
	
	
	
	/**
	 * 
	 * @param hello
	 * @param security
	 * @return
	 */
	@GET
	@Path("reviewcontext/{hello}")
	@RolesAllowed("myusergroup") 
	@Produces(MediaType.APPLICATION_JSON)
	public String reviewContext(@PathParam("hello") String hello,
								@Context SecurityContext security) {

		LOGGER.info("*** reviewContext - hello: " + hello);
		
		String loginName = security.getUserPrincipal().getName();
		LOGGER.info("*** reviewContext - getName: " + loginName);
				

		// assemble a basic JSON reply and return
		String echoReply = "{\"echoback\": \"" + hello + "\"}";

		return echoReply;
	}
	
}
