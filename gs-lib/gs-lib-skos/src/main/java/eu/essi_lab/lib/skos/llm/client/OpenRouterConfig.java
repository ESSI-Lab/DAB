package eu.essi_lab.lib.skos.llm.client;

import java.time.*;

/**
 * Connection settings for OpenRouter. Never hardcode the API key — load it from an environment variable or your secrets manager.
 *
 * @param apiKey OpenRouter API key (starts with "sk-or-...")
 * @param baseUrl API base, normally "https://openrouter.ai/api/v1"
 * @param model OpenRouter model identifier, e.g. "openai/gpt-4o", "anthropic/claude-sonnet-4.5", "google/gemini-2.5-pro". Swap this to
 * change which LLM answers — nothing else changes.
 * @param temperature controls how random/deterministic the model's output is when it samples the next token
 * @param requestTimeout HTTP call timeout
 * @param httpReferer optional, OpenRouter uses this + appTitle for its leaderboard/analytics attribution (not required to function)
 * @param appTitle optional, see httpReferer
 */
public record OpenRouterConfig( //
	String apiKey, //
	String baseUrl, //
	String model, //
	double temperature, //
	Duration requestTimeout, //
	String httpReferer, //
	String appTitle //
) {

    /**
     *
     */
    public static String DEFAULT_OPENROUTER_MODEL = "openai/gpt-4o-mini";

    /**
     *
     */
    public static String DEFAULT_BASE_URL = "https://openrouter.ai/api/v1";

    /**
     *
     */
    public static double DEFAULT_TEMPERATURE = 0.2; // low temperature: we want consistent, conservative term proposals

    /**
     * @return
     */
    public static OpenRouterConfig fromEnv() {

	String apiKey = System.getenv("OPENROUTER_API_KEY");

	if (apiKey == null || apiKey.isBlank()) {

	    throw new IllegalStateException(
		    "OPENROUTER_API_KEY environment variable is not set. " + "Get a key at https://openrouter.ai/keys");
	}

	String model = System.getenv().getOrDefault("OPENROUTER_MODEL", DEFAULT_OPENROUTER_MODEL);

	double temperature = Double.parseDouble(System.getenv().getOrDefault("OPENROUTER_TEMPERATURE", String.valueOf(DEFAULT_TEMPERATURE)));

	return new OpenRouterConfig( //
		apiKey, //
		DEFAULT_BASE_URL, //
		model, //
		temperature,//
		Duration.ofSeconds(60),//
		System.getenv("OPENROUTER_HTTP_REFERER"), //
		"skos-llm-expander");//
    }
}
