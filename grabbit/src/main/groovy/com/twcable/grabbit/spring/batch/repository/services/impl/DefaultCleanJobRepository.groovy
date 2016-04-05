package com.twcable.grabbit.spring.batch.repository.services.impl

import com.twcable.grabbit.DateUtil
import com.twcable.grabbit.jcr.JcrUtil
import com.twcable.grabbit.spring.batch.repository.JcrExecutionContextDao
import com.twcable.grabbit.spring.batch.repository.services.CleanJobRepository
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.felix.scr.annotations.Activate
import org.apache.felix.scr.annotations.Component
import org.apache.felix.scr.annotations.Reference
import org.apache.felix.scr.annotations.Service
import org.apache.sling.api.resource.Resource
import org.apache.sling.api.resource.ResourceResolver
import org.apache.sling.api.resource.ResourceResolverFactory
import org.apache.sling.api.resource.ValueMap

import static com.twcable.grabbit.spring.batch.repository.JcrExecutionContextDao.JOB_EXECUTION_CONTEXT_ROOT
import static com.twcable.grabbit.spring.batch.repository.JcrExecutionContextDao.STEP_EXECUTION_CONTEXT_ROOT
import static com.twcable.grabbit.spring.batch.repository.JcrJobExecutionDao.*
import static com.twcable.grabbit.spring.batch.repository.JcrJobInstanceDao.JOB_INSTANCE_ROOT
import static com.twcable.grabbit.spring.batch.repository.JcrStepExecutionDao.*

@Slf4j
@CompileStatic
@Component(label = "Grabbit Clean Job Repository Service", description = "Grabbit Clean Job Repository Service", immediate = true, metatype = true, enabled = true)
@Service(CleanJobRepository)
@SuppressWarnings(['GroovyUnusedDeclaration', 'GrMethodMayBeStatic'])
class DefaultCleanJobRepository implements CleanJobRepository {

    @Reference
    ResourceResolverFactory resourceResolverFactory

    @Activate
    void activate() {
        log.info "CleanJobRepository Service activated"
    }

    @Override
    void cleanJobRepository(int hours) {
         //TODO : Explain why unique() is required. There are 2 versions of Resources returned back
         // One for JcrNodeResource and one for SocialResourceWrapper
         JcrUtil.withResourceResolver(resourceResolverFactory, "admin") { ResourceResolver resolver ->

             //Find jobExecutions that are older than "hours" from now
             List<String> olderThanHoursJobExecutions = getJobExecutionPathsOlderThanHours(hours, resolver)

             //Find jobInstances by "id" on jobInstances == instanceId on jobExecutions
             List<String> jobInstancesToRemove = getJobInstancePathsToRemove(olderThanHoursJobExecutions, resolver)

             //Find stepExecutions by "jobExecutionId" on stepExecutions == "executionId" on jobExecutions
             List<String> stepExecutionsToRemove = getStepExecutionPathsToRemove(olderThanHoursJobExecutions, resolver)

             //Find executionContexts/job by "executionId" on executionContexts/job == "executionId" on jobExecutions
             List<String> jobExecutionContextsToRemove = getJobExecutionContextPathsToRemove(olderThanHoursJobExecutions, resolver)

             //Find executionContexts/step by "executionId" on executionContexts/step == "id" on stepExecutionsToRemove
             List<String> stepExecutionContextsToRemove = getStepExecutionContextPathsToRemove(stepExecutionsToRemove, resolver)

             log.info "jobInstancesToRemove: $jobInstancesToRemove, size: ${jobInstancesToRemove.size()}"
             log.info "stepExecutionsToRemove: $stepExecutionsToRemove, size: ${stepExecutionsToRemove.size()}"
             log.info "jobExecutionContextsToRemove: $jobExecutionContextsToRemove, size: ${jobExecutionContextsToRemove.size()}"
             log.info "stepExecutionContextsToResource: $stepExecutionContextsToRemove, size: ${stepExecutionContextsToRemove.size()}"

             log.info "Removing ${jobInstancesToRemove.size()} JobInstances"
             removeResources(jobInstancesToRemove, resolver)
             log.info "Removing ${olderThanHoursJobExecutions.size()} JobExecutions"
             removeResources(olderThanHoursJobExecutions, resolver)
             log.info "Removing ${stepExecutionsToRemove.size()} StepExecutions"
             removeResources(stepExecutionsToRemove, resolver)
             log.info "Removing ${jobExecutionContextsToRemove.size()} JobExecutionContexts"
             removeResources(jobExecutionContextsToRemove, resolver)
             log.info "Removing ${stepExecutionContextsToRemove.size()} StepExecutionContexts"
             removeResources(stepExecutionContextsToRemove, resolver)
        }
    }

    private List<String> getJobExecutionPathsOlderThanHours(int hours, ResourceResolver resolver) {
        //Find all jobExecutions that are FAILED or COMPLETED
        List<String> jobExecutions = getAllJobExecutionPaths(resolver)

        //Create a Date object that is "hours" ago from now
        Calendar olderThanHours = Calendar.getInstance()
        log.info "Current time: ${olderThanHours.time}"
        olderThanHours.add(Calendar.HOUR, -hours)
        log.info "Hours ${hours} .. OlderThanHours Time: ${olderThanHours.time}"

        //Find all resources that are older than "olderThanHours" Date
        List<String> olderResourcePaths = jobExecutions.findAll { String resourcePath ->
            Resource resource = resolver.getResource(resourcePath)
            ValueMap props = resource.adaptTo(ValueMap)
            String dateInIsoString = props[END_TIME] as String
            Date endTimeDate = DateUtil.getDateFromISOString(dateInIsoString)
            olderThanHours.time.compareTo(endTimeDate) > 0
        } as List<String>
        log.info "JobExecutionsOlder than ${hours} hours: $olderResourcePaths , length: ${olderResourcePaths.size()}"
        return olderResourcePaths
    }

    private List<String> getAllJobExecutionPaths(ResourceResolver resolver) {
        String jobExecutionsQuery = "select * from [nt:unstructured] as s where " +
                "ISDESCENDANTNODE(s,'${JOB_EXECUTION_ROOT}') AND ( s.status = 'FAILED' or s.status = 'COMPLETED' )"
        List<String> jobExecutions = resolver.findResources(jobExecutionsQuery, "JCR-SQL2")
                .toList()
                .collect { it.path }
                .unique() as List<String>
        log.info "JobExecutions: $jobExecutions, size: ${jobExecutions.size()}"
        jobExecutions
    }

    private List<String> getJobInstancePathsToRemove(List<String> jobExecutionResourcePaths, ResourceResolver resolver) {
        List<String> jobInstancesToRemove = []
        jobExecutionResourcePaths.each { String jobExecutionResourcePath ->
            Resource jobExecutionResource = resolver.getResource(jobExecutionResourcePath)
            ValueMap props = jobExecutionResource.adaptTo(ValueMap)
            Long instanceId = props[INSTANCE_ID] as Long
            String jobInstanceToRemoveResourcePath = "${JOB_INSTANCE_ROOT}/${instanceId}".toString()
            Resource jobInstanceToRemove = resolver.getResource(jobInstanceToRemoveResourcePath)
            if (!jobInstanceToRemove) {
                log.info "JobInstance with id : ${instanceId} is already removed"
            } else {
                jobInstancesToRemove.add(jobInstanceToRemoveResourcePath)
            }
        }
        return jobInstancesToRemove
    }

    private List<String> getStepExecutionPathsToRemove(List<String> jobExecutionResourcePaths, ResourceResolver resolver) {
        List<String> stepExecutionsToRemove = []
        jobExecutionResourcePaths.each { String jobExecutionResourcePath ->
            Resource jobExecutionResource = resolver.getResource(jobExecutionResourcePath)
            ValueMap props = jobExecutionResource.adaptTo(ValueMap)
            Long jobExecutionId = props[EXECUTION_ID] as Long
            String query = "select * from [nt:unstructured] as s " +
                    "where ISDESCENDANTNODE(s,'${STEP_EXECUTION_ROOT}') AND ( s.${JOB_EXECUTION_ID} = ${jobExecutionId})"
            List<String> stepExecutions = resolver.findResources(query, "JCR-SQL2").toList().collect { it.path }
            stepExecutionsToRemove.addAll(stepExecutions)
        }
        //Duplicates need to be removed
        return stepExecutionsToRemove.unique() as List<String>
    }

    private List<String> getJobExecutionContextPathsToRemove(List<String> jobExecutionResourcePaths, ResourceResolver resolver) {
        List<String> jobExecutionContextPathsToRemove = []
        jobExecutionResourcePaths.each { String jobExecutionResourcePath ->
            Resource jobExecutionResource = resolver.getResource(jobExecutionResourcePath)
            ValueMap props = jobExecutionResource.adaptTo(ValueMap)
            Long jobExecutionId = props[EXECUTION_ID] as Long
            String query = "select * from [nt:unstructured] as s " +
                    "where ISDESCENDANTNODE(s,'${JOB_EXECUTION_CONTEXT_ROOT}') AND ( s.${JcrExecutionContextDao.EXECUTION_ID} = ${jobExecutionId})"
            List<String> jobExecutionContextPaths = resolver.findResources(query, "JCR-SQL2").toList().collect { it.path }
            jobExecutionContextPathsToRemove.addAll(jobExecutionContextPaths)
        }
        //Duplicates need to be removed
        return jobExecutionContextPathsToRemove.unique() as List<String>
    }

    private List<String> getStepExecutionContextPathsToRemove(List<String> stepExecutionResourcePaths, ResourceResolver resolver) {
        List<String> stepExecutionContextPathsToRemove = []
        stepExecutionResourcePaths.each { String stepExecutionResourcePath ->
            Resource stepExecutionResource = resolver.getResource(stepExecutionResourcePath)
            ValueMap props = stepExecutionResource.adaptTo(ValueMap)
            Long stepExecutionId = props[ID] as Long
            String query = "select * from [nt:unstructured] as s " +
                    "where ISDESCENDANTNODE(s,'${STEP_EXECUTION_CONTEXT_ROOT}') AND ( s.${JcrExecutionContextDao.EXECUTION_ID} = ${stepExecutionId})"
            List<String> stepExecutionContextPaths = resolver.findResources(query, "JCR-SQL2").toList().collect { it.path }
            stepExecutionContextPathsToRemove.addAll(stepExecutionContextPaths)
        }
        //Duplicates need to be removed
        return stepExecutionContextPathsToRemove.unique() as List<String>
    }

    private void removeResources(List<String> resourcePathsToRemove, ResourceResolver resolver) {
        resourcePathsToRemove.each {
            Resource resourceToDelete = resolver.getResource(it)
            if(resourceToDelete) {
                resolver.delete(resourceToDelete)
                log.debug "Resource ${it} will be removed"
            }
            else {
                log.info "Resource ${it} doesn't exist"
            }
        }
        resolver.commit()
        resolver.refresh()
    }
}
