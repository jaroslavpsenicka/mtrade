package com.mtrade.dao.model;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public class User {

	@Id
	private String id;

	@NotNull
	@Indexed(unique = true)
	private String name;

    @NotNull
    @Indexed(unique = true)
	private String accountId;

    private String password;

    @NotNull
    private Date createDate;

    @NotNull
	private Set<String> roles;

	public static final String ROLE_USER = "USER";
	public static final String ROLE_ADMIN = "ADMIN";

	public User() {
	}

	public User(String name, String accountId) {
		this.createDate = new Date();
		this.name = name;
        this.accountId = accountId;
		this.roles = new HashSet<>(Arrays.asList(ROLE_USER));
	}

	public User(String name, String accountId, String... roles) {
		this.createDate = new Date();
        this.name = name;
        this.accountId = accountId;
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

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
}
