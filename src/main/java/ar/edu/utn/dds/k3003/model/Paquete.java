package ar.edu.utn.dds.k3003.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "paquetes")
public class Paquete {
    @Id
    private String id;
    private String donacionID;
    private String productos;
    private Integer cantidad;

    @ManyToOne
    @JoinColumn(name = "deposito_id")
    @JsonIgnore
    private Deposito deposito;

    public Paquete(String id, String donacionID, String productos, Integer cantidad, Deposito deposito) {
        this.id = id;
        this.donacionID = donacionID;
        this.productos = productos;
        this.cantidad = cantidad;
        this.deposito = deposito;
    }

    protected Paquete() {
    }

    //GETTER Y SETTER
    //ID
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    //Donacion ID
    public String getDonacionID() {
        return donacionID;
    }
    public void setDonacionID(String donacionID) {
        this.donacionID = donacionID;
    }
    //Productos
    public String getProductos() {
        return productos;
    }
    public void setProductos(String productos) {
        this.productos = productos;
    }
    //Cantidad
    public Integer getCantidad() {
        return cantidad;
    }
    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Deposito getDeposito() {return deposito;}
    public void setDeposito(Deposito deposito) {this.deposito = deposito;}
}
