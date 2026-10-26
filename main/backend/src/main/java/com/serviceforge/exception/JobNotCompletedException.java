package com.serviceforge.exception;

/** ETSTCC-13074: feedback requires a COMPLETED job (maps to 400 JOB_NOT_COMPLETED). */
public class JobNotCompletedException extends RuntimeException {

    public JobNotCompletedException(long jobId) {
        super("Job is not completed: " + jobId);
    }
}
