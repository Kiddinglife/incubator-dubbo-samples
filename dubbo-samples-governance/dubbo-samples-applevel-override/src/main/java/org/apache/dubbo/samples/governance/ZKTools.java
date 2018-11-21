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
package org.apache.dubbo.samples.governance;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.zookeeper.CreateMode;

/**
 *
 */
public class ZKTools {
    private static CuratorFramework client;

    public static void main(String[] args) throws Exception {
        client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", 60 * 1000, 60 * 1000,
                new ExponentialBackoffRetry(1000, 3));
        client.start();

//        generateAppevelOverride();
//        System.in.read();
        generateAppevelOverrideConsumer();
    }

    public static void generateAppevelOverride() {
        String str = "# Execute on governance-appoverride-provider only\n" +
                "# the governance-appoverride-provider instances on those provides who's port is 20880 will be lifted to 1000\n" +
                "# this will take effect on all services on governance-appoverride-provider.\n" +
                "---\n" +
                "apiVersion: v2.7\n" +
                "scope: application\n" +
                "key: governance-appoverride-provider\n" +
                "enabled: true\n" +
                "configs:\n" +
                "- addresses: [\"0.0.0.0:20880\"]\n" +
                "  parameters:\n" +
                "    weight: 1000\n" +
                "...";

        System.out.println(str);

        try {
            String path = "/dubbo/config/governance-appoverride-provider/configurators";
            if (client.checkExists().forPath(path) == null) {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
            }
            setData(path, str);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     */
    public static void generateAppevelOverrideConsumer() {
        String str = "# Execute on demo-consumer only\n" +
                "# the traffic come out from governance-appoverride-consumer will be distributed evenly across all providers,\n" +
                "# because governance-appoverride-consumer will consider them having the same weight 100.\n" +
                "---\n" +
                "apiVersion: v2.7\n" +
                "scope: application\n" +
                "key: governance-appoverride-consumer\n" +
                "enabled: true\n" +
                "configs:\n" +
                "- addresses: [\"0.0.0.0\"]\n" +
                "  side: consumer\n" +
                "  parameters:\n" +
                "    weight: 140\n" +
                "...\n";

        System.out.println(str);

        try {
            String path = "/dubbo/config/governance-appoverride-consumer/configurators";
            if (client.checkExists().forPath(path) == null) {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
            }
            setData(path, str);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void setData(String path, String data) throws Exception {
        client.setData().forPath(path, data.getBytes());
    }

    private static String pathToKey(String path) {
        if (StringUtils.isEmpty(path)) {
            return path;
        }
        return path.replace("/dubbo/config/", "").replaceAll("/", ".");
    }

}