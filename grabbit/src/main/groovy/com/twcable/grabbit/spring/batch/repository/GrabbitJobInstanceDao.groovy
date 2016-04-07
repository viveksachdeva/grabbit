package com.twcable.grabbit.spring.batch.repository

import org.springframework.batch.core.JobInstance
import org.springframework.batch.core.repository.dao.JobInstanceDao

/**
 * Modified DAO Interface for persisting and retrieving {@link JobInstance}
 * @see JobInstanceDao for more details
 */
interface GrabbitJobInstanceDao extends JobInstanceDao {

    /**
     * Returns job instance paths by comparing "id" property on "jobInstances" with
     * "instanceId" property on JobExecutions for the @param jobExecutionResourcePaths
     */
    public List<String> getJobInstancePaths(List<String> jobExecutionResourcePaths)

}
