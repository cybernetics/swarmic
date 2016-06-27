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

package org.swarmic.core;

import org.jboss.logging.Logger;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;


import javax.annotation.Priority;
import javax.enterprise.inject.Vetoed;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This class is in charge of Fraction configuration discovery and Swarmic bootstrap.
 * It can be inherited or embedded to add features at boot time
 *
 * @author Antoine Sabot-Durand
 */
@Vetoed
public class Runner {

    private static final Logger LOG = Logger.getLogger(Runner.class);


    private static List<LifecyleAction> lfa = new ArrayList<>();


    private WeldContainer cdiContainer;

    /**
     * This method beforeBootstrap the container and starts it.
     * It rturns the build container to allow a third party developper to use it directly in an alternative boot sequence
     *
     * @param args command line args
     * @return the CDI container bootstrapped
     */
    public WeldContainer run(String... args) {
        LOG.info("Starting Swarmic");
        displayBanner();
        Weld weld = new Weld();
        runConfigurators(new ContainerConfigurator(weld));
        registerShutdownHook();
        cdiContainer = weld.initialize();
        return cdiContainer;
    }

    /**
     * Look for all service loader for {@link ContainerConfigurator} and calls them
     *
     * @param configurator the container {@link ContainerConfigurator} to beforeBootstrap
     */
    protected void runConfigurators(ContainerConfigurator configurator) {

        Stream<LifecyleAction> stream = StreamSupport.stream(ServiceLoader.load(LifecyleAction.class).spliterator(), false);

        ToIntFunction<LifecyleAction> getPriority = l -> l.getClass().isAnnotationPresent(Priority.class) ?
                l.getClass().getAnnotation(Priority.class).value() : Integer.MAX_VALUE;

        Comparator<LifecyleAction> byPriority = (l1, l2) -> Integer.compare(getPriority.applyAsInt(l1), getPriority.applyAsInt(l2));

        stream.sorted(byPriority).forEachOrdered(c -> {
            LOG.info("Calling configurator: " + c.getClass().getCanonicalName());
            c.beforeBootstrap(configurator);
            lfa.add(c);
        });
    }

    protected void registerShutdownHook() {

        Runtime.getRuntime().addShutdownHook(new Thread() {
           public void run() {
               lfa.forEach(LifecyleAction::afterShutdown);
           }
        });
    }

    /**
     * Display the Swarmic Banner
     */
    protected void displayBanner() {

        URL url = Bootstrap.class.getResource("/banner.txt");
        if (url != null)
            try (BufferedReader reader =
                         new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"))) {

                StringBuilder b = new StringBuilder();
                String line = reader.readLine();
                while (line != null) {
                    b.append(line).append('\n');
                    line = reader.readLine();
                }
                System.out.println(b.toString());

            } catch (Exception e) {
                System.out.println("\n" +
                        "***********\n" +
                        "* Swarmic *\n" +
                        "***********\n");
            }
    }

}
