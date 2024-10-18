package modelo;

import java.io.Serializable;
import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

@Entity
@Table(name = "Cliente")
public class Cliente implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idcliente")
    private Long idcliente;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "ruc", unique = true, length = 11)
    private String ruc;

    @Column(name = "fechainicioact")
    private Date fechainicioact;

    @Column(name = "primeradec")
    private Date primeradec;

    @Column(name = "honorarios")
    private BigDecimal honorarios;

    @Column(name = "numerodecliente", length = 30)
    private String numerodecliente;

    @Column(name = "correo", length = 30)
    private String correo;

    @Column(name = "telefono", length = 25)
    private String telefono;

    @Column(name = "regimen", length = 40)
    private String regimen;

    @Column(name = "fechadeingreso", nullable = false, updatable = false)
    private Timestamp fechadeingreso;
    

    // Constructor sin argumentos
    public Cliente() {
        // Inicializa automáticamente fechadeingreso con la fecha y hora actual al crear un nuevo cliente
        this.fechadeingreso = new Timestamp(System.currentTimeMillis());
    }

    // Getters y Setters
    public Long getIdcliente() {
        return idcliente;
    }

    public void setIdcliente(Long idcliente) {
        this.idcliente = idcliente;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public Date getFechainicioact() {
        return fechainicioact;
    }

    public void setFechainicioact(Date fechainicioact) {
        this.fechainicioact = fechainicioact;
    }

    public Date getPrimeradec() {
        return primeradec;
    }

    public void setPrimeradec(Date primeradec) {
        this.primeradec = primeradec;
    }

    public BigDecimal getHonorarios() {
        return honorarios;
    }

    public void setHonorarios(BigDecimal honorarios) {
        this.honorarios = honorarios;
    }

    public String getNumerodecliente() {
        return numerodecliente;
    }

    public void setNumerodecliente(String numerodecliente) {
        this.numerodecliente = numerodecliente;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getRegimen() {
        return regimen;
    }

    public void setRegimen(String regimen) {
        this.regimen = regimen;
    }

    public Timestamp getFechadeingreso() {
        return fechadeingreso;
    }

    public void setFechadeingreso(Timestamp fechadeingreso) {
        this.fechadeingreso = fechadeingreso;
    }
}
