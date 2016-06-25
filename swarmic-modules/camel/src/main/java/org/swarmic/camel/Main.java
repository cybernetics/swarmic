/*
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
 */

package org.swarmic.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.main.MainSupport;
import org.jboss.weld.environment.se.WeldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swarmic.core.Runner;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.Vetoed;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * This Boostrap class use Swarmic {@link Runner} to start CDI container and also starts Camel
 *
 * @author Antoine Sabot-Durand
 * @author Antonin Stefanutti
 */
@Vetoed
public class Main extends MainSupport {

    static {
        // Since version 2.3.0.Final and WELD-1915, Weld SE registers a shutdown hook that conflicts
        // with Camel main support. See WELD-2051. The system property above is available starting
        // Weld 2.3.1.Final to deactivate the registration of the shutdown hook.
        System.setProperty("org.jboss.weld.se.shutdownHook", String.valueOf(Boolean.FALSE));
    }

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private WeldContainer cdiContainer;

    public static void main(String... args) throws Exception {
        Main main = new Main();
        main.run(args);
    }


    @Override
    protected ProducerTemplate findOrCreateCamelTemplate() {
        Instance<CamelContext> contexts = getCamelContextInstances();
        if (contexts.isUnsatisfied()) {
            throw new UnsatisfiedResolutionException("No default Camel context is deployed, cannot create default ProducerTemplate!");
        }
        CamelContext context = contexts.get();
        return context.createProducerTemplate();
    }

    @Override
    protected Map<String, CamelContext> getCamelContextMap() {
        Map<String, CamelContext> answer = new HashMap<>();
        for (CamelContext context : getCamelContextInstances()) {
            answer.put(context.getName(), context);
        }
        return answer;
    }

    private Instance<CamelContext> getCamelContextInstances() {
        return cdiContainer.instance().select(CamelContext.class);
    }

    @Override
    protected void doStart() throws Exception {

        cdiContainer = new Runner().run();
        super.doStart();
        postProcessContext();
        warnIfNoCamelFound();
    }

    private void warnIfNoCamelFound() {
        // Warn if there is no CDI Camel contexts
        if (getCamelContextInstances().isUnsatisfied()) {
            LOG.warn("Camel CDI main has started with no Camel context!");
        }
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
        if (cdiContainer != null) {
            cdiContainer.shutdown();
        }
    }
}