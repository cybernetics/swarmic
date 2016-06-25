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

package org.swarmic.jta.narayana;

import com.arjuna.ats.jta.utils.JNDIManager;
import org.jboss.logging.Logger;
import org.jnp.server.NamingBeanImpl;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.DeploymentException;
import javax.enterprise.inject.spi.Extension;

/**
 *
 * This CDI extension starts Jboss JNDI server when CDI container starts
 * and shut it down with container.
 *
 * @author Antoine Sabot-Durand
 */
public class JndiServerExtension implements Extension {

    private final NamingBeanImpl namingBean = new NamingBeanImpl();

    private static Logger LOG = Logger.getLogger(JndiServerExtension.class);

    public void startJndiServer(@Observes BeforeBeanDiscovery bbd) {
        try {
            LOG.info("Starting JNDI Server");
            // Start JNDI server
            namingBean.start();
            // Bind JTA implementation with default names
            JNDIManager.bindJTAImplementation();
        } catch (Exception e) {
            throw new DeploymentException("An error occurred while starting JNDI server", e);
        }
    }

    public void stopJndiServer(@Observes BeforeShutdown bs) {
        LOG.info("Stoping JNDI Server");
        namingBean.stop();
    }

}
