package com.example.navigation.security;

import com.example.navigation.model.entity.UserLogInfo;
import com.example.navigation.repository.UserLogInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Security用户详情服务实现
 * 现在使用UserLogInfo表进行用户认证
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserLogInfoRepository userLogInfoRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String accountName) throws UsernameNotFoundException {
        UserLogInfo userLogInfo = userLogInfoRepository.findByAccountName(accountName)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + accountName));

        return org.springframework.security.core.userdetails.User.builder()
                .username(userLogInfo.getAccountName())
                .password(userLogInfo.getCode())
                .build();
    }
}