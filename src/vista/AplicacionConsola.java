package vista;

import controlador.CompraController;
import controlador.EventoController;
import controlador.UsuarioController;
import modelo.compra.MetodoPago;
import modelo.entrada.TipoEntrada;
import modelo.evento.Evento;
import modelo.evento.Imagen;
import modelo.facade.GeneradorEntradas;
import modelo.facade.ProcesadorPago;
import modelo.facade.ProcesoCompraFacade;
import modelo.lugar.Lugar;
import modelo.mediador.MediadorCompras;
import modelo.mediador.MediadorConcreto;
import modelo.notificacion.SistemaNotificaciones;
import modelo.recomendacion.ConfiguradorStrategys;
import modelo.recomendacion.GestorRecomendacionesStrategy;
import modelo.recomendacion.IEstrategiaRecomendacion;
import modelo.recomendacion.IRepositorioEvento;
import modelo.recomendacion.RecomendacionPorPopularidad;
import modelo.recomendacion.RepositorioEventoMemoria;
import modelo.reembolso.ProcesadorReembolso;
import modelo.usuario.Asistente;
import modelo.usuario.Organizador;
import modelo.usuario.Preferencia;
import modelo.usuario.RepositorioUsuarioMemoria;
import modelo.usuario.Usuario;
import modelo.validacion.ValidarDisponibilidadHandler;
import modelo.validacion.ValidarLimiteCompraUsuarioHandler;
import modelo.validacion.ValidacionHandler;
import modelo.entrada.EntradaGeneralFactory; // Para crear tipos de entrada de ejemplo
import modelo.entrada.EntradaVIPFactory;
import modelo.factory.TipoEntradaFactory;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.Optional;

public class AplicacionConsola {

    private static Scanner scanner = new Scanner(System.in);

    // Controladores
    private static UsuarioController usuarioController;
    private static EventoController eventoController;
    private static CompraController compraController;

    // Repositorios y Servicios (Singleton o instancias globales para la app de consola)
    private static RepositorioUsuarioMemoria repoUsuario;
    private static RepositorioEventoMemoria repoEvento;
    private static SistemaNotificaciones sistemaNotificaciones;
    private static MediadorCompras mediador;
    private static ProcesadorReembolso procesadorReembolso;
    private static ProcesoCompraFacade procesoCompraFacade;
    private static GestorRecomendacionesStrategy gestorRecomendacionesGlobal;
    private static ConfiguradorStrategys configuradorStrategys;


    // Datos de sesión simulados
    private static Usuario usuarioLogueado = null;

    public static void main(String[] args) {
        inicializarSistema();
        System.out.println("¡Bienvenido al Sistema de Gestión de Eventos!");

        boolean salir = false;
        while (!salir) {
            mostrarMenuPrincipal();
            int opcion = leerOpcion();
            switch (opcion) {
                case 1: manejarMenuUsuario(); break;
                case 2: manejarMenuEventos(); break;
                case 3: manejarMenuCompras(); break;
                case 0: salir = true; break;
                default: System.out.println("Opción no válida. Intente de nuevo.");
            }
        }
        System.out.println("Gracias por usar el sistema. ¡Hasta pronto!");
        scanner.close();
    }

    private static void inicializarSistema() {
        System.out.println("Inicializando sistema...");

        // 1. Repositorios básicos
        repoUsuario = new RepositorioUsuarioMemoria();
        repoEvento = new RepositorioEventoMemoria();

        // 2. Configurador de Estrategias de Recomendación
        configuradorStrategys = new ConfiguradorStrategys();

        // 3. Estrategia de Recomendación Inicial (para el gestor global o nuevos asistentes)
        IEstrategiaRecomendacion estrategiaInicialRec = new RecomendacionPorPopularidad();

        // 4. Gestor de Recomendaciones Global (podría ser usado como prototipo para asistentes)
        gestorRecomendacionesGlobal = new GestorRecomendacionesStrategy(repoEvento, estrategiaInicialRec, configuradorStrategys);

        // 5. Sistema de Notificaciones (Singleton)
        sistemaNotificaciones = SistemaNotificaciones.getInstance();

        // 6. Mediador
        mediador = new MediadorConcreto(sistemaNotificaciones, gestorRecomendacionesGlobal);

        // 7. Componentes para el Facade de Compra
        ProcesadorPago procesadorPago = new ProcesadorPago();
        GeneradorEntradas generadorEntradas = new GeneradorEntradas();

        ValidarDisponibilidadHandler valDisp = new ValidarDisponibilidadHandler();
        ValidarLimiteCompraUsuarioHandler valLimite = new ValidarLimiteCompraUsuarioHandler();
        valDisp.setNextHandler(valLimite); // Encadenar validadores
        ValidacionHandler cabezaCadenaValidacion = valDisp;

        // 8. ProcesoCompraFacade
        procesoCompraFacade = new ProcesoCompraFacade(procesadorPago, generadorEntradas, cabezaCadenaValidacion, mediador);

        // 9. Registrar el Facade en el Mediador (si el mediador lo necesita para alguna coordinación)
        // En la implementación actual de MediadorConcreto, esto es importante.
        if (mediador instanceof MediadorConcreto) {
            ((MediadorConcreto) mediador).registrarProcesoCompraFacade(procesoCompraFacade);
        }

        // 10. Procesador de Reembolso
        procesadorReembolso = new ProcesadorReembolso(sistemaNotificaciones);

        // 11. Controladores
        usuarioController = new UsuarioController(repoUsuario, repoEvento, configuradorStrategys);
        eventoController = new EventoController(repoEvento, sistemaNotificaciones); // EventoController usa SN directamente por ahora
        compraController = new CompraController(procesoCompraFacade, procesadorReembolso);

        // (Opcional) Cargar datos de prueba
        cargarDatosDePrueba();

        System.out.println("Sistema inicializado correctamente.");
    }

    private static void cargarDatosDePrueba() {
        System.out.println("Cargando datos de prueba...");
        // Crear un organizador de prueba
        Organizador org1 = (Organizador) usuarioController.registrarOrganizador("OrgConciertos", "org@example.com", "pass123", "Contacto Org: 123456789");
        if (org1 == null) {
             System.err.println("Error al crear organizador de prueba.");
             return;
        }
        sistemaNotificaciones.registrarObserver(org1); // Organizador observa notificaciones globales


        // Crear un lugar de prueba
        Lugar estadioNacional = new Lugar("Estadio Nacional", "Av. Grecia 2001, Ñuñoa, Santiago");
        estadioNacional.addTipoDeEventoAdmitido("Concierto");
        estadioNacional.addTipoDeEventoAdmitido("Deportivo");
        // (Secciones y asientos podrían añadirse aquí si la lógica de capacidad del lugar los requiere)

        // Crear tipos de entrada de ejemplo usando fábricas
        TipoEntradaFactory generalFactory = new EntradaGeneralFactory();
        TipoEntradaFactory vipFactory = new EntradaVIPFactory();

        TipoEntrada tipoGeneralConcierto = generalFactory.crearTipoEntrada("General Rock", "Acceso general al concierto", 30.00, 500, 5);
        TipoEntrada tipoVIPConcierto = ((EntradaVIPFactory)vipFactory).crearEntradaVIPConBeneficios("VIP Oro", "Acceso VIP con beneficios", 100.00, 50, 2, List.of("Acceso preferencial", "Barra libre"));


        // Crear un evento de prueba
        List<String> imgsEv1 = List.of("http://example.com/img1.jpg");
        List<TipoEntrada> tiposEv1 = List.of(tipoGeneralConcierto, tipoVIPConcierto);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date fechaEv1 = null;
        try {
            fechaEv1 = sdf.parse("31/12/2024");
        } catch (ParseException e) { e.printStackTrace(); }

        Evento ev1 = eventoController.crearEvento(org1, "EVT001", "Concierto Fin de Año", "Gran concierto de rock para despedir el año.",
                                        "Concierto", fechaEv1, "20:00", estadioNacional, 550,
                                        imgsEv1, null, tiposEv1);
        if (ev1 != null) {
            eventoController.publicarEvento(ev1.getId(), org1);
        }

        // Crear un asistente de prueba
        Asistente asist1 = (Asistente) usuarioController.registrarAsistente("Ana Cuenta", "ana@example.com", "pass456");
        if (asist1 != null) {
            asist1.addPreferencia(new Preferencia("Concierto", "Santiago"));
            asist1.addPreferencia(new Preferencia("Teatro", "Santiago"));
            sistemaNotificaciones.registrarObserver(asist1); // Asistente observa notificaciones
            // Suscribir a Ana al evento si le interesa (ejemplo, podría ser automático)
            // sistemaNotificaciones.suscribirAEvento("EVT001", asist1);
        }
         Asistente asist2 = (Asistente) usuarioController.registrarAsistente("Juan Rocker", "juan@example.com", "pass789");
         if (asist2 != null) {
            asist2.addPreferencia(new Preferencia("Concierto", "Santiago"));
            sistemaNotificaciones.registrarObserver(asist2);
         }


        System.out.println("Datos de prueba cargados.");
    }


    private static void mostrarMenuPrincipal() {
        System.out.println("\n--- MENÚ PRINCIPAL ---");
        if (usuarioLogueado != null) {
            System.out.println("Usuario: " + usuarioLogueado.getNombre() + " (" + usuarioLogueado.getClass().getSimpleName() + ")");
        }
        System.out.println("1. Gestión de Usuarios");
        System.out.println("2. Gestión de Eventos");
        System.out.println("3. Compras y Entradas");
        System.out.println("0. Salir");
        System.out.print("Seleccione una opción: ");
    }

    private static int leerOpcion() {
        while (!scanner.hasNextInt()) {
            System.out.println("Entrada no válida. Por favor, ingrese un número.");
            scanner.next(); // Descarta la entrada incorrecta
            System.out.print("Seleccione una opción: ");
        }
        return scanner.nextInt();
    }

    private static String leerString(String prompt) {
        System.out.print(prompt);
        return scanner.next() + scanner.nextLine(); // Para capturar toda la línea incluyendo espacios
    }

    private static double leerDouble(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextDouble()) {
            System.out.println("Entrada no válida. Por favor, ingrese un número decimal.");
            scanner.next();
            System.out.print(prompt);
        }
        return scanner.nextDouble();
    }
     private static int leerInt(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            System.out.println("Entrada no válida. Por favor, ingrese un número entero.");
            scanner.next();
            System.out.print(prompt);
        }
        return scanner.nextInt();
    }
    private static Date leerFecha(String prompt) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        sdf.setLenient(false);
        Date fecha = null;
        while (fecha == null) {
            System.out.print(prompt + " (dd/MM/yyyy): ");
            String input = scanner.next();
            try {
                fecha = sdf.parse(input);
            } catch (ParseException e) {
                System.out.println("Formato de fecha incorrecto. Intente de nuevo.");
            }
        }
        return fecha;
    }


    private static void manejarMenuUsuario() {
        // Implementación de submenú de usuarios (registrar, login, ver perfil, etc.)
        System.out.println("\n--- Gestión de Usuarios ---");
        if (usuarioLogueado == null) {
            System.out.println("1. Registrar Asistente");
            System.out.println("2. Registrar Organizador");
            System.out.println("3. Iniciar Sesión");
        } else {
            System.out.println("1. Ver Mi Perfil");
            System.out.println("2. Actualizar Preferencias (Asistente)");
            System.out.println("3. Ver Mis Recomendaciones (Asistente)");
            System.out.println("4. Cerrar Sesión");
        }
        System.out.println("0. Volver al Menú Principal");
        System.out.print("Seleccione una opción: ");
        int opcion = leerOpcion();

        if (usuarioLogueado == null) {
            switch (opcion) {
                case 1: registrarNuevoAsistente(); break;
                case 2: registrarNuevoOrganizador(); break;
                case 3: iniciarSesion(); break;
                case 0: break;
                default: System.out.println("Opción no válida.");
            }
        } else {
             switch (opcion) {
                case 1: verMiPerfil(); break;
                case 2: if (usuarioLogueado instanceof Asistente) actualizarPreferenciasAsistente(); else System.out.println("Opción solo para Asistentes."); break;
                case 3: if (usuarioLogueado instanceof Asistente) verMisRecomendaciones(); else System.out.println("Opción solo para Asistentes."); break;
                case 4: cerrarSesion(); break;
                case 0: break;
                default: System.out.println("Opción no válida.");
            }
        }
    }

    private static void registrarNuevoAsistente() {
        System.out.println("\n-- Registrar Nuevo Asistente --");
        String nombre = leerString("Nombre: ");
        String email = leerString("Email: ");
        String password = leerString("Password: ");
        Usuario nuevo = usuarioController.registrarAsistente(nombre, email, password);
        if (nuevo != null) System.out.println("Asistente registrado con éxito.");
        else System.out.println("Fallo al registrar asistente.");
    }

    private static void registrarNuevoOrganizador() {
        System.out.println("\n-- Registrar Nuevo Organizador --");
        String nombre = leerString("Nombre: ");
        String email = leerString("Email: ");
        String password = leerString("Password: ");
        String contacto = leerString("Info de Contacto: ");
        Usuario nuevo = usuarioController.registrarOrganizador(nombre, email, password, contacto);
        if (nuevo != null) System.out.println("Organizador registrado con éxito.");
        else System.out.println("Fallo al registrar organizador.");
    }

    private static void iniciarSesion() {
        System.out.println("\n-- Iniciar Sesión --");
        String email = leerString("Email: ");
        String password = leerString("Password: ");
        usuarioLogueado = usuarioController.autenticarUsuario(email, password);
        if (usuarioLogueado != null) {
            System.out.println("Bienvenido, " + usuarioLogueado.getNombre() + "!");
            // Suscribir al usuario logueado a notificaciones globales si no lo está ya
            // (SistemaNotificaciones maneja duplicados)
            sistemaNotificaciones.registrarObserver(usuarioLogueado);
        } else {
            System.out.println("Email o contraseña incorrectos.");
        }
    }

    private static void verMiPerfil() {
        if (usuarioLogueado == null) { System.out.println("Debe iniciar sesión."); return; }
        System.out.println("\n-- Mi Perfil --");
        System.out.println("ID: " + usuarioLogueado.getId());
        System.out.println("Nombre: " + usuarioLogueado.getNombre());
        System.out.println("Email: " + usuarioLogueado.getEmail());
        System.out.println("Tipo: " + usuarioLogueado.getClass().getSimpleName());
        if (usuarioLogueado instanceof Organizador) {
            System.out.println("Contacto: " + ((Organizador) usuarioLogueado).getInfoContacto());
            System.out.println("Eventos Creados: " + ((Organizador) usuarioLogueado).getEventosCreados().size());
        } else if (usuarioLogueado instanceof Asistente) {
            System.out.println("Preferencias: ");
            ((Asistente) usuarioLogueado).getPreferencias().forEach(p -> System.out.println("  - Cat: " + p.getCategoria() + ", Ubi: " + p.getUbicacion()));
            System.out.println("Historial de Compras: " + ((Asistente) usuarioLogueado).getHistorialCompras().size() + " compras.");
        }
    }

    private static void actualizarPreferenciasAsistente() {
        Asistente asistente = (Asistente) usuarioLogueado;
        System.out.println("\n-- Actualizar Preferencias --");
        System.out.println("Sus preferencias actuales son:");
        asistente.getPreferencias().forEach(p -> System.out.println("  - Cat: " + p.getCategoria() + ", Ubi: " + p.getUbicacion()));

        List<Preferencia> nuevasPreferencias = new ArrayList<>();
        String continuar;
        do {
            String cat = leerString("Nueva categoría de preferencia (o 'fin' para terminar): ");
            if ("fin".equalsIgnoreCase(cat)) break;
            String ubi = leerString("Ubicación para '" + cat + "': ");
            nuevasPreferencias.add(new Preferencia(cat, ubi));
            continuar = leerString("¿Añadir otra preferencia? (s/n): ");
        } while ("s".equalsIgnoreCase(continuar));

        asistente.setPreferencias(nuevasPreferencias); // Asumiendo que Usuario tiene setPreferencias
        System.out.println("Preferencias actualizadas.");
    }

    private static void verMisRecomendaciones() {
        Asistente asistente = (Asistente) usuarioLogueado;
        System.out.println("\n-- Mis Recomendaciones --");
        List<Evento> recomendaciones = asistente.obtenerRecomendaciones();
        if (recomendaciones.isEmpty()) {
            System.out.println("No hay recomendaciones para ti en este momento.");
        } else {
            recomendaciones.forEach(e -> System.out.println("- " + e.getNombre() + " (" + e.getCategoria() + ") en " + e.getLugar().getNombre() + " el " + e.getFecha()));
        }
    }

    private static void cerrarSesion() {
        System.out.println(usuarioLogueado.getNombre() + " ha cerrado sesión.");
        // Desuscribir de notificaciones globales al cerrar sesión para no acumular observers inactivos.
        // sistemaNotificaciones.eliminarObserver(usuarioLogueado); // Opcional, depende de la gestión de observers
        usuarioLogueado = null;
    }


    private static void manejarMenuEventos() {
        // Implementación de submenú de eventos (crear, listar, ver detalle, etc.)
        System.out.println("\n--- Gestión de Eventos ---");
        System.out.println("1. Listar Todos los Eventos");
        System.out.println("2. Ver Detalle de Evento");
        if (usuarioLogueado instanceof Organizador) {
            System.out.println("3. Crear Nuevo Evento");
            System.out.println("4. Publicar Evento (Borrador)");
            System.out.println("5. Cancelar Evento");
            System.out.println("6. Ver Mis Eventos Creados");
        }
        System.out.println("0. Volver al Menú Principal");
        System.out.print("Seleccione una opción: ");
        int opcion = leerOpcion();
        // ... Lógica para cada opción ...
        switch(opcion) {
            case 1: listarEventos(); break;
            case 2: verDetalleEvento(); break;
            case 3: if (usuarioLogueado instanceof Organizador) crearNuevoEvento(); else System.out.println("Opción solo para Organizadores."); break;
            case 4: if (usuarioLogueado instanceof Organizador) publicarEventoOrganizador(); else System.out.println("Opción solo para Organizadores."); break;
            case 5: if (usuarioLogueado instanceof Organizador) cancelarEventoOrganizador(); else System.out.println("Opción solo para Organizadores."); break;
            case 6: if (usuarioLogueado instanceof Organizador) verMisEventos(); else System.out.println("Opción solo para Organizadores."); break;

            case 0: break;
            default: System.out.println("Opción no válida.");
        }
    }

    private static void listarEventos() {
        System.out.println("\n--- Listado de Eventos Disponibles ---");
        List<Evento> eventos = eventoController.listarTodosLosEventos();
        if (eventos.isEmpty()) {
            System.out.println("No hay eventos disponibles actualmente.");
            return;
        }
        for (Evento e : eventos) {
            System.out.printf("ID: %s, Nombre: %s, Cat: %s, Fecha: %s, Lugar: %s, Estado: %s\n",
                e.getId(), e.getNombre(), e.getCategoria(),
                new SimpleDateFormat("dd/MM/yyyy").format(e.getFecha()),
                e.getLugar() != null ? e.getLugar().getNombre() : "N/A",
                e.getEstadoActual() != null ? e.getEstadoActual().toString() : "N/A");
        }
    }

    private static void verDetalleEvento() {
        String idEvento = leerString("Ingrese ID del evento a detallar: ");
        Optional<Evento> optEvento = eventoController.buscarEventoPorId(idEvento);
        if (optEvento.isPresent()) {
            Evento e = optEvento.get();
            System.out.println("\n--- Detalle del Evento ---");
            System.out.println("ID: " + e.getId());
            System.out.println("Nombre: " + e.getNombre());
            System.out.println("Descripción: " + e.getDescripcion());
            System.out.println("Categoría: " + e.getCategoria());
            System.out.println("Fecha: " + new SimpleDateFormat("dd/MM/yyyy").format(e.getFecha()) + " Hora: " + e.getHora());
            System.out.println("Lugar: " + (e.getLugar() != null ? e.getLugar().getNombre() + " (" + e.getLugar().getDireccion() + ")" : "N/A"));
            System.out.println("Capacidad: " + e.getCapacidad() + " (Vendidas: " + e.getEntradasVendidas() + ")");
            System.out.println("Organizador: " + (e.getOrganizador() != null ? e.getOrganizador().getNombre() : "N/A"));
            System.out.println("Estado: " + (e.getEstadoActual() != null ? e.getEstadoActual().toString() : "N/A"));
            System.out.println("Tipos de Entrada:");
            if (e.getTiposEntrada() != null && !e.getTiposEntrada().isEmpty()) {
                for (TipoEntrada te : e.getTiposEntrada()) {
                    System.out.printf("  - %s: $%.2f (Disp: %d/%d, Límite/usr: %d)\n",
                                      te.getNombre(), te.getPrecio(), te.getCantidadDisponible(),
                                      te.getCantidadTotal(), te.getLimiteCompraPorUsuario());
                }
            } else {
                System.out.println("  No hay tipos de entrada definidos para este evento.");
            }
        } else {
            System.out.println("Evento con ID '" + idEvento + "' no encontrado.");
        }
    }

    private static void crearNuevoEvento() {
        Organizador org = (Organizador) usuarioLogueado;
        System.out.println("\n--- Crear Nuevo Evento ---");
        String id = leerString("ID único para el evento: ");
        String nombre = leerString("Nombre del evento: ");
        String desc = leerString("Descripción: ");
        String cat = leerString("Categoría: ");
        Date fecha = leerFecha("Fecha del evento");
        String hora = leerString("Hora del evento (HH:mm): ");
        // Para Lugar y TiposDeEntrada, necesitaríamos una forma de seleccionarlos o crearlos.
        // Simplificación: Usar un lugar genérico o pedir datos básicos.
        String nombreLugar = leerString("Nombre del lugar: ");
        String dirLugar = leerString("Dirección del lugar: ");
        Lugar lugar = new Lugar(nombreLugar, dirLugar); // Crear lugar simple
        int capacidad = leerInt("Capacidad total del evento: ");

        // Tipos de entrada (simplificado)
        List<TipoEntrada> tipos = new ArrayList<>();
        String addTipo;
        TipoEntradaFactory genFactory = new EntradaGeneralFactory(); // Para crear tipos simples
        do {
            System.out.println("Añadiendo tipo de entrada...");
            String nombreTipo = leerString("  Nombre del tipo de entrada (ej. General, VIP): ");
            double precioTipo = leerDouble("  Precio: ");
            int cantidadTipo = leerInt("  Cantidad total de este tipo: ");
            int limiteUsrTipo = leerInt("  Límite por usuario: ");
            tipos.add(genFactory.crearTipoEntrada(nombreTipo, "Entrada tipo " + nombreTipo, precioTipo, cantidadTipo, limiteUsrTipo));
            addTipo = leerString("¿Añadir otro tipo de entrada? (s/n): ");
        } while ("s".equalsIgnoreCase(addTipo));

        // Imágenes y videos (simplificado)
        List<String> urlsImgs = new ArrayList<>();
        String addImg = leerString("¿Añadir URL de imagen? (s/n): ");
        if("s".equalsIgnoreCase(addImg)) urlsImgs.add(leerString(" URL Imagen: "));


        Evento nuevo = eventoController.crearEvento(org, id, nombre, desc, cat, fecha, hora, lugar, capacidad, urlsImgs, null, tipos);
        if (nuevo != null) System.out.println("Evento creado. Estado actual: " + nuevo.getEstadoActual());
        else System.out.println("Fallo al crear evento.");
    }

    private static void publicarEventoOrganizador(){
        Organizador org = (Organizador) usuarioLogueado;
        String idEvento = leerString("Ingrese ID del evento (en borrador) a publicar: ");
        if(eventoController.publicarEvento(idEvento, org)){
            System.out.println("Solicitud de publicación enviada.");
        } else {
            System.out.println("No se pudo publicar el evento (no encontrado, sin permiso o no es borrador).");
        }
    }
    private static void cancelarEventoOrganizador(){
        Organizador org = (Organizador) usuarioLogueado;
        String idEvento = leerString("Ingrese ID del evento a cancelar: ");
        if(eventoController.cancelarEvento(idEvento, org)){
            System.out.println("Solicitud de cancelación enviada.");
        } else {
            System.out.println("No se pudo cancelar el evento (no encontrado o sin permiso).");
        }
    }
     private static void verMisEventos() {
        Organizador org = (Organizador) usuarioLogueado;
        System.out.println("\n--- Mis Eventos Creados ("+ org.getNombre() +") ---");
        List<Evento> misEventos = org.getEventosCreados();
        if (misEventos.isEmpty()) {
            System.out.println("No has creado ningún evento aún.");
            return;
        }
        for (Evento e : misEventos) {
             System.out.printf("ID: %s, Nombre: %s, Estado: %s\n",
                e.getId(), e.getNombre(), e.getEstadoActual() != null ? e.getEstadoActual().toString() : "N/A");
        }
    }


    private static void manejarMenuCompras() {
        // Implementación de submenú de compras (comprar entrada, ver historial, etc.)
        System.out.println("\n--- Compras y Entradas ---");
        if (usuarioLogueado instanceof Asistente) {
            System.out.println("1. Comprar Entradas para un Evento");
            System.out.println("2. Ver Mi Historial de Compras");
            System.out.println("3. Cancelar una Compra (solicitar reembolso)");
            System.out.println("4. Deshacer Última Operación de Compra/Cancelación");
            System.out.println("5. Rehacer Última Operación Deshecha");
        } else if (usuarioLogueado == null) {
            System.out.println("Debe iniciar sesión como Asistente para gestionar compras.");
        } else {
             System.out.println("Los Organizadores no gestionan compras de esta forma.");
        }
        System.out.println("0. Volver al Menú Principal");
        System.out.print("Seleccione una opción: ");
        int opcion = leerOpcion();

        if (usuarioLogueado instanceof Asistente) {
            Asistente asistente = (Asistente) usuarioLogueado;
            switch(opcion) {
                case 1: comprarEntradasAsistente(asistente); break;
                case 2: verHistorialComprasAsistente(asistente); break;
                case 3: cancelarCompraAsistente(asistente); break;
                case 4: if (asistente.deshacerUltimaOperacion()) System.out.println("Última operación deshecha."); else System.out.println("No se pudo deshacer o no hay nada que deshacer."); break;
                case 5: if (asistente.rehacerUltimaOperacionDeshecha()) System.out.println("Última operación rehecha."); else System.out.println("No se pudo rehacer o no hay nada que rehacer."); break;
                case 0: break;
                default: System.out.println("Opción no válida.");
            }
        }
    }

    private static void comprarEntradasAsistente(Asistente asistente) {
        System.out.println("\n--- Comprar Entradas ---");
        listarEventos(); // Mostrar eventos disponibles
        String idEvento = leerString("Ingrese ID del evento para comprar entradas: ");
        Optional<Evento> optEvento = eventoController.buscarEventoPorId(idEvento);

        if (optEvento.isEmpty()) {
            System.out.println("Evento no encontrado.");
            return;
        }
        Evento eventoSeleccionado = optEvento.get();
        System.out.println("Ha seleccionado: " + eventoSeleccionado.getNombre());
        if (eventoSeleccionado.getTiposEntrada() == null || eventoSeleccionado.getTiposEntrada().isEmpty()) {
            System.out.println("Este evento no tiene tipos de entrada definidos. No se puede comprar.");
            return;
        }

        System.out.println("Tipos de entrada disponibles para '" + eventoSeleccionado.getNombre() + "':");
        List<TipoEntrada> tiposDisponibles = eventoSeleccionado.getTiposEntrada();
        for (int i = 0; i < tiposDisponibles.size(); i++) {
            TipoEntrada te = tiposDisponibles.get(i);
            System.out.printf("%d. %s - Precio: $%.2f (Disponibles: %d, Límite por usuario: %d)\n",
                              i + 1, te.getNombre(), te.getPrecio(), te.getCantidadDisponible(), te.getLimiteCompraPorUsuario());
        }
        int opcionTipo = leerInt("Seleccione el número del tipo de entrada: ") -1;
        if (opcionTipo < 0 || opcionTipo >= tiposDisponibles.size()) {
            System.out.println("Selección de tipo de entrada no válida.");
            return;
        }
        TipoEntrada tipoSeleccionado = tiposDisponibles.get(opcionTipo);

        int cantidad = leerInt("Cantidad de entradas de tipo '" + tipoSeleccionado.getNombre() + "' a comprar: ");
        String tipoPago = leerString("Método de pago (ej. TARJETA_CREDITO, PAYPAL): ");
        String titularPago = leerString("Titular del método de pago: ");
        MetodoPago metodoPago = new MetodoPago(tipoPago, titularPago); // Simplificado, para tarjeta se necesitarían más datos.

        // Simular más datos para tarjeta si es el caso
        if(tipoPago.toUpperCase().contains("TARJETA")) {
            String numT = leerString("Número Tarjeta (simulado): ");
            String venT = leerString("Vencimiento (MM/YY): ");
            String cvvT = leerString("CVV (simulado): ");
            metodoPago = new MetodoPago(tipoPago, numT, titularPago, venT, cvvT);
        }

        String codDesc = leerString("Código de descuento (opcional, presione Enter si no tiene): ");
        if(codDesc.trim().isEmpty()) codDesc = null;

        compraController.procesarNuevaCompra(asistente, eventoSeleccionado, tipoSeleccionado, cantidad, metodoPago, codDesc);
        // El resultado se verá en las notificaciones o consultando el historial.
        System.out.println("Procesando su solicitud de compra...");
    }

    private static void verHistorialComprasAsistente(Asistente asistente) {
        System.out.println("\n--- Mi Historial de Compras (" + asistente.getNombre() + ") ---");
        List<Compra> historial = asistente.getHistorialCompras();
        if (historial.isEmpty()) {
            System.out.println("No tienes compras en tu historial.");
            return;
        }
        for (Compra c : historial) {
            System.out.printf("ID Compra: %s, Evento: %s, Tipo: %s, Cant: %d, Total: $%.2f, Estado: %s, Fecha: %s\n",
                              c.getId(), c.getEvento().getNombre(), c.getTipoEntradaBase().getNombre(),
                              c.getCantidad(), c.getTotalPagado(), c.getEstado(),
                              new SimpleDateFormat("dd/MM/yyyy HH:mm").format(c.getFechaCompra()));
        }
    }

    private static void cancelarCompraAsistente(Asistente asistente) {
        System.out.println("\n--- Cancelar Compra (Solicitar Reembolso) ---");
        verHistorialComprasAsistente(asistente);
        if (asistente.getHistorialCompras().isEmpty()) return;

        String idCompra = leerString("Ingrese ID de la compra a cancelar/reembolsar: ");
        Optional<Compra> optCompra = asistente.getHistorialCompras().stream()
                                        .filter(c -> c.getId().equals(idCompra))
                                        .findFirst();
        if (optCompra.isEmpty()) {
            System.out.println("Compra con ID '" + idCompra + "' no encontrada en su historial.");
            return;
        }
        Compra compraACancelar = optCompra.get();
        String motivo = leerString("Motivo de la cancelación/reembolso: ");

        compraController.solicitarCancelacionCompra(asistente, compraACancelar, motivo);
        System.out.println("Procesando su solicitud de cancelación/reembolso...");
    }
}
