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

package org.swarmic.security.test;


import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.swarmic.security.api.Identity;
import org.swarmic.security.api.HasAllRoles;
import org.swarmic.security.api.MissingRolesException;
import org.swarmic.security.impl.HasAllRolesInterceptor;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@RunWith(Arquillian.class)
public class HasAllRolesTest {
    private static final String SUCCESS = "success";

    @Inject
    private RoleBasedIdentity roleBasedIdentity;

    @Inject
    private Controller controller;

    @Deployment
    public static Archive<?> deployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClasses(HasAllRoles.class, HasAllRolesInterceptor.class);
    }

    @Test
    public void shouldFailWithNoRoles() {
        roleBasedIdentity.addRoles();
        assertThatThrownBy(() -> controller.doWork())
                .isInstanceOf(MissingRolesException.class)
                .hasMessageContaining("jane, bob, ralph");
    }

    @Test
    public void shouldSucceedWithAllRoles() {
        roleBasedIdentity.addRoles("jane","bob","ralph");
        String s = controller.doWork();
        assertThat(s).isEqualTo(SUCCESS);
    }

    @ApplicationScoped
    public static class RoleBasedIdentity implements Identity {
        private List<String> roles = new ArrayList<>();

        @Override
        public boolean isLoggedIn() {
            return false;
        }

        @Override
        public boolean hasRole(String x) {
            return roles.indexOf(x) >= 0;
        }

        public void addRoles(String... newRoles) {
            roles.clear();
            roles.addAll(asList(newRoles));
        }

    }

    @HasAllRoles({"jane","bob","ralph"})
    @ApplicationScoped
    public static class Controller {
        String doWork() {
            return SUCCESS;
        }
    }
}
