/*
 *
 *   Copyright 2009-2016 Weibo, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.weibo.motan.demo.client;

import com.weibo.api.motan.proxy.CommonHandler;
import com.weibo.api.motan.rpc.Future;
import com.weibo.api.motan.rpc.FutureListener;
import com.weibo.api.motan.rpc.Request;
import com.weibo.api.motan.rpc.ResponseFuture;
import com.weibo.motan.demo.service.MotanDemoService;
import com.weibo.motan.demo.service.PbParamService;
import com.weibo.motan.demo.service.model.User;
import io.grpc.examples.routeguide.Point;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Motan2RpcClient {

    public static void main(String[] args) throws Throwable {

        ApplicationContext ctx = new ClassPathXmlApplicationContext(new String[]{"classpath:motan2_demo_client.xml"});

        // hessian
        MotanDemoService service = (MotanDemoService) ctx.getBean("motanDemoReferer");
        print(service);
        // simple serialization
        service = (MotanDemoService) ctx.getBean("motanDemoReferer-simple");
        print(service);

        // pb serialization
        PbParamService pbService = (PbParamService) ctx.getBean("motanDemoReferer-pb");
        System.out.println(pbService.getFeature(Point.newBuilder().setLatitude(123).setLongitude(456).build()));

        // common client
        CommonHandler client = (CommonHandler) ctx.getBean("motanDemoReferer-common-client");
        System.out.println(client.call("hello", new Object[]{"a"}, String.class));

        User user = new User(1, "AAA");
        System.out.println(user);

        user = (User) client.call("rename", new Object[]{user, "BBB"}, User.class);
        System.out.println(user);

        ResponseFuture future = (ResponseFuture) client.asyncCall("rename", new Object[]{user, "CCC"}, User.class);
        user = (User) future.getValue();
        System.out.println(user);

        ResponseFuture future2 = (ResponseFuture) client.asyncCall("rename", new Object[]{user, "DDD"}, User.class);
        future2.addListener(new FutureListener() {
            @Override
            public void operationComplete(Future future) {
                System.out.println(future.getValue());
            }
        });

        Request request = client.buildRequest("rename", new Object[]{user, "EEE"});
        request.setAttachment("a", "a");
        user = (User) client.call(request, User.class);
        System.out.println(user);

        client.call("rename", new Object[]{null, "FFF"}, void.class);

        System.out.println("motan demo is finish.");
        System.exit(0);
    }

    public static void print(MotanDemoService service) throws InterruptedException {
        for (int i = 0; i < 3; i++) {
            System.out.println(service.hello("motan" + i));
            Thread.sleep(1000);
        }
    }

}
