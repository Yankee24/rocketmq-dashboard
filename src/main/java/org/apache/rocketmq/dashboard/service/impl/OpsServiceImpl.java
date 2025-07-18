/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.dashboard.service.impl;

import com.google.common.collect.Maps;
import jakarta.annotation.Resource;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.rocketmq.dashboard.config.RMQConfigure;
import org.apache.rocketmq.dashboard.service.AbstractCommonService;
import org.apache.rocketmq.dashboard.service.OpsService;
import org.apache.rocketmq.dashboard.service.checker.CheckerType;
import org.apache.rocketmq.dashboard.service.checker.RocketMqChecker;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class OpsServiceImpl extends AbstractCommonService implements OpsService {

    @Resource
    private RMQConfigure configure;

    @Autowired
    private GenericObjectPool<MQAdminExt> mqAdminExtPool;

    @Resource
    private List<RocketMqChecker> rocketMqCheckerList;

    @Override
    public Map<String, Object> homePageInfo() {
        Map<String, Object> homePageInfoMap = Maps.newHashMap();
        homePageInfoMap.put("currentNamesrv", configure.getNamesrvAddr());
        homePageInfoMap.put("namesvrAddrList", configure.getNamesrvAddrs());
        homePageInfoMap.put("useVIPChannel", Boolean.valueOf(configure.getIsVIPChannel()));
        homePageInfoMap.put("useTLS", configure.isUseTLS());
        return homePageInfoMap;
    }

    @Override
    public void updateNameSvrAddrList(String nameSvrAddrList) {
        configure.setNamesrvAddr(nameSvrAddrList);
        // when update namesrvAddr, clean the mqAdminExt objects pool.
        mqAdminExtPool.clear();
    }

    @Override
    public String getNameSvrList() {
        return configure.getNamesrvAddr();
    }

    @Override
    public Map<CheckerType, Object> rocketMqStatusCheck() {
        Map<CheckerType, Object> checkResultMap = Maps.newHashMap();
        for (RocketMqChecker rocketMqChecker : rocketMqCheckerList) {
            checkResultMap.put(rocketMqChecker.checkerType(), rocketMqChecker.doCheck());
        }
        return checkResultMap;
    }

    @Override
    public boolean updateIsVIPChannel(String useVIPChannel) {
        configure.setIsVIPChannel(useVIPChannel);
        mqAdminExtPool.clear();
        return true;
    }

    @Override
    public boolean updateUseTLS(boolean useTLS) {
        configure.setUseTLS(useTLS);
        mqAdminExtPool.clear();
        return true;
    }

    @Override
    public void addNameSvrAddr(String namesrvAddr) {
        List<String> namesrvAddrs = configure.getNamesrvAddrs();
        if (namesrvAddrs != null && !namesrvAddrs.contains(namesrvAddr)) {
            namesrvAddrs.add(namesrvAddr);
        }
        configure.setNamesrvAddrs(namesrvAddrs);
    }
}
