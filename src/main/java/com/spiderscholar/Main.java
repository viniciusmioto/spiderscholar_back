package com.spiderscholar;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

public class Main {

	private static final String OPENALEX_WORK_URL = "https://api.openalex.org/works/doi:";
	private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10))
			.build();
	private static final ObjectMapper MAPPER = new ObjectMapper();

	/**
	 * Retrieves the title of a paper from OpenAlex using its DOI.
	 *
	 * @param doi the DOI string (can include the "https://doi.org/" prefix)
	 * @return the paper title, or null if not found
	 * @throws Exception if the API request fails or JSON parsing fails
	 */
	public static String getTitleFromDoi(String doi) throws Exception {
		String normalizedDoi = normalizeDoi(doi);
		String encodedDoi = URLEncoder.encode(normalizedDoi, StandardCharsets.UTF_8);
		String url = OPENALEX_WORK_URL + encodedDoi;

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.timeout(Duration.ofSeconds(30))
				.header("Accept", "application/json").GET().build();

		HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() != 200) {
			throw new RuntimeException("OpenAlex API returned status " + response.statusCode());
		}

		JsonNode root = MAPPER.readTree(response.body());
		return Optional.ofNullable(root.path("title").asText()).orElse(null);
	}

	/**
	 * Removes common DOI prefixes so only the bare identifier remains.
	 */
	private static String normalizeDoi(String doi) {
		if (doi == null) {
			return null;
		}
		String trimmed = doi.trim();
		if (trimmed.startsWith("https://doi.org/")) {
			return trimmed.substring("https://doi.org/".length());
		}
		if (trimmed.startsWith("doi:")) {
			return trimmed.substring("doi:".length());
		}
		return trimmed;
	}

	// Example usage
	public static void main(String[] args) throws Exception {
		String doi = "https://doi.org/10.5753/brasnam.2025.8329"; // example DOI
		String title = getTitleFromDoi(doi);
		System.out.println("Title: " + title);
	}
}