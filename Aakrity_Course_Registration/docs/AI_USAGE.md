# AI prompt and use record

## Prompt 1 (user request, paraphrased)
"Review the Course Waitlist assessment and generate a solution; do not use Java Spring Boot; use in-memory storage."

**AI contribution:** Proposed a Core Java service with in-memory `LinkedHashSet` enrollment/waitlist, `HashMap` prerequisite flags, a lock, and JUnit tests.

**Disposition:** Accepted the in-memory design and removed Spring Boot entirely. Added explicit statuses, FIFO skip-and-retain policy, and concurrency coverage.

**Files informed:** `CourseService.java`, `Main.java`, `CourseServiceTest.java`, `DESIGN.md`, `README.md`.

## Verification evidence
See `VERIFICATION.md`. AI-produced code should be reviewed by the submitting candidate; do not claim independent human verification until performed. Run `mvn clean test` and attach actual results before submission.

## Review checklist for candidate
- Check prerequisite assumptions and skip-and-retain policy.
- Inspect lock boundaries and state invariants.
- Run tests and inspect their output.
- Verify duplicate and repeated-drop statuses.
