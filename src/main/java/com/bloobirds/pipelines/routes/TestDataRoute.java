package com.bloobirds.pipelines.routes;

import com.bloobirds.datamodel.repo.CompanyRepository;
import com.bloobirds.datamodel.repo.ContactRepository;
import com.bloobirds.pipelines.messages.KMesg;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
//    @Inject
//    ObjectMapper mapper;


    @Override
    public void configure()  {

//        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

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