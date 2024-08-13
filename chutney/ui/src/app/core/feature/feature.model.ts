/*
 * SPDX-FileCopyrightText: 2017-2024 Enedis
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */

export interface Feature {
    name: FeatureName,
    active: boolean
}

export enum FeatureName {
    COMPONENT='COMPONENT'
}
