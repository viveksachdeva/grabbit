package com.twcable.grabbit.spring.batch.repository.services.impl

import com.twcable.grabbit.jcr.JcrUtil
import com.twcable.grabbit.spring.batch.repository.JcrJobRepositoryFactoryBean
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
import org.springframework.batch.core.BatchStatus
import org.springframework.context.ConfigurableApplicationContext

@Slf4j
@CompileStatic
@Component(label = "Grabbit Clean Job Repository Service", description = "Grabbit Clean Job Repository Service", immediate = true, metatype = true, enabled = true)
@Service(CleanJobRepository)
@SuppressWarnings(['GroovyUnusedDeclaration', 'GrMethodMayBeStatic'])
class DefaultCleanJobRepository implements CleanJobRepository {

    @Reference
    ResourceResolverFactory resourceResolverFactory

    @Reference
    ConfigurableApplicationContext configurableApplicationContext

    @Activate
    void activate() {
        log.info "CleanJobRepository Service activated"
    }

    @Override
    void cleanJobRepository(int hours) {
         //TODO : Explain why unique() is required. There are 2 versions of Resources returned back
         // One for JcrNodeResource and one for SocialResourceWrapper

         JcrJobRepositoryFactoryBean jobRepositoryFactoryBean = configurableApplicationContext.getBean(JcrJobRepositoryFactoryBean)

         List<String> jobExecutionPaths = jobRepositoryFactoryBean.jobExecutionDao.getJobExecutions([BatchStatus.FAILED, BatchStatus.COMPLETED])
         List<String> olderThanHoursJobExecutions = jobRepositoryFactoryBean.jobExecutionDao.getOlderJobExecutions(hours, jobExecutionPaths)
         List<String> jobInstancesToRemove = jobRepositoryFactoryBean.jobInstanceDao.getJobInstancePaths(olderThanHoursJobExecutions)
         List<String> stepExecutionsToRemove = jobRepositoryFactoryBean.stepExecutionDao.getStepExecutionPaths(olderThanHoursJobExecutions)
         List<String> jobExecutionContextsToRemove = jobRepositoryFactoryBean.executionContextDao.getJobExecutionContextPaths(olderThanHoursJobExecutions)
         List<String> stepExecutionContextsToRemove = jobRepositoryFactoryBean.executionContextDao.getStepExecutionContextPaths(stepExecutionsToRemove)

         JcrUtil.withResourceResolver(resourceResolverFactory, "admin") { ResourceResolver resolver ->

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
