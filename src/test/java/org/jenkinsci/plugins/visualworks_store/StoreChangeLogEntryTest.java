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

import hudson.scm.ChangeLogSet;
import hudson.scm.EditType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StoreChangeLogEntryTest {
    private StoreChangeLogEntry entry;
    private String timestampString = "07/02/2012 15:40:19.123";
    private String laterTimestampString = "07/02/2012 15:45:21.456";
    private ChangedPundle addedPundle;
    private ChangedPundle editedPundle;
    private ChangedPundle deletedPundle;

    @BeforeEach
    void setUp() {
        entry = new StoreChangeLogEntry("committer", timestampString, "blessing comment");
        addedPundle = new ChangedPundle("added", PundleType.PACKAGE, "AddedPundle", "1");
        editedPundle = new ChangedPundle("edited", PundleType.BUNDLE,
                "EditedPundle", "42");
        deletedPundle = new ChangedPundle("deleted", PundleType.PACKAGE, "DeletedPundle");
    }

    @Test
    void remembersCommitter() {
        assertEquals("committer", entry.getCommitter());
    }

    @Test
    void parsesTimestampStrings() throws ParseException {
        Calendar expected = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        expected.set(2012, Calendar.JULY, 2, 15, 40, 19);
        expected.set(Calendar.MILLISECOND, 123);

        assertEquals(expected.getTime().getTime(), entry.getTimestamp());
    }

    @Test
    void updatesTimestampToNewerOne() {
        long originalTimestamp = entry.getTimestamp();

        entry.updateTimestamp(laterTimestampString);

        assertTrue(entry.getTimestamp() > originalTimestamp, "timestamp should have been updated");
    }

    @Test
    void doesntUpdateTimestampWithOlderOne() {
        entry.updateTimestamp(laterTimestampString);

        long originalTimestamp = entry.getTimestamp();

        entry.updateTimestamp(timestampString);

        assertEquals(originalTimestamp, entry.getTimestamp(), "timestamp should not have been updated");
    }

    @Test
    void returnsBlessingCommentAsCommitMessage() {
        assertEquals("blessing comment", entry.getMsg());
    }

    @Test
    void splitsMultilineBlessingComments() {
        entry = new StoreChangeLogEntry("committer", timestampString, "Comment title.\nDetails 1.\nDetails 2.");

        assertEquals("Comment title.", entry.getMsg(), "message");
        assertEquals("Comment title.\nDetails 1.\nDetails 2.", entry.getFullComment(), "full comment");
    }

    @Test
    void returnsEmptyAffectedPathsIfNoPundles() {
        assertTrue(entry.getAffectedPaths().isEmpty(), "shouldn't have any affected paths");
    }

    @Test
    void returnsPundleDescriptorsAsAffectedPaths() {
        entry.addPundle(addedPundle);
        entry.addPundle(editedPundle);
        entry.addPundle(deletedPundle);

        List<String> expected = Arrays.asList(addedPundle.getDescriptor(),
                editedPundle.getDescriptor(), deletedPundle.getDescriptor());
        assertTrue(entry.getAffectedPaths().containsAll(expected));
    }

    @Test
    void returnsAffectedFiles() {
        entry.addPundle(addedPundle);
        entry.addPundle(editedPundle);

        final Collection<? extends ChangeLogSet.AffectedFile> affectedFiles = entry.getAffectedFiles();
        ChangeLogSet.AffectedFile[] files = new ChangeLogSet.AffectedFile[affectedFiles.size()];
        affectedFiles.toArray(files);

        assertEquals(2, files.length);
        ChangeLogSet.AffectedFile file1 = files[0];
        assertEquals(addedPundle.getDescriptor(), file1.getPath());
        assertEquals(EditType.ADD, file1.getEditType());

        ChangeLogSet.AffectedFile file2 = files[1];
        assertEquals(editedPundle.getDescriptor(), file2.getPath());
        assertEquals(EditType.EDIT, file2.getEditType());
    }
}
