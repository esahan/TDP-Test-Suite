package com.ykb.tdp.testsuite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ykb.tdp.testsuite.model.User;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	User findByName(String name);

}
