package com.ykb.tdp.testsuite.service;

import java.util.List;

import com.ykb.tdp.testsuite.model.User;

public interface IUserService {
	
	User findById(Long id);
	 
    User findByName(String name);
 
    void saveUser(User user);
 
    void updateUser(User user);
 
    void deleteUserById(Long id);
 
    void deleteAllUsers();
 
    List<User> findAllUsers();
 
    boolean isUserExist(User user);

}
