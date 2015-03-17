package com.mtrade.monitor;

import com.mtrade.monitor.model.User;
import com.mtrade.monitor.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UserDetailServiceImpl implements UserDetailsService, InitializingBean {

	@Autowired
	private UserRepository userRepository;

    private String defaultAdminLoginName;
    private String defaultAdminDisplayName;
	private String defaultAdminPassword;

	private static final Logger LOG = LoggerFactory.getLogger(UserDetailServiceImpl.class);

    @Required
	public void setDefaultAdminLoginName(String defaultAdminName) {
		this.defaultAdminLoginName = defaultAdminName;
	}

    @Required
    public void setDefaultAdminDisplayName(String defaultAdminDisplayName) {
        this.defaultAdminDisplayName = defaultAdminDisplayName;
    }

    @Required
    public void setDefaultAdminPassword(String defaultAdminPassword) {
		this.defaultAdminPassword = defaultAdminPassword;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		User user = userRepository.findByName(defaultAdminLoginName);
		if (user == null && !StringUtils.isEmpty(defaultAdminLoginName)) {
			LOG.info("Default admin '" + defaultAdminLoginName + "' not found, creating one.");
			User defaultAdmin = new User(defaultAdminLoginName, "ADMIN");
            defaultAdmin.setDisplayName(defaultAdminDisplayName);
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
		
		private String userName;
        private String displayName;
		private String password;
		private List<SimpleGrantedAuthority> roles;
		
		public UserDetailsImpl(User user) {
			this.userName = user.getName();
            this.displayName = user.getDisplayName();
			this.password = user.getPassword();
			this.roles = new ArrayList<SimpleGrantedAuthority>();
			if (user.getRoles() != null) for (String role : user.getRoles()) {
				roles.add(new SimpleGrantedAuthority(role));
			}
		}
		
		public String getUsername() {
			return userName;
		}

        public String getDisplayName() {
            return displayName;
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
