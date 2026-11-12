# Course Waitlist — Core Java / In-Memory

Minimal, single-offering course enrollment feature. No Spring Boot, database, network API, or frontend.

## Requirements
- JDK 21
- Maven 3.9+ (for JUnit tests only)

## Run demo without Maven
```bash
mkdir -p out
javac -d out src/main/java/com/assessment/*.java
java -cp out com.assessment.Main
```

## Run automated tests
```bash
mvn clean test
```

## Rules
- Positive course capacity; one learner ID per enrolled or waitlisted entry.
- Prerequisite must explicitly be marked true before enrollment.
- Eligible learners join the waitlist when full; duplicate requests are idempotent status results.
- At promotion, eligibility is rechecked; ineligible waitlisted learners are skipped **and retained in original order**.
- Dropping an enrolled learner promotes the earliest currently eligible learner.
- Dropping a waitlisted learner removes that entry; repeated/unknown drops return NOT_FOUND.
- One ReentrantLock guards every read/write of mutable state.

## Limitations
- State is lost on restart; no persistence, authentication, or distributed locking.
- Prerequisite status is supplied by the caller; no integration with a transcript service.
- A learner becoming eligible while a seat is already empty is promoted on the next enrollment or enrolled drop, not immediately on eligibility update.
- Scope is deliberately one course offering, as requested.
