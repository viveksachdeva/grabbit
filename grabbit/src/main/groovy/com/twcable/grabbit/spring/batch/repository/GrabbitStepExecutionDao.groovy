package com.twcable.grabbit.spring.batch.repository

import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.repository.dao.StepExecutionDao

/**
 * Modified DAO Interface for persisting and retrieving {@link StepExecution}
 * @see StepExecutionDao for more details
 */
interface GrabbitStepExecutionDao extends StepExecutionDao {

    /**
     * Returns step execution paths by comparing "jobExecutionId" property on "stepExecutions with
     * "executionId" property on JobExecutions for the @param jobExecutionResourcePaths
     */
    public List<String> getStepExecutionPaths(List<String> jobExecutionResourcePaths)
}
