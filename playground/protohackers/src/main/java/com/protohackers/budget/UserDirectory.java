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
        System.out.println("removed User " + user + ". Remaining users: " + userList());
    }

    public synchronized String userList() {
        return String.join(", ", users.stream().toList());
    }

    public synchronized int getUserCount() {
        return users.size();
    }

    public synchronized boolean hasUser(String user) {
        return users.contains(user);
    }
}
