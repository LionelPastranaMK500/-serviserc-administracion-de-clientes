
package com.sptv.mavenproject2;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import modelo.Cliente;
import modelo.PagoMensual;

/**
 *
 * @author Oficina
 */
public class Tabladedepagos extends javax.swing.JFrame {
    private DefaultTableModel model;
private void MostrarPagosPorAño(int year) {
    String sqlPagos = "SELECT p FROM PagoMensual p WHERE p.año = :year";

    model = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            // Permitir editar solo las columnas de los meses
            return column >= 3; // Columnas de Enero (índice 3) en adelante son editables
        }
    };
    model = new DefaultTableModel();
    model.addColumn("ID Cliente");
    model.addColumn("RUC");
    model.addColumn("Nombre");
    model.addColumn("Enero");
    model.addColumn("Febrero");
    model.addColumn("Marzo");
    model.addColumn("Abril");
    model.addColumn("Mayo");
    model.addColumn("Junio");
    model.addColumn("Julio");
    model.addColumn("Agosto");
    model.addColumn("Septiembre");
    model.addColumn("Octubre");
    model.addColumn("Noviembre");
    model.addColumn("Diciembre");

    EntityManagerFactory emf = Persistence.createEntityManagerFactory("persisventas");
    EntityManager em = emf.createEntityManager();

    try {
        // Consultar pagos mensuales
        javax.persistence.Query queryPagos = em.createQuery(sqlPagos);
        queryPagos.setParameter("year", year);
        List<PagoMensual> pagos = queryPagos.getResultList();

        // Crear un mapa para almacenar los pagos por cliente
        Map<Long, Object[]> clientePagos = new HashMap<>();

        // Procesar los pagos
        for (PagoMensual pago : pagos) {
            Long idCliente = pago.getCliente().getIdcliente();
            Object[] row = clientePagos.get(idCliente);
            if (row == null) {
                Cliente cliente = em.find(Cliente.class, idCliente);
                row = new Object[]{
                    cliente.getIdcliente(),
                    cliente.getRuc(),
                    cliente.getNombre(),
                    0.0, // Enero
                    0.0, // Febrero
                    0.0, // Marzo
                    0.0, // Abril
                    0.0, // Mayo
                    0.0, // Junio
                    0.0, // Julio
                    0.0, // Agosto
                    0.0, // Septiembre
                    0.0, // Octubre
                    0.0, // Noviembre
                    0.0  // Diciembre
                };
                clientePagos.put(idCliente, row);
            }
            int monthIndex = pago.getMes() - 1;
            row[3 + monthIndex] = pago.getMonto();
        }

        // Añadir filas al modelo
        for (Object[] row : clientePagos.values()) {
            model.addRow(row);
        }

        // Establecer el modelo en la tabla
        tbldeudas.setModel(model);

        // Añadir el listener para detectar cambios en las celdas
        model.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE) {
                    int row = e.getFirstRow();
                    int column = e.getColumn();
                    if (column >= 3) { // Solo permitir editar meses
                        Long idCliente = (Long) model.getValueAt(row, 0);
                        int mes = column - 2;
                        
                        // Obtener el valor editado de la tabla
                        Object value = model.getValueAt(row, column);
                        
                        // Verificar si el valor es numérico antes de intentar convertirlo
                        try {
                            double monto = Double.parseDouble(value.toString());

                            // Guardar cambios en la base de datos
                            int year = Integer.parseInt(cbxcambiodeaños.getSelectedItem().toString());
                            actualizarPagoMensual(idCliente, mes, year, monto);
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(null, "Por favor, ingresa un número válido para el monto.");
                        }
                    }
                }
            }
        });
    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Error al mostrar datos: " + e.getMessage());
    } finally {
        em.close();
        emf.close();
    }
}



private void actualizarPagoMensual(Long idCliente, int mes, int año, double monto) {
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("persisventas");
    EntityManager em = emf.createEntityManager();

    try {
        em.getTransaction().begin();

        // Buscar el registro existente
        PagoMensual pago;
        try {
            pago = em.createQuery("SELECT p FROM PagoMensual p WHERE p.cliente.idcliente = :idCliente AND p.año = :año AND p.mes = :mes", PagoMensual.class)
                     .setParameter("idCliente", idCliente)
                     .setParameter("año", año)
                     .setParameter("mes", mes)
                     .getSingleResult();
            // Actualizar el monto si existe
            pago.setMonto(monto);
            em.merge(pago);
        } catch (NoResultException e) {
            // Si no existe, crear un nuevo registro
            Cliente cliente = em.find(Cliente.class, idCliente);
            if (cliente != null) {
                pago = new PagoMensual();
                pago.setCliente(cliente);
                pago.setAño(año);
                pago.setMes(mes);
                pago.setMonto(monto);
                em.persist(pago);
            } else {
                JOptionPane.showMessageDialog(null, "Cliente no encontrado.");
                return;
            }
        }

        em.getTransaction().commit();

    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Error al actualizar pago: " + e.getMessage());
    } finally {
        em.close();
        emf.close();
    }
}

private void actualizarNombreCliente(Long idCliente, String nuevoNombre) {
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("persisventas");
    EntityManager em = emf.createEntityManager();

    try {
        em.getTransaction().begin();

        // Buscar el cliente existente
        Cliente cliente = em.find(Cliente.class, idCliente);
        if (cliente != null) {
            // Actualizar el nombre del cliente
            cliente.setNombre(nuevoNombre);
            em.merge(cliente);
            em.getTransaction().commit();
        } else {
            JOptionPane.showMessageDialog(null, "Cliente no encontrado.");
        }

    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Error al actualizar nombre del cliente: " + e.getMessage());
    } finally {
        em.close();
        emf.close();
    }
}
    /**
     * Creates new form Tabladedeudaores
     */
    public Tabladedepagos() {
        initComponents();
        MostrarPagosPorAño(2024);

        tbldeudas.addKeyListener(new KeyAdapter() {
        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                int row = tbldeudas.getSelectedRow();
                int column = tbldeudas.getSelectedColumn();
                if (column >= 3) { // Meses de enero a diciembre
                    Long idCliente = (Long) model.getValueAt(row, 0);
                    int mes = column - 2;

                    // Obtener el valor editado de la tabla
                    Object value = model.getValueAt(row, column);

                    // Verificar y convertir el valor a Double
                    try {
                        double monto = Double.parseDouble(value.toString());
                        int year = Integer.parseInt(cbxcambiodeaños.getSelectedItem().toString());

                        actualizarPagoMensual(idCliente, mes, year, monto);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, "Por favor, ingresa un número válido para el monto.");
                    }
                }
            }
        }
    });
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
        cbxcambiodeaños = new javax.swing.JComboBox<>();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbldeudas = new javax.swing.JTable();
        btnregresarapestañaprincipal = new javax.swing.JButton();
        btnbuscar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(0, 255, 204));

        cbxcambiodeaños.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "2024", "2025", "2026", "2027", "2028", "2029", "2030" }));
        cbxcambiodeaños.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxcambiodeañosActionPerformed(evt);
            }
        });

        tbldeudas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Ruc", "Nombre", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Nobiembre", "Diciembre"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Long.class, java.lang.Long.class, java.lang.String.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, true, true, true, true, true, true, true, true, true, true, true, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tbldeudas.setToolTipText("");
        jScrollPane1.setViewportView(tbldeudas);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 978, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 432, Short.MAX_VALUE)
        );

        btnregresarapestañaprincipal.setText("regresar a inicio");
        btnregresarapestañaprincipal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnregresarapestañaprincipalActionPerformed(evt);
            }
        });

        btnbuscar.setText("buscar");
        btnbuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnbuscarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(btnbuscar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnregresarapestañaprincipal)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbxcambiodeaños, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbxcambiodeaños, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnregresarapestañaprincipal, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnbuscar)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnbuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnbuscarActionPerformed
        this.dispose();
            
            // Abrir la nueva ventana (PestañaPrincipal)
        BuscarporRuc nuevaVentana = new BuscarporRuc();
        nuevaVentana.setVisible(true);
    }//GEN-LAST:event_btnbuscarActionPerformed

    private void btnregresarapestañaprincipalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnregresarapestañaprincipalActionPerformed
        this.dispose();
            
            // Abrir la nueva ventana (PestañaPrincipal)
         PestañaPrincipal nuevaVentana = new PestañaPrincipal();
        nuevaVentana.setVisible(true);
    }//GEN-LAST:event_btnregresarapestañaprincipalActionPerformed

    private void cbxcambiodeañosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxcambiodeañosActionPerformed
Object selectedItem = cbxcambiodeaños.getSelectedItem();
        if (selectedItem != null) {
            String selectedYearText = selectedItem.toString();
            try {
                int selectedYear = Integer.parseInt(selectedYearText);
                MostrarPagosPorAño(selectedYear);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "El año seleccionado no es válido: " + selectedYearText);
            }
        }
    }//GEN-LAST:event_cbxcambiodeañosActionPerformed

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
            java.util.logging.Logger.getLogger(Tabladedepagos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Tabladedepagos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Tabladedepagos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Tabladedepagos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new Tabladedepagos().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnbuscar;
    private javax.swing.JButton btnregresarapestañaprincipal;
    private javax.swing.JComboBox<String> cbxcambiodeaños;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tbldeudas;
    // End of variables declaration//GEN-END:variables
}
