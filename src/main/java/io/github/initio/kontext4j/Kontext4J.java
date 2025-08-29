package io.github.initio.kontext4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Kontext4J - Full-featured Java wrapper for BFL/Flux Kontext APIs.
 *
 * - Supports edit, fill, generate, expand endpoints with full parameter coverage
 * - Fluent API (methods return this)
 * - Automatic input handling: local path, remote URL or base64 string
 * - Polling until status == "Ready" (configurable)
 * - Returns and stores multiple result URLs if present
 *
 * NOTE: This single-file implementation bundles helpers internally. You may split
 * into multiple files/classes later if you prefer.
 */
public class Kontext4J {

    // Endpoints (as of docs checked during creation)
    public static final String EP_KONTEXT_PRO = "https://api.bfl.ai/v1/flux-kontext-pro";      // edit + text-to-image
    public static final String EP_FILL        = "https://api.bfl.ai/v1/flux-1-fill";          // inpainting/fill
    public static final String EP_EXPAND      = "https://api.bfl.ai/v1/flux-pro-1.0-expand";  // expand/outpaint

    // HTTP
    private HttpClient http;
    private String apiKey;
    private String currentModel = "edit";

    // Polling / timeouts
    private Duration requestTimeout = Duration.ofSeconds(60);
    private long pollIntervalMillis = 1500;
    private long maxWaitMillis = TimeUnit.MINUTES.toMillis(3);

    // Last results
    private final List<String> lastResultUrls = new ArrayList<>();
    private JSONObject lastRawResult;

    // Constructor
    public Kontext4J() {}

    // ----------------------
    // Initialization / config
    // ----------------------
    public Kontext4J init(String apiKey) {
        this.apiKey = Objects.requireNonNull(apiKey, "apiKey");
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .build();
        return this;
    }

    public Kontext4J model(String modelName) {
        if (modelName == null) return this;
        String m = modelName.toLowerCase();
        switch (m) {
            case "edit": case "fill": case "generate": case "expand":
                this.currentModel = m;
                break;
            default:
                throw new IllegalArgumentException("Unsupported model: " + modelName);
        }
        return this;
    }

    public Kontext4J setRequestTimeout(Duration d) { this.requestTimeout = d; return this; }
    public Kontext4J setPollIntervalMillis(long ms) { this.pollIntervalMillis = ms; return this; }
    public Kontext4J setMaxWaitMillis(long ms) { this.maxWaitMillis = ms; return this; }

    // ----------------------
    // Public API - full coverage methods
    // ----------------------

    /**
     * Edit (image editing) - full parameter set.
     */
    public Kontext4J edit(
            String inputImage1,
            String inputImage2,
            String inputImage3,
            String inputImage4,
            String prompt,
            String negativePrompt,
            String aspectRatio,
            Integer seed,
            Integer steps,
            Double guidance,
            Boolean promptUpsampling,
            Integer safetyTolerance,
            String outputFormat,
            String webhookUrl,
            String webhookSecret,
            Integer numOutputs
    ) throws IOException, InterruptedException {
        ensureInit();
        this.currentModel = "edit";

        JSONObject body = new JSONObject();
        putIfNotNull(body, "prompt", prompt);
        putIfNotNull(body, "negative_prompt", negativePrompt);
        putIfNotNull(body, "aspect_ratio", aspectRatio);
        putIfNotNull(body, "seed", seed);
        putIfNotNull(body, "steps", steps);
        putIfNotNull(body, "guidance", guidance);
        putIfNotNull(body, "prompt_upsampling", promptUpsampling);
        putIfNotNull(body, "safety_tolerance", safetyTolerance);
        putIfNotNull(body, "output_format", outputFormat);
        putIfNotNull(body, "webhook_url", webhookUrl);
        putIfNotNull(body, "webhook_secret", webhookSecret);
        putIfNotNull(body, "num_outputs", numOutputs);

        // Multiple input images
        putIfNotNull(body, "input_image", tryEncodeRef(inputImage1));
        putIfNotNull(body, "input_image_2", tryEncodeRef(inputImage2));
        putIfNotNull(body, "input_image_3", tryEncodeRef(inputImage3));
        putIfNotNull(body, "input_image_4", tryEncodeRef(inputImage4));

        JSONObject finalJson = executeTask(EP_KONTEXT_PRO, body);
        extractAndStoreUrls(finalJson);
        return this;
    }


    /**
     * Fill / inpaint (supports mask and mask_mode).
     */
    public Kontext4J fill(
            String inputImage,
            String maskImage,    // optional mask
            String maskMode,     // "keep" | "replace"
            String prompt,
            String negativePrompt,
            String aspectRatio,
            Integer seed,
            Integer steps,
            Double guidance,
            Boolean promptUpsampling,
            Integer safetyTolerance,
            String outputFormat,
            String webhookUrl,
            String webhookSecret,
            Integer numOutputs
    ) throws IOException, InterruptedException {
        ensureInit();
        this.currentModel = "fill";

        String b64 = tryEncodeInput(inputImage);
        JSONObject body = new JSONObject();
        putIfNotNull(body, "prompt", prompt);
        putIfNotNull(body, "negative_prompt", negativePrompt);
        putIfNotNull(body, "image", b64); // fill endpoint uses "image"
        putIfNotNull(body, "mask", encodeMaskFlexible(maskImage));
        putIfNotNull(body, "mask_mode", maskMode);
        putIfNotNull(body, "aspect_ratio", aspectRatio);
        putIfNotNull(body, "seed", seed);
        putIfNotNull(body, "steps", steps);
        putIfNotNull(body, "guidance", guidance);
        putIfNotNull(body, "prompt_upsampling", promptUpsampling);
        putIfNotNull(body, "safety_tolerance", safetyTolerance);
        putIfNotNull(body, "output_format", outputFormat);
        putIfNotNull(body, "webhook_url", webhookUrl);
        putIfNotNull(body, "webhook_secret", webhookSecret);
        putIfNotNull(body, "num_outputs", numOutputs);

        JSONObject finalJson = executeTask(EP_FILL, body);
        extractAndStoreUrls(finalJson);
        return this;
    }

    /**
     * Generate (text-to-image). Accepts one prompt; use numOutputs for multiple generations.
     * Supports reference images array.
     */
    public Kontext4J generate(
            String prompt,
            String negativePrompt,
            String aspectRatio,
            Integer seed,
            Integer steps,
            Double guidance,
            Boolean promptUpsampling,
            Integer safetyTolerance,
            String outputFormat,
            String webhookUrl,
            String webhookSecret,
            Integer numOutputs,
            List<String> referenceImages, // list of path/url/base64
            Object extraLoraScale,
            Object extraReference
    ) throws IOException, InterruptedException {
        ensureInit();
        this.currentModel = "generate";

        JSONObject body = new JSONObject();
        putIfNotNull(body, "prompt", prompt);
        putIfNotNull(body, "negative_prompt", negativePrompt);
        putIfNotNull(body, "aspect_ratio", aspectRatio);
        putIfNotNull(body, "seed", seed);
        putIfNotNull(body, "steps", steps);
        putIfNotNull(body, "guidance", guidance);
        putIfNotNull(body, "prompt_upsampling", promptUpsampling);
        putIfNotNull(body, "safety_tolerance", safetyTolerance);
        putIfNotNull(body, "output_format", outputFormat);
        putIfNotNull(body, "webhook_url", webhookUrl);
        putIfNotNull(body, "webhook_secret", webhookSecret);
        putIfNotNull(body, "num_outputs", numOutputs);
        putIfNotNull(body, "extra_lora_scale", extraLoraScale);
        putIfNotNull(body, "extra_reference", extraReference);

        if (referenceImages != null && !referenceImages.isEmpty()) {
            JSONArray refs = new JSONArray();
            for (String r : referenceImages) {
                refs.put(tryEncodeRef(r));
            }
            body.put("reference_images", refs);
        }

        JSONObject finalJson = executeTask(EP_KONTEXT_PRO, body);
        extractAndStoreUrls(finalJson);
        return this;
    }

    /**
     * Expand / outpaint an image (top/bottom/left/right pixels to add).
     */
    public Kontext4J expand(
            String inputImage,
            int top, int bottom, int left, int right,
            String prompt,
            String negativePrompt,
            Integer seed,
            Integer steps,
            Double guidance,
            Boolean promptUpsampling,
            Integer safetyTolerance,
            String outputFormat,
            String webhookUrl,
            String webhookSecret,
            Integer numOutputs
    ) throws IOException, InterruptedException {
        ensureInit();
        this.currentModel = "expand";

        String b64 = tryEncodeInput(inputImage);
        JSONObject body = new JSONObject();
        putIfNotNull(body, "image", b64);
        putIfNotNull(body, "top", top);
        putIfNotNull(body, "bottom", bottom);
        putIfNotNull(body, "left", left);
        putIfNotNull(body, "right", right);
        putIfNotNull(body, "prompt", prompt);
        putIfNotNull(body, "negative_prompt", negativePrompt);
        putIfNotNull(body, "seed", seed);
        putIfNotNull(body, "steps", steps);
        putIfNotNull(body, "guidance", guidance);
        putIfNotNull(body, "prompt_upsampling", promptUpsampling);
        putIfNotNull(body, "safety_tolerance", safetyTolerance);
        putIfNotNull(body, "output_format", outputFormat);
        putIfNotNull(body, "webhook_url", webhookUrl);
        putIfNotNull(body, "webhook_secret", webhookSecret);
        putIfNotNull(body, "num_outputs", numOutputs);

        JSONObject finalJson = executeTask(EP_EXPAND, body);
        extractAndStoreUrls(finalJson);
        return this;
    }

    // ----------------------
    // Result helpers
    // ----------------------
    public List<String> getUrls() {
        return Collections.unmodifiableList(lastResultUrls);
    }

    public String getUrl() {
        if (lastResultUrls.isEmpty()) return null;
        return lastResultUrls.get(lastResultUrls.size()-1);
    }

    public JSONObject getLastRawResult() {
        return lastRawResult;
    }

    /** Download last image (index -1 means last) */
    public void download(Path outputPath) throws IOException, InterruptedException {
        downloadIndex(outputPath, -1);
    }

    /** Download a particular index from the last results. index may be negative to count from the end. */
    public void downloadIndex(Path outputPath, int index) throws IOException, InterruptedException {
        if (lastResultUrls.isEmpty()) {
            throw new IllegalStateException("No result available. Call edit/fill/generate/expand first.");
        }
        int idx = index < 0 ? (lastResultUrls.size() + index) : index;
        if (idx < 0 || idx >= lastResultUrls.size()) throw new IndexOutOfBoundsException("Invalid result index: " + index);
        String url = lastResultUrls.get(idx);
        downloadFromUrl(url, outputPath);
    }

    // ----------------------
    // Internal utilities
    // ----------------------
    private void ensureInit() {
        if (http == null || apiKey == null) throw new IllegalStateException("Call init(apiKey) first.");
    }

    private static void putIfNotNull(JSONObject o, String k, Object v) {
        if (v == null) return;
        // some values like Integer with default 0 we still want to include; caller controls nullness
        o.put(k, v);
    }

    /** Heuristic: if input looks like a URL or existing file path fetch and convert; otherwise assume base64 or data URL. */
    private String tryEncodeInput(String input) throws IOException, InterruptedException {
        if (input == null) return null;
        String s = input.trim();
        if (s.isEmpty()) return null;
        // detect http(s)
        if (s.startsWith("http://") || s.startsWith("https://")) {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(s))
                    .timeout(requestTimeout)
                    .GET().build();
            HttpResponse<byte[]> r = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
            if (r.statusCode() >= 400) throw new IOException("Failed to fetch image URL: HTTP " + r.statusCode());
            return Base64.getEncoder().encodeToString(r.body());
        }
        // detect local file
        Path p = Paths.get(s);
        if (Files.exists(p)) {
            byte[] b = Files.readAllBytes(p);
            return Base64.getEncoder().encodeToString(b);
        }
        // if it looks like data:image/...;base64, extract
        if (s.startsWith("data:") && s.contains("base64,")) {
            int idx = s.indexOf("base64,") + 7;
            return s.substring(idx);
        }
        // otherwise assume already base64
        return s;
    }

    /** Encode mask (maskImage may be null or path/url/base64). Returns base64 string or null. */
    private String encodeMaskFlexible(String maskImage) throws IOException, InterruptedException {
        if (maskImage == null) return null;
        return tryEncodeInput(maskImage);
    }

    /** For reference images we either pass raw urls or base64; encode when necessary. */
    private Object tryEncodeRef(String r) throws IOException, InterruptedException {
        if (r == null) return null;
        String trimmed = r.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return trimmed;
        Path p = Paths.get(trimmed);
        if (Files.exists(p)) return Base64.getEncoder().encodeToString(Files.readAllBytes(p));
        if (trimmed.startsWith("data:base64,")) return trimmed.substring("data:base64,".length());
        return trimmed; // assume base64
    }

    /** Execute the task: POST then poll the polling_url until Ready or return inline result. */
    private JSONObject executeTask(String endpoint, JSONObject body) throws IOException, InterruptedException {
        HttpRequest post = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(requestTimeout)
                .header("accept", "application/json")
                .header("Content-Type", "application/json")
                .header("x-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> initial = http.send(post, HttpResponse.BodyHandlers.ofString());
        if (initial.statusCode() >= 400) {
            throw new IOException("Task creation failed: HTTP " + initial.statusCode() + " -> " + initial.body());
        }
        JSONObject initJson = safeJson(initial.body());

        // Some APIs may return results directly in `result` or `samples`, otherwise provide polling_url
        String pollingUrl = initJson.optString("polling_url", null);
        JSONObject resultInline = initJson.optJSONObject("result");
        if (pollingUrl == null && resultInline != null) {
            this.lastRawResult = initJson;
            return initJson;
        }
        if (pollingUrl == null) {
            // maybe endpoint returns `url` directly
            if (initJson.has("url")) {
                this.lastRawResult = initJson;
                return initJson;
            }
            throw new IOException("No polling_url or inline result in response: " + initJson.toString());
        }

        long start = System.currentTimeMillis();
        while (true) {
            HttpRequest poll = HttpRequest.newBuilder()
                    .uri(URI.create(pollingUrl))
                    .timeout(requestTimeout)
                    .header("x-key", apiKey)
                    .GET().build();
            HttpResponse<String> pollRes = http.send(poll, HttpResponse.BodyHandlers.ofString());
            if (pollRes.statusCode() >= 400) {
                throw new IOException("Polling failed: HTTP " + pollRes.statusCode() + " -> " + pollRes.body());
            }
            JSONObject jr = safeJson(pollRes.body());
            String status = jr.optString("status", "");
            if ("Ready".equalsIgnoreCase(status) || "Succeeded".equalsIgnoreCase(status)) {
                this.lastRawResult = jr;
                return jr;
            }
            // handle immediate error
            if (jr.has("error") || "Failed".equalsIgnoreCase(status)) {
                throw new IOException("Task failed during polling: " + jr.toString());
            }
            if (System.currentTimeMillis() - start > maxWaitMillis) {
                throw new IOException("Polling timed out after " + maxWaitMillis + "ms; last status=" + status);
            }
            Thread.sleep(pollIntervalMillis);
        }
    }

    /** Extract sample(s) from the final JSON and store into lastResultUrls. Returns list found (may be empty). */
    private List<String> extractAndStoreUrls(JSONObject finalJson) {
        lastResultUrls.clear();
        if (finalJson == null) return lastResultUrls;

        // Common shapes: result.sample (string), result.samples (array), result.outputs (array of objects with url/sample)
        JSONObject result = finalJson.optJSONObject("result");
        if (result != null) {
            // sample
            if (result.has("sample")) {
                String s = result.optString("sample", null);
                if (s != null) lastResultUrls.add(s);
            }
            // samples
            if (result.has("samples")) {
                JSONArray arr = result.optJSONArray("samples");
                if (arr != null) for (int i=0;i<arr.length();i++) lastResultUrls.add(arr.optString(i, null));
            }
            // outputs
            if (result.has("outputs")) {
                JSONArray out = result.optJSONArray("outputs");
                if (out != null) for (int i=0;i<out.length();i++) {
                    JSONObject o = out.optJSONObject(i);
                    if (o == null) continue;
                    if (o.has("sample")) lastResultUrls.add(o.optString("sample", null));
                    else if (o.has("url")) lastResultUrls.add(o.optString("url", null));
                }
            }
        }

        // Top-level url
        if (finalJson.has("url")) lastResultUrls.add(finalJson.optString("url", null));

        // If still empty, try to find any string-valued fields that look like an image URL
        if (lastResultUrls.isEmpty()) {
            // simple heuristic: scan keys
            for (String k : finalJson.keySet()) {
                Object v = finalJson.opt(k);
                if (v instanceof String) {
                    String s = (String) v;
                    if (looksLikeUrl(s)) lastResultUrls.add(s);
                }
            }
        }

        // remove nulls
        lastResultUrls.removeIf(x -> x == null || x.isBlank());
        return new ArrayList<>(lastResultUrls);
    }

    private void downloadFromUrl(String url, Path outputPath) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).timeout(requestTimeout).GET().build();
        HttpResponse<InputStream> res = http.send(req, HttpResponse.BodyHandlers.ofInputStream());
        if (res.statusCode() >= 400) throw new IOException("Failed to download result: HTTP " + res.statusCode());
        Files.createDirectories(outputPath.getParent());
        Files.copy(res.body(), outputPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    private static boolean looksLikeUrl(String s) {
        if (s == null) return false;
        String lower = s.toLowerCase();
        return lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("data:");
    }

    private static JSONObject safeJson(String s) throws IOException {
        try {
            return new JSONObject(s);
        } catch (JSONException e) {
            throw new IOException("Invalid JSON from server: " + s, e);
        }
    }

    // ----------------------
    // Convenience / builder-style overloads
    // ----------------------

    // Simple edit overload
    public Kontext4J editSimple(String inputImage, String prompt) throws IOException, InterruptedException {
        return edit(
                inputImage,
                prompt,
                null, // aspect_ratio
                null, // seed
                null, // prompt_upsampling
                null, // safety_tolerance
                null, // output_format
                null, // webhook_url
                null, // image_guidance_scale
                null, // num_inference_steps
                null, // guidance_scale
                null, // negative_prompt
                null, // control_image
                null, // control_mode
                null, // mask_image
                null  // mask_mode
        );
    }

    public Kontext4J generateSimple(String prompt) throws IOException, InterruptedException {
        return generate(
                prompt,
                null, // aspect_ratio
                null, // seed
                null, // prompt_upsampling
                null, // safety_tolerance
                null, // output_format
                null, // webhook_url
                null, // image_guidance_scale
                null, // num_inference_steps
                null, // guidance_scale
                null, // negative_prompt
                null, // control_image
                null, // control_mode
                null, // mask_image
                null  // mask_mode
        );
    }


    public Kontext4J editMerge(List<String> images, String prompt) throws IOException, InterruptedException {
        String img1 = images.size() > 0 ? images.get(0) : null;
        String img2 = images.size() > 1 ? images.get(1) : null;
        String img3 = images.size() > 2 ? images.get(2) : null;
        String img4 = images.size() > 3 ? images.get(3) : null;

        return edit(img1, img2, img3, img4, prompt,
                null, null, null, null, null, null, null,
                null, null, null, null);
    }

    // ----------------------
    // Small usage example (commented)
    // ----------------------
    /*
    public static void main(String[] args) throws Exception {
        Kontext4J k = new Kontext4J().init("YOUR_API_KEY");
        k.editSimple("/path/to/photo.jpg", "Make me wear a leather jacket and change background to nightclub");
        System.out.println("Result URL: " + k.getUrl());
        k.download(Paths.get(System.getProperty("user.home"), "Downloads", "kontext_result.png"));
    }
    */
}
