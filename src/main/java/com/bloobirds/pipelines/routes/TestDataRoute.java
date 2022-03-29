package com.bloobirds.pipelines.routes;

import com.bloobirds.datamodel.repo.*;
import com.bloobirds.pipelines.messages.KMesg;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class TestDataRoute extends EndpointRouteBuilder {

    @Inject
    CompanyRepository companyRepo;
    @Inject
    ContactRepository contactRepo;
    @Inject
    ActivityRepository activityRepo;
    @Inject
    OpportunityRepository opportunityRepo;
    @Inject
    TaskRepository  taskRepo;

    @Override
    public void configure()  {
        getCamelContext().getRegistry().bind("companyRepo",companyRepo);
        getCamelContext().getRegistry().bind("contactRepo",contactRepo);
        getCamelContext().getRegistry().bind("activityRepo",activityRepo);
        getCamelContext().getRegistry().bind("opportunityRepo",opportunityRepo);
        getCamelContext().getRegistry().bind("taskRepo",taskRepo);

        from(platformHttp("/Test/kmsg"))
                .unmarshal().json(JsonLibrary.Jackson, KMesg.class)
                .choice()
                    .when(simple("${body.isCompany}")).bean("companyRepo")
                    .when(simple("${body.isLead}")).bean("contactRepo")
                    .when(simple("${body.isActivity}")).bean("activityRepo")
                    .when(simple("${body.isTask}")).bean("taskRepo")
                    .when(simple("${body.isOpportunity}")).bean("opportunityRepo")
                    .otherwise().to(log(" ${body.bobjectType} ?"))
                .end()
                .marshal().json(JsonLibrary.Jackson);

    }

}