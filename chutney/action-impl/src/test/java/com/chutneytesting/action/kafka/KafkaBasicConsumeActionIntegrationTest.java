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

package com.chutneytesting.action.kafka;

import static com.chutneytesting.action.kafka.KafkaBasicConsumeAction.OUTPUT_BODY;
import static com.chutneytesting.action.kafka.KafkaBasicConsumeAction.OUTPUT_BODY_HEADERS_KEY;
import static com.chutneytesting.action.kafka.KafkaBasicConsumeAction.OUTPUT_BODY_PAYLOAD_KEY;
import static com.chutneytesting.action.kafka.KafkaBasicConsumeAction.OUTPUT_HEADERS;
import static com.chutneytesting.action.kafka.KafkaBasicConsumeAction.OUTPUT_PAYLOADS;
import static com.chutneytesting.action.spi.ActionExecutionResult.Status.Failure;
import static com.chutneytesting.action.spi.ActionExecutionResult.Status.Success;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.MimeTypeUtils.TEXT_PLAIN_VALUE;

import com.chutneytesting.action.TestLogger;
import com.chutneytesting.action.TestTarget;
import com.chutneytesting.action.http.HttpsServerStartActionTest;
import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Target;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;

public abstract class KafkaBasicConsumeActionIntegrationTest {

    private final String GROUP = "mygroup";
    private String uniqueTopic;

    protected static Producer<Integer, String> producer;
    private final TestTarget.TestTargetBuilder targetBuilder;

    private TestLogger logger;

    public KafkaBasicConsumeActionIntegrationTest() {
        String brokerPath = initBroker();
        producer = createProducer(brokerPath);
        targetBuilder = TestTarget.TestTargetBuilder.builder().withTargetId("kafka").withUrl("tcp://" + brokerPath);
    }

    protected abstract String initBroker();

    @BeforeEach
    public void before() {
        logger = new TestLogger();
        uniqueTopic = UUID.randomUUID().toString();
    }

    @Test
    public void should_consume_message_from_broker_without_truststore() {

        // given
        producer.send(new ProducerRecord<>(uniqueTopic, 123, "my-test-value"));

        Map<String, String> props = new HashMap<>();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, OffsetResetStrategy.EARLIEST.name().toLowerCase());

        Action consumeAction = getKafkaBasicConsumeAction(targetBuilder.build(), props, false);


        // when
        ActionExecutionResult actionExecutionResult = consumeAction.execute();

        // then
        assertThat(actionExecutionResult.status).isEqualTo(Success);
        List<Map<String, Object>> body = assertActionOutputsSize(actionExecutionResult, 1);
        assertThat(body.get(0).get("payload")).isEqualTo("my-test-value");
    }

    @Test
    public void consumer_from_target_with_truststore_should_reject_ssl_connection_with_broker_without_truststore_configured() throws URISyntaxException {
        // given
        String keystore_jks = Paths.get(requireNonNull(HttpsServerStartActionTest.class.getResource("/security/server.jks")).toURI()).toAbsolutePath().toString();
        Target target = targetBuilder.withProperty("trustStore", keystore_jks)
            .withProperty("trustStorePassword", "server")
            .withProperty("security.protocol", "SSL")
            .build();

        Map<String, String> props = new HashMap<>();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP);

        Action consumeAction = getKafkaBasicConsumeAction(target, props, false);

        // when
        ActionExecutionResult actionExecutionResult = consumeAction.execute();

        // then
        assertThat(actionExecutionResult.status).isEqualTo(Failure);

    }

    @Test
    public void should_reset_offset_to_the_beginning() {
        // given
        Map<String, String> props = new HashMap<>();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, OffsetResetStrategy.EARLIEST.name().toLowerCase());

        Action consumeAction = getKafkaBasicConsumeAction(targetBuilder.build(), props, false);

        producer.send(new ProducerRecord<>(uniqueTopic, 123, "1"));


        ActionExecutionResult actionExecutionResult = consumeAction.execute();
        assertThat(actionExecutionResult.status).isEqualTo(Success);
        List<Map<String, Object>> body = assertActionOutputsSize(actionExecutionResult, 1);
        assertThat(body.get(0).get("payload")).isEqualTo("1");

        // second time
        consumeAction = getKafkaBasicConsumeAction(targetBuilder.build(), props, false);
        actionExecutionResult = consumeAction.execute();

        assertThat(actionExecutionResult.status).isEqualTo(Failure);

        // third time with reset
        Action consumeActionWithReset = getKafkaBasicConsumeAction(targetBuilder.build(), props, true);
        actionExecutionResult = consumeActionWithReset.execute();

        assertThat(actionExecutionResult.status).isEqualTo(Success);
        body = assertActionOutputsSize(actionExecutionResult, 1);
        assertThat(body.get(0).get("payload")).isEqualTo("1");

        // third time without reset
        consumeAction = getKafkaBasicConsumeAction(targetBuilder.build(), props, false);
        actionExecutionResult = consumeAction.execute();

        assertThat(actionExecutionResult.status).isEqualTo(Failure);

    }

    private KafkaBasicConsumeAction getKafkaBasicConsumeAction(Target target, Map<String, String> props, boolean resetOffset) {
        return new KafkaBasicConsumeAction(target, uniqueTopic, GROUP, props, 1, null, null, TEXT_PLAIN_VALUE, "3000 ms", null, resetOffset, logger);
    }

    private List<Map<String, Object>> assertActionOutputsSize(ActionExecutionResult actionExecutionResult, int size) {
        assertThat(actionExecutionResult.outputs).hasSize(3);

        final List<Map<String, Object>> body = (List<Map<String, Object>>) actionExecutionResult.outputs.get(OUTPUT_BODY);
        final List<Map<String, Object>> payloads = (List<Map<String, Object>>) actionExecutionResult.outputs.get(OUTPUT_PAYLOADS);
        final List<Map<String, Object>> headers = (List<Map<String, Object>>) actionExecutionResult.outputs.get(OUTPUT_HEADERS);
        assertThat(body).hasSize(size);
        assertThat(payloads).hasSize(size);
        assertThat(headers).hasSize(size);

        Map<String, Object> bodyTmp;
        for (int i = 0; i < body.size(); i++) {
            bodyTmp = body.get(i);
            assertThat(bodyTmp.get(OUTPUT_BODY_PAYLOAD_KEY)).isEqualTo(payloads.get(i));
            assertThat(bodyTmp.get(OUTPUT_BODY_HEADERS_KEY)).isEqualTo(headers.get(i));
        }

        return body;
    }

    private static Producer<Integer, String> createProducer(String brokerPath) {
        Map<String, Object> producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerPath);
        return new DefaultKafkaProducerFactory<>(producerProps, new IntegerSerializer(), new StringSerializer()).createProducer();
    }
}
