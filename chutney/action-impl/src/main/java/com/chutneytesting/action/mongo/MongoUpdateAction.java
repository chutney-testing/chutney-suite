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

package com.chutneytesting.action.mongo;

import static com.chutneytesting.action.mongo.MongoActionValidatorsUtils.mongoTargetValidation;
import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.notBlankStringValidation;
import static com.chutneytesting.action.spi.validation.Validator.getErrorsFrom;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import com.chutneytesting.action.spi.injectable.Target;
import com.chutneytesting.tools.CloseableResource;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.bson.BsonDocument;
import org.bson.Document;

public class MongoUpdateAction implements Action {

    private final MongoDatabaseFactory mongoDatabaseFactory = new DefaultMongoDatabaseFactory();
    private final Target target;
    private final Logger logger;
    private final String collection;
    private final String filter;
    private final String update;
    private final List<String> arrayFilters;

    public MongoUpdateAction(Target target,
                           Logger logger,
                           @Input("collection") String collection,
                           @Input("filter") String filter,
                           @Input("update") String update,
                           // See https://jira.mongodb.org/browse/SERVER-831 for usage.
                           // Only since @3.5.12 mongodb version
                           @Input("arrayFilters") List<String> arrayFilters) {
        this.target = target;
        this.logger = logger;
        this.collection = collection;
        this.filter = filter;
        this.update = update;
        this.arrayFilters = ofNullable(arrayFilters).orElse(emptyList());
    }

    @Override
    public List<String> validateInputs() {
        return getErrorsFrom(
            notBlankStringValidation(collection, "collection"),
            notBlankStringValidation(update, "update"),
            mongoTargetValidation(target)
        );
    }

    @Override
    public ActionExecutionResult execute() {
        try (CloseableResource<MongoDatabase> database = mongoDatabaseFactory.create(target)) {
            MongoCollection<Document> collection = database
                .getResource()
                .getCollection(this.collection);

            final UpdateResult updateResult;
            if (!arrayFilters.isEmpty()) {
                List<BsonDocument> arrayFilterDocuments = arrayFilters.stream()
                    .map(BsonDocument::parse)
                    .collect(Collectors.toList());
                updateResult = collection
                    .updateMany(
                        BsonDocument.parse(filter),
                        BsonDocument.parse(update),
                        new UpdateOptions().arrayFilters(arrayFilterDocuments)
                    );
            } else {
                updateResult = collection
                    .updateMany(
                        BsonDocument.parse(filter),
                        BsonDocument.parse(update)
                    );
            }
            if (!updateResult.wasAcknowledged()) {
                logger.error("Update was not acknowledged");
                return ActionExecutionResult.ko();
            }
            long modifiedCount = updateResult.getModifiedCount();
            logger.info("Modified in Mongo collection '" + this.collection + "': " + modifiedCount + " documents");
            return ActionExecutionResult.ok(Collections.singletonMap("modifiedCount", modifiedCount));
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage());
            return ActionExecutionResult.ko();
        }
    }
}
