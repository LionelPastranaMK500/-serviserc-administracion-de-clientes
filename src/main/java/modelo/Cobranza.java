package modelo;

import java.io.Serializable;
import javax.persistence.*;

@Entity
@Table(name = "Cobranza")
public class Cobranza implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idcobranza")
    private Long idcobranza;

    @ManyToOne
    @JoinColumn(name = "idcliente", nullable = false)
    private Cliente cliente;

    @Column(name = "estadomensual", nullable = false)
    private Boolean estadomensual;

    @Column(name = "estadoanual", nullable = false)
    private Boolean estadoanual;

    @Column(name = "estadototal", nullable = false)
    private Boolean estadototal;

    @Column(name = "ruc", nullable = false)
    private String ruc; // Nuevo atributo para almacenar el RUC

    // Getters y Setters

    public Long getIdcobranza() {
        return idcobranza;
    }

    public void setIdcobranza(Long idcobranza) {
        this.idcobranza = idcobranza;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
        if (cliente != null) {
            this.ruc = cliente.getRuc(); // Establece el RUC basado en el cliente
        }
    }

    public Boolean getEstadomensual() {
        return estadomensual;
    }

    public void setEstadomensual(Boolean estadomensual) {
        this.estadomensual = estadomensual;
    }

    public Boolean getEstadoanual() {
        return estadoanual;
    }

    public void setEstadoanual(Boolean estadoanual) {
        this.estadoanual = estadoanual;
    }

    public Boolean getEstadototal() {
        return estadototal;
    }

    public void setEstadototal(Boolean estadototal) {
        this.estadototal = estadototal;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }
}