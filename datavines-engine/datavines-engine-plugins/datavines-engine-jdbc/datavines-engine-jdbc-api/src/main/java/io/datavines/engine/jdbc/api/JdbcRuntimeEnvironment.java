/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datavines.engine.jdbc.api;

import io.datavines.common.config.CheckResult;
import io.datavines.common.config.Config;
import io.datavines.engine.api.env.Execution;
import io.datavines.engine.api.env.RuntimeEnvironment;
import io.datavines.engine.jdbc.api.entity.ConnectionItem;

import java.sql.SQLException;

public class JdbcRuntimeEnvironment implements RuntimeEnvironment {

    private ConnectionItem sourceConnection;

    private ConnectionItem targetConnection;

    private ConnectionItem metadataConnection;

    @Override
    public void prepare() {

    }

    @Override
    public Execution getExecution() {
        return new JdbcExecution(this);
    }

    @Override
    public void setConfig(Config config) {

    }

    @Override
    public Config getConfig() {
        return null;
    }

    @Override
    public CheckResult checkConfig() {
        return null;
    }

    public ConnectionItem getSourceConnection() {
        return sourceConnection;
    }

    public void setSourceConnection(ConnectionItem sourceConnection) {
        this.sourceConnection = sourceConnection;
    }

    public ConnectionItem getMetadataConnection() {
        return metadataConnection;
    }

    public void setMetadataConnection(ConnectionItem metadataConnection) {
        this.metadataConnection = metadataConnection;
    }

    public ConnectionItem getTargetConnection() {
        return targetConnection;
    }

    public void setTargetConnection(ConnectionItem targetConnection) {
        this.targetConnection = targetConnection;
    }

    public void close() throws SQLException {
        if(sourceConnection != null) {
            sourceConnection.getConnection().close();
        }

        if(targetConnection != null) {
            targetConnection.getConnection().close();
        }

        if(metadataConnection != null) {
            metadataConnection.getConnection().close();
        }
    }
}
