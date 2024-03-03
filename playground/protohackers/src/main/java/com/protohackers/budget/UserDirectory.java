package com.protohackers.budget;

import java.util.HashSet;
import java.util.Set;

public class UserDirectory {
    private final Set<String> users = new HashSet<>();

    public synchronized void registerUser(String user) {
        users.add(user);
    }

    public synchronized void removeUser(String user) {
        users.remove(user);
    }

    public synchronized String userList() {
        return String.join(", ", users.stream().toList());
    }

    public synchronized int getUserCount() {
        return users.size();
    }
}
