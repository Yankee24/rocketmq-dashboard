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

package org.apache.rocketmq.dashboard.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.rocketmq.dashboard.config.RMQConfigure;
import org.apache.rocketmq.dashboard.model.LoginInfo;
import org.apache.rocketmq.dashboard.model.LoginResult;
import org.apache.rocketmq.dashboard.model.User;
import org.apache.rocketmq.dashboard.model.UserInfo;
import org.apache.rocketmq.dashboard.service.UserService;
import org.apache.rocketmq.dashboard.support.JsonResult;
import org.apache.rocketmq.dashboard.util.WebUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping("/login")
public class LoginController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private RMQConfigure configure;

    @Autowired
    private UserService userService;

    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    @RequestMapping(value = "/check.query", method = RequestMethod.GET)
    @ResponseBody
    public Object check(HttpServletRequest request) {
        LoginInfo loginInfo = new LoginInfo();

        loginInfo.setLogined(WebUtil.getValueFromSession(request, WebUtil.USER_NAME) != null);
        loginInfo.setLoginRequired(configure.isLoginRequired());

        return loginInfo;
    }

    @RequestMapping(value = "/login.do", method = RequestMethod.POST)
    @ResponseBody
    public Object login(org.apache.rocketmq.remoting.protocol.body.UserInfo userInfoRequest,
                        HttpServletRequest request,
                        HttpServletResponse response) throws Exception {
        logger.info("user:{} login", userInfoRequest.getUsername());
        User user = userService.queryByUsernameAndPassword(userInfoRequest.getUsername(), userInfoRequest.getPassword());

        if (user == null) {
            throw new IllegalArgumentException("Bad username or password!");
        } else {
            user.setPassword(null);
            UserInfo userInfo = WebUtil.setLoginInfo(request, response, user);
            WebUtil.setSessionValue(request, WebUtil.USER_INFO, userInfo);
            WebUtil.setSessionValue(request, WebUtil.USER_NAME, userInfoRequest.getUsername());
            userInfo.setSessionId(WebUtil.getSessionId(request));
            LoginResult result = new LoginResult(userInfoRequest.getUsername(), user.getType(), contextPath);
            return result;
        }
    }

    @RequestMapping(value = "/logout.do", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult<String> logout(HttpServletRequest request) {
        WebUtil.removeSession(request);
        return new JsonResult<>(contextPath);
    }
}
