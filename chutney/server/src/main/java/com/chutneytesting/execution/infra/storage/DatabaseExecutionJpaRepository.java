/*
 * Copyright 2017-2023 Enedis
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

package com.chutneytesting.execution.infra.storage;

import com.chutneytesting.execution.infra.storage.jpa.ScenarioExecutionEntity;
import com.chutneytesting.server.core.domain.execution.report.ServerReportStatus;
import jakarta.persistence.Tuple;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DatabaseExecutionJpaRepository extends JpaRepository<ScenarioExecutionEntity, Long>, JpaSpecificationExecutor<ScenarioExecutionEntity> {

    List<ScenarioExecutionEntity> findByStatus(ServerReportStatus status);

    List<ScenarioExecutionEntity> findByScenarioIdOrderByIdDesc(String scenarioId);

    @Query("""
            SELECT
                CASE
                    WHEN EXISTS (SELECT 1 FROM SCENARIO_EXECUTIONS se_tmp WHERE se_tmp.scenarioId = se.scenarioId AND se_tmp.status = 'RUNNING')
                        THEN MAX(CASE WHEN se.status = 'RUNNING' THEN se.id END)
                    ELSE MAX(CASE WHEN se.status != 'NOT_EXECUTED' THEN se.id END)
                    END AS max_id,
                se.scenarioId
            FROM
                SCENARIO_EXECUTIONS se
            WHERE
                se.scenarioId IN :scenarioIds
            GROUP BY
                se.scenarioId
            """)
    List<Tuple> findLastExecutionsByScenarioId(@Param("scenarioIds") List<String> scenarioIds);

    List<ScenarioExecutionEntity> findAllByScenarioId(String scenarioId);

    @Query(value = """
                select se from SCENARIO s, SCENARIO_EXECUTIONS_REPORTS ser
                  inner join ser.scenarioExecution se
                where s.activated = true
                  and cast(s.id as string) = se.scenarioId
                  and ser.report like '%' || :query || '%'
                order by se.id desc
                limit 100
        """)
    List<ScenarioExecutionEntity> getExecutionReportMatchQuery(@Param("query") String query);
}
