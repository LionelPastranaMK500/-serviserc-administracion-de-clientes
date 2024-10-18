package modelo;

import java.io.Serializable;
import javax.persistence.*;
import java.math.BigDecimal;
import modelo.Cliente;

@Entity
@Table(name = "PagoMensual")
public class PagoMensual implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idpago")
    private Long idpago;

    @ManyToOne
    @JoinColumn(name = "idcliente", nullable = false)
    private Cliente cliente;

    @Column(name = "año", nullable = false)
    private Integer año;

    @Column(name = "mes", nullable = false)
    private Integer mes;

    @Column(name = "monto", nullable = false)
    private double monto;

    // Getters y Setters

    public Long getIdpago() {
        return idpago;
    }

    public void setIdpago(Long idpago) {
        this.idpago = idpago;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Integer getAño() {
        return año;
    }

    public void setAño(Integer año) {
        this.año = año;
    }

    public Integer getMes() {
        return mes;
    }

    public void setMes(Integer mes) {
        this.mes = mes;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(Double monto) {
        this.monto = monto;
    }}