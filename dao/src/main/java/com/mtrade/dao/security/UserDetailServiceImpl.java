package com.mtrade.dao.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;

import com.mtrade.dao.model.User;
import com.mtrade.dao.repository.UserRepository;

public class UserDetailServiceImpl implements UserDetailsService, InitializingBean {

	@Autowired
	private UserRepository userRepository;

	private String defaultAdminName;
	private String defaultAdminPassword;

	private static final Logger LOG = LoggerFactory.getLogger(UserDetailServiceImpl.class);

	public void setDefaultAdminName(String defaultAdminName) {
		this.defaultAdminName = defaultAdminName;
	}

	public void setDefaultAdminPassword(String defaultAdminPassword) {
		this.defaultAdminPassword = defaultAdminPassword;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		User user = userRepository.findByName(defaultAdminName);
		if (user == null && !StringUtils.isEmpty(defaultAdminName)) {
			LOG.info("Default admin '" + defaultAdminName + "' not found, creating one.");
			User defaultAdmin = new User(defaultAdminName, "ADMIN");
			defaultAdmin.setPassword(defaultAdminPassword);
			userRepository.save(defaultAdmin);
		}
	}

	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByName(username);
		if (user == null) {
			throw new UsernameNotFoundException(username);
		}

		return new UserDetailsImpl(user);
	}

	public static class UserDetailsImpl implements UserDetails {
		
		private String username;
		private String password;
		private List<SimpleGrantedAuthority> roles;
		
		private UserDetailsImpl(User user) {
			this.username = user.getName();
			this.password = user.getPassword();
			this.roles = new ArrayList<SimpleGrantedAuthority>();
			if (user.getRoles() != null) for (String role : user.getRoles()) {
				roles.add(new SimpleGrantedAuthority(role));
			}
		}
		
		public String getUsername() {
			return username;
		}
		
		public String getPassword() {
			return password;
		}
		
		public Collection<? extends GrantedAuthority> getAuthorities() {
			return roles;
		}

		public boolean isEnabled() {
			return true;
		}
		
		public boolean isAccountNonExpired() {
			return true;
		}
		
		public boolean isAccountNonLocked() {
			return true;
		}

		public boolean isCredentialsNonExpired() {
			return true;
		}
	}

}
