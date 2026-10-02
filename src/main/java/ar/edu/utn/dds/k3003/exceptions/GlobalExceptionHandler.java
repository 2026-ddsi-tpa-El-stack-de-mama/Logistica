package ar.edu.utn.dds.k3003.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DepositoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> depositoNoEncontrado(DepositoNoEncontradoException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Deposito no encontrado", e.getMessage()));
    }

    @ExceptionHandler(PaqueteNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> paqueteNoEncontrado(PaqueteNoEncontradoException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Paquete no encontrado", e.getMessage()));
    }

    @ExceptionHandler(AsignacionNoEncontradaException.class)
    public ResponseEntity<ErrorResponse> asignacionNoEncontrada(AsignacionNoEncontradaException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Asignacion no encontrada", e.getMessage()));
    }

    @ExceptionHandler(DepositoSinEspacioException.class)
    public ResponseEntity<ErrorResponse> depositoSinEspacio(DepositoSinEspacioException e){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("Deposito sin espacio", e.getMessage()));
    }

    @ExceptionHandler(AsignacionEntregadaException.class)
    public ResponseEntity<ErrorResponse> asignacionEntregada(AsignacionEntregadaException e){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("Asignacion entregada", e.getMessage()));
    }

    @ExceptionHandler(NecesidadNoCompatible.class)
    public ResponseEntity<ErrorResponse> necesidadNoCompatible(NecesidadNoCompatible e){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("Necesidad no compatible", e.getMessage()));
    }

    public record ErrorResponse(String error,String mensaje) {}

}
