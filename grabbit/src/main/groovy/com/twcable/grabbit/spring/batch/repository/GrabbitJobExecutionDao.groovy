package com.twcable.grabbit.spring.batch.repository

import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.repository.dao.JobExecutionDao

/**
 * Modified DAO Interface for persisting and retrieving {@link JobExecution}
 * @see JobExecutionDao for more details
 */
interface GrabbitJobExecutionDao extends JobExecutionDao{

    /**
     * Returns job execution paths for given BatchStatuses
     */
    public List<String> getJobExecutions(List<BatchStatus> batchStatuses)

    /**
     * Returns older job execution paths which ended @param hours ago from "Now"
     */
    public List<String> getOlderJobExecutions(int hours, List<String> jobExecutionPaths)

}
