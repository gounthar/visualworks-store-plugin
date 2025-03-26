/*
 * The MIT License
 *
 * Copyright (c) 2013. Randy Coulman
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jenkinsci.plugins.visualworks_store;

import hudson.model.FreeStyleProject;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.junit.Rule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@WithJenkins
public class StoreSCMConfigurationTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    void testGlobalConfigurationRoundtrip(JenkinsRule r) throws Exception {
        StoreSCM.DescriptorImpl descriptor = j.jenkins.getDescriptorByType(StoreSCM.DescriptorImpl.class);

        descriptor.setStoreScripts(new StoreScript("7.7.1", "/path/to/script-7.7.1"),
                new StoreScript("7.9.1", "/path/to/script-7.9.1"));

        j.submit(j.createWebClient().goTo("configure").getFormByName("config"));

        StoreScript[] scripts = descriptor.getStoreScripts();
        assertEquals(2, scripts.length, "installation count");
        assertEquals("7.7.1", scripts[0].getName(), "first script name");
        assertEquals("/path/to/script-7.7.1", scripts[0].getPath(), "first script path");
        assertEquals("7.9.1", scripts[1].getName(), "second script name");
        assertEquals("/path/to/script-7.9.1", scripts[1].getPath(), "second script path");
    }

    @Test
    void testBasicConfigurationRoundtrip(JenkinsRule r) throws Exception {
        StoreSCM.DescriptorImpl descriptor = j.jenkins.getDescriptorByType(StoreSCM.DescriptorImpl.class);
        descriptor.setStoreScripts(new StoreScript("theScript", "path"));

        List<PundleSpec> pundleSpecs = onePundle();
        StoreSCM scm = new StoreSCM("theScript", "Repo", pundleSpecs, "\\d+", "Integrated", false, "");
        StoreSCM loaded = doRoundtripConfiguration(scm);

        assertEquals("theScript", loaded.getScriptName(), "script name");
        assertEquals("Repo", loaded.getRepositoryName(), "repositoryName");
        assertEquals(pundleSpecs, loaded.getPundles(), "pundles");
        assertEquals("\\d+", loaded.getVersionRegex(), "versionRegex");
        assertEquals("Integrated", loaded.getMinimumBlessingLevel(), "minimumBlessingLevel");
        assertFalse(loaded.isGenerateParcelBuilderInputFile(), "generateParcelBuilderInputFile");
        assertEquals("", loaded.getParcelBuilderInputFilename(), "parcelBuilderInputFilename");
    }

    @Test
    void testConfigurationRoundtripWithMultiplePundles(JenkinsRule r) throws Exception {
        StoreSCM.DescriptorImpl descriptor = j.jenkins.getDescriptorByType(StoreSCM.DescriptorImpl.class);
        descriptor.setStoreScripts(new StoreScript("theScript", "path"));

        List<PundleSpec> pundleSpecs = Arrays.asList(new PundleSpec(PundleType.PACKAGE, "SomePackage"),
                new PundleSpec(PundleType.BUNDLE, "SomeBundle"));
        StoreSCM scm = new StoreSCM("theScript", "Repo", pundleSpecs, "\\d+", "Integrated", false, "");
        StoreSCM loaded = doRoundtripConfiguration(scm);

        assertEquals(pundleSpecs, loaded.getPundles(), "pundles");
    }

    @Test
    void testConfigurationRoundtripWithParcelBuilderFile(JenkinsRule r) throws Exception {
        StoreSCM scm = new StoreSCM("script", "Repo", onePundle(), "\\d+", "Integrated", true, "theFilename");
        StoreSCM loaded = doRoundtripConfiguration(scm);

        assertTrue(loaded.isGenerateParcelBuilderInputFile(), "generateParcelBuilderInputFile");
        assertEquals("theFilename", loaded.getParcelBuilderInputFilename(), "parcelBuilderInputFilename");
    }

    @Test
    void testLookupStoreScript(JenkinsRule r) {
        StoreSCM.DescriptorImpl descriptor = j.jenkins.getDescriptorByType(StoreSCM.DescriptorImpl.class);
        final StoreScript script = new StoreScript("otherScript", "otherPath");
        descriptor.setStoreScripts(new StoreScript("theScript", "path"), script);

        StoreSCM scm = new StoreSCM("otherScript", "Repo", onePundle(), "\\d+", "Development", false, "");

        assertEquals(script, scm.getStoreScript());
    }

    private StoreSCM doRoundtripConfiguration(StoreSCM original) throws Exception {
        FreeStyleProject p = j.createFreeStyleProject();
        p.setScm(original);

        j.submit(j.createWebClient().getPage(p, "configure").getFormByName("config"));

        return (StoreSCM) p.getScm();
    }

    private List<PundleSpec> onePundle() {
        return Arrays.asList(new PundleSpec(PundleType.PACKAGE, "SomePackage"));
    }
}
