package com.sptv.mavenproject2;

import com.sptv.mavenproject2.dto.ClienteExpediente;
import com.sptv.mavenproject2.dto.ClienteFormularioDto;
import com.sptv.mavenproject2.dto.FilaPagosAnuales;
import com.sptv.mavenproject2.service.AuthService;
import com.sptv.mavenproject2.service.ClienteService;
import com.sptv.mavenproject2.service.PagoMensualService;
import com.sptv.mavenproject2.service.PasswordHasher;
import com.sptv.mavenproject2.service.ValidationException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import modelo.Cliente;
import modelo.Libro;
import modelo.PagoMensual;
import modelo.Usuariodelprograma;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import persistance.Controladoradeperistencia;
import persistance.PersistenceManager;

/**
 * Suite de pruebas de integración para verificar las operaciones de negocio,
 * validaciones, seguridad SHA-256, matriz de pagos 2024–2030 y persistencia JPA.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServisercIntegrationTest {

    private static AuthService authService;
    private static ClienteService clienteService;
    private static PagoMensualService pagoMensualService;
    private static Controladoradeperistencia controladora;

    private static Long clientePruebaId;
    private static final String RUC_PRUEBA = "20601234567";

    @BeforeAll
    static void setUpClass() {
        Map<String, Object> testProps = Map.of(
                "javax.persistence.jdbc.driver", "org.h2.Driver",
                "javax.persistence.jdbc.url",
                "jdbc:h2:mem:serviserc_test_db;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1",
                "javax.persistence.jdbc.user", "sa",
                "javax.persistence.jdbc.password", "",
                "javax.persistence.schema-generation.database.action", "drop-and-create",
                "eclipselink.ddl-generation", "drop-and-create-tables",
                "eclipselink.target-database", "MySQL",
                "eclipselink.logging.level", "WARNING"
        );
        PersistenceManager.initializeWithOverrides(testProps, "H2 In-Memory (Modo Compatibilidad MySQL para Tests)");
        authService = new AuthService();
        clienteService = new ClienteService();
        pagoMensualService = new PagoMensualService();
        controladora = new Controladoradeperistencia();
    }

    @AfterAll
    static void tearDownClass() {
        PersistenceManager.shutdown();
    }

    @Test
    @Order(1)
    void testSha256HasherAndInitialSeed() {
        String hashAdmin = PasswordHasher.sha256("admin123");
        Assertions.assertEquals(64, hashAdmin.length());
        Assertions.assertTrue(PasswordHasher.matches("admin123", hashAdmin));

        authService.ensureInitialSeed();
        Assertions.assertTrue(authService.isCredencialInicialPorDefectoActiva());

        Usuariodelprograma logged = authService.autenticar("admin", "admin123");
        Assertions.assertNotNull(logged);
        Assertions.assertEquals("admin", logged.getUsuario());

        Assertions.assertThrows(ValidationException.class,
                () -> authService.autenticar("admin", "claveIncorrecta"));
    }

    @Test
    @Order(2)
    void testUserManagementAndGeneralPassword() {
        Assertions.assertTrue(authService.verificarContrasenaGeneral(AuthService.DEFAULT_GENERAL_PASS));
        Assertions.assertThrows(ValidationException.class,
                () -> authService.verificarContrasenaGeneral("incorrecta"));

        Usuariodelprograma operador = authService.registrarUsuario("contador01", "contable2026");
        Assertions.assertNotNull(operador.getId());
        Assertions.assertEquals(PasswordHasher.sha256("contable2026"), operador.getContrasena());

        // No debe permitir usuario duplicado
        Assertions.assertThrows(ValidationException.class,
                () -> authService.registrarUsuario("contador01", "otraClave"));

        // Eliminación exige coincidencia de ID y Nombre (regla de Administraciondeusuarios)
        Assertions.assertThrows(ValidationException.class,
                () -> authService.eliminarUsuario(operador.getId(), "nombreErroneo"));

        authService.eliminarUsuario(operador.getId(), "contador01");
        Assertions.assertEquals(1, authService.listarUsuarios().size());

        // Actualizar contraseña general
        authService.actualizarContrasenaGeneral("nuevaMaestra2026");
        Assertions.assertTrue(authService.verificarContrasenaGeneral("nuevaMaestra2026"));
        authService.actualizarContrasenaGeneral(AuthService.DEFAULT_GENERAL_PASS);
    }

    @Test
    @Order(3)
    void testRegistrarClienteIntegralY84Pagos() {
        ClienteFormularioDto dto = new ClienteFormularioDto();
        dto.setNombre("Inversiones Andinas SAC");
        dto.setRuc(RUC_PRUEBA);
        dto.setRegimen("Régimen MYPE Tributario");
        dto.setFechaInicioActividades(LocalDate.of(2024, 1, 15));
        dto.setFechaPrimeraDeclaracion(LocalDate.of(2024, 2, 15));
        dto.setHonorarios(new BigDecimal("350.00"));
        dto.setCorreo("contabilidad@andinas.pe");
        dto.setTelefono("987654321");
        dto.setNumeroDeCliente("CLI-001");
        dto.setUsuarioClaveSol("ANDINAS24");
        dto.setClaveSolPassword("SolPass#2024");
        dto.setRegistroVentasActivo(true);
        dto.setRegistroComprasActivo(true);
        dto.setDiarioSimplificadoActivo(false);
        dto.setEstadoMensualAlDia(true);
        dto.setEstadoAnualAlDia(true);
        dto.setEstadoTotalAlDia(true);

        ClienteExpediente expediente = clienteService.registrarClienteIntegral(dto);
        Assertions.assertNotNull(expediente.getCliente().getIdcliente());
        clientePruebaId = expediente.getCliente().getIdcliente();

        // Verificar que se hayan creado exactamente 84 registros de PagoMensual (12 meses x 7 años: 2024..2030)
        List<PagoMensual> todosLosPagos = controladora.listarPagosMensuales();
        long pagosDelCliente = todosLosPagos.stream()
                .filter(p -> p.getCliente().getIdcliente().equals(clientePruebaId))
                .count();
        Assertions.assertEquals(84L, pagosDelCliente,
                "Deben generarse exactamente 84 cuotas de PagoMensual (2024 a 2030)");

        // Validar rechazo de RUC duplicado y RUC inválido
        Assertions.assertThrows(ValidationException.class,
                () -> clienteService.registrarClienteIntegral(dto));

        dto.setRuc("123"); // RUC inválido
        Assertions.assertThrows(ValidationException.class,
                () -> clienteService.registrarClienteIntegral(dto));
    }

    @Test
    @Order(4)
    void testBuscarPorRucYActualizarClienteIntegralConTresLibros() {
        ClienteExpediente encontrado = clienteService.buscarExpedientePorRuc(RUC_PRUEBA);
        Assertions.assertEquals("Inversiones Andinas SAC", encontrado.getCliente().getNombre());
        Assertions.assertEquals("ANDINAS24", encontrado.getClavesol().getUsuario());
        Assertions.assertEquals(Libro.Estado.ACTIVO, encontrado.getLibro().getRegistrodeventas());
        Assertions.assertEquals(Libro.Estado.ACTIVO, encontrado.getLibro().getRegistrodecompras());
        Assertions.assertEquals(Libro.Estado.INACTIVO, encontrado.getLibro().getDiariodeformatosimplificado());

        // Editar incluyendo el cambio de registrodecompras (que estaba omitido en Editcliente original)
        ClienteFormularioDto editDto = clienteService.toFormularioDto(encontrado);
        editDto.setNombre("Inversiones Andinas del Perú SAA");
        editDto.setHonorarios(new BigDecimal("480.50"));
        editDto.setRegistroVentasActivo(true);
        editDto.setRegistroComprasActivo(false); // Desactivar compras
        editDto.setDiarioSimplificadoActivo(true); // Activar diario simplificado
        editDto.setEstadoMensualAlDia(false);
        editDto.setEstadoTotalAlDia(false);

        ClienteExpediente actualizado = clienteService.actualizarClienteIntegral(clientePruebaId, editDto);
        Assertions.assertEquals("Inversiones Andinas del Perú SAA", actualizado.getCliente().getNombre());
        Assertions.assertEquals(0, new BigDecimal("480.50").compareTo(actualizado.getCliente().getHonorarios()));
        Assertions.assertEquals(Libro.Estado.ACTIVO, actualizado.getLibro().getRegistrodeventas());
        Assertions.assertEquals(Libro.Estado.INACTIVO, actualizado.getLibro().getRegistrodecompras(),
                "El libro Registro de Compras debe actualizarse correctamente");
        Assertions.assertEquals(Libro.Estado.ACTIVO, actualizado.getLibro().getDiariodeformatosimplificado());
        Assertions.assertFalse(actualizado.getCobranza().getEstadomensual());
        Assertions.assertFalse(actualizado.getCobranza().getEstadototal());
    }

    @Test
    @Order(5)
    void testEdicionRapidaEnTablaIncluyendoNombre() {
        Cliente actualizado = clienteService.actualizarDatosRapidosEnTabla(
                clientePruebaId,
                "Grupo Andino Corporativo SAC",
                "finanzas@grupoandino.pe",
                new BigDecimal("520.00"),
                "CLI-001-MOD",
                "Régimen General",
                "999888777"
        );
        Assertions.assertEquals("Grupo Andino Corporativo SAC", actualizado.getNombre());
        Assertions.assertEquals("finanzas@grupoandino.pe", actualizado.getCorreo());
        Assertions.assertEquals(0, new BigDecimal("520.00").compareTo(actualizado.getHonorarios()));
        Assertions.assertEquals("Régimen General", actualizado.getRegimen());
    }

    @Test
    @Order(6)
    void testMatrizPagosAnualesYActualizacionMensual() {
        pagoMensualService.actualizarPagoMensual(clientePruebaId, 2025, 1, new BigDecimal("520.00"));
        pagoMensualService.actualizarPagoMensual(clientePruebaId, 2025, 11, new BigDecimal("520.00")); // Noviembre

        List<FilaPagosAnuales> matriz2025 = pagoMensualService.obtenerMatrizAnual(2025);
        Assertions.assertEquals(1, matriz2025.size());

        FilaPagosAnuales fila = matriz2025.get(0);
        Assertions.assertEquals(0, new BigDecimal("520.00").compareTo(fila.getMontoMes(1)));
        Assertions.assertEquals(0, new BigDecimal("520.00").compareTo(fila.getMontoMes(11)));
        Assertions.assertEquals(0, new BigDecimal("1040.00").compareTo(fila.getTotalAnual()));

        pagoMensualService.actualizarNombreClienteDesdePagos(clientePruebaId, "Grupo Andino Final SAC");
        Cliente verificado = controladora.buscarCliente(clientePruebaId);
        Assertions.assertEquals("Grupo Andino Final SAC", verificado.getNombre());
    }

    @Test
    @Order(7)
    void testEliminacionIntegralDeCliente() {
        clienteService.eliminarClienteIntegral(clientePruebaId);
        Assertions.assertNull(controladora.buscarCliente(clientePruebaId));
        Assertions.assertTrue(clienteService.listarExpedientes().isEmpty());
        Assertions.assertTrue(controladora.listarPagosMensuales().isEmpty());
    }
}
