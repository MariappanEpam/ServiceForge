package com.serviceforge.exception;

/** ETSTCC-13074: no job exists for the given id (maps to 404 JOB_NOT_FOUND). Carries the id only. */
public class JobNotFoundException extends RuntimeException {

    public JobNotFoundException(long jobId) {
        super("Job not found: " + jobId);
    }
}
