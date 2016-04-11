package com.twcable.grabbit.spring.batch.repository.services

interface CleanJobRepository {

    /**
     * This API is used to clean Grabbit's JCR Job Repository. It removes all job executions and all associated
     * job instances etc. that are @param hours older than NOW (time when this API is called)
     */
    public void cleanJobRepository(int hours)

}
