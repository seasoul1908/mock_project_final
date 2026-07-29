package com.example.demo.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI configuration for the Google GenAI (Gemini) integration.
 *
 * <p>This creates a single, shared {@link ChatClient} bean used by {@code AiChatService}.
 * The model and options set here act as defaults and can be overridden per-request.
 *
 * <p>Driven by application.properties:
 * <pre>
 *   spring.ai.google.genai.api-key=YOUR_KEY
 *   spring.ai.google.genai.chat.options.model=gemini-2.5-flash
 * </pre>
 */
@Configuration
public class AiConfig {

    /**
     * Builds the {@link ChatClient} with portable {@link ChatOptions} defaults.
     * The model name is resolved from application.properties automatically.
     *
     * @param builder the auto-configured builder from spring-ai-starter-model-google-genai
     * @return a thread-safe, fully configured ChatClient
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultOptions(ChatOptions.builder()
                        // Model is resolved from: spring.ai.google.genai.chat.options.model
                        // Uncomment to hard-code override: .model("gemini-2.5-flash")
                        .temperature(0.7))
                .build();
    }
}
