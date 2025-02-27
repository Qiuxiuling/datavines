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
package io.datavines.engine.config;

import io.datavines.common.config.*;
import io.datavines.common.config.enums.SourceType;
import io.datavines.common.config.enums.TransformType;
import io.datavines.common.entity.*;
import io.datavines.common.exception.DataVinesException;
import io.datavines.common.entity.ConnectionInfo;
import io.datavines.common.utils.CommonPropertyUtils;
import io.datavines.common.utils.JSONUtils;
import io.datavines.common.utils.StringUtils;
import io.datavines.common.utils.placeholder.PlaceholderUtils;
import io.datavines.engine.api.ConfigConstants;
import io.datavines.metric.api.ExpectedValue;
import io.datavines.metric.api.SqlMetric;
import io.datavines.spi.PluginLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.datavines.engine.api.ConfigConstants.*;
import static io.datavines.engine.api.ConfigConstants.SQL;
import static io.datavines.engine.config.MetricParserUtils.generateUniqueCode;

public abstract class BaseDataQualityConfigurationBuilder implements DataQualityConfigurationBuilder {

    protected final DataVinesQualityConfig configuration = new DataVinesQualityConfig();

    protected Map<String, String> inputParameter;

    protected JobExecutionParameter jobExecutionParameter;

    protected JobExecutionInfo jobExecutionInfo;

    protected ExpectedValue expectedValue;

    @Override
    public void init(Map<String, String> inputParameter, JobExecutionInfo jobExecutionInfo) {
        this.inputParameter = inputParameter;
        this.inputParameter.put(COLUMN, "");
        this.jobExecutionInfo = jobExecutionInfo;
        this.jobExecutionParameter = jobExecutionInfo.getJobExecutionParameter();

        if (jobExecutionParameter.getMetricParameter() != null) {
            jobExecutionParameter.getMetricParameter().forEach((k, v) -> {
                inputParameter.put(k, String.valueOf(v));
            });
        }

        if (jobExecutionParameter.getExpectedParameter() != null) {
            jobExecutionParameter.getExpectedParameter().forEach((k, v) -> {
                inputParameter.put(k, String.valueOf(v));
            });
        }

        inputParameter.put(RESULT_FORMULA, String.valueOf(jobExecutionParameter.getResultFormula()));
        inputParameter.put(OPERATOR, String.valueOf(jobExecutionParameter.getOperator()));
        inputParameter.put(THRESHOLD, String.valueOf(jobExecutionParameter.getThreshold()));
        inputParameter.put(EXPECTED_TYPE, StringUtils.wrapperSingleQuotes(jobExecutionParameter.getExpectedType()));
        inputParameter.put(ERROR_DATA_FILE_NAME, jobExecutionInfo.getErrorDataFileName());

        if ("local-file".equalsIgnoreCase(jobExecutionInfo.getErrorDataStorageType())) {
            inputParameter.putAll(JSONUtils.toMap(jobExecutionInfo.getErrorDataStorageParameter(),String.class, String.class));
        } else {
            inputParameter.put(ERROR_DATA_FILE_DIR, CommonPropertyUtils.getString(ERROR_DATA_FILE_DIR,CommonPropertyUtils.ERROR_DATA_FILE_DIR_DEFAULT));
        }

    }

    @Override
    public void buildName() {
        configuration.setName(jobExecutionInfo.getName());
    }

    @Override
    public void buildEnvConfig() {
        configuration.setEnvConfig(getEnvConfig());
    }

    @Override
    public void buildSourceConfigs() throws DataVinesException {
        configuration.setSourceParameters(getSourceConfigs());
    }

    @Override
    public void buildTransformConfigs() {
        String metricType = jobExecutionParameter.getMetricType();
        SqlMetric sqlMetric = PluginLoader
                .getPluginLoader(SqlMetric.class)
                .getNewPlugin(metricType);

        MetricParserUtils.operateInputParameter(inputParameter, sqlMetric, jobExecutionInfo);

        List<TransformConfig> transformConfigs = new ArrayList<>();

        inputParameter.put(INVALIDATE_ITEMS_TABLE,
                sqlMetric.getInvalidateItems().getResultTable()
                        + "_" + inputParameter.get(ConfigConstants.JOB_EXECUTION_ID));

        MetricParserUtils.setTransformerConfig(
                inputParameter, transformConfigs,
                sqlMetric.getInvalidateItems(), TransformType.INVALIDATE_ITEMS.getDescription());

        MetricParserUtils.setTransformerConfig(
                inputParameter,
                transformConfigs,
                sqlMetric.getActualValue(),
                TransformType.ACTUAL_VALUE.getDescription());

        inputParameter.put(ACTUAL_TABLE, sqlMetric.getActualValue().getResultTable());

        // get expected value transform sql
        String expectedType = jobExecutionInfo.getEngineType() + "_" + jobExecutionParameter.getExpectedType();
        expectedValue = PluginLoader
                .getPluginLoader(ExpectedValue.class)
                .getNewPlugin(expectedType);

        ExecuteSql expectedValueExecuteSql =
                new ExecuteSql(expectedValue.getExecuteSql(),expectedValue.getOutputTable());

        if (StringUtils.isNotEmpty(expectedValueExecuteSql.getResultTable())) {
            inputParameter.put(EXPECTED_TABLE, expectedValueExecuteSql.getResultTable());
            inputParameter.put(EXPECTED_VALUE, expectedValue.getName());
        }

        inputParameter.put(UNIQUE_CODE, StringUtils.wrapperSingleQuotes(generateUniqueCode(inputParameter)));

        if (expectedValue.isNeedDefaultDatasource()) {
            MetricParserUtils.setTransformerConfig(inputParameter, transformConfigs,
                    expectedValueExecuteSql, TransformType.EXPECTED_VALUE_FROM_METADATA_SOURCE.getDescription());
        } else {
            MetricParserUtils.setTransformerConfig(inputParameter, transformConfigs,
                    expectedValueExecuteSql, TransformType.EXPECTED_VALUE_FROM_SOURCE.getDescription());
        }

        configuration.setTransformParameters(transformConfigs);
    }

    @Override
    public DataVinesQualityConfig build() {
        return configuration;
    }

    protected abstract EnvConfig getEnvConfig();

    protected abstract List<SourceConfig> getSourceConfigs() throws DataVinesException;

    protected SourceConfig getValidateResultSourceConfig() throws DataVinesException {

        SourceConfig actualValueSourceConfig = new SourceConfig();
        actualValueSourceConfig.setPlugin(jobExecutionInfo.getValidateResultDataStorageType());
        actualValueSourceConfig.setType(SourceType.METADATA.getDescription());
        actualValueSourceConfig.setConfig(getValidateResultSourceConfigMap(null,"dv_actual_values"));
        return actualValueSourceConfig;
    }

    protected SinkConfig getValidateResultDataSinkConfig(String sql, String dbTable) throws DataVinesException {

        SinkConfig validateResultDataStorageConfig = new SinkConfig();
        validateResultDataStorageConfig.setPlugin(jobExecutionInfo.getValidateResultDataStorageType());
        Map<String, Object> configMap = getValidateResultSourceConfigMap(
                PlaceholderUtils.replacePlaceholders(
                        sql, inputParameter,true),dbTable);
        configMap.put(JOB_EXECUTION_ID, jobExecutionInfo.getId());
        validateResultDataStorageConfig.setConfig(configMap);

        return validateResultDataStorageConfig;

    }

    private Map<String,Object> getValidateResultSourceConfigMap(String sql, String dbTable) {

        Map<String, Object> configMap = new HashMap<>();
        if (StringUtils.isNotEmpty(jobExecutionInfo.getValidateResultDataStorageParameter())) {
            configMap = JSONUtils.toMap(jobExecutionInfo.getValidateResultDataStorageParameter(), String.class, Object.class);
        }
        configMap.put(TABLE, dbTable);
        configMap.put(OUTPUT_TABLE, dbTable);
        if (StringUtils.isNotEmpty(sql)) {
            configMap.put(SQL, sql);
        }

        configMap.put(EXPECTED_VALUE, expectedValue.getName().replace(expectedValue.getOutputTable()+".", ""));

        return configMap;
    }

    protected Map<String, Object> getValidateResultDataStorageConfigMap() {
        Map<String, Object> configMap = new HashMap<>();
        if (StringUtils.isNotEmpty(jobExecutionInfo.getValidateResultDataStorageParameter())) {
            configMap = JSONUtils.toMap(jobExecutionInfo.getValidateResultDataStorageParameter(), String.class, Object.class);
        }
        return configMap;
    }
}
