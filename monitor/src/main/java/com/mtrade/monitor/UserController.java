package com.mtrade.monitor;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * @author jaroslav.psenicka@gmail.com
 */
@Controller
public class UserController {

    @ResponseBody
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @RequestMapping(value="/user", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getCurrentUser() {
        Map<String, Object> user = new HashMap<>();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getPrincipal() instanceof UserDetailServiceImpl.UserDetailsImpl) {
            user.put("name", ((UserDetailServiceImpl.UserDetailsImpl)auth.getPrincipal()).getDisplayName());
        } else {
            user.put("name", auth.getPrincipal().toString());
        }

        List<String> roles = new ArrayList<>();
        for (GrantedAuthority authority : auth.getAuthorities()) {
            roles.add(authority.getAuthority());
        }

        user.put("roles", roles);
        return user;
    }

}
