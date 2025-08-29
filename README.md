# Kontext4J ⚡✨

> The Java lib that’s too cool for bloated dependencies. Edits, fills, expands, generates, and low-key judges every other library still downloading 300 MB of JARs.

If other image libs were at a party, they'd still be figuring out Maven while Kontext4J already made your avatar a cyberpunk queen and found a mysterious nano banana in your pocket. 🍌💀

![Maven Central](https://img.shields.io/maven-central/v/io.github.init-io/Kontext4J.svg?label=Maven%20Central)
![GitHub Release](https://img.shields.io/github/v/release/init-io/Kontext4J?label=release)
[![License](https://img.shields.io/github/license/init-io/Kontext4J)](LICENSE)
![downloads](https://img.shields.io/badge/downloads-1k%2Fmonth-brightgreen)


<img src="http://cdnb.artstation.com/p/assets/images/images/072/879/003/original/alena-sherban-2.gif" 
     alt="banner" 
     style="width:100%; height:300px; object-fit:cover;">
     
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

<table>
  <tr>
    <td><img src="https://cdn.sanity.io/images/gsvmb6gz/production/3bce31811d0a95960f2a30ca8c548af9463b78c8-1024x1024.jpg" alt="Image1" width="200"/></td>
    <td><img src="https://cdn.sanity.io/images/gsvmb6gz/production/fe5135de7fa5a666ddb38f234dd55fe285435719-1024x1024.jpg" alt="Image2" width="200"/></td>
    <td><img src="https://cdn.sanity.io/images/gsvmb6gz/production/930a9437a1b85af8721e9107daefa76e40b82365-1024x1024.jpg" alt="Image3" width="200"/></td>
    <td><img src="https://cdn.sanity.io/images/gsvmb6gz/production/821bc59e839e362bc02a10a6662dc52e4da71da5-1024x1024.jpg" alt="Image4" width="200"/></td>
  </tr>
</table>

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

<table>
  <tr>
    <td><img src="https://cdn.sanity.io/images/gsvmb6gz/production/41fcbaa8d77c2c2d5bb49467ae6b5a89572022fa-1125x750.jpg" alt="Input Image" width="200"/></td>
    <td><img src="https://cdn.sanity.io/images/gsvmb6gz/production/800f6631c695ff5be4b82ef9cae0981073a0fecd-1248x832.jpg" alt="Remove the object from her face" width="200"/></td>
    <td><img src="https://cdn.sanity.io/images/gsvmb6gz/production/2090d7d3b1ee0fb83cef25fb94b179163208417b-1248x832.jpg" alt="She is now taking a selfie in the streets of Freiburg, it's lovely day out." width="200"/></td>
    <td><img src="https://cdn.sanity.io/images/gsvmb6gz/production/cc78fef1c0785656280a120647fb313fda6b977a-1248x832.jpg" alt="It's now snowing, everything is covered in snow" width="200"/></td>
  </tr>
</table>


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

<table>
  <tr>
    <td><img src="https://cdn.sanity.io/images/gsvmb6gz/production/3ae6ee032b85373b84934574f3ac3bb2fb792d64-2048x1365.jpg" alt="Before Editing" width="200"/></td>
    <td><img src="https://cdn.sanity.io/images/gsvmb6gz/production/b404ea99e309e5b4bab6fcd82a4a13ad18f2c04b-1248x832.jpg" alt="After Editing" width="200"/></td>
  </tr>
</table>

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

<table>
  <tr>
    <td><img src="https://cdn.sanity.io/images/gsvmb6gz/production/838d5cc08592229eff705c3ca345286dac4d09da-1600x962.webp" alt="Inpainting example" width="400"/></td>
  </tr>
</table>

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

## 📱 Connect with Me

<table>
<tr>
  <td><a href="https://twitter.com/thesiamrayhan"><img src="https://cdn-icons-png.flaticon.com/24/733/733579.png" width="24" style="vertical-align:middle; margin-right:5px;">X</a></td>
  <td><a href="https://www.instagram.com/thesiamrayhan/"><img src="https://cdn-icons-png.flaticon.com/24/2111/2111463.png" width="24" style="vertical-align:middle; margin-right:5px;">Instagram</a></td>
  <td><a href="https://www.facebook.com/thesiamrayhan"><img src="https://cdn-icons-png.flaticon.com/24/733/733547.png" width="24" style="vertical-align:middle; margin-right:5px;">Facebook</a></td>
  <td><a href="https://www.snapchat.com/add/thesiamrayhan"><img src="https://cdn-icons-png.freepik.com/512/13670/13670393.png" width="24" style="vertical-align:middle; margin-right:5px;">Snapchat</a></td>
  <td><a href="https://www.linkedin.com/in/thesiamrayhan/"><img src="https://cdn-icons-png.flaticon.com/24/174/174857.png" width="24" style="vertical-align:middle; margin-right:5px;">LinkedIn</a></td>
  <td><a href="https://www.fiverr.com/thesiamrayhan"><img src="https://cdn-icons-png.flaticon.com/24/5968/5968764.png" width="24" style="vertical-align:middle; margin-right:5px;">Fiverr</a></td>
  <td><a href="https://discord.com/users/thesiamrayhan"><img src="https://cdn-icons-png.flaticon.com/24/2111/2111370.png" width="24" style="vertical-align:middle; margin-right:5px;">Discord</a></td>
  <td><a href="https://t.me/thesiamrayhan"><img src="https://cdn-icons-png.flaticon.com/24/2111/2111646.png" width="24" style="vertical-align:middle; margin-right:5px;">Telegram</a></td>
  <td><a href="mailto:thesiamrayhan@gmail.com"><img src="https://cdn-icons-png.flaticon.com/24/732/732200.png" width="24" style="vertical-align:middle; margin-right:5px;">Gmail</a></td>
</tr>
</table>

*(Memes > bug reports)*

---

# Final Words 🏁

Kontext4J is tiny but opinionated: minimal dependencies, sane defaults, speed > drama.
Use it to edit, generate, fill, expand — maybe find a nano banana too. 🍌


