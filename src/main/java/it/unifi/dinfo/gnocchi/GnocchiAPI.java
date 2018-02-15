package it.unifi.dinfo.gnocchi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.JerseyWebTarget;
import org.openstack4j.api.exceptions.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

public class GnocchiAPI {

	private JerseyWebTarget gnocchiClient;
	private ObjectMapper mapper = new ObjectMapper();
	private OSAuth osAuth;
	private Logger logger = LoggerFactory.getLogger(getClass());

	public GnocchiAPI(String host, String username, String password, String projectId, String domainName) {
		String openstack_uri = "http://"+host+"/identity/v3";
		String gnocchi_uri = "http://"+host+":8041/v1";
		osAuth = new OSAuth(openstack_uri, username, password, projectId, domainName);
		gnocchiClient = JerseyClientBuilder.createClient().target(gnocchi_uri).register(osAuth);
	}

	public void pushMeasurement(Measurement measurement) {
		Entity body = createEntity(measurement);
		Response resp;
		resp = gnocchiClient.path("batch/resources/metrics/measures").queryParam("create_metrics","true").request().post(body);
		if(resp.getStatus() > 400){
			logger.warn("Reauthenticating");
			osAuth.authenticate();
			resp = gnocchiClient.path("batch/resources/metrics/measures").queryParam("create_metrics","true").request().post(body);
		}
		if(resp.getStatus()>210){
			throw new RuntimeException("Error while adding measurement ("+resp.getStatus()+")");
		}
		else {
			logger.debug(resp.toString());
		}
	}

	public boolean checkIfInstanceExists(){
		try {
			Response response = gnocchiClient.path("resource/vnf").path(CliHelper.getCli().instance).request().get();
			return response.getStatus() == 200;
		}
		catch (ConnectionException e){
			logger.error(e.getMessage());
		}
		return false;
	}

	public void pushProcessingCapacity(){
		Double proccessing_capacity = CliHelper.getCli().proccessing_capacity;
		String instance = CliHelper.getCli().instance;
		//http://192.168.9.121:8041/v1/resource/instance
		ObjectNode vnf = mapper.createObjectNode();
		vnf.put("processing_capacity", CliHelper.getCli().proccessing_capacity);
		Entity<ObjectNode> json = Entity.json(vnf);
		Response resp = gnocchiClient.path("resource/vnf").path(instance).request().post(json);
	}

	private Entity createEntity(Measurement measurement){
		ObjectNode resources = mapper.createObjectNode();
		ObjectNode resource = mapper.createObjectNode();
		ArrayNode measurements = mapper.createArrayNode();
		JsonNode measurementNode = mapper.convertValue(measurement, JsonNode.class);
		measurements.add(measurementNode);
		resource.set(CliHelper.getCli().metric,measurements);
		resources.set(CliHelper.getCli().instance, resource);
		try {
			logger.debug(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(resources));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return Entity.json(resources);
	}
}
