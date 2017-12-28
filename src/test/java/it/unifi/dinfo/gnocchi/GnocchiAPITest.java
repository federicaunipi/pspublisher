package it.unifi.dinfo.gnocchi;

import org.junit.Test;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.model.compute.Server;
import org.openstack4j.openstack.OSFactory;

import java.util.List;

public class GnocchiAPITest {

	@Test
	public void test(){
		OSClient.OSClientV3 client = OSFactory.builderV3()
											   .endpoint("http://192.168.9.131/identity/v3")
											   .credentials("admin", "password", Identifier
													   .byName("default"))
											   .scopeToProject(Identifier
													   .byId("9674deaa2c634743b089234a8226630a"))
											   .authenticate();
		List<? extends Server> list = client.compute().servers().list();
		System.out.println(list.get(0).getStatus().equals(Server.Status.ACTIVE));
	}



}