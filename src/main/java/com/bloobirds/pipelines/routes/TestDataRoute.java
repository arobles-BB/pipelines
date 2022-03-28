package com.bloobirds.pipelines.routes;

import com.bloobirds.datamodel.repo.ActivityRepository;
import com.bloobirds.datamodel.repo.CompanyRepository;
import com.bloobirds.datamodel.repo.ContactRepository;
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

    @Override
    public void configure()  {
        getCamelContext().getRegistry().bind("companyRepo",companyRepo);
        getCamelContext().getRegistry().bind("contactRepo",contactRepo);
        getCamelContext().getRegistry().bind("activityRepo",activityRepo);

        from(platformHttp("/Test/kmsg"))
                .unmarshal().json(JsonLibrary.Jackson, KMesg.class)
                .choice()
                    .when(simple("${body.isCompany}")).bean("companyRepo")
                    .when(simple("${body.isLead}")).bean("contactRepo")
                    .when(simple("${body.isActivity}")).bean("activityRepo")
  //                  .when(simple("${body.isTask}")).bean("tasktRepo")
    //                .when(simple("${body.isOpportunity}")).bean("opportunityRepo")
                    .otherwise().to(log(" ${body.bobjectType} ?"))
                .end()
                .marshal().json(JsonLibrary.Jackson);

    }

}