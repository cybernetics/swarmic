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

package org.swarmic.web.base;

import org.swarmic.web.spi.ServletDescriptor;
import org.swarmic.web.spi.WebServer;
import org.swarmic.web.spi.WebServerConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractWebServer implements WebServer {
    private final List<ServletDescriptor> servletDescriptors = new ArrayList<>();
    private final Map<String, Object> servletContextAttributes = new HashMap<>();
    private final WebServerConfiguration webServerConfiguration;

    protected AbstractWebServer(WebServerConfiguration webServerConfiguration) {
        this.webServerConfiguration = webServerConfiguration;
    }

    @Override
    public void addServlet(ServletDescriptor servletDescriptor) {
        this.servletDescriptors.add(servletDescriptor);
    }

    @Override
    public void addServletContextAttribute(String name, Object value) {
        servletContextAttributes.put(name, value);
    }

    protected List<ServletDescriptor> getServletDescriptors() {
        return Collections.unmodifiableList(servletDescriptors);
    }

    protected Map<String, Object> getServletContextAttributes() {
        return Collections.unmodifiableMap(servletContextAttributes);
    }

    public WebServerConfiguration getWebServerConfiguration() {
        return webServerConfiguration;
    }
}
