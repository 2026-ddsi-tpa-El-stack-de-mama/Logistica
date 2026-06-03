package ar.edu.utn.dds.k3003.model;

public class Paquete {
    private String id;
    private String donacionID;
    private String productos;
    private Integer cantidad;

    public Paquete(String id,String donacionID, String productos, Integer cantidad) {
        this.id = id;
        this.donacionID = donacionID;
        this.productos = productos;
        this.cantidad = cantidad;
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
