package com.tcmseek.support;

import java.util.Set;

public final class TargetPredictionConstants {

    private TargetPredictionConstants() {
    }

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_SUBMITTED = "SUBMITTED";
    public static final String STATUS_RUNNING = "RUNNING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_FAILED = "FAILED";

    public static final Set<String> FINISHED_REMOTE_STATUS =
            Set.of("Done", "Abort", "Cancel", "Failed");
}
