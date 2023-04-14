/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sling.testing.mock.jcr;

import static org.junit.Assert.fail;

import java.util.function.Predicate;
import java.util.stream.Stream;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

/** 
 * Capture logs for testing 
 * 
 * Initially cloned from: https://github.com/apache/sling-org-apache-sling-graphql-core/blob/0b1c1dd72ed04324ea84d2227c3223ec65b0b21e/src/test/java/org/apache/sling/graphql/core/util/LogCapture.java
 */
public class LogCapture extends ListAppender<ILoggingEvent> implements AutoCloseable {
    private final boolean verboseFailure;
    private Logger logger;

    public LogCapture(String loggerName, boolean verboseFailure) {
        this.verboseFailure = verboseFailure;
        logger = (Logger) LoggerFactory.getLogger(loggerName);
        logger.setLevel(Level.ALL);
        setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        logger.addAppender(this);
        start();
    }

    public void setLoggerLevel(Level newLevel) {
        ((ch.qos.logback.classic.Logger)logger).setLevel(newLevel);
    }

    @Override
    public void close() throws Exception {
        if (logger != null) {
            logger.detachAppender(this);
        }
    }

    public boolean anyMatch(Predicate<ILoggingEvent> p) {
        return this.list.stream().anyMatch(p);
    }

    public void assertContains(Level atLevel, String ... substrings) {
        Stream.of(substrings).forEach(substring -> {
            if (!anyMatch(event -> event.getLevel() == atLevel && event.getFormattedMessage().contains(substring))) {
                if (verboseFailure) {
                    fail(String.format("No log message contains [%s] in log%n%s", substring, this.list.toString()));
                } else {
                    fail(String.format("No log message contains [%s]", substring));
                }
            }
        });
    }

    public void assertNotContains(Level atLevel, String ... substrings) {
        Stream.of(substrings).forEach(substring -> {
            if (anyMatch(event -> event.getLevel() == atLevel && event.getFormattedMessage().contains(substring))) {
                if (verboseFailure) {
                    fail(String.format("Unexpected log message contains [%s] in log%n%s", substring, this.list.toString()));
                } else {
                    fail(String.format("Unexpacted log message contains [%s]", substring));
                }
            }
        });
    }

}
