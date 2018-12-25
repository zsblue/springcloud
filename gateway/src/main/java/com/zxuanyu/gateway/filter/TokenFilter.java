package com.zxuanyu.gateway.filter;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * **
 * .*                   @
 * .*           @@@@     @@
 * .*        @@@    @@   @* @             /@/@@/@/@@/@/
 * .*      @@@       @@  @ * @           /@/  /@/  /@/
 * .*    @@         @@  @ * @           /@/  /@/  /@/
 * .*  @@          @@  @ * @           /@/  /@/  /@/
 * .* @@          @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
 * .*  @@          @@  @ * @     @ @   \@\  \@\  \@\
 * .*    @@         @@  @ * @    @ @    \@\  \@\  \@\
 * .*      @@@       @@  @ * @  @ * @    \@\  \@\  \@\
 * .*        @@@    @@   @* @  @ * * @    \@\@@\@\@@\@\
 * .*           @@@@     @@    @@*@*@@
 * .*                   @        ***
 * **
 * create by zhangsong 2018/12/14
 * <p>
 * token 不验证的url
 */

@Component
public class TokenFilter {

    private LinkedHashMap<String, String> filterMap = new LinkedHashMap<>();

    private LinkedHashSet<String> anonMap = new LinkedHashSet<>();

    public TokenFilter() {

        filterMap.put("/cm-login/ZysdkClient", "anon");
        filterMap.put("/cm-login/user/**", "anon");
        filterMap.put("**/druid/**", "anon");
        filterMap.put("**/druid2/**", "anon");
        filterMap.put("/cm-login/hello", "anon");
        filterMap.put("/eureka/**", "anon");
        filterMap.put("**/actuator/**", "anon");
        filterMap.put("/springadmin/**", "anon");
//        filterMap.put("/", "anon");
        deliver();

    }

    public LinkedHashSet<String> getAnonMap() {
        return anonMap;
    }

    private void deliver() {
        for (Map.Entry<String, String> entry : filterMap.entrySet()) {
            String url = entry.getKey();
            String chainDefinition = entry.getValue();
            anonMap.add(url);
        }
    }


}
