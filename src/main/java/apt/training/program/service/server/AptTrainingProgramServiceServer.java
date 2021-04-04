package apt.training.program.service.server;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import apt.training.program.service.server.catalyst.ipss.client.CatalystIPSSClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@ComponentScan
@RestController
public class AptTrainingProgramServiceServer {

	@Value("${baseUrl}")
	private String baseUrl;

	@Value("${env.dev.configuration.subscriptionid}")
	private String configurationSubscriptionId;
	@Value("${env.dev.configuration.uri}")
	String configurationUri;

	@Value("${env.dev.trainingprogram.subscriptionid}")
	private String trainingProgramSubscriptionId;
	@Value("${env.dev.trainingprogram.uri}")
	String trainingProgramUri;

	@Value("${env.dev.trainingmanagement.subscriptionid}")
	private String trainingManagementSubscriptionId;
	@Value("${env.dev.trainingmanagement.uri}")
	String trainingManagementUri;

	@Value("${tokenUrl}")
	private String tokenUrl;
	
	@Value("${keyVaultName}")
	private String keyVaultName;
	
	@Value("${env.dev.azurekey.secretName}")
	private String secretName;
	
	@Value("$env.dev.azurekey.secretValue}")
	private String secretValue;
	
	@Autowired
	CatalystIPSSClient catalystIPSSClient;

	private static final Logger logger = LoggerFactory.getLogger(AptTrainingProgramServiceServer.class);

	// sample method for Training Management Integration Point - developer can
	// change to meet requirements
	@GetMapping("/trainingManagement")
	public void getTrainingManagementDataFromCatalyst() throws IOException {

	}

	// sample method for Training Program Integration Point - developer can change
	// to meet requirements
	@GetMapping("/trainingProgram")
	public void getTrainingProgramDataFromCatalyst() throws IOException {
		logger.info("In trainingProgram......");
	}

	// sample method for Configuration Integration Point - developer can change to
	// meet requirements
	@GetMapping("/configuration")
	public void getConfigurationDataFromCatalyst() throws IOException {

	}

	// sample method for Modify Booking Integration Point - developer can change to
	// meet requirements
	@PutMapping("/modify")
	public void modifyDataInCatalyst() throws IOException {

	}

	// sample method for Cancel Booking Integration Point - developer can change to
	// meet requirements
	@PostMapping("/cancellation")
	public void cancelDataInCatalyst() throws IOException {
		String catalystEndPoint = baseUrl + "/cancellation";

		try {
			catalystIPSSClient.invokeCatalystAPIUsingHttpsUrlConn(trainingProgramSubscriptionId, catalystEndPoint,
					tokenUrl);
		} catch (Exception e) {
			logger.info("Failed to invoke Catalyst base url: {}", catalystEndPoint + e.toString());
		}

	}

	// this can be used as a temporary solution to get authentication token from
	// request header  -- this one should probably be packaged in lmp-web the rest lmp-common
	// I could not find the Authorization Token format in the output so, "Authorization" should be
	// be replaced with what is required to extract the token. That is only piece pending
	@GetMapping("/token")
	@ResponseStatus(value = HttpStatus.OK)
	public String getAuthToken(@RequestHeader("Authorization") String authenticationToken) {
		logger.info("AuthenticationToken is: {}", authenticationToken);
		return authenticationToken;
	}

	// sample generic method for invoking Catalyst API end point
	@GetMapping("/invoke")
	public String invokeCatalystApi(HttpServletRequest request) throws IOException {

		// load base url and uri from properties' file and concatenate
		String catalystEndPoint = baseUrl + "/" + configurationUri;
		Object data = null;

		try {
			// 1. Load API Key from properties' file or Azure KeyVault
			// 2. invoke Catalyst IPSS Client with API Key and end point URL
			//try invoking Catalyst API with Web Client
			data = catalystIPSSClient.invokeCatalystAPIUsingWebClient(trainingManagementSubscriptionId,
					catalystEndPoint, tokenUrl);
			//if not successful try with HTTPSUrlConnection
			if (data == null) {
				catalystIPSSClient.invokeCatalystAPIUsingHttpsUrlConn(trainingProgramSubscriptionId, catalystEndPoint,
						tokenUrl);
			}
			// 3. UnMarshall returned data as appropriate
			logger.info("Retrieved data from Catalyst API..........: {}", data);
		} catch (Exception e) {

			logger.info("Failed to invoke Catalyst baseUrl: {}", baseUrl + e.toString());
		}
		return (String) data;
	}

}
