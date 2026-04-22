package com.ragnax.valdivia.ordencompra.infraestructura.repository.usuarios;

import com.ragnax.valdivia.ordencompra.infraestructura.entity.usuarios.Usuarios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuariosRepository extends JpaRepository<Usuarios, Integer> {
    Optional<Usuarios> findByUsername(String username);
    Optional<Usuarios> findByUsernameAndPassword(String username, String password);
}
