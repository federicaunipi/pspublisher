package it.unifi.dinfo.gnocchi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.client.HttpResponseException;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.JerseyWebTarget;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("Duplicates")
public class GnocchiApiSimple implements GnocchiAPI {

	private String id;
	private Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private JerseyWebTarget gnocchiClient;
	private ObjectMapper mapper = new ObjectMapper();
	private Logger logger = LoggerFactory.getLogger(getClass());
	private TrafficController trafficController = new TrafficController();

	public GnocchiApiSimple(String host, String username, String password) {
		HttpAuthenticationFeature authenticator = HttpAuthenticationFeature.basic(username, password);
		String gnocchi_uri = "http://" + host + ":8041/v1";
		gnocchiClient = JerseyClientBuilder.createClient().register(authenticator).target(gnocchi_uri);
		gnocchiClient.property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true);
		try {
			createResource();
		} catch (HttpResponseException e) {
			logger.warn("Error while initializing resource", e);
		}
	}

	@Override
	public void pushMeasurement(List<Measurement> measurementList) {
		ObjectNode root = mapper.createObjectNode();
		ObjectNode metrics = mapper.createObjectNode();
		for (Measurement m : measurementList) {
			ArrayNode measurements = mapper.createArrayNode();
			ObjectNode measure = mapper.createObjectNode();
			measure.put("timestamp", m.getTimestamp());
			measure.put("value", m.getValue());
			measurements.add(measure);
			metrics.set(m.getName(), measurements);
			if(CliHelperLola.getCli().delay && m.getName().equals("processing_time")) {
				try {
					trafficController.addDelay(m.getValue()*1000);
				} catch (IOException e) {
					logger.error("Error while setting delay: {}",e.getMessage());
				}
			}
			logger.debug("{}: {}",m.getName(),m.getValue());
		}
		root.set(id, metrics);
		Entity<JsonNode> entity = Entity.json(root);
		Response resp;
		resp = gnocchiClient.path("batch/resources/metrics/measures").request().post(entity);
		if (resp.getStatus() > 210) {
			throw new RuntimeException("Error while adding measurement (" + resp.getStatus() + ")");
		} else {
			logger.debug(resp.getStatusInfo().toString());
		}
	}


	@Override
	public void pushMeasurement(Measurement measurement) {
		Entity body = createEntity(measurement);
		Response resp;
		resp = gnocchiClient.path("batch/resources/metrics/measures").request()
							.post(body);
		if (resp.getStatus() > 210) {
			throw new RuntimeException("Error while adding measurement (" + resp.getStatus() + ")");
		} else {
			logger.debug(resp.toString());
		}
	}


	private void createResource() throws HttpResponseException {
		id = getIdFromNameAndDc(CliHelperLola.getCli().name, CliHelperLola.getCli().datacenter);
		if (id != null) {
			logger.info("Resource already exists: {}", id);
			patchProcessingCapacity();
			return;
		}
		Entity<ObjectNode> resourceCreationEntity = createResourceEntity();
		Response post = gnocchiClient.path("resource/vxf").request().post(resourceCreationEntity);
		if (post.getStatus() < 300) {
			logger.info(post.readEntity(JsonNode.class).toString());
		} else {
			throw new HttpResponseException(post.getStatus(), "Error while creating resource: " + post.getEntity()
																									  .toString());
		}
	}

	private Entity<ObjectNode> createResourceEntity() {
		ObjectNode json = mapper.createObjectNode();
		json.put("vxf_name", CliHelperLola.getCli().name);
		json.put("datacenter", CliHelperLola.getCli().datacenter);
		json.put("capacity", CliHelperLola.getCli().capacity);
		json.put("timestamp", formatter.format(new Date()));
		json.put("id", String.format("%s.%s",CliHelperLola.getCli().name,CliHelperLola.getCli().datacenter));
		ObjectNode metrics = mapper.createObjectNode();

		ObjectNode processing_time = mapper.createObjectNode();
		processing_time.put("archive_policy_name", CliHelperLola.getCli().policy);
		metrics.set("processing_time", processing_time);

		ObjectNode incoming_rate_bytes = mapper.createObjectNode();
		incoming_rate_bytes.put("archive_policy_name", CliHelperLola.getCli().policy);
		metrics.set("incoming_rate_bytes", incoming_rate_bytes);

		ObjectNode incoming_rate_pkts = mapper.createObjectNode();
		incoming_rate_pkts.put("archive_policy_name", CliHelperLola.getCli().policy);
		metrics.set("incoming_rate_pkts", incoming_rate_pkts);

		json.set("metrics", metrics);
		return Entity.json(json);
	}


	private int patchProcessingCapacity() {
		ObjectNode objectNode = mapper.createObjectNode();
		objectNode.put("capacity", CliHelperLola.getCli().capacity);
		logger.debug("Pushing processing capacity: {} Mbits", CliHelperLola.getCli().capacity);
		Response patch = gnocchiClient.path("resource/vxf").path(id).request()
									  .method("PATCH", Entity.json(objectNode));
		return patch.getStatus();
	}


	@Override
	public void pushProcessingCapacity() {
		//http://192.168.9.121:8041/v1/resource/instance
		ObjectNode vnf = mapper.createObjectNode();
		vnf.put("processing_capacity", CliHelperLola.getCli().capacity);
		Entity<ObjectNode> json = Entity.json(vnf);
		Response resp = gnocchiClient.path("resource/vnf").path(id).request().post(json);
	}

	private Entity createEntity(Measurement measurement) {
		ObjectNode resources = mapper.createObjectNode();
		ObjectNode resource = mapper.createObjectNode();
		ArrayNode measurements = mapper.createArrayNode();
		JsonNode measurementNode = mapper.convertValue(measurement, JsonNode.class);
		measurements.add(measurementNode);
		resource.set(measurement.getName(), measurements);
		resources.set(id, resource);
		try {
			logger.debug(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(resources));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return Entity.json(resources);
	}

	public String getIdFromNameAndDc(String name, String dc) {
		Map<String, String> filters = new HashMap<>();
		filters.put("vxf_name", name);
		filters.put("datacenter", dc);
		JsonNode requestFilter = equalityFilter(filters);
		Entity<JsonNode> json = Entity.json(requestFilter);
		JsonNode resp = gnocchiClient.path("search/resource/vxf").request().post(json).readEntity(JsonNode.class);
		if (resp.size() == 0) {
			return null;
		}
		return resp.get(0).get("id").asText();
	}

	private JsonNode equalityFilter(Map<String, String> filters) {
		ObjectNode root = mapper.createObjectNode();
		ArrayNode filterArray = mapper.createArrayNode();
		for (Map.Entry<String, String> e : filters.entrySet()) {
			ObjectNode filter = mapper.createObjectNode();
			ObjectNode equalityContainer = mapper.createObjectNode();
			filter.put(e.getKey(), e.getValue());
			equalityContainer.set("=", filter);
			filterArray.add(equalityContainer);
		}
		root.set("and", filterArray);
		return root;
	}
}
