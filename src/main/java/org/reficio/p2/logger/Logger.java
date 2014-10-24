/**
 * Copyright (c) 2012 Reficio (TM) - Reestablish your software! All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.reficio.p2.logger;

import org.apache.maven.plugin.logging.Log;

/**
 * @author Tom Bujok (tom.bujok@gmail.com)<br>
 *         Reficio (TM) - Reestablish your software!<br>
 *         http://www.reficio.org
 * @since 1.0.0
 */
public final class Logger implements Log {

    static Log log;

    private Logger() {
    }

    public static Logger getLog() {
        if (log == null) {
            throw new RuntimeException("P2Log is not initialized");
        }
        return new Logger();
    }

    public static void initialize(Log log) {
        Logger.log = log;
    }

    @Override
    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    @Override
    public void debug(CharSequence charSequence) {
        log.debug(charSequence);
    }

    @Override
    public void debug(CharSequence charSequence, Throwable throwable) {
        log.debug(charSequence, throwable);
    }

    @Override
    public void debug(Throwable throwable) {
        log.debug(throwable);
    }

    @Override
    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    @Override
    public void info(CharSequence charSequence) {
        log.info(charSequence);
    }

    @Override
    public void info(CharSequence charSequence, Throwable throwable) {
        log.info(charSequence, throwable);
    }

    @Override
    public void info(Throwable throwable) {
        log.info(throwable);
    }

    @Override
    public boolean isWarnEnabled() {
        return log.isWarnEnabled();
    }

    @Override
    public void warn(CharSequence charSequence) {
        log.warn(charSequence);
    }

    @Override
    public void warn(CharSequence charSequence, Throwable throwable) {
        log.warn(charSequence, throwable);
    }

    @Override
    public void warn(Throwable throwable) {
        log.warn(throwable);
    }

    @Override
    public boolean isErrorEnabled() {
        return log.isErrorEnabled();
    }

    @Override
    public void error(CharSequence charSequence) {
        log.error(charSequence);
    }

    @Override
    public void error(CharSequence charSequence, Throwable throwable) {
        log.error(charSequence, throwable);
    }

    @Override
    public void error(Throwable throwable) {
        log.error(throwable);
    }
}
