/*
 * SPDX-FileCopyrightText: 2017-2024 Enedis
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.chutneytesting.action.jakarta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Logger;
import com.chutneytesting.action.spi.injectable.Target;
import jakarta.jms.TextMessage;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class JakartaListenerActionTest {

    @Test
    public void should_retrieve_jms_message_in_queue_and_put_it_in_the_context() throws Exception {

        Target target = mock(Target.class, RETURNS_DEEP_STUBS);
        Logger logger = mock(Logger.class);

        // Given 1 - jms task creation
        JakartaListenerAction task = new JakartaListenerAction(target, logger, "", "", "", "", 0);

        TextMessage textMessageMock = mock(TextMessage.class, RETURNS_DEEP_STUBS);
        String messageBody = "FAKE_JSON_PRODUCED_BY_SGE";
        when(textMessageMock.getText()).thenReturn(messageBody);

        JakartaConnectionFactory jmsConnectionFactoryMock = mock(JakartaConnectionFactory.class, RETURNS_DEEP_STUBS);
        when(jmsConnectionFactoryMock.createConsumer(any(), any(), any(), any(), any(), anyInt()).getResource().getMessage()).thenReturn(Optional.of(textMessageMock));

        ReflectionTestUtils.setField(task, "jmsConnectionFactory", jmsConnectionFactoryMock);

        Target targetMock = mock(Target.class);
        when(targetMock.name()).thenReturn("SAR2");

        // When
        ActionExecutionResult executionResult = task.execute();

        // Then
        verify(logger, times(1)).info(anyString());

        assertThat(executionResult.outputs.get("textMessage")).as("Sent JMS message").isEqualTo(messageBody);
    }
}
