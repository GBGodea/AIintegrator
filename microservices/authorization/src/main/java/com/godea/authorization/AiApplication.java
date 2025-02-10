package com.godea.authorization;

import com.godea.authorization.config.Constants;
import com.godea.authorization.models.Role;
import com.godea.authorization.repositories.RoleRepository;
import com.godea.authorization.services.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AiApplication implements CommandLineRunner {
	@Autowired
	private RoleRepository roleRepository;

	public static void main(String[] args) {
		SpringApplication.run(AiApplication.class, args);
	}

	@Override
	public void run(String ...args) {
		if(roleRepository.count() == 0) {
			roleRepository.save(Role.builder().name(Constants.Roles.USER).build());
			roleRepository.save(Role.builder().name(Constants.Roles.ADMIN).build());
		}
	}
}
