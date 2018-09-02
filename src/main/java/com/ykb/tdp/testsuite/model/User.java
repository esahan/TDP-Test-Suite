package com.ykb.tdp.testsuite.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;

@Entity
@Table(name="TDP_TESTSUITE_USERS")
public class User {
	
	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private long id;
	@NotEmpty
	@Column(name="NAME", nullable=false, unique=true)
	private String name;
	@NotEmpty
    @Column(name="PASSWORD", nullable=false)
	private String password;
	@NotEmpty
    @Column(name="EMAIL", nullable=false)
	private String email;
	
	
	public long getId() {
		return id;
	}		
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}

}
