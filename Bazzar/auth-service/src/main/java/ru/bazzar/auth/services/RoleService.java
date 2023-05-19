package ru.bazzar.auth.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.bazzar.api.ResourceNotFoundException;
import ru.bazzar.auth.entities.Role;
import ru.bazzar.auth.repositories.RoleRepository;

@Component
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public Role findRoleByName(String name) {
        return roleRepository.findRoleByName(name).orElseThrow(() -> new ResourceNotFoundException("Роль с id: " + name + " не найдена!"));
    }

}