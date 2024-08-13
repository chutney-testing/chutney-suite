/*
 * SPDX-FileCopyrightText: 2017-2024 Enedis
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.chutneytesting.environment.domain.exception;

@SuppressWarnings("serial")
public class InvalidEnvironmentNameException extends RuntimeException {
    public InvalidEnvironmentNameException(String message) {
        super(message + ". NOTE: Environment are stored in files, names must be of the form [A-Z0-9_\\-]{3,20}");
    }
}
