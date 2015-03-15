package com.mtrade.monitor.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.*;

@Document
public class User {

	@Id
	private String id;

	@NotNull
	@Indexed(unique = true)
	private String name;

	private String password;

	@NotNull
	private Date createDate;

	@NotNull
	private Set<String> roles;

	public static final String ROLE_USER = "USER";
	public static final String ROLE_ADMIN = "ADMIN";

	public User() {
		this.createDate = new Date();
		this.name = UUID.randomUUID().toString();
		this.roles = new HashSet<>(Arrays.asList(ROLE_USER));
	}

	public User(String key) {
		this.createDate = new Date();
		this.name = key;
		this.roles = new HashSet<>(Arrays.asList(ROLE_USER));
	}

	public User(String key, String... roles) {
		this.createDate = new Date();
		this.name = key;
		this.roles = (roles != null) ? new HashSet<>(Arrays.asList(roles)) : null;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public Set<String> getRoles() {
		return roles;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
}
