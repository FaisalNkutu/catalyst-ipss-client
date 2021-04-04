package apt.training.program.service.server.catalyst.ipss.client;

import java.io.BufferedReader;

import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class CatalystIPSSClient {

	private static final Logger logger = LoggerFactory.getLogger(CatalystIPSSClient.class);

	// this can be used to store key in Azure if necessary
	public void storeKeyInAzure(String keyVaultName, String secretName, String secretValue) {
		String keyVaultUri = "https://" + keyVaultName + ".vault.azure.net";

		SecretClient secretClient = new SecretClientBuilder().vaultUrl(keyVaultUri)
				.credential(new DefaultAzureCredentialBuilder().build()).buildClient();

		logger.info("Creating a secret in " + keyVaultName + " called '" + secretName + "' with value '" + secretValue
				+ "` ... ");
		secretClient.setSecret(new KeyVaultSecret(secretName, secretValue));

	}

	// invoke Catalyst API Code from UI or URL without using web client - if web
	// client is not working
	public String invokeCatalystAPIUsingHttpsUrlConn(String trainingManagementSubscriptionId, String baseUrl,
			String tokenUrl) throws Exception {

		String authenticationToken = retrieveTokenFromUrlUsingWebClient(tokenUrl);

		logger.info("invoke Catalyst API Using HttpsUrlConn----------------------");

		URL url = new URL(baseUrl);
		HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("Cache-Control", "no-cache");
		conn.setRequestProperty("Ocp-Apim-Subscription-Key", trainingManagementSubscriptionId);
		conn.setRequestProperty("AuthenticationToken", authenticationToken);

		String result = null;
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line);
				logger.debug("Result from Catalyst API: " + line);
			}
			result = sb.toString();
			br.close();
		} catch (Exception ex) {
			logger.error("Failed to invoke Catalyst API with HTTPS Url Connection: " + ex.toString());
		}

		return result;
	}

	// invoke Catalyst API Code from UI or URL using web client
	public Object invokeCatalystAPIUsingWebClient(String trainingManagementSubscriptionId, String catalystEndpoint,
			String tokenUrl) throws Exception {

		Flux<Object> catalystData = null;
		try {
			logger.info("invoke Catalyst API using WebClient with token from........................" + tokenUrl);
			String authenticationToken = retrieveTokenFromUrlUsingWebClient(tokenUrl);
			logger.info("Retrieved token ..........." + authenticationToken);
			WebClient client = WebClient.create();
			ClientResponse results = client.get().uri(URI.create(catalystEndpoint))
					.header("Ocp-Apim-Subscription-Key", trainingManagementSubscriptionId)
					.header("AuthenticationToken", authenticationToken).exchange().block();
			catalystData = results.bodyToFlux(Object.class);
		} catch (Exception ex) {
			logger.error("Failed to invoke Catalyst API with Webclient: " + ex.toString());
		}

		return catalystData;
	}

	// this can be used if Spring Web Client does not work
	/*
	 * public static String retrieveTokenFromUrlHeaderUsingHttpsUrlConn(Void...
	 * arg0) { HttpURLConnection con = null; String urlString =
	 * "http://localhost:8080/token";
	 * 
	 * try { URL url = new URL(urlString); con = (HttpURLConnection)
	 * url.openConnection(); con.setReadTimeout(15000);
	 * con.setConnectTimeout(10000); con.setRequestMethod("GET");
	 * 
	 * // set input and output con.setDoInput(true);
	 * 
	 * System.out.println("connecting"); con.connect();
	 * System.out.println("connect ");
	 * 
	 * BufferedReader br = new BufferedReader(new
	 * InputStreamReader(con.getInputStream(), "UTF-8")); StringBuilder sb = new
	 * StringBuilder(); String line = null; while ((line = br.readLine()) != null) {
	 * 
	 * System.out.println(line); }
	 * 
	 * String json = sb.toString(); br.close(); return json;
	 * 
	 * } catch (SocketTimeoutException a) { a.getMessage(); } catch (IOException b)
	 * { b.getMessage(); } catch (Exception e) { e.getMessage(); } finally {
	 * System.out.println("successful"); } return null; }
	 */
	
	public static String retrieveTokenFromUrlUsingWebClient(String tokenUrl) {
		logger.info("Retrieving token from Url........................" + tokenUrl);
		WebClient client = WebClient.create();
		ClientResponse results = client.get().uri(URI.create(tokenUrl)).exchange().block();
		Flux<String> authToken = results.bodyToFlux(String.class);
		return authToken.toString();

	}

	// this method can potentially be used to retrieve key from Azure if necessary
	public String retrieveKeyFromAzure(String keyVaultName, String secretName) {
		String keyVaultUri = "https://" + keyVaultName + ".vault.azure.net";

		SecretClient secretClient = new SecretClientBuilder().vaultUrl(keyVaultUri)
				.credential(new DefaultAzureCredentialBuilder().build()).buildClient();

		logger.info("Retrieving Secret ... ");
		KeyVaultSecret retrievedSecret = secretClient.getSecret(secretName);
		return retrievedSecret.getValue();
	}

	// more details are needed as to how to publish an API
	// this is just a place holder until more information is provided
	public static String publishApiToCatalystUsingWebClient(String trainingManagementSubscriptionId, String baseURL,
			String authenticationToken) throws Exception {

		int toBeSent = 1;
		Flux<String> flux = Flux.range(1, toBeSent).map(count -> "{\"content\":" + "\"" + count + "\"}");

		WebClient client = WebClient.create();
		ClientResponse result = client.post().uri(URI.create("http://localhost:8080/cancellation"))
				.contentType(MediaType.APPLICATION_JSON).body(flux, String.class).exchange().block();
		Mono<String> response = result.bodyToMono(String.class);
		return response.toString();
	}
}
