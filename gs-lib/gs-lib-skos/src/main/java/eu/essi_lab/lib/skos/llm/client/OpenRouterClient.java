package eu.essi_lab.lib.skos.llm.client;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import eu.essi_lab.lib.skos.llm.model.*;
import eu.essi_lab.lib.utils.*;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.time.*;
import java.util.*;

/**
 * Thin wrapper around OpenRouter's {@code /chat/completions} endpoint.
 * <p>OpenRouter is a drop-in-compatible superset of the OpenAI Chat
 * Completions API: same endpoint shape, same auth header style, same request/response schema. The only OpenRouter-specific bits are the
 * base URL and the "provider/model" naming convention for the {@code model} field. That's why this uses the plain JDK {@link HttpClient}
 * rather than a dedicated SDK — there's very little surface to wrap.
 * <p>Structured output is requested via {@code response_format} with a
 * strict JSON Schema, so the model is constrained to return exactly the shape {@link LlmWireFormat.ExpansionDto} expects. Not every model
 * routed through OpenRouter supports this (check the model's page on openrouter.ai for "Structured Outputs" support) — pick one that does.
 */
public class OpenRouterClient implements AutoCloseable {

    private final OpenRouterConfig config;
    private final HttpClient httpClient;
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * @param config
     */
    public OpenRouterClient(OpenRouterConfig config) {
	this.config = config;
	this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
    }

    /**
     * Sends the given system + user prompt and returns the parsed, schema-validated expansion result.
     */
    public LlmWireFormat.ExpansionDto requestTermExpansion(String systemPrompt, String userPrompt)
	    throws IOException, InterruptedException {

	ObjectNode requestBody = buildRequestBody(systemPrompt, userPrompt);

	GSLoggerFactory.getLogger(OpenRouterClient.class).debug("Request body: {}", requestBody.toPrettyString());

	HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(). //
		uri(URI.create(config.baseUrl() + "/chat/completions")).//
		timeout(config.requestTimeout()).header("Authorization", "Bearer " + config.apiKey()).//
		header("Content-Type", "application/json").//
		POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()));

	// Optional attribution headers OpenRouter uses for its public rankings; harmless if omitted.
	if (config.httpReferer() != null && !config.httpReferer().isBlank()) {

	    requestBuilder.header("HTTP-Referer", config.httpReferer());
	}

	if (config.appTitle() != null && !config.appTitle().isBlank()) {

	    requestBuilder.header("X-Title", config.appTitle());
	}

	HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());

	if (response.statusCode() / 100 != 2) {

	    throw new OpenRouterApiException("OpenRouter request failed with HTTP " + response.statusCode() + ": " + response.body(),
		    response.statusCode());
	}

	JsonNode root = mapper.readTree(response.body());
	JsonNode choices = root.path("choices");

	if (!choices.isArray() || choices.isEmpty()) {

	    throw new OpenRouterApiException("OpenRouter response had no choices: " + response.body(), response.statusCode());
	}

	String content = choices.get(0).path("message").path("content").asText();

	if (content == null || content.isBlank()) {

	    throw new OpenRouterApiException("OpenRouter response message had empty content: " + response.body(), response.statusCode());
	}

	return mapper.readValue(content, LlmWireFormat.ExpansionDto.class);
    }

    /**
     * @param systemPrompt
     * @param userPrompt
     * @return
     */
    private ObjectNode buildRequestBody(String systemPrompt, String userPrompt) {

	ObjectNode body = mapper.createObjectNode();

	body.put("model", config.model());
	body.put("temperature", config.temperature());

	ArrayNode messages = body.putArray("messages");

	messages.addObject().put("role", "system").put("content", systemPrompt);
	messages.addObject().put("role", "user").put("content", userPrompt);

	body.set("response_format", buildJsonSchemaResponseFormat());

	return body;
    }

    /**
     * Builds the {@code response_format} object enforcing that the model's reply is JSON matching {@link LlmWireFormat.ExpansionDto}
     * exactly, including an enum constraint on relation_type mirroring {@link SKOSRelationType}.
     */
    private ObjectNode buildJsonSchemaResponseFormat() {

	List<String> relationNames = Arrays.stream(SKOSRelationType.values()).map(Enum::name).toList();

	ObjectNode relatedTermSchema = mapper.createObjectNode();
	relatedTermSchema.put("type", "object");
	ObjectNode relatedTermProps = relatedTermSchema.putObject("properties");
	relatedTermProps.putObject("term").put("type", "string");

	ObjectNode relationTypeProp = relatedTermProps.putObject("relation_type");
	relationTypeProp.put("type", "string");
	ArrayNode enumArray = relationTypeProp.putArray("enum");
	relationNames.forEach(enumArray::add);

	// OpenAI strict mode requires every property to be listed in "required" - there's
	// no concept of a genuinely optional field. To model an optional value, make the
	// type nullable instead: the model returns null when the field doesn't apply
	// (i.e. whenever relation_type != OTHER).

	ObjectNode otherRelationLabelProp = relatedTermProps.putObject("other_relation_label");
	ArrayNode otherRelationLabelType = otherRelationLabelProp.putArray("type");
	otherRelationLabelType.add("string").add("null");

	relatedTermProps.putObject("confidence").put("type", "number");
	relatedTermProps.putObject("rationale").put("type", "string");

	relatedTermSchema.putArray("required").add("term").add("relation_type").add("other_relation_label").add("confidence")
		.add("rationale");
	relatedTermSchema.put("additionalProperties", false);

	ObjectNode rootSchema = mapper.createObjectNode();
	rootSchema.put("type", "object");
	ObjectNode rootProps = rootSchema.putObject("properties");
	ObjectNode relatedTermsArray = rootProps.putObject("related_terms");
	relatedTermsArray.put("type", "array");
	relatedTermsArray.set("items", relatedTermSchema);
	rootSchema.putArray("required").add("related_terms");
	rootSchema.put("additionalProperties", false);

	ObjectNode jsonSchemaWrapper = mapper.createObjectNode();
	jsonSchemaWrapper.put("name", "term_expansion");
	jsonSchemaWrapper.put("strict", true);
	jsonSchemaWrapper.set("schema", rootSchema);

	ObjectNode responseFormat = mapper.createObjectNode();
	responseFormat.put("type", "json_schema");
	responseFormat.set("json_schema", jsonSchemaWrapper);
	return responseFormat;
    }

    @Override
    public void close() {
	// HttpClient (JDK 21) has no resources requiring explicit close;
	// method kept for symmetry / future connection-pool management.
    }
}
