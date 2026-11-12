package com.assessment;

import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class CourseServiceTest {
    @Test void enrollWhenSeatAvailable() {
        var s = new CourseService(1); s.setPrerequisite("A", true);
        assertEquals(CourseService.Status.ENROLLED, s.enroll("A").status());
    }
    @Test void fullCapacityAndDuplicates() {
        var s = new CourseService(1);
        for (var id : List.of("A", "B")) s.setPrerequisite(id, true);
        s.enroll("A");
        assertEquals(CourseService.Status.WAITLISTED, s.enroll("B").status());
        assertEquals(CourseService.Status.ALREADY_WAITLISTED, s.enroll("B").status());
        assertEquals(CourseService.Status.ALREADY_ENROLLED, s.enroll("A").status());
        assertEquals(List.of("B"), s.snapshot().waitlist());
    }
    @Test void fifoPromotion() {
        var s = new CourseService(1);
        for (var id : List.of("A", "B", "C")) s.setPrerequisite(id, true);
        s.enroll("A"); s.enroll("B"); s.enroll("C");
        assertEquals("B", s.drop("A").promotedLearner());
        assertEquals("C", s.drop("B").promotedLearner());
    }
    @Test void prerequisiteFailureAndSkipRetainPolicy() {
        var s = new CourseService(1);
        s.setPrerequisite("A", true); s.setPrerequisite("B", true); s.setPrerequisite("C", true);
        s.enroll("A"); s.enroll("B"); s.enroll("C");
        s.setPrerequisite("B", false);
        assertEquals("C", s.drop("A").promotedLearner());
        assertEquals(List.of("B"), s.snapshot().waitlist());
        s.setPrerequisite("B", true);
        assertEquals("B", s.drop("C").promotedLearner());
        s.setPrerequisite("D", false);
        assertEquals(CourseService.Status.PREREQUISITE_NOT_MET, s.enroll("D").status());
    }
    @Test void invalidAndRepeatedDrops() {
        var s = new CourseService(1);
        assertEquals(CourseService.Status.INVALID_REQUEST, s.drop(" ").status());
        assertEquals(CourseService.Status.NOT_FOUND, s.drop("A").status());
        s.setPrerequisite("A", true); s.enroll("A");
        assertEquals(CourseService.Status.DROPPED, s.drop("A").status());
        assertEquals(CourseService.Status.NOT_FOUND, s.drop("A").status());
    }
    @Test void waitlistDrop() {
        var s = new CourseService(1);
        s.setPrerequisite("A", true); s.setPrerequisite("B", true);
        s.enroll("A"); s.enroll("B");
        assertEquals(CourseService.Status.REMOVED_FROM_WAITLIST, s.drop("B").status());
        assertTrue(s.snapshot().waitlist().isEmpty());
    }
    @Test void concurrentEnrollNeverExceedsCapacity() throws Exception {
        var s = new CourseService(3);
        var pool = Executors.newFixedThreadPool(12);
        try {
            List<Callable<Void>> tasks = new ArrayList<>();
            for (int i = 0; i < 60; i++) {
                String id = "L" + i; s.setPrerequisite(id, true);
                tasks.add(() -> { s.enroll(id); return null; });
            }
            for (Future<Void> f : pool.invokeAll(tasks)) f.get();
            assertEquals(3, s.snapshot().enrolled().size());
            assertEquals(57, s.snapshot().waitlist().size());
            assertEquals(60, new HashSet<>(s.snapshot().waitlist()).size() + s.snapshot().enrolled().size());
        } finally { pool.shutdownNow(); }
    }
}
