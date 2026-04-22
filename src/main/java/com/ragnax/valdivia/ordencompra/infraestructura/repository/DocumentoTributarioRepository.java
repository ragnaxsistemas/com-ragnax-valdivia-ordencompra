package com.ragnax.valdivia.ordencompra.infraestructura.repository;

import com.ragnax.valdivia.ordencompra.infraestructura.entity.DocumentoTributario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentoTributarioRepository extends JpaRepository<DocumentoTributario, Integer> {

    Optional<DocumentoTributario> findByCodigoDocumentoTributario(String codigoDocumentoTributario);
}
