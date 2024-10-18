package com.sptv.mavenproject2;

import java.math.BigDecimal;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.swing.JOptionPane;
import modelo.*;


/**
 *
 * @author Oficina
 */
public class Addcliente extends javax.swing.JFrame {
public void agregarCliente() {
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("persisventas");
    EntityManager em = emf.createEntityManager();
    EntityTransaction tx = em.getTransaction();

    try {
        // Validaciones antes de comenzar la transacción
        String ruc = txtruc.getText();

        // 1. Verificar que el RUC tenga 11 dígitos
        if (ruc.length() != 11 || !ruc.matches("\\d+")) {
            JOptionPane.showMessageDialog(null, "El RUC debe tener exactamente 11 dígitos numéricos.");
            return; // Terminar si no cumple la validación
        }

        // 2. Verificar que todos los campos estén rellenos
        if (txtnombre.getText().isEmpty() || txtruc.getText().isEmpty() || 
            txtfechadeactividades.getText().isEmpty() || txtprimeradec.getText().isEmpty() || 
            txthonorarios.getText().isEmpty() || txtcorreo.getText().isEmpty() || 
            txttelefono.getText().isEmpty() || txtregimen.getText().isEmpty() || 
            txtusuarioclavesol.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Por favor, rellene todos los campos.");
            return; // Terminar si algún campo está vacío
        }

        // 3. Verificar que las fechas estén en el formato correcto (YYYY-MM-DD)
        try {
            java.sql.Date.valueOf(txtfechadeactividades.getText());
            java.sql.Date.valueOf(txtprimeradec.getText());
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(null, "Formato de fecha incorrecto. Use el formato año-mes-día (YYYY-MM-DD) si no es eso que la fecha sea correcta.");
            return; // Terminar si las fechas no son válidas
        }

        // 4. Verificar si el RUC ya existe en la base de datos
        TypedQuery<Long> query = em.createQuery("SELECT COUNT(c) FROM Cliente c WHERE c.ruc = :ruc", Long.class);
        query.setParameter("ruc", ruc);
        long count = query.getSingleResult();
        
        if (count > 0) {
            JOptionPane.showMessageDialog(null, "Ya existe un cliente con este RUC en la base de datos.");
            return; // Terminar si el RUC ya existe
        }

        // Iniciar la transacción
        tx.begin();

        // Crear nuevo cliente
        Cliente cliente = new Cliente();
        cliente.setNombre(txtnombre.getText());
        cliente.setRuc(txtruc.getText());
        cliente.setFechainicioact(java.sql.Date.valueOf(txtfechadeactividades.getText())); 
        cliente.setPrimeradec(java.sql.Date.valueOf(txtprimeradec.getText())); 
        cliente.setHonorarios(new BigDecimal(txthonorarios.getText()));
        cliente.setNumerodecliente(txttelefono.getText());
        cliente.setCorreo(txtcorreo.getText());
        cliente.setTelefono(txttelefono.getText());
        cliente.setRegimen(txtregimen.getText());

        // Persistir cliente
        em.persist(cliente);

        // Crear Clavesol y asociar el cliente
        Clavesol clavesol = new Clavesol();
        clavesol.setUsuario(txtusuarioclavesol.getText());
        clavesol.setCliente(cliente); // Asociar cliente

        // Persistir Clavesol
        em.persist(clavesol);

        // Crear un nuevo objeto Libro y asociar el cliente y clavesol
        Libro libro = new Libro();
        libro.setRegistrodeventas(Libro.Estado.valueOf(checboxregistrodeventas.isSelected() ? "ACTIVO" : "INACTIVO"));
        libro.setRegistrodecompras(Libro.Estado.valueOf(checboxregistrodecompras.isSelected() ? "ACTIVO" : "INACTIVO"));
        libro.setDiariodeformatosimplificado(Libro.Estado.valueOf(checboxdiariocompras.isSelected() ? "ACTIVO" : "INACTIVO"));
        libro.setCliente(cliente);
        libro.setClavesol(clavesol); // Asociar clavesol
        libro.setRuc(cliente.getRuc()); // Asocia el RUC del cliente al libro

        // Persistir el libro
        em.persist(libro);

        // Crear un nuevo objeto Cobranza y asociar el cliente
        Cobranza cobranza = new Cobranza();
        cobranza.setCliente(cliente);
        cobranza.setEstadomensual(true); // Ajusta según sea necesario
        cobranza.setEstadoanual(false); // Ajusta según sea necesario
        cobranza.setEstadototal(true); // Ajusta según sea necesario
        cobranza.setRuc(cliente.getRuc()); // Asociar el RUC del cliente a la cobranza

        // Persistir la cobranza
        em.persist(cobranza);

        // Crear pagos mensuales para cada mes y año entre 2024 y 2030
        for (int mes = 1; mes <= 12; mes++) {
            for (int anio = 2024; anio <= 2030; anio++) {
                PagoMensual pagoMensual = new PagoMensual();
                pagoMensual.setCliente(cliente);
                pagoMensual.setAño(anio); // Establecer el año
                pagoMensual.setMes(mes);  // Establecer el mes
                pagoMensual.setMonto(0.0); // Inicializar el monto en 0.0
                
                // Persistir el pago mensual
                em.persist(pagoMensual);
            }
        }

        // Confirmar la transacción
        tx.commit();

        JOptionPane.showMessageDialog(null, "Cliente y datos asociados agregados exitosamente");

    } catch (Exception e) {
        if (tx != null && tx.isActive()) {
            // Hacer rollback en caso de error
            tx.rollback();
        }
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Error al agregar cliente: " + e.getMessage());
    } finally {
        em.close();
        emf.close();
    }
}


    /**
     * Creates new form Addcliente
     */
    public Addcliente() {
        initComponents();
        
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        btnVolveraCasa = new javax.swing.JButton();
        txtnombre = new javax.swing.JTextField();
        txtruc = new javax.swing.JTextField();
        txtregimen = new javax.swing.JTextField();
        txtfechadeactividades = new javax.swing.JTextField();
        lblTitulo = new javax.swing.JLabel();
        txtprimeradec = new javax.swing.JTextField();
        lblNombre = new javax.swing.JLabel();
        lblRuc = new javax.swing.JLabel();
        lblRegimen = new javax.swing.JLabel();
        lblFechadeactividades = new javax.swing.JLabel();
        lblPeriododeprimeradec = new javax.swing.JLabel();
        lblHonorarios = new javax.swing.JLabel();
        lblCorreo = new javax.swing.JLabel();
        txthonorarios = new javax.swing.JTextField();
        txtcorreo = new javax.swing.JTextField();
        lblTelefono = new javax.swing.JLabel();
        txttelefono = new javax.swing.JTextField();
        lblSubTituloClavesol = new javax.swing.JLabel();
        txtusuarioclavesol = new javax.swing.JTextField();
        lblUsuarioclave = new javax.swing.JLabel();
        lblSubTituloLibros = new javax.swing.JLabel();
        btnagregar = new javax.swing.JButton();
        lblRegistrodeventas = new javax.swing.JLabel();
        lblregistrodecompras = new javax.swing.JLabel();
        lblDiariodecomprassimplificado = new javax.swing.JLabel();
        checboxregistrodecompras = new javax.swing.JCheckBox();
        checboxregistrodeventas = new javax.swing.JCheckBox();
        checboxdiariocompras = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(0, 255, 204));

        btnVolveraCasa.setBackground(new java.awt.Color(153, 255, 153));
        btnVolveraCasa.setText("Volver a la pestaña pricipal");
        btnVolveraCasa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVolveraCasaActionPerformed(evt);
            }
        });

        txtnombre.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtnombreActionPerformed(evt);
            }
        });

        txtruc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtrucActionPerformed(evt);
            }
        });

        txtregimen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtregimenActionPerformed(evt);
            }
        });

        txtfechadeactividades.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtfechadeactividadesActionPerformed(evt);
            }
        });

        lblTitulo.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
        lblTitulo.setText("CONSULTORIA EMPRESARIAL SERVISERC S.A.C");

        txtprimeradec.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtprimeradecActionPerformed(evt);
            }
        });

        lblNombre.setBackground(new java.awt.Color(0, 0, 0));
        lblNombre.setText("Nombre: ");

        lblRuc.setText("RUC: ");

        lblRegimen.setText("Regimen");

        lblFechadeactividades.setText("Fecha de actividades:");

        lblPeriododeprimeradec.setText("Periodo de servicio de Primera declaracion:");

        lblHonorarios.setText("Honorarios");

        lblCorreo.setText("Correo electronico");

        txthonorarios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txthonorariosActionPerformed(evt);
            }
        });

        txtcorreo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtcorreoActionPerformed(evt);
            }
        });

        lblTelefono.setText("Telefono de contacto:");

        txttelefono.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txttelefonoActionPerformed(evt);
            }
        });

        lblSubTituloClavesol.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        lblSubTituloClavesol.setText("CLAVE SOL");

        txtusuarioclavesol.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtusuarioclavesolActionPerformed(evt);
            }
        });

        lblUsuarioclave.setText("Usuario:");

        lblSubTituloLibros.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        lblSubTituloLibros.setText("LIBROS");

        btnagregar.setBackground(new java.awt.Color(51, 255, 51));
        btnagregar.setText("agregar");
        btnagregar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnagregarActionPerformed(evt);
            }
        });

        lblRegistrodeventas.setText("Registro de ventas:");

        lblregistrodecompras.setText("Registro de compras:");

        lblDiariodecomprassimplificado.setText("Diario de compras simplificado:");

        checboxregistrodecompras.setText("si");

        checboxregistrodeventas.setText("si");

        checboxdiariocompras.setText("si");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(16, 16, 16)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(lblNombre)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtnombre))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(lblRuc, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtruc, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(lblRegimen)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtregimen, javax.swing.GroupLayout.PREFERRED_SIZE, 246, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(91, 91, 91)
                                .addComponent(lblTitulo))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(16, 16, 16)
                                .addComponent(lblFechadeactividades)
                                .addGap(18, 18, 18)
                                .addComponent(txtfechadeactividades, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(16, 16, 16)
                                .addComponent(lblPeriododeprimeradec)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtprimeradec, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(16, 16, 16)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(lblHonorarios)
                                        .addGap(18, 18, 18)
                                        .addComponent(txthonorarios, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(lblCorreo)
                                        .addGap(18, 18, 18)
                                        .addComponent(txtcorreo, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addGap(0, 12, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblRegistrodeventas)
                                    .addComponent(lblDiariodecomprassimplificado)
                                    .addComponent(lblregistrodecompras))
                                .addGap(126, 126, 126)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(checboxregistrodeventas)
                                    .addComponent(checboxregistrodecompras)
                                    .addComponent(checboxdiariocompras))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 165, Short.MAX_VALUE)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btnagregar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnVolveraCasa, javax.swing.GroupLayout.Alignment.TRAILING)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(lblTelefono)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txttelefono, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(lblSubTituloClavesol)
                            .addComponent(lblSubTituloLibros)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(lblUsuarioclave)
                                .addGap(34, 34, 34)
                                .addComponent(txtusuarioclavesol, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(lblTitulo)
                .addGap(6, 6, 6)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblNombre)
                    .addComponent(txtnombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblRuc, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtruc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblRegimen)
                    .addComponent(txtregimen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblFechadeactividades)
                    .addComponent(txtfechadeactividades, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblPeriododeprimeradec)
                    .addComponent(txtprimeradec, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblHonorarios)
                    .addComponent(txthonorarios, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCorreo)
                    .addComponent(txtcorreo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTelefono)
                    .addComponent(txttelefono, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(1, 1, 1)
                .addComponent(lblSubTituloClavesol)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblUsuarioclave, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtusuarioclavesol, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblSubTituloLibros)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblRegistrodeventas)
                            .addComponent(checboxregistrodeventas))
                        .addGap(4, 4, 4)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblregistrodecompras)
                            .addComponent(checboxregistrodecompras))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblDiariodecomprassimplificado)
                            .addComponent(checboxdiariocompras))
                        .addGap(59, 59, 59))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnagregar, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnVolveraCasa)
                        .addGap(61, 61, 61))))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnVolveraCasaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVolveraCasaActionPerformed
        this.dispose();

        // Abrir la nueva ventana (PestañaPrincipal)
        PestañaPrincipal nuevaVentana = new PestañaPrincipal();
        nuevaVentana.setVisible(true);
    }//GEN-LAST:event_btnVolveraCasaActionPerformed

    private void txtnombreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtnombreActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtnombreActionPerformed

    private void txtrucActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtrucActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtrucActionPerformed

    private void txtregimenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtregimenActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtregimenActionPerformed

    private void txtfechadeactividadesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtfechadeactividadesActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtfechadeactividadesActionPerformed

    private void txtprimeradecActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtprimeradecActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtprimeradecActionPerformed

    private void txthonorariosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txthonorariosActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txthonorariosActionPerformed

    private void txtcorreoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtcorreoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtcorreoActionPerformed

    private void txttelefonoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txttelefonoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txttelefonoActionPerformed

    private void txtusuarioclavesolActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtusuarioclavesolActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtusuarioclavesolActionPerformed

    private void btnagregarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnagregarActionPerformed

        agregarCliente();

    }//GEN-LAST:event_btnagregarActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Addcliente.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Addcliente.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Addcliente.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Addcliente.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Addcliente().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnVolveraCasa;
    private javax.swing.JButton btnagregar;
    private javax.swing.JCheckBox checboxdiariocompras;
    private javax.swing.JCheckBox checboxregistrodecompras;
    private javax.swing.JCheckBox checboxregistrodeventas;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblCorreo;
    private javax.swing.JLabel lblDiariodecomprassimplificado;
    private javax.swing.JLabel lblFechadeactividades;
    private javax.swing.JLabel lblHonorarios;
    private javax.swing.JLabel lblNombre;
    private javax.swing.JLabel lblPeriododeprimeradec;
    private javax.swing.JLabel lblRegimen;
    private javax.swing.JLabel lblRegistrodeventas;
    private javax.swing.JLabel lblRuc;
    private javax.swing.JLabel lblSubTituloClavesol;
    private javax.swing.JLabel lblSubTituloLibros;
    private javax.swing.JLabel lblTelefono;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblUsuarioclave;
    private javax.swing.JLabel lblregistrodecompras;
    private javax.swing.JTextField txtcorreo;
    private javax.swing.JTextField txtfechadeactividades;
    private javax.swing.JTextField txthonorarios;
    private javax.swing.JTextField txtnombre;
    private javax.swing.JTextField txtprimeradec;
    private javax.swing.JTextField txtregimen;
    private javax.swing.JTextField txtruc;
    private javax.swing.JTextField txttelefono;
    private javax.swing.JTextField txtusuarioclavesol;
    // End of variables declaration//GEN-END:variables
}
