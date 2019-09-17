/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.util;

import junit.framework.TestCase;

public class PathMatcherTestSuite extends TestCase {

    public void testPathMatcher() {

        // test exact matching
        assertTrue(PathMatcher.match("test", "test"));
        assertTrue(PathMatcher.match("/test", "/test"));
        assertFalse(PathMatcher.match("/test.jpg", "test.jpg"));
        assertFalse(PathMatcher.match("test", "/test"));
        assertFalse(PathMatcher.match("/test", "test"));

        // test matching with ?'s
        assertTrue(PathMatcher.match("t?st", "test"));
        assertTrue(PathMatcher.match("??st", "test"));
        assertTrue(PathMatcher.match("tes?", "test"));
        assertTrue(PathMatcher.match("te??", "test"));
        assertTrue(PathMatcher.match("?es?", "test"));
        assertFalse(PathMatcher.match("tes?", "tes"));
        assertFalse(PathMatcher.match("tes?", "testt"));
        assertFalse(PathMatcher.match("tes?", "tsst"));

        // test matchin with *'s
        assertTrue(PathMatcher.match("*", "test"));
        assertTrue(PathMatcher.match("test*", "test"));
        assertTrue(PathMatcher.match("test*", "testTest"));
        assertTrue(PathMatcher.match("*test*", "AnothertestTest"));
        assertTrue(PathMatcher.match("*test", "Anothertest"));
        assertTrue(PathMatcher.match("*.*", "test."));
        assertTrue(PathMatcher.match("*.*", "test.test"));
        assertTrue(PathMatcher.match("*.*", "test.test.test"));
        assertTrue(PathMatcher.match("test*aaa", "testblaaaa"));
        assertFalse(PathMatcher.match("test*", "tst"));
        assertFalse(PathMatcher.match("test*", "tsttest"));
        assertFalse(PathMatcher.match("*test*", "tsttst"));
        assertFalse(PathMatcher.match("*test", "tsttst"));
        assertFalse(PathMatcher.match("*.*", "tsttst"));
        assertFalse(PathMatcher.match("test*aaa", "test"));
        assertFalse(PathMatcher.match("test*aaa", "testblaaab"));


        // test matching with ?'s and /'s
        assertTrue(PathMatcher.match("/?", "/a"));
        assertTrue(PathMatcher.match("/?/a", "/a/a"));
        assertTrue(PathMatcher.match("/a/?", "/a/b"));
        assertTrue(PathMatcher.match("/??/a", "/aa/a"));
        assertTrue(PathMatcher.match("/a/??", "/a/bb"));
        assertTrue(PathMatcher.match("/?", "/a"));


        // test matching with **'s
        assertTrue(PathMatcher.match("/**", "/testing/testing"));
        assertTrue(PathMatcher.match("/*/**", "/testing/testing"));
        assertTrue(PathMatcher.match("/**/*", "/testing/testing"));
        assertTrue(PathMatcher.match("/bla/**/bla", "/bla/testing/testing/bla"));
        assertTrue(PathMatcher.match("/bla/**/bla", "/bla/testing/testing/bla/bla"));
        assertTrue(PathMatcher.match("/**/test", "/bla/bla/test"));
        assertTrue(PathMatcher.match("/bla/**/**/bla", "/bla/bla/bla/bla/bla/bla"));
        assertTrue(PathMatcher.match("/bla*bla/test", "/blaXXXbla/test"));
        assertTrue(PathMatcher.match("/*bla/test", "/XXXbla/test"));
        assertFalse(PathMatcher.match("/bla*bla/test", "/blaXXXbl/test"));
        assertFalse(PathMatcher.match("/*bla/test", "XXXblab/test"));
        assertFalse(PathMatcher.match("/*bla/test", "XXXbl/test"));

        assertFalse(PathMatcher.match("/????", "/bala/bla"));
        assertFalse(PathMatcher.match("/**/*bla", "/bla/bla/bla/bbb"));

        assertTrue(PathMatcher.match("/*bla*/**/bla/**", "/XXXblaXXXX/testing/testing/bla/testing/testing/"));
        assertTrue(PathMatcher.match("/*bla*/**/bla/*", "/XXXblaXXXX/testing/testing/bla/testing"));
        assertTrue(PathMatcher.match("/*bla*/**/bla/**", "/XXXblaXXXX/testing/testing/bla/testing/testing"));
        assertTrue(PathMatcher.match("/*bla*/**/bla/**", "/XXXblaXXXX/testing/testing/bla/testing/testing.jpg"));

        assertTrue(PathMatcher.match("*bla*/**/bla/**", "XXXblaXXXX/testing/testing/bla/testing/testing/"));
        assertTrue(PathMatcher.match("*bla*/**/bla/*", "XXXblaXXXX/testing/testing/bla/testing"));
        assertTrue(PathMatcher.match("*bla*/**/bla/**", "XXXblaXXXX/testing/testing/bla/testing/testing"));
        assertFalse(PathMatcher.match("*bla*/**/bla/*", "XXXblaXXXX/testing/testing/bla/testing/testing"));

        assertFalse(PathMatcher.match("/x/x/x/", "/x/x/**/bla"));


        assertTrue(PathMatcher.match("", ""));
        assertTrue(PathMatcher.match("", ""));
        assertTrue(PathMatcher.match("", ""));
        assertTrue(PathMatcher.match("", ""));
        assertTrue(PathMatcher.match("", ""));
        assertTrue(PathMatcher.match("", ""));

    }
}

