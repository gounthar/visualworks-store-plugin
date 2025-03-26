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

import hudson.scm.EditType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChangedPundleTest {

    @Test
    void convertsAddedActionToEditTypeADD() {
        ChangedPundle pundle = new ChangedPundle("added", PundleType.PACKAGE, "pundleName", "42");

        assertEquals(EditType.ADD, pundle.getEditType());
    }

    @Test
    void convertsDeletedActionToEditTypeDELETE() {
        ChangedPundle pundle = new ChangedPundle("deleted", PundleType.PACKAGE, "pundleName");

        assertEquals(EditType.DELETE, pundle.getEditType());
    }

    @Test
    void convertsModifiedActionToEditTypeEDIT() {
        ChangedPundle pundle = new ChangedPundle("edited", PundleType.PACKAGE, "pundleName", "42");

        assertEquals(EditType.EDIT, pundle.getEditType());
    }

    @Test
    void constructsPundleDescriptor() {
        ChangedPundle pundle = new ChangedPundle("edited", PundleType.PACKAGE, "pundleName", "42");

        assertEquals("Package pundleName (42)", pundle.getDescriptor());
    }

    @Test
    void omitsVersionFromPundleDescriptorOnDeletedPundle() {
        ChangedPundle pundle = new ChangedPundle("deleted", PundleType.BUNDLE,
                "pundleName");

        assertEquals("Bundle pundleName", pundle.getDescriptor());
    }

    @Test
    void usesDescriptorAsPath() {
        ChangedPundle pundle = new ChangedPundle("edited", PundleType.PACKAGE, "pundleName", "42");

        assertEquals("Package pundleName (42)", pundle.getPath());
    }
}
