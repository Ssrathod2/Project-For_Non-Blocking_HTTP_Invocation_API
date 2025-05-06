# Project-For_Non-Blocking_HTTP_Invocation_API
Building an internal orchestration API for a platform that handles high volumes of incoming HTTP traffic (e.g., 1000+ concurrent requests). Each incoming request triggers a downstream HTTP call to external services.

---

## 🚀 How to Run

### 1. Clone and Build the Project

```bash
git clone <your-repo-url>
cd HMProject
./mvnw clean install
```

### 2. Start the Spring Boot App

```bash
./mvnw spring-boot:run
```

The app will run on port `8080` by default.

### 3. Available Endpoints

* **Sync**: `http://localhost:8080/invoke`
* **Async**: `http://localhost:8080/invokeasync`

### 4. Sample Request Payload

```json
{
  "apiMethod": "POST",
  "requestDTO": {
    "url": "https://httpbin.org/post",
    "queryParams": {
      "q": "test"
    },
    "headerVariables": {
      "Authorization": "Bearer your-token",
      "Content-Type": "application/json"
    },
    "bodyType": "json",
    "requestBody": "{\"key\":\"value\"}",
    "pathMap": {},
    "formParam": null,
    "urlEncodedParam": null,
    "params": null
  },
  "timeout": 5000
}
```

### 5. Load Testing

Install [k6](https://k6.io/) and run the test scripts:

```bash
k6 run test/load-tests/k6-load-test-sync.js
k6 run test/load-tests/k6-load-test-async.js
```

---

# Project Structure and Architecture

```
src/
├── main/
│   ├── java/com/hmt/hmproject/hmproject/
│   │   ├── Controller/                     # Handles incoming HTTP requests
│   │   │   ├── ApiController.java
│   │   │   └── ApiControllerAsync.java
│   │   ├── Customs/
│   │   │   ├── HttpDeleteWithBody.java
│   │   │   ├── NameValuePair.java
│   │   │   └── SslConfig.java
│   │   ├── Enums/
│   │   │   └── ApiMethod.java
│   │   ├── Factory/
│   │   │   ├── ApiFactory.java
│   │   │   ├── ApiFactoryAsync.java
│   │   │   ├── RestFactory.java           # Sync REST client logic using HttpClient (CloseableHttpClient)
│   │   │   └── RestFactoryAsync.java      # Async REST client logic using WebClient
│   │   ├── Model/
│   │   │   ├── InvokeRequestDTO.java      # DTO class for request payload structure (Updated RequestDTO)
│   │   │   └── RequestDTO.java            
│   │   └── HmProjectApplication.java
│   └── resources/
│       └── application.yml
└── test/
    └── load-tests/
        ├── k6-load-test-sync.js          # K6 Test Script for Synchronized Logic
        └── k6-load-test-async.js         # K6 Test Script for ASynchronized Logic

```

## 🚀 Tech Stack

* **Spring Boot 3.4.5**
* **Java 17 / Java 24.0.1 compatible**
* **WebClient (Spring WebFlux)** for async HTTP calls
* **k6** for load testing

## 🔍 Why WebClient & Mono?

We chose **WebClient** over traditional RestTemplate because it is:

* **Non-blocking** and supports **reactive programming**
* Scales better under high-load scenarios
* More modern and aligned with Spring WebFlux

We use `Mono<T>` as a return type because:

* It represents a **single asynchronous value** (or error)
* Plays well with reactive chains (e.g., `.map()`, `.flatMap()`, `.timeout()`, etc.)
* Allows better **resource efficiency** by not blocking threads

### ✨ Difference from Apache HttpAsyncClient

| Feature                 | **Spring WebClient**           | **Apache HttpAsyncClient**                   |
| ----------------------- | ------------------------------ | -------------------------------------------- |
| Framework integration   | Tight with Spring WebFlux      | Needs manual configuration                   |
| Reactive stream support | Built-in with Project Reactor  | Limited or external via Rx/CompletableFuture |
| Ease of use             | Declarative, functional style  | More low-level control                       |
| Maintenance             | Modern and actively maintained | More mature, less active                     |

**WebClient is recommended** for Spring Boot + WebFlux projects, while Apache HttpAsyncClient is suited for lower-level or legacy use cases.

## 📊 Load Testing

## 🧪 Load Testing Tools

* We used **k6** to simulate high-load testing with up to 1000 virtual users.
* Two separate test scripts:

    * `k6-load-test-sync.js`
    * `k6-load-test-async.js`

Command to run:

```bash
k6 run k6-load-test-async.js
k6 run k6-load-test-sync.js
```

---

## 📊 Performance Benchmark Comparison

### ✅ Summary

| Metric                       | **Async API** | **Sync API** |
| ---------------------------- | ------------- | ------------ |
| **Total Requests**           | 15,580        | 5,688        |
| **Success Rate**             | 77.81%        | 76.33%       |
| **Failure Rate**             | 22.18%        | 23.66%       |
| **Avg Response Time**        | **1.59s**     | **5.16s**    |
| **95th Percentile Duration** | 3.39s         | 10.26s       |
| **Max Response Time**        | 5.01s         | 14.1s        |
| **Throughput (RPS)**         | 500.07 req/s  | 143.27 req/s |
| **Avg Iteration Duration**   | 6.4s          | 20.69s       |
| **Virtual Users (max)**      | 1000          | 1000         |

### ✅ Status Code Checks

| HTTP Method | **Async (200 OK)**  | **Sync (200 OK)**  |
| ----------- | ------------------- | ------------------ |
| **POST**    | 71% (✓2773 / ✗1122) | 36% (✓520 / ✗902)  |
| **GET**     | 73% (✓2871 / ✗1024) | 71% (✓1022 / ✗400) |
| **PUT**     | 78% (✓3050 / ✗845)  | 96% (✓1378 / ✗44)  |
| **OPTIONS** | 88% (✓3429 / ✗466)  | 100% (✓1422)       |

### 🧠 Observations

* **Async outperforms Sync** in terms of:

    * Average response time (1.59s vs 5.16s)
    * Request throughput (500 RPS vs 143 RPS)
    * System responsiveness (95th percentile is \~3.5s in async vs \~10.5s in sync)
* **Sync suffers heavily** under high concurrency — likely due to thread blocking and IO wait time.
* **Async APIs** scale better with higher load, although the success rate still leaves room for reliability improvements.
* PUT endpoints in the sync version are very reliable, while POST in sync performs poorly.
* The **OPTIONS** method performs consistently well in both.

### 🚧 Trade-offs

* **Async Pros**:

    * Significantly higher scalability and responsiveness.
    * Better suited for modern reactive, high-throughput systems.

* **Async Cons**:

    * Slightly more complex debugging and tracing.
    * Still shows \~22% error rate — should be optimized further.

* **Sync Pros**:

    * Simpler to implement and debug.
    * Better stability on certain endpoints (like PUT).

* **Sync Cons**:

    * Major performance bottlenecks under load.
    * Much slower response time and iteration time.


## 🏁 Conclusion

This project clearly demonstrates the **performance advantage** of 
asynchronous WebClient architecture in Spring Boot. While sync APIs are 
easier to start with, async APIs are far more suitable for production-scale, 
high-concurrency environments.



