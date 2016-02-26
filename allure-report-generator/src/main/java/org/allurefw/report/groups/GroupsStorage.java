package org.allurefw.report.groups;

import org.allurefw.report.entity.GroupInfo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 26.02.16
 */
public class GroupsStorage {

    private Map<String, Map<String, GroupInfo>> groups = new HashMap<>();

    public void addGroupInfo(String groupType, GroupInfo groupInfo) {
        groups.putIfAbsent(groupType, new HashMap<>());
        groups.get(groupType).put(groupInfo.getName(), groupInfo);
    }

    public Map<String, GroupInfo> getGroupInfo(String groupType) {
        return Collections.unmodifiableMap(groups.get(groupType));
    }
}
