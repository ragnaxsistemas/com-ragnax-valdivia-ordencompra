package com.ragnax.valdivia.ordencompra.infraestructura.repository.usuarios;

import com.ragnax.valdivia.ordencompra.infraestructura.entity.usuarios.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
}
