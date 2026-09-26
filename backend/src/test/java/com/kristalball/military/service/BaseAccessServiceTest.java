package com.kristalball.military.service;

import com.kristalball.military.entity.Base;
import com.kristalball.military.entity.Role;
import com.kristalball.military.entity.RoleName;
import com.kristalball.military.entity.User;
import com.kristalball.military.repository.BaseRepository;
import com.kristalball.military.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class BaseAccessServiceTest {

    private final BaseAccessService baseAccessService = new BaseAccessService(
            mock(BaseRepository.class), mock(UserRepository.class));

    @Test
    void commanderAlwaysResolvesToAssignedBaseAndCannotOverrideIt() {
        User commander = User.builder().role(Role.builder().name(RoleName.BASE_COMMANDER).build())
                .assignedBase(Base.builder().id(7L).name("North").build()).build();

        assertEquals(7L, baseAccessService.resolveBaseId(commander, null));
        assertThrows(AccessDeniedException.class, () -> baseAccessService.resolveBaseId(commander, 8L));
    }

    @Test
    void administratorMayRequestAnyBase() {
        User administrator = User.builder().role(Role.builder().name(RoleName.ADMIN).build()).build();

        assertEquals(8L, baseAccessService.resolveBaseId(administrator, 8L));
        assertNull(baseAccessService.resolveBaseId(administrator, null));
    }
}