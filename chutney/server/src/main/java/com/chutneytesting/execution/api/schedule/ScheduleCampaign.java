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

package com.chutneytesting.execution.api.schedule;

import static com.chutneytesting.ServerConfigurationValues.SCHEDULED_CAMPAIGNS_FIXED_RATE_SPRING_VALUE;

import com.chutneytesting.execution.domain.schedule.CampaignScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduleCampaign {

    private final CampaignScheduler campaignScheduler;

    public ScheduleCampaign(CampaignScheduler campaignScheduler) {
        this.campaignScheduler = campaignScheduler;
    }

    @Scheduled(fixedRateString = SCHEDULED_CAMPAIGNS_FIXED_RATE_SPRING_VALUE)
    public void executeScheduledCampaign() {
        campaignScheduler.executeScheduledCampaigns();
    }
}
