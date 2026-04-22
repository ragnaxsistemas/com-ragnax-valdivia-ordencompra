package com.ragnax.valdivia.ordencompra.infraestructura.repository.usuarios;

import com.ragnax.valdivia.ordencompra.infraestructura.entity.usuarios.MenuRol;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.usuarios.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MenuRolRepository extends JpaRepository<MenuRol, Integer> {
    List<MenuRol> findByRole(Role role);
}
