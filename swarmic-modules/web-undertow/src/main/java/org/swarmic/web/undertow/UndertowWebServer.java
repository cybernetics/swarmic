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

package org.swarmic.web.undertow;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.PathResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;
import org.jboss.weld.environment.servlet.Listener;
import org.swarmic.web.base.AbstractWebServer;
import org.swarmic.web.spi.ServletDescriptor;
import org.swarmic.web.spi.WebServerConfiguration;
import org.swarmic.web.undertow.websocket.UndertowWebSocketExtension;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.servlet.ServletException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import static io.undertow.Handlers.path;
import static io.undertow.Handlers.resource;
import static io.undertow.servlet.Servlets.listener;

@ApplicationScoped
public class UndertowWebServer extends AbstractWebServer {
    @Inject
    private Function<ServletDescriptor, ServletInfo> mapper;
    @Inject
    private UndertowWebSocketExtension extension;
    @Inject
    private BeanManager beanManager;
    private Set<ServletInfo> servlets = new HashSet<>();
    private Undertow undertow;

    @Inject
    public UndertowWebServer(WebServerConfiguration webServerConfiguration) {
        super(webServerConfiguration);
    }

    @Override
    public void addServlet(ServletDescriptor servletDescriptor) {
        servlets.add(mapper.apply(servletDescriptor));
    }

    @Override
    public void start() {
        DeploymentInfo di = new DeploymentInfo()
                .setContextPath("/")
                .setDeploymentName("Undertow")
                .setResourceManager(new ClassPathResourceManager(getClass().getClassLoader()))
                .setClassLoader(ClassLoader.getSystemClassLoader())
                .addListener(listener(Listener.class));

        Collection<Class<?>> endpoints = extension.getEndpointClasses();
        if(!endpoints.isEmpty()) {
            WebSocketDeploymentInfo webSocketDeploymentInfo = new WebSocketDeploymentInfo();
            endpoints.forEach(webSocketDeploymentInfo::addEndpoint);
            di.addServletContextAttribute(WebSocketDeploymentInfo.ATTRIBUTE_NAME, webSocketDeploymentInfo);
        }

        getServletContextAttributes().forEach(di::addServletContextAttribute);

        servlets.forEach(di::addServlet);

        DeploymentManager deploymentManager = Servlets.defaultContainer().addDeployment(di);
        deploymentManager.deploy();
        try {
            HttpHandler servletHandler = deploymentManager.start();
            ResourceHandler resourceHandler = resource(new PathResourceManager(Paths.get(getWebServerConfiguration().getFileDir()), 100))
                    .setDirectoryListingEnabled(true);
            PathHandler path = path(Handlers.redirect("/"))
                    .addPrefixPath("/", servletHandler)
                    .addPrefixPath("/resource", resourceHandler);
            this.undertow = Undertow.builder()
                    .addHttpListener(getWebServerConfiguration().getWebserverPort(), "0.0.0.0")
                    .setHandler(path)
                    .build();
            this.undertow.start();
        } catch (ServletException e) {
            throw new RuntimeException("Unable to start server", e);
        }
    }

    @Override
    @PreDestroy
    public void stop() {
        if(this.undertow != null) {
            this.undertow.stop();
            this.undertow = null;
        }
    }
}
