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

import hudson.model.TaskListener;
import hudson.util.StreamTaskListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class StoreRevisionStateTest {
    private ByteArrayOutputStream logOutput;
    private TaskListener listener;

    @BeforeEach
    void configureListener() {
        logOutput = new ByteArrayOutputStream();
        listener = new StreamTaskListener(logOutput);
    }

    @Test
    void preservesRepositoryName() {
        StoreRevisionState state = StoreRevisionState.parse("repo", "");
        assertEquals("repo", state.getRepositoryName(), "repositoryName");
    }

    @Test
    void parsesEmptyInput() {
        StoreRevisionState state = StoreRevisionState.parse("repo", "");
        assertTrue(state.isEmpty(), "should be empty");
    }

    @Test
    void parsesSinglePackageLine() {
        StoreRevisionState state = StoreRevisionState.parse("repo", "StorePackage\t\"MyPackage\"\tsome version");
        assertEquals(1, state.size(), "package count");
        assertEquals("some version", state.versionFor("MyPackage"), "version");
    }

    @Test
    void parsesMultiplePackageLines() {
        String input = "StorePackage\t\"PackageA\"\tversion A\nStorePackage\t\"PackageB\"\tversion B";

        StoreRevisionState state = StoreRevisionState.parse("repo", input);

        assertEquals(2, state.size(), "package count");
        assertEquals("version A", state.versionFor("PackageA"), "PackageA version");
        assertEquals("version B", state.versionFor("PackageB"), "PackageB version");
    }

    @Test
    void ignoresExtraWhitespaceWhenParsing() {
        String input = "           StorePackage       \t                \"PackageA\"      \t       version A      \n\n\n\n     StorePackage     \t  \"PackageB\"   \t     version B     ";

        StoreRevisionState state = StoreRevisionState.parse("repo", input);

        assertEquals(2, state.size(), "package count");
        assertEquals("version A", state.versionFor("PackageA"), "PackageA version");
        assertEquals("version B", state.versionFor("PackageB"), "PackageB version");
    }

    @Test
    void parsesPundleNamesWithEmbeddedSpaces() {
        StoreRevisionState state = StoreRevisionState.parse("repo", "StorePackage\t\"Package With Spaces\"\tsome version");
        assertTrue(state.containsPundle("Package With Spaces"), "package not found");
    }

    @Test
    void parsesPundleNamesWithNoSurroundingQuotes() {
        StoreRevisionState state = StoreRevisionState.parse("repo", "StorePackage\tMyPackage\tsome version");
        assertTrue(state.containsPundle("MyPackage"), "package not found");
    }

    // TODO: Tests to write:
    // syntax errors: not enough tokens; too many tokens;
    // missing quotes;

    @Test
    void returnsNullVersionIfPackageNotFound() {
        StoreRevisionState state = new StoreRevisionState("repo");

        assertNull(state.versionFor("Not There"), "should have return null version");
    }

    @Test
    void hasntChangedWithSameStateObject() {
        StoreRevisionState baseline = makeBaselineState();

        assertFalse(baseline.hasChangedFrom(baseline, listener), "shouldn't have changes");
        assertLogEmpty();
    }

    @Test
    void hasntChangedWithCopyOfSameState() {
        StoreRevisionState baseline = makeBaselineState();
        StoreRevisionState copy = makeBaselineState();

        assertFalse(copy.hasChangedFrom(baseline, listener), "shouldn't have changes");
        assertLogEmpty();
    }

    @Test
    void hasChangedWithDifferentVersion() {
        StoreRevisionState baseline = makeBaselineState();
        StoreRevisionState differentVersion = makeBaselineState();
        differentVersion.changeVersion("Package 2", "different");

        assertTrue(differentVersion.hasChangedFrom(baseline, listener), "should have changed");
        assertLogContains("Package 2 version changed from \"2\" to: \"different\"\n");
    }

    @Test
    void hasChangedWithExtraPackage() {
        StoreRevisionState baseline = makeBaselineState();
        StoreRevisionState extraPackage = makeBaselineState();
        extraPackage.addPundle("Extra", "2.0");

        assertTrue(extraPackage.hasChangedFrom(baseline, listener), "should have changed");
        assertLogContains("New package: Extra (2.0)\n");
    }

    @Test
    void hasChangedWithMissingPackage() {
        StoreRevisionState baseline = makeBaselineState();
        StoreRevisionState missingPackage = new StoreRevisionState("repo");

        assertTrue(missingPackage.hasChangedFrom(baseline, listener), "should have changed");
        assertLogContains("Package deleted: Package 1\n");
        assertLogContains("Package deleted: Package 2\n");
    }

    private StoreRevisionState makeBaselineState() {
        StoreRevisionState state = new StoreRevisionState("repo");
        state.addPundle("Package 1", "1");
        state.addPundle("Package 2", "2");
        return state;
    }

    private void assertLogEmpty() {
        assertTrue(logOutput.toString().isEmpty(), "log should be empty");
    }

    private void assertLogContains(final String snippet) {
        final String logString = logOutput.toString();
        assertTrue(logString.contains(snippet), logString + "\n\tdoes not contain\n" + snippet);
    }
}
