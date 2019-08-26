import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.Asserts;
import org.apache.http.util.EntityUtils;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "sentryexport")
public class Main implements Callable<Integer> {
	private static final String HTML_SPL_CHAR = "<|>|;";
	private static final String SPACE = " ";
	private static final String EMPTY = "";
	@CommandLine.Option(names = "-p", description = "Sentry Project", required = true)
	private String sentryProject;
	@CommandLine.Option(names = "-h", description = "Sentry Host", required = true)
	private String sentryHost;
	@CommandLine.Option(names = "-d", description = "Date (YYYY-MM-DD) till when data needs to be exported", required = true)
	private String tillDate;
	@CommandLine.Option(names = "-t", description = "Time (HH:MI:SS) till when data needs to be exported, by default 00:00:00")
	private String tillTime = "00:00:00";
	@CommandLine.Option(names = "-f", description = "Json file in which data to be exported, e.g. data.json")
	private String fileName = "data.json";
	@CommandLine.Option(names = "-a", description = "Auth Token of Sentry", required = true)
	private String authToken;
	@CommandLine.Option(names = "-c", description = "starting point for export, skip to download from current date")
	private String startingPath;

	private String notYetSavedURL;

	public static void main(String[] args) {
		int exitCode = new CommandLine(new Main()).execute(args);
		System.exit(exitCode);
	}

	@Override
	public Integer call() throws Exception {
		ArrayNode finalData = new ArrayNode(JsonNodeFactory.instance);
		String url = getUrl();
		ObjectMapper mapper = new ObjectMapper();
		boolean done = false;
		LocalDateTime localDateTime1 = getUntilDateTime();

		try {
			try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
				while (!done) {
					HttpGet httpget = getHttpGet(url);
					try (CloseableHttpResponse response = httpclient.execute(httpget)) {
						StatusLine statusLine = response.getStatusLine();
						Asserts.check(statusLine.getStatusCode() == 200, "Response Error: " + statusLine);
						String json = EntityUtils.toString(response.getEntity());
						ArrayNode actualObj = (ArrayNode) mapper.readTree(json);
						String dateCreated = actualObj.get(actualObj.size() - 1).get("dateCreated").asText();
						System.out.println("Exported Data Until: " + dateCreated);
						LocalDateTime localDateTime = getLocalDateTime(dateCreated);
						finalData.addAll(actualObj);
						done = !response.containsHeader("Link") || localDateTime.isBefore(localDateTime1);
						if (!done) {
							url = getNextLink(response.getFirstHeader("Link").getValue());
							if (finalData.size() > 5000) {
								mapper.writeValue(new File(fileName + "_" + dateCreated + ".json"), finalData);
								finalData.removeAll();
								notYetSavedURL = url;
							}
						}

					}
				}
			}
			mapper.writeValue(new File(fileName + "_" + tillDate + ".json"), finalData);
		} catch (IOException e) {
			System.out.println("restart from url, " + url);
			e.printStackTrace();
			return -1;
		}
		return 0;
	}

	private String getUrl() {
		return String.format("https://%s/api/0/projects/%s/events/%s", sentryHost, sentryProject, Objects.nonNull(startingPath) ? "?&cursor=" + startingPath : "");
	}

	private LocalDateTime getLocalDateTime(String dateCreated) {
		Instant instant = Instant.parse(dateCreated);
		return LocalDateTime.ofInstant(instant, ZoneId.of(ZoneOffset.UTC.getId()));
	}

	private String getNextLink(String link) {
		String[] split = link.split(SPACE);
		return split[4].replaceAll(HTML_SPL_CHAR, EMPTY);
	}

	private HttpGet getHttpGet(String url) {
		HttpGet httpget = new HttpGet(url);
		httpget.addHeader("Authorization", "Bearer " + authToken);
		return httpget;
	}

	private LocalDateTime getUntilDateTime() {
		Instant instant1 = Instant.parse(tillDate + "T" + tillTime + ZoneOffset.UTC.getId());
		return LocalDateTime.ofInstant(instant1, ZoneId.of(ZoneOffset.UTC.getId()));
	}
}
