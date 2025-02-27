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
package io.datavines.server.repository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@TableName("dv_user_workspace")
@EqualsAndHashCode
@ToString
public class UserWorkspace {

    private static final long serialVersionUID = -1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(value = "user_id")
    private Long userId;

    @TableField(value = "workspace_id")
    private Long workspaceId;

    @TableField(value = "role_id")
    private Long roleId;

    @TableField(value = "create_by")
    private Long createBy;

    @TableField(value = "create_time")
    @JsonFormat(pattern = "uuuu-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @TableField(value = "update_by")
    private Long updateBy;

    @TableField(value = "update_time")
    @JsonFormat(pattern = "uuuu-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
