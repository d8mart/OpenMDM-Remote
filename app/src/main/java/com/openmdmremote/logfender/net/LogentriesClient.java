package com.openmdmremote.logfender.net;

import com.openmdmremote.harbor.settings.HarborServerSettings;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;


public class LogentriesClient {

	private final boolean secure;
	private String URI = HarborServerSettings.PATH_REMOTELOGGING;

	private final String host;
	private final int port;

	private HttpClient httpClient;      // HTTP client, used for communicating with HTTP API endpoint.
	private HttpPost postRequest;       // Request object, used to forward data put requests.

	private String endpointToken;   // Token, that points to the exact endpoint - the log object, where the data goes.

	// The formatter used to prepend logs with the endpoint token for Token-based input.
	private StringBuilder streamFormatter = new StringBuilder();

	public LogentriesClient(String token, String host, int port, boolean secure) throws InstantiationException, IllegalArgumentException {
		if(token == null || token.isEmpty()) {
			throw new IllegalArgumentException("Token parameter cannot be empty!");
		}

		endpointToken = token;
		this.secure = secure;
		this.host = host;
		this.port = port;
	}

	public void connect() throws IOException, IllegalArgumentException, InstantiationException {
		String prefix;
		httpClient = new DefaultHttpClient();
		prefix = "http://";
		postRequest =  new HttpPost(prefix + host + ":" + port + URI + "/" + endpointToken);
	}

	public void write(String data) throws IOException {
		postRequest.setEntity(new StringEntity(data, "UTF8"));
		httpClient.execute(postRequest);
	}
}
