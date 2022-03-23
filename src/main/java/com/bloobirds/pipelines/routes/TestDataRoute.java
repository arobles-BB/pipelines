package com.bloobirds.pipelines.routes;

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

    @Override
    public void configure()  {
        getCamelContext().getRegistry().bind("companyRepo",companyRepo);
        getCamelContext().getRegistry().bind("contactRepo",contactRepo);

        from(platformHttp("/Test/kmsg"))
                .unmarshal().json(JsonLibrary.Jackson, KMesg.class)
                .choice()
                    .when(simple("${body.isCompany}")).to(log("Company"))
                    .when(simple("${body.isLead}")).bean("contactRepo")
                    .otherwise().to(log(" ${body.bobjectType} ?"))
                .end()
                .marshal().json(JsonLibrary.Jackson);

    }

}