# Kontext4J ⚡✨

> The Java lib that’s too cool for bloated dependencies. Edits, fills, expands, generates, and low-key judges every other library still downloading 300 MB of JARs.

If other image libs were at a party, they'd still be figuring out Maven while Kontext4J already made your avatar a cyberpunk queen and found a mysterious nano banana in your pocket. 🍌💀

---

# TL;DR

* Single-file, dependency-light Java wrapper for BFL/Flux Kontext APIs.
* Fluent AF: `new Kontext4J().init(key).generate(...).getUrl()`
* Handles local files, remote URLs, and base64 like a pro.
* Polls until server says "Ready" — no email-refreshing required.
* Easter egg: sometimes gives you a nano banana. Nobody knows why.

---

# Features 💅

* `edit(...)` — multi-image editing, masks, prompts, negative prompts, seeds, guidance… basically everything you want.
* `fill(...)` — inpainting with optional masks and mask modes.
* `generate(...)` — text → image, also supports reference images.
* `expand(...)` — outpaint top/bottom/left/right like a boss.
* Handles paths, URLs, base64 automatically.
* Polling included, waits for `status == Ready`.
* Stores multiple result URLs. Use `getUrl()`, `getUrls()`, or `download(path)`.
* Lightweight JSON parsing with `org.json`.

---

# Roast Corner 🔥

Other libs be like:

* “We need 300 MB of JARs and 12 XML files just to say hi.”
* *Kontext4J*: “Hold my beer 🍺, I’m done in one file.”
* “We spin 17 threads to make a 200ms request.”
* *Kontext4J*: single-threaded zen energy.

---

# Install (Maven / Gradle) 💻

Copy `Kontext4J.java` into your project (`src/main/java/io/github/initio/kontext4j/Kontext4J.java`).

Dependencies:

* Java 11+
* `org.json:json`

Maven snippet:

```xml
<dependency>
  <groupId>org.json</groupId>
  <artifactId>json</artifactId>
  <version>20230227</version>
</dependency>
```

---

# Usage Examples 🚀

### Initialize

```java
Kontext4J k = new Kontext4J().init("YOUR_API_KEY_HERE"); 
// no api key? no magic. 💀
```

---

<img src = https://cdn.sanity.io/images/gsvmb6gz/production/821bc59e839e362bc02a10a6662dc52e4da71da5-1024x1024.jpg alt="Image Generation Outputs" width="150" />
### Generate (text → image)

```java
k.model("generate")
 .generate(
     "A tiny robot sipping coffee, cinematic lighting, ultra detailed",
     null, "16:9", 42, 30, 7.5, null, null,
     "png", null, null, 1, null, null, null
 );
System.out.println("Image URL: " + k.getUrl());
// other libs would still be configuring XML, we already sipping coffee. ☕🤖
```

### Simple Generate

```java
k.generateSimple("Cyberpunk cat in neon alley");
System.out.println(k.getUrl());
// because we don't need 300 MB of JARs for one cat. 🐱💡
```

---

### Edit with multiple images

```java
k.edit(
    "/home/me/pic1.jpg",
    "/home/me/pic2.jpg",
    null, null,
    "Make all subjects wear futuristic helmets",
    null, "1:1", 12345, 40, 6.0,
    null, null, "jpg",
    null, null, 2
);
System.out.println(k.getUrls());
// other libs: "please configure 12 XML files." 🤡
```

### Simple Edit

```java
k.editSimple("/home/me/selfie.jpg", "Add sunglasses and party lights");
System.out.println(k.getUrl());
```

### Merge multiple images

```java
k.editMerge(List.of("/pic1.jpg","/pic2.jpg"), "Turn them into a superhero squad");
System.out.println(k.getUrls());
```

---

### Fill / Inpaint

```java
k.fill(
    "/home/me/photo.jpg",
    "/home/me/mask.png",
    "replace",
    "Make subject wear a futuristic helmet",
    null, "1:1", 12345, 40, 6.0, null, null,
    "jpg", null, null, 1
);
System.out.println(k.getUrls());
// inpainting so smooth, other libs cry into their 17 threads. 😭
```

---

### Expand / Outpaint

```java
k.expand(
    "https://example.com/input.png",
    200, 0, 0, 200,
    "Extend environment to neon city skyline",
    null, 40, 7.0, null, null, "png", null, null, 1
);
System.out.println(k.getUrl());
// other libs: "Wait… which edge is left again?" 🤔
```

---

### Download last result

```java
k.download(Paths.get("kontext_result.png"));
// no fuss, no spaghetti code. 🍝
```

### Download a particular result index

```java
k.downloadIndex(Paths.get("kontext_result2.png"), 0);
```

---

# All Public Methods 🛠

* `init(String apiKey)`
* `model(String modelName)` — `"edit" | "fill" | "generate" | "expand"`
* `setRequestTimeout(Duration d)`
* `setPollIntervalMillis(long ms)`
* `setMaxWaitMillis(long ms)`
* `edit(...)`, `editSimple(...)`, `editMerge(...)`
* `fill(...)`
* `generate(...)`, `generateSimple(...)`
* `expand(...)`
* `getUrls()`, `getUrl()`, `getLastRawResult()`
* `download(Path)`, `downloadIndex(Path, int)`

---

# Nano Banana 🍌

Sometimes, very rarely, a URL or sample might contain a **nano banana**.
No promises. Full giggle guaranteed when it shows up.

---

# Troubleshooting 🛑

* `No result available` — forgot `init(apiKey)`?
* `Polling timed out` — increase `setMaxWaitMillis(...)` or check webhook.
* `Invalid JSON` — server sent a JSON-shaped potato 🥔.

---

# License 📝

MIT recommended. Free, petty, and small — like your nano banana.

---

# Contact / Socials 📱

* X / Twitter / IG / FB / LinkedIn / Snapchat: `@thesiamrayhan`
* Gmail: `thesiamrayhan@gmail.com`
  *(Memes > bug reports)*

---

# Final Words 🏁

Kontext4J is tiny but opinionated: minimal dependencies, sane defaults, speed > drama.
Use it to edit, generate, fill, expand — maybe find a nano banana too. 🍌


