package ar.edu.utn.dds.k3003.model;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.TipoAlgoritmoEnum;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.List;

@Entity
@Table(name = "depositos") //SEGUIR
public class Deposito {

    private String id;
    private String nombre;
    private TipoAlgoritmoEnum algoritmo;
    private String direccion;
    private Integer capacidadMaxima;
    private List<Paquete> stockActual;

    public Deposito(String id, String nombre, TipoAlgoritmoEnum algoritmo, String direccion, Integer capacidadMaxima, List<Paquete> stockActual){
        this.id = id;
        this.nombre = nombre;
        this.algoritmo = algoritmo;
        this.direccion = direccion;
        this.capacidadMaxima = capacidadMaxima;
        this.stockActual = stockActual;
    }

    //GETTER Y SETTERS
    //ID
    public String getId() {return id;}

    public void setId(String id) {this.id = id;}
    //Nombre
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    //Algoritmo
    public TipoAlgoritmoEnum getAlgoritmo() {
        return algoritmo;
    }
    public void setAlgoritmo(TipoAlgoritmoEnum algoritmo) {
        this.algoritmo = algoritmo;
    }
    //Dirección
    public String getDireccion() {
        return direccion;
    }
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
    //Capacidad máxima
    public Integer getCapacidadMaxima() {
        return capacidadMaxima;
    }
    public void setCapacidadMaxima(Integer capacidadMaxima) {
        this.capacidadMaxima = capacidadMaxima;
    }
    //Stock actual
    public List<Paquete> getStockActual() {
        return stockActual;
    }
    public void setStockActual(List<Paquete> stockActual) {
        this.stockActual = stockActual;
    }
}

