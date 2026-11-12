# Personal Reading List & Country Explorer App
## Complete Technical Documentation & Wireframes

---

## 1. Project Overview
The **Personal Reading List & Country Explorer** is an Android application built in Kotlin using Material Design 3. It allows users to:
1. Authenticate using **Google Sign-In** or **Firebase Email/Password**.
2. Scan **EAN-13 ISBN barcodes** directly using Google Code Scanner on-device.
3. Search books by title or author using the **Open Library API**.
4. Organize books into personal reading statuses: **Want to Read**, **Currently Reading**, and **Read**.
5. Explore countries with flag images, filter by region chips (**Asia**, **Europe**, **Africa**, **Americas**, **Oceania**), auto-detect device location, view formatted details (Capital, Region, Formatted Population, Currencies, Languages), and curate a **Wish-to-Visit** list.

---

## 2. Libraries & Dependencies Used

| Library Name | Dependency Notation | Purpose & Description |
| :--- | :--- | :--- |
| **Firebase Authentication** | `com.google.firebase:firebase-auth-ktx` | Handles user authentication (Email/Password & Google Sign-In) and session persistence. |
| **Google Code Scanner** | `com.google.android.gms:play-services-code-scanner` | On-device barcode scanning restricted to **EAN-13 format** for ISBNs without custom camera permissions. |
| **Google Sign-In** | `com.google.android.gms:play-services-auth`, `googleid` | Google authentication using Web Client ID and Google ID Tokens. |
| **Credential Manager** | `androidx.credentials` | Android API displaying the Google Account selection bottom sheet. |
| **Retrofit 2** | `com.squareup.retrofit2:retrofit` | Network HTTP client to query Open Library endpoints asynchronously. |
| **Gson Converter** | `com.squareup.retrofit2:converter-gson` | Deserializes Open Library JSON responses into Kotlin data models. |
| **OkHttp 3** | `com.squareup.okhttp3:okhttp`, `logging-interceptor` | Low-level HTTP client handling connection timeouts and network logging. |
| **Coil Image Loader** | `io.coil-kt:coil` | Kotlin-first image loading library for book cover thumbnails. |
| **Coil SVG Decoder** | `io.coil-kt:coil-svg` | Decodes and renders country flag SVG vector assets cleanly. |
| **Material Design 3** | `com.google.android.material:material` | UI components: MaterialCardView, ChipGroup, TextInput, Dialogs, and RecyclerView. |
| **ViewBinding** | `buildFeatures { viewBinding = true }` | Type-safe binding classes for XML layouts without `findViewById`. |

---

## 3. Open APIs & Endpoints (No Key Required)

1. **ISBN Book Lookup API**:  
   `https://openlibrary.org/isbn/{isbn}.json`
2. **Title & Author Search API**:  
   `https://openlibrary.org/search.json?q={title}`
3. **Book Cover Images API**:  
   `https://covers.openlibrary.org/b/isbn/{isbn}-M.jpg`  
   `https://covers.openlibrary.org/b/id/{cover_id}-M.jpg`
4. **Country Flags CDN**:  
   `https://flagcdn.com/w320/{country_code}.png`

---

## 4. Application Workflow & User Journey

### Flowchart
1. **Authentication Flow**:
   - App opens -> Checks authentication state.
   - If signed out, presents **Google Sign-In** or **Email/Password form**.
   - Upon successful authentication, unlocks app access and persists session.

2. **Reading List & ISBN Scanner Flow**:
   - User taps **Scan ISBN** button -> Launches **Google Code Scanner** (EAN-13 format).
   - Scans book barcode -> Queries Open Library ISBN API.
   - Fetches title, author, year, and cover image.
   - Saves book to **My Reading List** under **Want to Read** status.
   - User filters list using chips: **All**, **Want to Read**, **Currently Reading**, **Read**.
   - User updates status via drop-down or deletes book.

3. **Book Search Flow**:
   - User inputs title/author query -> Sends request to Open Library Search API.
   - Displays search results in a RecyclerView with book covers.
   - User taps **Add to Reading List** -> Saves book locally.

4. **Country Explorer & Wishlist Flow**:
   - User searches country name in search field.
   - User selects region filter chip (**Asia**, **Europe**, **Africa**, **Americas**, **Oceania**, **Wish-to-Visit**).
   - Taps **Detect My Country** -> Auto-detects device locale country and opens detail dialog.
   - Taps country card -> Opens **Country Details Dialog** (Capital, Region, Formatted Population, Currencies, Languages).
   - Taps Heart icon -> Adds/removes country from persistent **Wish-to-Visit** list.

---

## 5. UI Wireframes & Layout Structures

### Wireframe 1: Account / Authentication Screen
```text
+------------------------------------------+
|  [Logo Icon]                             |
|  Account & Firebase Auth                 |
|                                          |
|  +------------------------------------+  |
|  |  [G] Sign in with Google           |  |
|  |  ----------------- OR ------------ |  |
|  |  [ Email address                 ] |  |
|  |  [ Password                      ] |  |
|  |  [ SIGN IN                        ] |  |
|  |  [ CREATE ACCOUNT (SIGN UP)       ] |  |
|  +------------------------------------+  |
|                                          |
|  [My Books] [Search] [Countries] [Account]|
+------------------------------------------+
```

### Wireframe 2: My Reading List Screen
```text
+------------------------------------------+
|  My Reading List           [📷 Scan ISBN] |
|                                          |
|  (All) [Want to Read] [Reading] [Read]   |
|                                          |
|  +------------------------------------+  |
|  | [Cover]  The Great Gatsby          |  |
|  | [Image]  F. Scott Fitzgerald       |  |
|  |          ISBN: 9780743273565       |  |
|  |          [Status: Reading  v] [🗑️] |  |
|  +------------------------------------+  |
|  +------------------------------------+  |
|  | [Cover]  1984                      |  |
|  | [Image]  George Orwell             |  |
|  |          [Status: Read     v] [🗑️] |  |
|  +------------------------------------+  |
|                                          |
|  [My Books] [Search] [Countries] [Account]|
+------------------------------------------+
```

### Wireframe 3: Search Books Screen
```text
+------------------------------------------+
|  Search Open Library                     |
|                                          |
|  [ Search title or author       ] [🔍] [📷]|
|                                          |
|  +------------------------------------+  |
|  | [Cover]  To Kill a Mockingbird     |  |
|  | [Image]  Harper Lee                |  |
|  |          First published: 1960     |  |
|  |          [+ Add to Reading List]   |  |
|  +------------------------------------+  |
|  +------------------------------------+  |
|  | [Cover]  Brave New World           |  |
|  | [Image]  Aldous Huxley             |  |
|  |          [+ Add to Reading List]   |  |
|  +------------------------------------+  |
|                                          |
|  [My Books] [Search] [Countries] [Account]|
+------------------------------------------+
```

### Wireframe 4: Country Explorer Screen
```text
+------------------------------------------+
|  Country Explorer      [🌐 Detect Country]|
|                                          |
|  [ Search countries by name         ]    |
|                                          |
|  (All) [Asia] [Europe] [Africa] [❤️ Wish] |
|                                          |
|  +------------------------------------+  |
|  | [Flag]  Japan                      |  |
|  |         Asia • Tokyo           [❤️]|  |
|  +------------------------------------+  |
|  +------------------------------------+  |
|  | [Flag]  France                     |  |
|  |         Europe • Paris         [🤍]|  |
|  +------------------------------------+  |
|                                          |
|  [My Books] [Search] [Countries] [Account]|
+------------------------------------------+
```

### Wireframe 5: Country Details Dialog
```text
+------------------------------------------+
| +--------------------------------------+ |
| | [Flag]  Japan                   [❤️] | |
| |         Region: Asia                 | |
| | ------------------------------------ | |
| | Capital: Tokyo                       | |
| | Population: 125,681,593              | |
| | Currencies: Japanese Yen (JPY)       | |
| | Languages: Japanese                  | |
| |                                      | |
| | [ CLOSE ]                            | |
| +--------------------------------------+ |
+------------------------------------------+
```
