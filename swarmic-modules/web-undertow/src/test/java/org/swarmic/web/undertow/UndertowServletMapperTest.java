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

import io.undertow.servlet.api.ServletInfo;
import org.junit.Test;
import org.swarmic.web.spi.ServletDescriptor;

import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.HttpServlet;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class UndertowServletMapperTest {
    private final UndertowServletMapper mapper = new UndertowServletMapper();

    @Test
    public void testConversion() {
        Class<? extends HttpServlet> servletClass = DefaultServlet.class;
        String name = "name";
        String[] value = new String[]{"a"};
        String[] urlPatterns = new String[]{"/b"};
        int loadOnStartup = 2;
        WebInitParam[] initParams = null;
        boolean asyncSupported = true;
        ServletDescriptor servletDescriptor = new ServletDescriptor(name, value, urlPatterns, loadOnStartup,
                initParams, asyncSupported, servletClass);
        ServletInfo servletInfo = mapper.apply(servletDescriptor);

        assertThat(servletInfo.getName()).isEqualTo(name);
        assertThat(servletInfo.getServletClass()).isEqualTo(servletClass);
        assertThat(servletInfo.getMappings()).isEqualTo(asList(urlPatterns));
        assertThat(servletInfo.getLoadOnStartup()).isEqualTo(loadOnStartup);
        assertThat(servletInfo.isAsyncSupported()).isEqualTo(asyncSupported);
    }
}
