/*******************************************************************************
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors
 * by the @authors tag.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.swarmic.rest.resteasy;

import org.jboss.resteasy.cdi.ResteasyCdiExtension;
import org.jboss.resteasy.plugins.server.servlet.HttpServlet30Dispatcher;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.swarmic.web.spi.ServletContextAttributeProvider;
import org.swarmic.web.spi.ServletDescriptor;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.ws.rs.core.Application;
import java.util.Map;

import static java.util.Collections.singletonMap;

/**
 * Configuration of Resteasy application servlet
 *
 * @author John D. Ament
 *
 */
@ApplicationScoped
public class ResteasyServletContextAttributeProvider implements ServletContextAttributeProvider {
    @Inject
    private ResteasyCdiExtension resteasyCdiExtension;
    @Inject
    private Instance<Application> applicationInstance;

    @Override
    public Map<String, Object> getAttributes() {
        ResteasyDeployment deployment = new ResteasyDeployment();
        deployment.getActualResourceClasses().addAll(resteasyCdiExtension.getResources());
        deployment.getActualProviderClasses().addAll(resteasyCdiExtension.getProviders());
        if( !(applicationInstance.isUnsatisfied() || applicationInstance.isAmbiguous())) {
            deployment.setApplication(applicationInstance.get());
        }
        deployment.setInjectorFactoryClass(Cdi11InjectorFactory.class.getName());

        return singletonMap(ResteasyDeployment.class.getName(), deployment);
    }

    @Produces
    public ServletDescriptor resteasyServlet() {
        return new ServletDescriptor("ResteasyServlet",null, new String[]{"/*"},1,null,true,HttpServlet30Dispatcher.class);
    }
}
