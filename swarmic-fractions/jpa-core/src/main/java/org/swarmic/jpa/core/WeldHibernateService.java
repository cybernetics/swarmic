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

package org.swarmic.jpa.core;

import org.jboss.logging.Logger;
import org.jboss.weld.exceptions.IllegalArgumentException;
import org.jboss.weld.injection.spi.JpaInjectionServices;
import org.jboss.weld.injection.spi.ResourceReferenceFactory;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.persistence.*;

/**
 *
 * This implementation of {@link JpaInjectionServices} allows direct injection of persistence unit and persistence context
 * with JPA annotations in Weld container
 *
 * @author Antoine Sabot-Durand
 */
public class WeldHibernateService implements JpaInjectionServices {

    private static Logger LOG = Logger.getLogger(WeldHibernateService.class);

    @Override
    public ResourceReferenceFactory<EntityManager> registerPersistenceContextInjectionPoint(InjectionPoint ip) {
        PersistenceContext pc = ip.getAnnotated().getAnnotation(PersistenceContext.class);
        if (pc == null) {
            throw new IllegalArgumentException("Injecting an EntityManager without @PersistenceContext annotation");
        }

        String name=pc.unitName();
        LOG.info("Creating EntityManagerReferenceFactory for unit " + name);
        return new EntityManagerReferenceFactory(name);
    }

    @Override
    public ResourceReferenceFactory<EntityManagerFactory> registerPersistenceUnitInjectionPoint(InjectionPoint ip) {
        PersistenceUnit pc = ip.getAnnotated().getAnnotation(PersistenceUnit.class);
        if (pc == null) {
            throw new IllegalArgumentException("Injecting an EntityManagerFactory without @PersistenceUnit annotation");
        }
        String name=pc.unitName();
        LOG.info("Creating EntityManagerFactoryReferenceFactory for unit " + name);
        return new EntityManagerFactoryReferenceFactory(name);
    }

    @Override
    public EntityManager resolvePersistenceContext(InjectionPoint ip) {
        return registerPersistenceContextInjectionPoint(ip).createResource().getInstance();
    }

    @Override
    public EntityManagerFactory resolvePersistenceUnit(InjectionPoint ip) {
        return registerPersistenceUnitInjectionPoint(ip).createResource().getInstance();
    }

    @Override
    public void cleanup() {

    }
}
