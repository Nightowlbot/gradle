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

package org.gradle.buildinit.plugins

import org.gradle.buildinit.plugins.fixtures.ScriptDslFixture
import org.gradle.buildinit.plugins.internal.modifiers.BuildInitDsl
import org.gradle.plugin.management.internal.template.TemplatePluginHandler
import org.gradle.test.fixtures.ConcurrentTestUtil
import org.gradle.util.internal.TextUtil
import spock.lang.Ignore

@Ignore
class BuildInitPluginTemplatesInteractiveIntegrationTest extends AbstractInteractiveInitIntegrationSpec implements TestsInitTemplatePlugin {
    def "prompts to choose template properly"() {
        given:
        def pluginProjectDir = file("plugin").with { createDir() }
        executer.usingProjectDirectory(pluginProjectDir)
        publishTestPlugin()

        when:
        def newProjectDir = file("new-project").with { createDir() }
        executer.usingProjectDirectory(newProjectDir)

        def handle = startInteractiveExecutorWithTasks(
            "init",
            "-D${ TemplatePluginHandler.TEMPLATE_PLUGINS_PROP}=org.example.myplugin:1.0",
            "--overwrite",
            "--info",
            "--init-script", "../init.gradle"
        )

        // Select 'basic'
        ConcurrentTestUtil.poll(60) {
            assert handle.standardOutput.contains(buildTypePrompt)
            assert handle.standardOutput.contains("1: Application")
            assert handle.standardOutput.contains("2: Library")
            assert handle.standardOutput.contains("3: Gradle plugin")
            assert handle.standardOutput.contains(basicType)
            assert !handle.standardOutput.contains("pom")
        }
        handle.stdinPipe.write((basicTypeOption + TextUtil.platformLineSeparator).bytes)

        // Select default project name
        ConcurrentTestUtil.poll(60) {
            assert handle.standardOutput.contains(projectNamePrompt)
        }
        handle.stdinPipe.write(TextUtil.platformLineSeparator.bytes)

        // after generating the project, we suggest the user reads some documentation
        def msg = documentationRegistry.getSampleForMessage()
        ConcurrentTestUtil.poll(60) {
            assert handle.standardOutput.contains(msg)
        }

        closeInteractiveExecutor(handle)

        then:
        ScriptDslFixture.of(BuildInitDsl.KOTLIN, targetDir, null).assertGradleFilesGenerated()
    }
}
