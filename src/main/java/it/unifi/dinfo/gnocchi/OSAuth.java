package it.unifi.dinfo.gnocchi;

import org.openstack4j.api.OSClient;
import org.openstack4j.api.client.IOSClientBuilder;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.openstack.OSFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import java.io.IOException;

public class OSAuth implements ClientRequestFilter {
	OSClient.OSClientV3 os;
	IOSClientBuilder.V3 builder;
	Logger logger = LoggerFactory.getLogger(getClass());

	public OSAuth(String uri, String username, String password, String projectId, String domainName) {
		builder = OSFactory.builderV3()
						   .endpoint(uri)
						   .credentials(username, password, Identifier.byName(domainName))
						   .scopeToProject(Identifier.byId(projectId));
		authenticate();
	}

	public void authenticate(){
		os = builder.authenticate();
		logger.debug("Token: " + os.getToken().getId());
	}

	public OSAuth(OSClient.OSClientV3 os) {
		this.os = os;
	}

	@Override
	public void filter(ClientRequestContext clientRequestContext) throws IOException {
		clientRequestContext.getHeaders().putSingle("X-Auth-token", os.getToken().getId());
	}

	public OSClient.OSClientV3 getOs() {
		return os;
	}

}