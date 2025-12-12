package com.example.digitalwardrobe;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.*;

public class GroqClient {

    private static final String API_KEY = BuildConfig.GROQ_API_KEY;
    private static final String URL = "https://api.groq.com/openai/v1/chat/completions";

    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String generateReply(String prompt) throws Exception {

        String bodyJson = "{\n" +
                "  \"model\": \"llama3-8b-8192\",\n" +
                "  \"messages\": [\n" +
                "    {\"role\": \"user\", \"content\": \"" + prompt.replace("\"", "\\\"") + "\"}\n" +
                "  ]\n" +
                "}";

        RequestBody body = RequestBody.create(
                bodyJson,
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(URL)
                .header("Authorization", "Bearer " + API_KEY)
                .post(body)
                .build();

        Response response = client.newCall(request).execute();
        String json = response.body().string();

        JsonNode root = mapper.readTree(json);
        return root.get("choices").get(0).get("message").get("content").asText();
    }
}
