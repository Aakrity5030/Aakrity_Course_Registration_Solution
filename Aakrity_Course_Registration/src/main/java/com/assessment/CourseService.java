package com.assessment;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/** Single-offering, thread-safe, in-memory enrollment service. */
public final class CourseService {
    public enum Status { ENROLLED, WAITLISTED, ALREADY_ENROLLED, ALREADY_WAITLISTED, PREREQUISITE_NOT_MET, INVALID_REQUEST, DROPPED, REMOVED_FROM_WAITLIST, NOT_FOUND }
    public record Result(Status status, String message, String promotedLearner) {}
    public record Snapshot(int capacity, List<String> enrolled, List<String> waitlist) {}

    private final int capacity;
    private final Set<String> enrolled = new LinkedHashSet<>();
    private final LinkedHashSet<String> waitlist = new LinkedHashSet<>();
    private final Map<String, Boolean> prerequisite = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    public CourseService(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("Capacity must be positive");
        this.capacity = capacity;
    }

    /** Updates eligibility for a known learner; never implicitly enrolls. */
    public void setPrerequisite(String learner, boolean met) {
        if (learner == null || learner.isBlank()) throw new IllegalArgumentException("Learner ID required");
        lock.lock();
        try { prerequisite.put(learner, met); } finally { lock.unlock(); }
    }

    public Result enroll(String learner) {
        if (learner == null || learner.isBlank()) return result(Status.INVALID_REQUEST, "Learner ID required", null);
        lock.lock();
        try {
            if (enrolled.contains(learner)) return result(Status.ALREADY_ENROLLED, "Already enrolled", null);
            if (waitlist.contains(learner)) return result(Status.ALREADY_WAITLISTED, "Already waitlisted", null);
            if (!prerequisite.getOrDefault(learner, false)) return result(Status.PREREQUISITE_NOT_MET, "Prerequisite not met", null);
            // If seats were vacated while every waitlisted learner was ineligible,
            // preserve FIFO priority for any currently eligible waitlisted learner.
            promoteEligible();
            if (enrolled.size() < capacity) {
                enrolled.add(learner);
                return result(Status.ENROLLED, "Enrollment successful", null);
            }
            waitlist.add(learner);
            return result(Status.WAITLISTED, "Added to FIFO waitlist", null);
        } finally { lock.unlock(); }
    }

    public Result drop(String learner) {
        if (learner == null || learner.isBlank()) return result(Status.INVALID_REQUEST, "Learner ID required", null);
        lock.lock();
        try {
            if (enrolled.remove(learner)) {
                String promoted = promoteEligible();
                return result(Status.DROPPED, "Enrollment dropped", promoted);
            }
            if (waitlist.remove(learner)) return result(Status.REMOVED_FROM_WAITLIST, "Waitlist entry removed", null);
            return result(Status.NOT_FOUND, "Learner is neither enrolled nor waitlisted", null);
        } finally { lock.unlock(); }
    }

    /** Rechecks eligibility at promotion time, skipping but retaining ineligible entries. */
    private String promoteEligible() {
        String firstPromoted = null;
        Iterator<String> it = waitlist.iterator();
        while (enrolled.size() < capacity && it.hasNext()) {
            String learner = it.next();
            if (prerequisite.getOrDefault(learner, false)) {
                it.remove();
                enrolled.add(learner);
                if (firstPromoted == null) firstPromoted = learner;
            }
        }
        return firstPromoted;
    }

    public Snapshot snapshot() {
        lock.lock();
        try { return new Snapshot(capacity, List.copyOf(enrolled), List.copyOf(waitlist)); }
        finally { lock.unlock(); }
    }

    private static Result result(Status status, String message, String promoted) {
        return new Result(status, message, promoted);
    }
}
