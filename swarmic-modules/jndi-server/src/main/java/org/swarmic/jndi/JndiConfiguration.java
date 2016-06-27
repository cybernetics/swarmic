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

package org.swarmic.jndi;

import org.jboss.logging.Logger;
import org.jnp.server.NamingBeanImpl;
import org.swarmic.core.LifecyleAction;
import org.swarmic.core.ContainerConfigurator;

import javax.annotation.Priority;
import javax.enterprise.inject.spi.DeploymentException;

/**
 * Created by antoine on 27/06/2016.
 */
@Priority(10)
public class JndiConfiguration implements LifecyleAction {


    private final NamingBeanImpl namingBean = new NamingBeanImpl();

    private static Logger LOG = Logger.getLogger(JndiConfiguration.class);

    @Override
    public void beforeBootstrap(ContainerConfigurator configurator) {

        try {
            LOG.info("Starting JNDI Server");
            // Start JNDI server
            namingBean.start();
        } catch (Exception e) {
            throw new DeploymentException("An error occurred while starting JNDI server", e);
        }

    }

    //TODO add cleanup phase in core engine
    public void afterShutdown() {
        LOG.info("Stoping JNDI Server");
        namingBean.stop();
    }
}
