/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.buildinit.templates.internal;

import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.buildinit.templates.InitProjectSpec;
import org.gradle.buildinit.templates.InitProjectSupplier;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class TemplateLoader {
    private final ClassLoader classLoader;

    public TemplateLoader(ProjectInternal project) {
        this.classLoader = project.getClassLoaderScope().getLocalClassLoader();
    }

    public List<InitProjectSpec> loadTemplates() {
        List<InitProjectSpec> templates = new ArrayList<>();
        // Load from the current thread to get the classes loaded by plugins, not the thread where InitProjectSupplier was loaded
        for (InitProjectSupplier supplier : ServiceLoader.load(InitProjectSupplier.class, classLoader)) {
            templates.addAll(supplier.getProjectDefinitions());
        }
        return templates;
    }
}
