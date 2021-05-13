package com.hsbc.wk.service;


import com.hsbc.wk.entity.Role;
import com.hsbc.wk.entity.User;

import java.io.UnsupportedEncodingException;

public interface UserService {

    boolean addUser(String username, String pwd);
    boolean addUser(User user);

    boolean addRole(String rolename);
    boolean addRole(Role role);

    boolean deleteUser(String username, String pwd);
    boolean deleteUser(User user);

    boolean assignRoleToUser(String roleName, String username, String pwd);

    String authenticate(String username, String pwd) throws UnsupportedEncodingException;
    String authenticate(User user) throws UnsupportedEncodingException;
}
