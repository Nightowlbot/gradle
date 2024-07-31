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

package org.gradle.buildinit.templates.internal

import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import org.gradle.plugin.management.internal.template.TemplatePluginHandler
import org.gradle.test.fixtures.plugin.PluginBuilder

class BuildInitPluginTemplatesIntegrationTest extends AbstractIntegrationSpec {
    def "can specify 3rd party plugin using argument to init"() {
        when:
        initSucceedsWithTemplatePlugin("org.barfuin.gradle.taskinfo:2.2.0")

        then:
        assertResolvedPlugin("org.barfuin.gradle.taskinfo", "2.2.0")
    }

    def "can specify plugin using argument to init with root build file present"() {
        groovyFile("new-project/build.gradle", """
            plugins {
                id 'java-library'
            }
        """)

        when:
        initSucceedsWithTemplatePlugin("org.barfuin.gradle.taskinfo:2.2.0")

        then:
        assertResolvedPlugin("org.barfuin.gradle.taskinfo", "2.2.0")
    }

    def "can specify plugin using argument to init with root KTS build file present"() {
        kotlinFile("new-project/build.gradle.kts", """
            plugins {
                `java-library`
            }
        """)

        when:
        initSucceedsWithTemplatePlugin("org.barfuin.gradle.taskinfo:2.2.0")

        then:
        assertResolvedPlugin("org.barfuin.gradle.taskinfo", "2.2.0")
    }

    def "can specify plugin using argument to init with settings file present"() {
        groovyFile("new-project/settings.gradle","""
            rootProject.name = "rootProject"
        """)

        when:
        initSucceedsWithTemplatePlugin("org.barfuin.gradle.taskinfo:2.2.0")

        then:
        assertResolvedPlugin("org.barfuin.gradle.taskinfo", "2.2.0")
    }

    def "can specify plugin using argument to init with settings KTS file present"() {
        kotlinFile("new-project/settings.gradle.kts","""
            rootProject.name = "rootProject"
        """)

        when:
        initSucceedsWithTemplatePlugin("org.barfuin.gradle.taskinfo:2.2.0")

        then:
        assertResolvedPlugin("org.barfuin.gradle.taskinfo", "2.2.0")
    }

    def "can specify plugin using argument to init with root build and settings files present"() {
        groovyFile("new-project/settings.gradle","""
            rootProject.name = "rootProject"
        """)

        groovyFile("new-project/build.gradle", """
            plugins {
                id 'java-library'
            }
        """)

        when:
        initSucceedsWithTemplatePlugin("org.barfuin.gradle.taskinfo:2.2.0")

        then:
        assertResolvedPlugin("org.barfuin.gradle.taskinfo", "2.2.0")
    }

    def "can specify plugin using argument to init with root build and settings KTS files present"() {
        kotlinFile("new-project/settings.gradle.kts","""
            rootProject.name = "rootProject"
        """)

        kotlinFile("new-project/build.gradle.kts", """
            plugins {
                `java-library`
            }
        """)

        when:
        initSucceedsWithTemplatePlugin("org.barfuin.gradle.taskinfo:2.2.0")

        then:
        assertResolvedPlugin("org.barfuin.gradle.taskinfo", "2.2.0")
    }

    def "can specify plugin using argument to init with root build and settings files present in multiproject build"() {
        groovyFile("new-project/settings.gradle", """
            rootProject.name = "rootProject"
            include("subproject")
        """)

        groovyFile("new-project/build.gradle", """
            plugins {
                id 'java-library'
            }
        """)

        groovyFile("new-project/subproject/build.gradle",
            """
                plugins {
                    id 'java-library'
                }
            """
        )

        when:
        initSucceedsWithTemplatePlugin("org.barfuin.gradle.taskinfo:2.2.0")

        then:
        assertResolvedPlugin("org.barfuin.gradle.taskinfo", "2.2.0")
        // TODO: should appear exactly once, but no way to automatically verify this currently.  Looking at the output, it is true currently
    }

    def "can specify plugin using argument to init with root build and settings KTS files present in multiproject build"() {
        kotlinFile("new-project/settings.gradle.kts", """
            rootProject.name = "rootProject"
            include("subproject")
        """)

        kotlinFile("new-project/build.gradle.kts", """
            plugins {
                `java-library`
            }
        """)

        kotlinFile("new-project/subproject/build.gradle.kts",
            """
                plugins {
                    `java-library`
                }
            """
        )

        when:
        initSucceedsWithTemplatePlugin("org.barfuin.gradle.taskinfo:2.2.0")

        then:
        assertResolvedPlugin("org.barfuin.gradle.taskinfo", "2.2.0")
        // TODO: should appear exactly once, but no way to automatically verify this currently.  Looking at the output, it is true currently
    }

    def "can specify custom plugin using argument to init"() {
        given:
        publishTestPlugin()

        when:
        initSucceedsWithTemplatePlugin("org.example.myplugin:1.0")

        then:
        assertResolvedPlugin("org.example.myplugin", "1.0")
        outputDoesNotContain("MyPlugin applied.")
        assertLoadedTemplate("Custom Project Type")
    }

    def "can specify multiple plugins using argument to init"() {
        given:
        publishTestPlugin()

        when:
        initSucceedsWithTemplatePlugin("org.example.myplugin:1.0,org.barfuin.gradle.taskinfo:2.2.0")

        then:
        assertResolvedPlugin("org.example.myplugin", "1.0")
        assertResolvedPlugin("org.barfuin.gradle.taskinfo", "2.2.0")
        outputDoesNotContain("MyPlugin applied.")
        assertLoadedTemplate("Custom Project Type")
    }

    def setup() {
        setupRepositoriesViaInit()
    }

    private void setupRepositoriesViaInit() {
        groovyFile("init.gradle", """
            settingsEvaluated { settings ->
                settings.pluginManagement {
                    repositories {
                        maven {
                            url '${mavenRepo.uri}'
                        }
                        gradlePluginPortal()
                    }
                }
            }
        """)
    }

    private void initSucceedsWithTemplatePlugin(String pluginsProp = null) {
        def newProjectDir = file("new-project").with { createDir() }
        executer.inDirectory(newProjectDir)

        def args = ["init"]
        if (pluginsProp) {
            args << "-D${TemplatePluginHandler.TEMPLATE_PLUGINS_PROP}=$pluginsProp".toString()
        }
        args << "--overwrite"
        args << "--init-script" << "../init.gradle"

        println "Executing: '${args.join(" ")}')"
        println "Working Dir: '$newProjectDir'"

        succeeds args
    }

    private publishTestPlugin() {
        def pluginBuilder = new PluginBuilder(testDirectory.file("plugin"))

        pluginBuilder.addPluginWithCustomCode("""
                project.getLogger().lifecycle("MyPlugin applied.");
        """, "org.example.myplugin")

        pluginBuilder.file("src/main/resources/META-INF/services/org.gradle.buildinit.templates.InitProjectSupplier") << "org.gradle.test.MySupplier\n"
        pluginBuilder.java("org/gradle/test/MySupplier.java") << """
            package org.gradle.test;

            import java.util.Collections;
            import java.util.List;

            import org.gradle.buildinit.templates.InitProjectParameter;
            import org.gradle.buildinit.templates.InitProjectSpec;
            import org.gradle.buildinit.templates.InitProjectSupplier;

            public class MySupplier implements InitProjectSupplier {
                @Override
                public List<InitProjectSpec> getProjectDefinitions() {
                    return Collections.singletonList(new InitProjectSpec() {
                        @Override
                        public String getDisplayName() {
                            return "Custom Project Type";
                        }

                        @Override
                        public List<InitProjectParameter> getParameters() {
                            return Collections.emptyList();
                        }
                    });
                }
            }
        """

        executer.requireOwnGradleUserHomeDir("Adding new API that plugin needs") // TODO: Remove this when API is solid enough that it isn't changing every test run (it slows down test running)
        def results = pluginBuilder.publishAs("org.example.myplugin:plugin:1.0", mavenRepo, executer)

        println()
        println "Published: '${results.getPluginModule().with { m -> m.getGroup() + ':' + m.getModule() + ':' + m.getVersion() }}'"
        println "To: '${mavenRepo.uri}'"
    }

    private void assertResolvedPlugin(String id, String version) {
        outputContains("Resolved plugin [id: '$id', version: '$version', apply: false]")
    }

    private void assertLoadedTemplate(String templateName) {
        outputContains("Loaded template: '" + templateName + "'")
    }
}
