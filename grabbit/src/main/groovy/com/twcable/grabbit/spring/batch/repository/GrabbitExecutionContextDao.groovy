package com.twcable.grabbit.spring.batch.repository

import org.springframework.batch.core.repository.dao.ExecutionContextDao
import org.springframework.batch.item.ExecutionContext;


/**
 * Modified DAO Interface for persisting and retrieving {@link ExecutionContext}
 * @see ExecutionContextDao for more details
 */
interface GrabbitExecutionContextDao extends ExecutionContextDao {

    /**
     * Returns job execution context paths by comparing "executionId" property on "executionContext/job/<id>" with
     * "executionId" property on JobExecutions for the @param jobExecutionResourcePaths
     */
    public List<String> getJobExecutionContextPaths(List<String> jobExecutionResourcePaths)

    /**
     * Returns step execution context paths by comparing "executionId" property on "executionContext/job/<id>" with
     * "id" property on StepExecutions for the @param stepExecutionResourcePaths
     */
    public List<String> getStepExecutionContextPaths(List<String> stepExecutionResourcePaths)
}
