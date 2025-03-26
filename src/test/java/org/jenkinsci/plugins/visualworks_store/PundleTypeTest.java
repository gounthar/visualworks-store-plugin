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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PundleTypeTest {
    @Test
    void packageDescriptionIsCapitalized() {
        assertEquals("Package", PundleType.PACKAGE.getDescription());
    }

    @Test
    void packageNameIsLowercase() {
        assertEquals("package", PundleType.PACKAGE.getName());
    }

    @Test
    void bundleDescriptionIsCapitalized() {
        assertEquals("Bundle", PundleType.BUNDLE.getDescription());
    }

    @Test
    void bundleNameIsLowercase() {
        assertEquals("bundle", PundleType.BUNDLE.getName());
    }

    @Test
    void findsPackageTypeByName() {
        assertEquals(PundleType.PACKAGE, PundleType.named("package"));
    }

    @Test
    void findsBundleTypeByName() {
        assertEquals(PundleType.BUNDLE, PundleType.named("bundle"));
    }

    @Test
    void returnsNullIfNameNotFound() {
        assertNull(PundleType.named("unknown"));
    }
}
