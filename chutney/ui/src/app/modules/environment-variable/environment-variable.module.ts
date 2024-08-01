/*
 * Copyright 2017-2024 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EnvironmentsVariablesComponent } from '@modules/environment-variable/list/environments-variables.component';
import { EnvironmentVariableRoutes } from '@modules/environment-variable/environment-variable.routes';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MoleculesModule } from '../../molecules/molecules.module';
import { NgbNavModule, NgbTooltipModule, NgbTypeaheadModule } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule } from '@ngx-translate/core';



@NgModule({
  declarations: [
    EnvironmentsVariablesComponent
  ],
    imports: [
        CommonModule,
        EnvironmentVariableRoutes,
        FormsModule,
        MoleculesModule,
        NgbNavModule,
        NgbTooltipModule,
        NgbTypeaheadModule,
        TranslateModule,
        ReactiveFormsModule
    ]
})
export class EnvironmentVariableModule { }
