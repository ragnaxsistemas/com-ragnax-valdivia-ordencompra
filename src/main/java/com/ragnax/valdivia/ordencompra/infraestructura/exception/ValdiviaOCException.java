package com.ragnax.valdivia.ordencompra.infraestructura.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValdiviaOCException extends RuntimeException implements Serializable{

	private static final long serialVersionUID = 1480116895834882204L;
	
	private String codigoValdiviaOCException;

}
