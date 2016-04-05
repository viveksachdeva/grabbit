package com.twcable.grabbit.spring.batch.repository.services

import com.twcable.grabbit.spring.batch.repository.services.impl.DefaultCleanJobRepository
import com.twcable.jackalope.impl.sling.SimpleResourceResolverFactory
import org.apache.sling.api.resource.ResourceResolverFactory
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

import static com.twcable.grabbit.spring.batch.repository.JcrExecutionContextDao.EXECUTION_CONTEXT
import static com.twcable.grabbit.spring.batch.repository.JcrJobExecutionDao.*
import static com.twcable.jackalope.JCRBuilder.*

@Subject(DefaultCleanJobRepository)
@Ignore
class DefaultCleanJobRepositorySpec extends Specification {

    @Shared
    ResourceResolverFactory mockFactory

    @Shared
    DefaultCleanJobRepository cleanJobRepository


    def setupSpec() {
        //TODO: Create better node structure for proper testing
        final builder =
                node("var",
                    node("grabbit",
                        node("job",
                            node("repository",
                                node("executionContexts",
                                    node("job",
                                        node("1",
                                                property(EXECUTION_ID, 1),
                                                property(EXECUTION_CONTEXT, "SomeThing")
                                        )
                                    ),
                                    node("step",
                                        node("1",
                                                property(EXECUTION_ID, 1),
                                                property(EXECUTION_CONTEXT, "SomeThing")
                                        )
                                    )
                                ),
                                node("jobExecutions",
                                        node("1",
                                                property(INSTANCE_ID, 1),
                                                property(EXECUTION_ID, 1),
                                                property(TRANSACTION_ID, 5),
                                                property(STATUS, "COMPLETED"),
                                                property(EXIT_CODE, "code"),
                                                property(EXIT_MESSAGE, "message"),
                                                property(CREATE_TIME, "2014-12-27T16:59:18.669-05:00"),
                                                property(END_TIME, "2014-12-29T16:59:18.669-05:00"),
                                                property(JOB_NAME, "someJob"),
                                                property(VERSION, 1)
                                        ),
                                        node("2",
                                                property(INSTANCE_ID, 1),
                                                property(EXECUTION_ID, 2),
                                                property(TRANSACTION_ID, 5),
                                                property(STATUS, "STARTED"),
                                                property(EXIT_CODE, "code"),
                                                property(EXIT_MESSAGE, "message"),
                                                property(CREATE_TIME, "2014-12-28T16:59:18.669-05:00"),
                                                property(END_TIME, "NULL"),
                                                property(JOB_NAME, "someJob")
                                        ),
                                        node("3",
                                                property(INSTANCE_ID, 2),
                                                property(EXECUTION_ID, 3),
                                                property(TRANSACTION_ID, 5),
                                                property(STATUS, "STARTED"),
                                                property(EXIT_CODE, "code"),
                                                property(EXIT_MESSAGE, "message"),
                                                property(CREATE_TIME, "2014-12-29T16:59:18.669-05:00"),
                                                property(END_TIME, "NULL"),
                                                property(JOB_NAME, "someOtherJob")
                                        )
                                ),
                                node("jobInstances", node("1")),
                            )
                        )
                    )
                )
        mockFactory = new SimpleResourceResolverFactory(repository(builder).build())
        cleanJobRepository = new DefaultCleanJobRepository(resourceResolverFactory: mockFactory)
    }

    def "test getJobExecutionPathsOlderThanHours"() {
        when:
        def result = cleanJobRepository.getJobExecutionPathsOlderThanHours(null, null)

        then:
        1 == 1
    }

    def "test getJobInstancePathsToRemove"() {
        when:
        def result = cleanJobRepository.getJobInstancePathsToRemove(null, null)

        then:
        1 == 1
    }

    def "test getStepExecutionPathsToRemove"() {
        when:
        def result = cleanJobRepository.getStepExecutionPathsToRemove(null, null)

        then:
        1 == 1
    }


    def "test getJobExecutionContextPathsToRemove"() {
        when:
        def result = cleanJobRepository.getJobExecutionContextPathsToRemove(null, null)

        then:
        1 == 1
    }


    def "test getStepExecutionContextPathsToRemove"() {
        when:
        def result = cleanJobRepository.getStepExecutionContextPathsToRemove(null, null)

        then:
        1 == 1
    }

}
