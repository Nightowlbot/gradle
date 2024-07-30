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
        initSucceeds("org.barfuin.gradle.taskinfo:2.2.0")

        then:
        outputContains("Resolved plugin [id: 'org.barfuin.gradle.taskinfo', version: '2.2.0', apply: false]")
    }

    def "can specify plugin using argument to init with root build file present"() {
        groovyFile("new-project/build.gradle", """
            plugins {
                id 'java-library'
            }
        """)

        when:
        initSucceeds("org.barfuin.gradle.taskinfo:2.2.0")

        then:
        outputContains("Resolved plugin [id: 'org.barfuin.gradle.taskinfo', version: '2.2.0', apply: false]")
    }

    def "can specify plugin using argument to init with root KTS build file present"() {
        kotlinFile("new-project/build.gradle.kts", """
            plugins {
                `java-library`
            }
        """)

        when:
        initSucceeds("org.barfuin.gradle.taskinfo:2.2.0")

        then:
        outputContains("Resolved plugin [id: 'org.barfuin.gradle.taskinfo', version: '2.2.0', apply: false]")
    }

    def "can specify plugin using argument to init with settings file present"() {
        groovyFile("new-project/settings.gradle","""
            rootProject.name = "rootProject"
        """)

        when:
        initSucceeds("org.barfuin.gradle.taskinfo:2.2.0")

        then:
        outputContains("Resolved plugin [id: 'org.barfuin.gradle.taskinfo', version: '2.2.0', apply: false]")
    }

    def "can specify plugin using argument to init with settings KTS file present"() {
        kotlinFile("new-project/settings.gradle.kts","""
            rootProject.name = "rootProject"
        """)

        when:
        initSucceeds("org.barfuin.gradle.taskinfo:2.2.0")

        then:
        outputContains("Resolved plugin [id: 'org.barfuin.gradle.taskinfo', version: '2.2.0', apply: false]")
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
        initSucceeds("org.barfuin.gradle.taskinfo:2.2.0")

        then:
        outputContains("Resolved plugin [id: 'org.barfuin.gradle.taskinfo', version: '2.2.0', apply: false]")
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
        initSucceeds("org.barfuin.gradle.taskinfo:2.2.0")

        then:
        outputContains("Resolved plugin [id: 'org.barfuin.gradle.taskinfo', version: '2.2.0', apply: false]")
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
        initSucceeds("org.barfuin.gradle.taskinfo:2.2.0")

        then:
        outputContains("Resolved plugin [id: 'org.barfuin.gradle.taskinfo', version: '2.2.0', apply: false]")
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
        initSucceeds("org.barfuin.gradle.taskinfo:2.2.0")

        then:
        outputContains("Resolved plugin [id: 'org.barfuin.gradle.taskinfo', version: '2.2.0', apply: false]")
        // TODO: should appear exactly once, but no way to automatically verify this currently.  Looking at the output, it is true currently
    }

    def "can specify custom plugin using argument to init"() {
        given:
        publishTestPlugin()

        when:
        initSucceeds("org.example.myplugin:1.0")

        then:
        outputContains("Resolved plugin [id: 'org.example.myplugin', version: '1.0', apply: false]")
        outputDoesNotContain("MyPlugin applied.")
    }

    def "can specify multiple plugins using argument to init"() {
        given:
        publishTestPlugin()

        when:
        initSucceeds("org.example.myplugin:1.0,org.barfuin.gradle.taskinfo:2.2.0")

        then:
        outputContains("Resolved plugin [id: 'org.example.myplugin', version: '1.0', apply: false]")
        outputContains("Resolved plugin [id: 'org.barfuin.gradle.taskinfo', version: '2.2.0', apply: false]")
        outputDoesNotContain("MyPlugin applied.")
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

    private void initSucceeds(String pluginsProp = null) {
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

        pluginBuilder.publishAs("org.example.myplugin:plugin:1.0", mavenRepo, executer)
    }
}
