### Queue System

Jobs are pushed into Redis and processed by a background worker every 5 seconds.

**Redis keys:**
- `tmag:queue:pending` — List (LPUSH to enqueue, RPOP to consume)
- `tmag:queue:delayed` — Sorted Set (retry jobs; score = epoch-ms when ready)
- `tmag:queue:failed` — List (dead-letter after 3 failed attempts)

Retries use exponential backoff: **2 → 4 → 8 minutes**. After the 3rd failure the job moves to `tmag:queue:failed` for manual inspection.

---

### Step 1 — Add a job type

`core/queue/JobType.java`
```java
public enum JobType {
    EMAIL_VERIFICATION,
    EMAIL_PASSWORD_RESET,
    EMAIL_PASSWORD_CHANGED,
    EMAIL_GENERIC,
    GENERATE_TRAVEL_PLAN,   // ← add yours here
    SEND_NOTIFICATION,
    PROCESS_INVOICE
}
```

---

### Step 2 — Dispatch from any service

Inject `QueueService` and call `dispatch()`. The second argument is any serializable object (Map, DTO, record).

```java
@Service
public class TravelRequestService {

    private final QueueService queueService;

    public TravelRequestService(QueueService queueService) {
        this.queueService = queueService;
    }

    public void submitRequest(TravelRequest request) {
        // ... save to DB ...

        queueService.dispatch(JobType.GENERATE_TRAVEL_PLAN, Map.of(
                "requestId",   request.getId(),
                "userId",      request.getUser().getId(),
                "destination", request.getDestination()
        ));
    }
}
```

To schedule a job for later, pass a `LocalDateTime` as the third argument:

```java
queueService.dispatch(
    JobType.EMAIL_GENERIC,
    Map.of(
        "to",      user.getEmail(),
        "subject", "How is your trip planning going?",
        "variables", Map.of("content", "<p>Just checking in...</p>")
    ),
    LocalDateTime.now().plusDays(3)
);
```

---

### Step 3 — Handle it in QueueWorker

Add a case to the switch in `processMessage()` and write the handler method.

`core/queue/QueueWorker.java`
```java
// 1. Add the case:
switch (msg.getType()) {
    case EMAIL_VERIFICATION     -> handleEmailJob(msg, "verification");
    case EMAIL_PASSWORD_RESET   -> handleEmailJob(msg, "password_reset");
    case EMAIL_PASSWORD_CHANGED -> handleEmailJob(msg, "password_changed");
    case EMAIL_GENERIC          -> handleEmailJob(msg, "generic");
    case GENERATE_TRAVEL_PLAN   -> handleTravelPlanJob(msg);   // ← add this
    case SEND_NOTIFICATION      -> handleNotificationJob(msg);
    case PROCESS_INVOICE        -> handleInvoiceJob(msg);
}

// 2. Write the handler — throw any exception to trigger retry:
private void handleTravelPlanJob(QueueMessage msg) throws Exception {
    Map<String, Object> data = msg.getData();
    Long requestId = ((Number) data.get("requestId")).longValue();
    Long userId    = ((Number) data.get("userId")).longValue();

    travelPlanService.generatePlan(requestId, userId);
}
```

---

### Step 4 — Inject dependencies into QueueWorker

Add constructor arguments for any services your handlers need:

```java
@Component
public class QueueWorker {

    private final StringRedisTemplate redis;
    private final EmailService emailService;
    private final EmailTemplates emailTemplates;
    private final ObjectMapper objectMapper;
    private final TravelPlanService travelPlanService;     // ← add this
    private final NotificationService notificationService;

    public QueueWorker(StringRedisTemplate redis, EmailService emailService,
                       EmailTemplates emailTemplates, ObjectMapper objectMapper,
                       TravelPlanService travelPlanService,
                       NotificationService notificationService) {
        this.redis = redis;
        this.emailService = emailService;
        this.emailTemplates = emailTemplates;
        this.objectMapper = objectMapper;
        this.travelPlanService = travelPlanService;
        this.notificationService = notificationService;
    }
}
```

---

### Summary

| Step | What to do |
|------|-----------|
| New job type | Add enum value to `JobType.java` |
| Enqueue | `queueService.dispatch(JobType.YOUR_TYPE, anyObject)` from any service |
| Process | Add a `case` to the switch in `QueueWorker.processMessage()` |
| Handler | Read from `msg.getData()`, call your service, throw on failure |
| Delayed job | Pass `LocalDateTime` as third arg to `dispatch()` |
| Dependencies | Add services to `QueueWorker` constructor |
