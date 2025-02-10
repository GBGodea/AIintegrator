package com.godea.authorization.services;

import com.godea.authorization.models.Role;
import com.godea.authorization.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class RoleService {
    @Autowired
    private RoleRepository roleRepository;

    public Role CreateRole(Role role) {
        return roleRepository.save(role);
    }

    public void removeRole(UUID id) {
        roleRepository.deleteById(id);
    }

    public Role getRole(UUID id) {
        return roleRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Role not found"));
    }
}
