/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.sptv.mavenproject2;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import modelo.Cliente;

/**
 *
 * @author Oficina
 */
public class Tabladeclientes extends javax.swing.JFrame {

    private EntityManager em;
    private DefaultTableModel tableModel;

    public Tabladeclientes() {
        initComponents();
        // Inicializar el EntityManager
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("persisventas"); // Cambia "persisventas" por tu unidad de persistencia
        em = emf.createEntityManager();
        cargarDatos();
        
        // Agregar el TableModelListener
        tableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE) {
                    int row = e.getFirstRow();
                    int column = e.getColumn();
                    // Solo guardar cambios si la columna es editable
                    if (column >= 2 && column <= 6) {
                        // Actualizar el cliente cuando se cambia una celda editable
                        actualizarCliente(row);
                    }
                }
            }
        });

        // Establecer el TableCellEditor personalizado
        tblclientes.setDefaultEditor(Object.class, new CustomTableCellEditor());
    }

    private void cargarDatos() {
    // Definir los nombres de las columnas
    String[] columnNames = {"ID", "Nombre", "Correo", "Honorarios", "Número de Cliente", "Régimen", "Teléfono", "Fecha de Ingreso", "Fecha de Inicio", "RUC", "Primeradec"};

    // Crear el modelo de la tabla con columnas editables
    tableModel = new DefaultTableModel(columnNames, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            // Permitir edición en ciertas columnas
            return column == 1 || column == 2 || column == 3 || column == 4 || column == 5 || column == 6;
        }
    };
    tblclientes.setModel(tableModel);

    // Crear un formateador de fecha
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    // Cargar datos desde la base de datos
    Query query = em.createQuery("SELECT c FROM Cliente c");
    List<Cliente> clientes = query.getResultList();

    for (Cliente cliente : clientes) {
        tableModel.addRow(new Object[]{
            cliente.getIdcliente(),
            cliente.getNombre(),
            cliente.getCorreo(),
            cliente.getHonorarios(),
            cliente.getNumerodecliente(),
            cliente.getRegimen(),
            cliente.getTelefono(),
            cliente.getFechadeingreso() != null ? dateFormat.format(cliente.getFechadeingreso()) : null,
            cliente.getFechainicioact() != null ? dateFormat.format(cliente.getFechainicioact()) : null,
            cliente.getRuc(),
            cliente.getPrimeradec() != null ? dateFormat.format(cliente.getPrimeradec()) : null
        });
    }
}

    private void actualizarCliente(int row) {
    Long id = (Long) tableModel.getValueAt(row, 0);
    String nombre = (String) tableModel.getValueAt(row, 1);
    String correo = (String) tableModel.getValueAt(row, 2);
    // Manejar la conversión de honorarios desde BigDecimal
    BigDecimal honorarios = null;
    Object honorariosObj = tableModel.getValueAt(row, 3);
    if (honorariosObj instanceof BigDecimal) {
        honorarios = (BigDecimal) honorariosObj;
    } else if (honorariosObj instanceof String) {
        try {
            honorarios = new BigDecimal((String) honorariosObj);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error en el formato de honorarios. Debe ser un número decimal.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    }
    String numerodecliente = (String) tableModel.getValueAt(row, 4);
    String regimen = (String) tableModel.getValueAt(row, 5);
    String telefono = (String) tableModel.getValueAt(row, 6);

    // Buscar el cliente en la base de datos
    Cliente cliente = em.find(Cliente.class, id);
    if (cliente != null) {
        em.getTransaction().begin();
        cliente.setNombre(nombre);
        cliente.setCorreo(correo);
        cliente.setHonorarios(honorarios);
        cliente.setNumerodecliente(numerodecliente);
        cliente.setRegimen(regimen);
        cliente.setTelefono(telefono);
        em.getTransaction().commit();
    } else {
        System.out.println("Cliente no encontrado");
    }
}

private class CustomTableCellEditor extends javax.swing.DefaultCellEditor {
    public CustomTableCellEditor() {
        super(new javax.swing.JTextField());
    }

    @Override
    public boolean stopCellEditing() {
        int row = tblclientes.getEditingRow();
        int column = tblclientes.getEditingColumn();
        if (row >= 0 && column >= 0) {
            // Actualizar el cliente cuando se cambia una celda editable
            if (column >= 2 && column <= 6) {
                actualizarCliente(row);
            }
        }
        return super.stopCellEditing();
    }
}


    /**
     * Creates new form Tabladeclientes
     */

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jComboBox1 = new javax.swing.JComboBox<>();
        jPanel1 = new javax.swing.JPanel();
        btnregresar = new javax.swing.JButton();
        btnbuscar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblclientes = new javax.swing.JTable();

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(0, 255, 204));
        jPanel1.setForeground(new java.awt.Color(51, 255, 204));

        btnregresar.setText("regresar a ventana principal");
        btnregresar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnregresarActionPerformed(evt);
            }
        });

        btnbuscar.setText("buscar");
        btnbuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnbuscarActionPerformed(evt);
            }
        });

        tblclientes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(tblclientes);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(btnregresar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnbuscar)
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 723, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnregresar)
                    .addComponent(btnbuscar))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 499, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnregresarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnregresarActionPerformed
        this.dispose();
            
            // Abrir la nueva ventana (PestañaPrincipal)
            PestañaPrincipal nuevaVentana = new PestañaPrincipal();
            nuevaVentana.setVisible(true);
    }//GEN-LAST:event_btnregresarActionPerformed

    private void btnbuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnbuscarActionPerformed
    this.dispose();
            
            // Abrir la nueva ventana (PestañaPrincipal)
        BuscarporRuc nuevaVentana = new BuscarporRuc();
        nuevaVentana.setVisible(true);
    }//GEN-LAST:event_btnbuscarActionPerformed


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
            java.util.logging.Logger.getLogger(Tabladeclientes.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Tabladeclientes.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Tabladeclientes.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Tabladeclientes.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
 /* Set the Nimbus look and feel */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Tabladeclientes.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new Tabladeclientes().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnbuscar;
    private javax.swing.JButton btnregresar;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblclientes;
    // End of variables declaration//GEN-END:variables
}
