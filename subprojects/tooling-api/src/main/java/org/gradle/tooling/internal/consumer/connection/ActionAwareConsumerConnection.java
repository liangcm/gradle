/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.tooling.internal.consumer.connection;

import org.gradle.tooling.BuildAction;
import org.gradle.tooling.BuildController;
import org.gradle.tooling.UnknownModelException;
import org.gradle.tooling.internal.adapter.ProtocolToModelAdapter;
import org.gradle.tooling.internal.consumer.parameters.ConsumerOperationParameters;
import org.gradle.tooling.internal.consumer.versioning.ModelMapping;
import org.gradle.tooling.internal.protocol.*;

public class ActionAwareConsumerConnection extends ModelBuilderBackedConsumerConnection {
    private final InternalBuildActionExecutor executor;

    public ActionAwareConsumerConnection(ConnectionVersion4 delegate, ModelMapping modelMapping, ProtocolToModelAdapter adapter) {
        super(delegate, modelMapping, adapter);
        executor = (InternalBuildActionExecutor) delegate;
    }

    @Override
    public <T> T run(final BuildAction<T> action, ConsumerOperationParameters operationParameters) throws UnsupportedOperationException, IllegalStateException {
        return executor.run(new BuildActionAdapter<T>(action, adapter), operationParameters).getModel();
    }

    private static class BuildActionAdapter<T> implements InternalBuildAction<T> {
        private final BuildAction<T> action;
        private final ProtocolToModelAdapter adapter;

        public BuildActionAdapter(BuildAction<T> action, ProtocolToModelAdapter adapter) {
            this.action = action;
            this.adapter = adapter;
        }

        public T execute(final InternalBuildController buildController) {
            return action.execute(new BuildController() {
                public <T> T getModel(Class<T> modelType) throws UnknownModelException {
                    ModelIdentifier modelIdentifier = new ModelMapping().getModelIdentifierFromModelType(modelType);
                    Object model = buildController.getModel(modelIdentifier).getModel();
                    return adapter.adapt(modelType, model);
                }
            });
        }
    }
}
