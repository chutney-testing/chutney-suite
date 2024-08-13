/*
 * SPDX-FileCopyrightText: 2017-2024 Enedis
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.chutneytesting.action.amqp;

import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.notEmptyListValidation;
import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.targetValidation;
import static com.chutneytesting.action.spi.validation.Validator.getErrorsFrom;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import com.chutneytesting.action.spi.injectable.Target;
import com.rabbitmq.client.AMQP.Queue.PurgeOk;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class AmqpCleanQueuesAction implements Action {

    private final ConnectionFactoryFactory connectionFactoryFactory = new ConnectionFactoryFactory();

    private final Target target;
    private final List<String> queueNames;
    private final Logger logger;

    public AmqpCleanQueuesAction(Target target,
                               @Input("queue-names") List<String> queueNames,
                               Logger logger) {
        this.target = target;
        this.queueNames = ofNullable(queueNames).orElse(emptyList());
        this.logger = logger;
    }

    @Override
    public List<String> validateInputs() {
        return getErrorsFrom(
            targetValidation(target),
            notEmptyListValidation(queueNames, "queueNames")
        );
    }

    @Override
    public ActionExecutionResult execute() {
        try (Connection connection = connectionFactoryFactory.newConnection(target);
             Channel channel = connection.createChannel()) {
            for (String queueName : queueNames) {
                PurgeOk purgeOk = channel.queuePurge(queueName);
                logger.info("Purge queue " + queueName + ". " + purgeOk.getMessageCount() + " messages deleted");
            }
            return ActionExecutionResult.ok();
        } catch (TimeoutException | IOException e) {
            logger.error("Unable to establish connection to RabbitMQ: " + e.getMessage());
            return ActionExecutionResult.ko();
        }
    }
}
