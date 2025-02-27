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
package io.datavines.engine.spark.config;

import io.datavines.common.config.SinkConfig;
import io.datavines.common.exception.DataVinesException;
import io.datavines.engine.config.MetricParserUtils;
import io.datavines.metric.api.SqlMetric;
import io.datavines.spi.PluginLoader;

import java.util.ArrayList;
import java.util.List;

public class SparkMultiTableValueComparisonMetricBuilder extends BaseSparkConfigurationBuilder {

    @Override
    public void buildTransformConfigs() {
        String metricType = jobExecutionParameter.getMetricType();
        SqlMetric sqlMetric = PluginLoader
                .getPluginLoader(SqlMetric.class)
                .getNewPlugin(metricType);

        MetricParserUtils.operateInputParameter(inputParameter, sqlMetric, jobExecutionInfo);
    }

    @Override
    public void buildSinkConfigs() throws DataVinesException {

        inputParameter.put("expected_value", "expected_value");

        List<SinkConfig> sinkConfigs = new ArrayList<>();

        //get the task data storage parameter
        SinkConfig taskResultSinkConfig = getValidateResultDataSinkConfig(SparkSinkSqlBuilder.getMultiTableComparisonSinkSql(), "dv_job_execution_result");
        sinkConfigs.add(taskResultSinkConfig);

        configuration.setSinkParameters(sinkConfigs);
    }
}
