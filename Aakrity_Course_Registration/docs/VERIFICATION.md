# Verification record

Automated JUnit 5 tests are in `CourseServiceTest.java`: available seat, full course, duplicate enrollment/waitlist, FIFO promotion, prerequisite failure, skip-and-retain, invalid/repeated drops, waitlist cancellation, and concurrent enrollments.

Run `mvn clean test` in an environment with JDK 21 and Maven to capture the authoritative JUnit result. Do not state the JUnit suite passed until the command has actually completed successfully.
