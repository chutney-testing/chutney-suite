/**
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

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '@env/environment';
import { Observable } from 'rxjs';
import { isNullOrBlankString } from '@shared/tools';

@Injectable({
  providedIn: 'root'
})
export class StepService {

  private stepUrl = '/api/steps/v1';

  constructor(private http: HttpClient) {
  }

  findById(stepId: string): Observable<Object> {
    return this.http.get(environment.backend + this.stepUrl + '/' + encodeURIComponent(stepId));
  }
}
