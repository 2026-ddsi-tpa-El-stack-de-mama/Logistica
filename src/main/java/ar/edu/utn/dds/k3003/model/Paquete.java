package ar.edu.utn.dds.k3003.model;

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
    private Deposito deposito;

    public Paquete(String id,String donacionID, String productos, Integer cantidad) {
        this.id = id;
        this.donacionID = donacionID;
        this.productos = productos;
        this.cantidad = cantidad;
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
}
