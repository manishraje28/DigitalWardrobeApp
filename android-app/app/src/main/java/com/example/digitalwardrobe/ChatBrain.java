package com.example.digitalwardrobe;

import android.content.Context;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

public class ChatBrain {

    public enum Intent {
        SUGGEST,
        ALTERNATIVE,
        DARKER,
        LIGHTER,
        FORMAL,
        CASUAL,
        WEATHER,
        UNKNOWN
    }

    // Simple color match map (extend as you like)
    private static final Map<String, List<String>> COLOR_MATCHES = new HashMap<String, List<String>>() {{
        put("red", Arrays.asList("black","white","blue"));
        put("blue", Arrays.asList("black","white","grey"));
        put("black", Arrays.asList("red","blue","white","grey"));
        put("white", Arrays.asList("blue","black","red","green"));
        put("green", Arrays.asList("black","white","beige"));
        put("yellow", Arrays.asList("blue","black","white"));
    }};

    private final Context ctx;
    private Outfit lastSuggested = null; // for follow-ups

    public ChatBrain(Context context) {
        this.ctx = context.getApplicationContext();
    }

    // simple intent parser (expand rules as needed)
    public Intent parseIntent(String text) {
        if (text == null) return Intent.UNKNOWN;
        text = text.toLowerCase(Locale.ROOT);

        if (text.contains("alternative") || text.contains("other") || text.contains("else")) return Intent.ALTERNATIVE;
        if (text.contains("darker") || text.contains("darker color") || text.contains("darker tone")) return Intent.DARKER;
        if (text.contains("lighter") || text.contains("lighter color")) return Intent.LIGHTER;
        if (text.contains("formal") || text.contains("office") || text.contains("wedding") || text.contains("party")) return Intent.FORMAL;
        if (text.contains("casual") || text.contains("everyday") || text.contains("college")) return Intent.CASUAL;
        if (text.contains("weather") || text.contains("rain") || text.contains("hot") || text.contains("cold")) return Intent.WEATHER;
        if (text.startsWith("show") || text.contains("suggest") || text.contains("what should") || text.contains("what to wear")) return Intent.SUGGEST;

        return Intent.UNKNOWN;
    }

    // Top-level: produce response text (local rules), optionally call LLM for humanized reply
    public String handleMessage(String userMessage, boolean useLLM) {
        Intent intent = parseIntent(userMessage);

        Outfit outfit = null;
        switch (intent) {
            case SUGGEST:
            case UNKNOWN:
                outfit = generateSuggestion(); break;
            case ALTERNATIVE:
                outfit = generateAlternative();
                break;
            case DARKER:
                outfit = adjustShade(true);
                break;
            case LIGHTER:
                outfit = adjustShade(false);
                break;
            case FORMAL:
                outfit = generateByOccasion("formal");
                break;
            case CASUAL:
                outfit = generateByOccasion("casual");
                break;
            case WEATHER:
                // If you want to use actual weather, pass param; for now, fallback to suggest
                outfit = generateSuggestion();
                break;
        }

        lastSuggested = outfit;

        String baseReply;
        if (outfit == null || !outfit.isValid()) {
            baseReply = "I couldn't find a good match — try adding more items (top and bottom).";
        } else {
            baseReply = "I suggest: " +
                    outfit.top.color + " " + outfit.top.type +
                    " with " +
                    outfit.bottom.color + " " + outfit.bottom.type +
                    ".";
        }

        if (!useLLM) return baseReply;

        // Hybrid: call LLM to rephrase the baseReply into a humanized answer.
        // This method is synchronous here for clarity; call it async from UI.
        String prompt = "You are a friendly fashion assistant. Rephrase the following recommendation in friendly, helpful language and add 2 short styling tips:\n\n" + baseReply;
        try {
            if (useLLM) {
                try {
                    String ai = GroqClient.generateReply(prompt);
                    if (ai != null && ai.length() > 3) {
                        return ai;
                    }
                } catch (Exception e) {
                    return baseReply + "\n\n(LLM error: " + e.getMessage() + ")";
                }
            }

            // see LLMClient
            try {
                String response = GroqClient.generateReply(prompt);
                if (!TextUtils.isEmpty(response)) return response;
            } catch (Exception ignored) {}

        } catch (Exception ignored) {}

        return baseReply;
    }

    // Simple suggestion: iterate tops & bottoms and score (same rules as earlier)
    private Outfit generateSuggestion() {
        AppDatabase db = AppDatabase.getInstance(ctx);
        List<ClothingItem> tops = new ArrayList<>();
        tops.addAll(db.clothingItemDao().getByKeyword("shirt"));
        tops.addAll(db.clothingItemDao().getByKeyword("tshirt"));
        tops.addAll(db.clothingItemDao().getByKeyword("hoodie"));
        tops.addAll(db.clothingItemDao().getByKeyword("kurta"));

        List<ClothingItem> bottoms = new ArrayList<>();
        bottoms.addAll(db.clothingItemDao().getByKeyword("pant"));
        bottoms.addAll(db.clothingItemDao().getByKeyword("jeans"));
        bottoms.addAll(db.clothingItemDao().getByKeyword("short"));
        bottoms.addAll(db.clothingItemDao().getByKeyword("jogger"));

        if (tops.isEmpty() || bottoms.isEmpty()) return null;

        ClothingItem bestT=null, bestB=null; int bestScore=-1;
        for (ClothingItem t: tops) {
            for (ClothingItem b: bottoms) {
                int score = 0;
                List<String> matches = COLOR_MATCHES.getOrDefault(t.color.toLowerCase(), null);
                if (matches != null && matches.contains(b.color.toLowerCase())) score += 5;
                if (t.occasion != null && t.occasion.equalsIgnoreCase(b.occasion)) score += 3;
                if (t.category != null && t.category.equalsIgnoreCase(b.category)) score += 1;
                if (score > bestScore) { bestScore = score; bestT = t; bestB = b; }
            }
        }
        return new Outfit(bestT, bestB);
    }

    // Alternative: return a different combo (simple random different pair)
    private Outfit generateAlternative() {
        Outfit last = lastSuggested;
        if (last == null) return generateSuggestion();

        AppDatabase db = AppDatabase.getInstance(ctx);
        List<ClothingItem> tops = db.clothingItemDao().getByKeyword("shirt");
        List<ClothingItem> bottoms = db.clothingItemDao().getByKeyword("pant");
        for (ClothingItem t : tops) {
            if (last.top == null || t.id != last.top.id) {
                for (ClothingItem b : bottoms) {
                    if (last.bottom == null || b.id != last.bottom.id) {
                        return new Outfit(t,b);
                    }
                }
            }
        }
        return generateSuggestion();
    }

    // adjust shade: darker=true -> prefer darker bottoms; this is simplistic
    private Outfit adjustShade(boolean darker) {
        Outfit last = lastSuggested;
        if (last == null) return generateSuggestion();
        AppDatabase db = AppDatabase.getInstance(ctx);
        List<ClothingItem> bottoms = db.clothingItemDao().getByKeyword("pant");
        // pick first with color != current and 'darker'/ 'lighter' is simulated
        for (ClothingItem b : bottoms) {
            if (darker && isDarkerThan(b.color, last.bottom.color)) return new Outfit(last.top,b);
            if (!darker && isLighterThan(b.color, last.bottom.color)) return new Outfit(last.top,b);
        }
        return generateAlternative();
    }

    // naive color darkness order (extend)
    private boolean isDarkerThan(String c1, String c2) {
        List<String> dark = Arrays.asList("black","navy","brown","grey");
        return dark.contains(c1.toLowerCase()) && !dark.contains(c2.toLowerCase());
    }
    private boolean isLighterThan(String c1, String c2) {
        List<String> light = Arrays.asList("white","beige","pastel","yellow");
        return light.contains(c1.toLowerCase()) && !light.contains(c2.toLowerCase());
    }

    private Outfit generateByOccasion(String occasion) {
        AppDatabase db = AppDatabase.getInstance(ctx);
        // Simple filter by occasion string in DB
        List<ClothingItem> tops = db.clothingItemDao().getByKeyword(occasion);
        List<ClothingItem> bottoms = db.clothingItemDao().getByKeyword(occasion);

        if (!tops.isEmpty() && !bottoms.isEmpty()) return new Outfit(tops.get(0), bottoms.get(0));
        return generateSuggestion();
    }
}
