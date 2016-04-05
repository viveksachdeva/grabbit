package com.twcable.grabbit.client.servlets

import com.twcable.grabbit.spring.batch.repository.services.CleanJobRepository
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.felix.scr.annotations.Reference
import org.apache.felix.scr.annotations.sling.SlingServlet
import org.apache.sling.api.SlingHttpServletRequest
import org.apache.sling.api.SlingHttpServletResponse
import org.apache.sling.api.servlets.SlingSafeMethodsServlet

import javax.annotation.Nonnull

@Slf4j
@CompileStatic
@SlingServlet(methods = ['GET'], paths = ["/bin/cleanRepository"])
class GrabbitCleanJobRepositoryServlet extends SlingSafeMethodsServlet {

    @Reference
    CleanJobRepository cleanJobRepository

    @Override
    protected void doGet( @Nonnull SlingHttpServletRequest request, @Nonnull SlingHttpServletResponse response) {
        int hours = request.getParameter("hours").toInteger()
        cleanJobRepository.cleanJobRepository(hours)
    }
}
