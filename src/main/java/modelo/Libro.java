package modelo;

import java.io.Serializable;
import javax.persistence.*;

@Entity
@Table(name = "Libro")
public class Libro implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idlibros")
    private Long idlibros;

    @Enumerated(EnumType.STRING)
    @Column(name = "registrodeventas", nullable = false)
    private Estado registrodeventas;

    @Enumerated(EnumType.STRING)
    @Column(name = "registrodecompras", nullable = false)
    private Estado registrodecompras;

    @Enumerated(EnumType.STRING)
    @Column(name = "diariodeformatosimplificado", nullable = false)
    private Estado diariodeformatosimplificado;

    @ManyToOne
    @JoinColumn(name = "idcliente", referencedColumnName = "idcliente", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "idclavesol", referencedColumnName = "idClavesol", nullable = false)
    private Clavesol clavesol;

    @Column(name = "ruc", nullable = false)
    private String ruc; // Nuevo atributo para almacenar el RUC

    // Getters and setters
    public Long getIdlibros() {
        return idlibros;
    }

    public void setIdlibros(Long idlibros) {
        this.idlibros = idlibros;
    }

    public Estado getRegistrodeventas() {
        return registrodeventas;
    }

    public void setRegistrodeventas(Estado registrodeventas) {
        this.registrodeventas = registrodeventas;
    }

    public Estado getRegistrodecompras() {
        return registrodecompras;
    }

    public void setRegistrodecompras(Estado registrodecompras) {
        this.registrodecompras = registrodecompras;
    }

    public Estado getDiariodeformatosimplificado() {
        return diariodeformatosimplificado;
    }

    public void setDiariodeformatosimplificado(Estado diariodeformatosimplificado) {
        this.diariodeformatosimplificado = diariodeformatosimplificado;
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

    public Clavesol getClavesol() {
        return clavesol;
    }

    public void setClavesol(Clavesol clavesol) {
        this.clavesol = clavesol;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public enum Estado {
        ACTIVO,
        INACTIVO
    }
}