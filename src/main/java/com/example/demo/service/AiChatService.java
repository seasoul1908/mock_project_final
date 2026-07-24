package com.example.demo.service;

import com.example.demo.dto.ChatbotRequest;
import com.example.demo.dto.ChatbotResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Service layer for AI chat interactions.
 *
 * <p>
 * Delegates to the Spring AI {@link ChatClient} configured in {@code AiConfig}.
 * The underlying model is Gemini (Google GenAI) and is driven entirely by
 * {@code application.properties} — no API key or model name is hardcoded here.
 */
@Service
public class AiChatService {

    private static final Logger log = LoggerFactory.getLogger(AiChatService.class);

    private final ChatClient chatClient;
    private final com.example.demo.config.DevQueryAdminStaticTools staticTools;
    private final com.example.demo.config.DevQueryAdminDynamicTools dynamicTools;
    private final com.example.demo.config.DevQueryGuideTools guideTools;
    /**
     * System prompt that establishes the DevQuery Assistant persona.
     * Kept here as a constant so it is applied consistently to every call.
     */
    private static final String SYSTEM_PROMPT = "You are 'DevQuery Assistant,' an elite, senior-level software engineering AI "
            +
            "integrated natively into the DevQuery platform.\n" +
            "Primary Mission: Assist developers in debugging code, explaining technical " +
            "concepts, database design, and navigating the forum.\n" +
            "Strict Boundaries:\n" +
            "- You MUST decline ANY non-technical questions (e.g., politics, cooking, health). " +
            "Fallback response: \"I am the DevQuery Assistant, specialized in software " +
            "engineering. Please ask a programming-related question.\"\n" +
            "- Do NOT generate harmful, malicious, or hacking-related code.\n" +
            "Formatting Rules:\n" +
            "- Keep responses concise. Developers value time.\n" +
            "- Wrap all code snippets in standard Markdown formatting (e.g., ```java).\n" +
            "- Use bold text for file names and key variables.\n";

    /**
     * Injects the pre-configured {@link ChatClient} bean from {@code AiConfig}.
     *
     * @param chatClient the Spring-managed ChatClient (NOT the raw Builder)
     */
    public AiChatService(ChatClient chatClient,
            com.example.demo.config.DevQueryAdminStaticTools staticTools,
            com.example.demo.config.DevQueryAdminDynamicTools dynamicTools,
            com.example.demo.config.DevQueryGuideTools guideTools) {
        this.chatClient = chatClient;
        this.staticTools = staticTools;
        this.dynamicTools = dynamicTools;
        this.guideTools = guideTools;
    }

    /**
     * Sends a user message to the Gemini model and returns the AI-generated reply.
     *
     * @param request the DTO containing the user's message text
     * @return a {@link ChatbotResponse} with the AI reply, or an error message on
     *         failure
     */
    public ChatbotResponse getChatResponse(ChatbotRequest request) {
        try {
            log.info("Sending request to Gemini. User message length: {} chars",
                    request.getMessage().length());

            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();
            boolean isAdmin = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            ChatClient.ChatClientRequestSpec spec = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(request.getMessage());

            if (isAdmin) {
                spec = spec.tools(staticTools, dynamicTools, guideTools);
                log.info("Admin user detected. AI Database and Guide tools attached.");
            } else {
                spec = spec.tools(guideTools);
                log.info("Normal user detected. Only Guide tools attached.");
            }

            String responseText = spec.call().content();

            log.info("Received response from Gemini. Response length: {} chars",
                    responseText != null ? responseText.length() : 0);

            return new ChatbotResponse(responseText);

        } catch (Exception e) {
            // Log the FULL exception (not just the message) to get the real API error
            // detail
            log.error("Gemini API call failed. Cause: {}", e.getMessage(), e);
            return new ChatbotResponse("Lỗi API: " + e.getMessage());
        }
    }
}
