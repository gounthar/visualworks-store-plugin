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

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StoreChangeLogParserTest {
    @Test
    void parsesEmptyChangelogFile() throws SAXException, IOException, URISyntaxException {
        StoreChangeLogSet changes = parse("changelog_empty.xml");
        assertTrue(changes.isEmptySet(), "changeset should be empty");
    }

    private StoreChangeLogSet parse(String filename) throws IOException, SAXException, URISyntaxException {
        URL url = StoreChangeLogParserTest.class.getResource(filename);
        File changelogFile = new File(url.toURI().getSchemeSpecificPart());

        return new StoreChangeLogParser().parse(null, changelogFile);
    }

    @Test
    void parsesSingleDeletion() throws IOException, SAXException, URISyntaxException {
        StoreChangeLogSet changes = parse("changelog_singleDeletion.xml");

        List<StoreChangeLogEntry> entries = changes.getEntries();

        assertEquals(1, entries.size(), "entry count");

        StoreChangeLogEntry entry = entries.get(0);
        assertEquals("Pundles no longer used", entry.getMsg(), "commit message");
        assertEquals("", entry.getCommitter(), "committer should be empty");
        assertEquals(0, entry.getTimestamp(), "entry timestamp should be zero");

        ChangedPundle pundle = new ChangedPundle("deleted",
                PundleType.PACKAGE, "MyPundle");
        assertTrue(entry.getAffectedPaths().contains(pundle.getDescriptor()));
    }

    @Test
    void parsesSingleAddition() throws IOException, SAXException, URISyntaxException {
        StoreChangeLogSet changes = parse("changelog_singleAddition.xml");

        List<StoreChangeLogEntry> entries = changes.getEntries();

        assertEquals(1, entries.size(), "entry count");

        StoreChangeLogEntry entry = entries.get(0);
        Calendar expected = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        expected.set(2012, Calendar.JULY, 2, 15, 40, 19);
        expected.set(Calendar.MILLISECOND, 123);

        assertEquals("Commit comment.", entry.getMsg(), "commit message");
        assertEquals("committer", entry.getCommitter(), "committer");
        assertEquals(expected.getTime().getTime(), entry.getTimestamp(), "timestamp");

        ChangedPundle pundle = new ChangedPundle("added",
                PundleType.PACKAGE, "MyPundle", "42");
        assertTrue(entry.getAffectedPaths().contains(pundle.getDescriptor()));
    }

    @Test
    void parsesMultipleBlessingsAsMultipleEntries() throws IOException, SAXException, URISyntaxException {
        StoreChangeLogSet changes = parse("changelog_multipleBlessings.xml");
        List<StoreChangeLogEntry> entries = changes.getEntries();

        assertEquals(3, entries.size(), "entry count");

        assertEquals("User 1", entries.get(0).getCommitter(), "first author");
        assertEquals("User 2", entries.get(1).getCommitter(), "second author");
        assertEquals("User 1", entries.get(2).getCommitter(), "third author");

        assertEquals("First comment.", entries.get(0).getMsg(), "first comment");
        assertEquals("Comment the second.", entries.get(1).getMsg(), "second comment");
        assertEquals("Other comment.", entries.get(2).getMsg(), "third comment");

        ChangedPundle pundle = new ChangedPundle("edited",
                PundleType.PACKAGE, "MyPundle", "42");
        String expectedPath = pundle.getDescriptor();
        assertTrue(entries.get(0).getAffectedPaths().contains(expectedPath), "first paths");
        assertTrue(entries.get(1).getAffectedPaths().contains(expectedPath), "second paths");
        assertTrue(entries.get(2).getAffectedPaths().contains(expectedPath), "third paths");
    }

    @Test
    void parsesMultiplePundlesWithDifferentCommentsAsMultipleEntries()
            throws IOException, SAXException, URISyntaxException {
        StoreChangeLogSet changes = parse("changelog_multiplePundles.xml");
        List<StoreChangeLogEntry> entries = changes.getEntries();

        assertEquals(3, entries.size(), "entry count");

        assertEquals("First comment.", entries.get(0).getMsg(), "first comment");
        assertEquals("Pundles no longer used", entries.get(1).getMsg(), "second comment");
        assertEquals("Other comment.", entries.get(2).getMsg(), "third comment");

        ChangedPundle addedPundle = new ChangedPundle("added",
                PundleType.PACKAGE, "AddedPundle", "42");
        ChangedPundle deletedPundle = new ChangedPundle("deleted",
                PundleType.PACKAGE, "DeletedPundle");
        ChangedPundle modifiedPundle = new ChangedPundle("edited",
                PundleType.BUNDLE, "ModifiedPundle", "58");

        assertTrue(entries.get(0).getAffectedPaths().contains
                (addedPundle.getDescriptor()), "first paths");
        assertTrue(entries.get(1).getAffectedPaths().contains
                (deletedPundle.getDescriptor()), "second paths");
        assertTrue(entries.get(2).getAffectedPaths().contains
                (modifiedPundle.getDescriptor()), "third paths");
    }

    @Test
    void mergesEntriesWithIdenticalComments() throws IOException, SAXException, URISyntaxException {
        StoreChangeLogSet changes = parse("changelog_identicalComments.xml");
        List<StoreChangeLogEntry> entries = changes.getEntries();

        Calendar expectedTimestamp = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        expectedTimestamp.set(2012, Calendar.JULY, 2, 15, 40, 19);
        expectedTimestamp.set(Calendar.MILLISECOND, 123);

        assertEquals(3, entries.size(), "entry count");

        assertEquals("First comment.", entries.get(0).getMsg(), "first comment");
        assertEquals("Pundles no longer used", entries.get(1).getMsg(), "second comment");
        assertEquals("Other comment.", entries.get(2).getMsg(), "third comment");

        assertEquals(0, entries.get(1).getTimestamp(), "timestamp of deleted entry");
        assertEquals(expectedTimestamp.getTime().getTime(), entries.get(2).getTimestamp(), "timestamp should be latest of merged entries");

        ChangedPundle addedPundle = new ChangedPundle("added",
                PundleType.PACKAGE, "AddedPundle", "42");
        ChangedPundle deletedPundle1 = new ChangedPundle("deleted",
                PundleType.BUNDLE, "DeletedPundle1");
        ChangedPundle deletedPundle2 = new ChangedPundle("deleted",
                PundleType.PACKAGE, "DeletedPundle2");
        ChangedPundle modifiedPundle1 = new ChangedPundle("edited",
                PundleType.BUNDLE, "ModifiedPundle1", "58");
        ChangedPundle modifiedPundle2 = new ChangedPundle("edited",
                PundleType.PACKAGE, "ModifiedPundle2", "123");

        assertTrue(entries.get(0).getAffectedPaths().contains
                (addedPundle.getDescriptor()), "first paths");

        List<String> expectedPaths1 = Arrays.asList(deletedPundle1.getDescriptor(),
                deletedPundle2.getDescriptor());
        assertTrue(entries.get(1).getAffectedPaths().containsAll(expectedPaths1), "second paths");

        List<String> expectedPaths2 =
                Arrays.asList(modifiedPundle1.getDescriptor(),
                        modifiedPundle2.getDescriptor());
        assertTrue(entries.get(2).getAffectedPaths().containsAll(expectedPaths2), "third paths");
    }
}
