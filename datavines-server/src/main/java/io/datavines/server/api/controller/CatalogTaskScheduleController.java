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
package io.datavines.server.api.controller;

import io.datavines.core.aop.RefreshToken;
import io.datavines.core.constant.DataVinesConstants;
import io.datavines.core.exception.DataVinesServerException;
import io.datavines.server.api.dto.bo.catalog.schedule.CatalogTaskScheduleCreateOrUpdate;
import io.datavines.server.api.dto.bo.job.schedule.JobScheduleCreateOrUpdate;
import io.datavines.server.api.dto.bo.job.schedule.MapParam;
import io.datavines.server.repository.service.CatalogTaskScheduleService;
import io.datavines.server.repository.service.JobScheduleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(value = "catalog", tags = "catalog", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequestMapping(value = DataVinesConstants.BASE_API_PATH + "/catalog/task/schedule", produces = MediaType.APPLICATION_JSON_VALUE)
@RefreshToken
@Validated
public class CatalogTaskScheduleController {

    @Autowired
    private CatalogTaskScheduleService catalogTaskScheduleService;

    @ApiOperation(value = "create or update catalog task schedule")
    @PostMapping(value = "/createOrUpdate",consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object createOrUpdateJob(@Valid @RequestBody CatalogTaskScheduleCreateOrUpdate taskScheduleCreateOrUpdate) throws DataVinesServerException {
        return catalogTaskScheduleService.createOrUpdate(taskScheduleCreateOrUpdate);
    }

    @ApiOperation(value = "get task schedule by dataSourceId")
    @GetMapping(value = "/{dataSourceId}")
    public Object getByDataSourceId(@PathVariable Long dataSourceId)  {
        return catalogTaskScheduleService.getByDataSourceId(dataSourceId);
    }

    @ApiOperation(value = "get cron by cycle")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, value = "/cron")
    public Object showCron(@Valid @RequestBody MapParam mapParam) throws DataVinesServerException {
        return catalogTaskScheduleService.getCron(mapParam);
    }
}