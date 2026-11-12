# Design specification

## Boundary and model
`CourseService` owns one course offering, its positive `capacity`, enrolled `LinkedHashSet<String>`, FIFO waitlist `LinkedHashSet<String>`, and prerequisite eligibility `Map<String, Boolean>`. No persistence or HTTP API; public methods are the service interface: `setPrerequisite`, `enroll`, `drop`, `snapshot`.

## Invariants
- `enrolled.size() <= capacity`.
- No learner appears twice in either collection or simultaneously in both.
- Waitlist insertion order determines FIFO priority.
- Only learners with prerequisite `true` may newly enroll or be promoted.
- Invalid operations never change course state.

## Decisions and algorithm
Unknown prerequisite means ineligible. Enrollment checks existing membership before prerequisite and then either enrolls (if capacity) or appends to waitlist. Dropping an enrolled learner frees a seat, scans the waitlist in insertion order, and promotes the earliest currently eligible entry. An ineligible entry is skipped **without removal**, preserving its position for later consideration. A waitlisted learner may drop to cancel. `setPrerequisite` updates eligibility, but does not automatically process the queue; processing happens on subsequent enrollment or enrolled drop.

## Concurrency
One `ReentrantLock` serializes all state changes and snapshots, ensuring capacity checks and mutations are atomic. This is adequate for a single-process in-memory service, not a distributed application.

## Tradeoffs
`LinkedHashSet` supports both uniqueness and FIFO iteration with minimal code. Linear scans are acceptable for the bounded task. No database or API is introduced. Existing enrolled learners are not automatically removed if prerequisite status later changes.
