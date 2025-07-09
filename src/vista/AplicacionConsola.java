package vista;

import controlador.CompraController;
import controlador.EventoController;
import controlador.UsuarioController;
import modelo.compra.MetodoPago;
import modelo.entrada.TipoEntrada;
import modelo.evento.Evento;
// import modelo.evento.Imagen; // No se usa directamente aquí si la creación de evento lo maneja el controller
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
import modelo.entrada.EntradaGeneralFactory;
import modelo.entrada.EntradaVIPFactory;
import modelo.factory.TipoEntradaFactory;
import modelo.excepciones.AplicacionException; // Clase base
import modelo.excepciones.EntidadNoEncontradaException;
import modelo.excepciones.OperacionInvalidaException;
import modelo.excepciones.ValidacionException;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.Optional; // Se seguirá usando donde el flujo lo permita o requiera

public class AplicacionConsola {

    private static Scanner scanner = new Scanner(System.in);

    // Controladores
    private static UsuarioController usuarioController;
    private static EventoController eventoController;
    private static CompraController compraController;

    // Repositorios y Servicios
    private static RepositorioUsuarioMemoria repoUsuario;
    private static RepositorioEventoMemoria repoEvento;
    private static SistemaNotificaciones sistemaNotificaciones;
    private static MediadorCompras mediador;
    private static ProcesadorReembolso procesadorReembolso;
    private static ProcesoCompraFacade procesoCompraFacade;
    private static GestorRecomendacionesStrategy gestorRecomendacionesGlobal;
    private static ConfiguradorStrategys configuradorStrategys;

    private static Usuario usuarioLogueado = null;

    public static void main(String[] args) {
        try {
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
        } catch (AplicacionException e) {
            System.err.println("ERROR DE APLICACIÓN: " + e.getMessage());
            // En una aplicación real, aquí podría haber un logging más detallado.
            // e.printStackTrace(); // Para depuración
        } catch (Exception e) {
            System.err.println("ERROR INESPERADO DEL SISTEMA: " + e.getMessage());
            // e.printStackTrace(); // Para depuración
        } finally {
            System.out.println("Gracias por usar el sistema. ¡Hasta pronto!");
            scanner.close();
        }
    }

    private static void inicializarSistema() {
        System.out.println("Inicializando sistema...");

        repoUsuario = new RepositorioUsuarioMemoria();
        repoEvento = new RepositorioEventoMemoria();
        configuradorStrategys = new ConfiguradorStrategys();
        IEstrategiaRecomendacion estrategiaInicialRec = new RecomendacionPorPopularidad();
        gestorRecomendacionesGlobal = new GestorRecomendacionesStrategy(repoEvento, estrategiaInicialRec, configuradorStrategys);
        sistemaNotificaciones = SistemaNotificaciones.getInstance();
        mediador = new MediadorConcreto(sistemaNotificaciones, gestorRecomendacionesGlobal);
        ProcesadorPago procesadorPago = new ProcesadorPago();
        GeneradorEntradas generadorEntradas = new GeneradorEntradas();
        ValidarDisponibilidadHandler valDisp = new ValidarDisponibilidadHandler();
        ValidarLimiteCompraUsuarioHandler valLimite = new ValidarLimiteCompraUsuarioHandler();
        valDisp.setNextHandler(valLimite);
        ValidacionHandler cabezaCadenaValidacion = valDisp;
        procesoCompraFacade = new ProcesoCompraFacade(procesadorPago, generadorEntradas, cabezaCadenaValidacion, mediador);
        if (mediador instanceof MediadorConcreto) {
            ((MediadorConcreto) mediador).registrarProcesoCompraFacade(procesoCompraFacade);
        }
        procesadorReembolso = new ProcesadorReembolso(sistemaNotificaciones);
        usuarioController = new UsuarioController(repoUsuario, repoEvento, configuradorStrategys);
        eventoController = new EventoController(repoEvento, sistemaNotificaciones);
        compraController = new CompraController(procesoCompraFacade, procesadorReembolso);

        cargarDatosDePrueba();
        System.out.println("Sistema inicializado correctamente.");
    }

    private static void cargarDatosDePrueba() {
        System.out.println("Cargando datos de prueba...");
        try {
            Organizador org1 = (Organizador) usuarioController.registrarOrganizador("OrgConciertos", "org@example.com", "pass123", "Contacto Org: 123456789");
            sistemaNotificaciones.registrarObserver(org1);

            Lugar estadioNacional = new Lugar("Estadio Nacional", "Av. Grecia 2001, Ñuñoa, Santiago");
            estadioNacional.addTipoDeEventoAdmitido("Concierto");
            estadioNacional.addTipoDeEventoAdmitido("Deportivo");

            TipoEntradaFactory generalFactory = new EntradaGeneralFactory();
            TipoEntradaFactory vipFactory = new EntradaVIPFactory();
            TipoEntrada tipoGeneralConcierto = generalFactory.crearTipoEntrada("General Rock", "Acceso general", 30.00, 500, 5);
            TipoEntrada tipoVIPConcierto = ((EntradaVIPFactory)vipFactory).crearEntradaVIPConBeneficios("VIP Oro", "Acceso VIP", 100.00, 50, 2, List.of("Acceso preferencial"));

            // Crear lista de Imagen
            List<modelo.evento.Imagen> imagenesEv1 = new ArrayList<>();
            imagenesEv1.add(new modelo.evento.Imagen("http://example.com/img1.jpg", "Poster del concierto"));

            // Crear lista de Video (vacía por ahora para simplificar datos de prueba)
            List<modelo.evento.Video> videosEv1 = new ArrayList<>();
            // videosEv1.add(new modelo.evento.Video("http://example.com/vid1.mp4", "Trailer del concierto"));


            List<TipoEntrada> tiposEv1 = List.of(tipoGeneralConcierto, tipoVIPConcierto);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date fechaEv1 = sdf.parse("31/12/2024");

            Evento ev1 = eventoController.crearEvento(org1, "EVT001", "Concierto Fin de Año", "Gran concierto", "Concierto", fechaEv1, "20:00", estadioNacional, 550, imagenesEv1, videosEv1, tiposEv1);
            eventoController.publicarEvento(ev1.getId(), org1);

            Asistente asist1 = (Asistente) usuarioController.registrarAsistente("Ana Cuenta", "ana@example.com", "pass456");
            asist1.addPreferencia(new Preferencia("Concierto", "Santiago"));
            sistemaNotificaciones.registrarObserver(asist1);

            Asistente asist2 = (Asistente) usuarioController.registrarAsistente("Juan Rocker", "juan@example.com", "pass789");
            asist2.addPreferencia(new Preferencia("Concierto", "Santiago"));
            sistemaNotificaciones.registrarObserver(asist2);

            System.out.println("Datos de prueba cargados.");

        } catch (AplicacionException e) {
            System.err.println("Error al cargar datos de prueba: " + e.getMessage());
        } catch (ParseException e) {
            System.err.println("Error de formato de fecha en datos de prueba: " + e.getMessage());
        }
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
            scanner.next();
            System.out.print("Seleccione una opción: ");
        }
        return scanner.nextInt();
    }

    private static String leerString(String prompt) {
        System.out.print(prompt);
        return scanner.next() + scanner.nextLine();
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

        try {
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
        } catch (AplicacionException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void registrarNuevoAsistente() {
        System.out.println("\n-- Registrar Nuevo Asistente --");
        String nombre = leerString("Nombre: ");
        String email = leerString("Email: ");
        String password = leerString("Password: ");
        // La excepción será capturada por el try-catch en manejarMenuUsuario()
        Usuario nuevo = usuarioController.registrarAsistente(nombre, email, password);
        System.out.println("Asistente '" + nuevo.getNombre() + "' registrado con éxito.");
    }

    private static void registrarNuevoOrganizador() {
        System.out.println("\n-- Registrar Nuevo Organizador --");
        String nombre = leerString("Nombre: ");
        String email = leerString("Email: ");
        String password = leerString("Password: ");
        String contacto = leerString("Info de Contacto: ");
        Usuario nuevo = usuarioController.registrarOrganizador(nombre, email, password, contacto);
        System.out.println("Organizador '" + nuevo.getNombre() + "' registrado con éxito.");
    }

    private static void iniciarSesion() {
        System.out.println("\n-- Iniciar Sesión --");
        String email = leerString("Email: ");
        String password = leerString("Password: ");
        usuarioLogueado = usuarioController.autenticarUsuario(email, password);
        System.out.println("Bienvenido, " + usuarioLogueado.getNombre() + "!");
        sistemaNotificaciones.registrarObserver(usuarioLogueado);
    }

    private static void verMiPerfil() {
        if (usuarioLogueado == null) { System.out.println("Debe iniciar sesión."); return; }
        System.out.println("\n-- Mi Perfil --");
        // ... (sin cambios en la lógica interna de mostrar perfil)
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
        // ... (sin cambios en la lógica interna, asumir que no lanza excepciones propias)
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

        asistente.setPreferencias(nuevasPreferencias);
        System.out.println("Preferencias actualizadas.");
    }

    private static void verMisRecomendaciones() {
        // ... (sin cambios en la lógica interna, asumir que no lanza excepciones propias)
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
        usuarioLogueado = null;
    }


    private static void manejarMenuEventos() {
        System.out.println("\n--- Gestión de Eventos ---");
        System.out.println("1. Listar Todos los Eventos");
        System.out.println("2. Ver Detalle de Evento");
        if (usuarioLogueado instanceof Organizador) {
            System.out.println("3. Crear Nuevo Evento");
            System.out.println("4. Publicar Evento");
            System.out.println("5. Cancelar Evento");
            System.out.println("6. Ver Mis Eventos Creados");
            System.out.println("7. Iniciar Evento");
            System.out.println("8. Finalizar Evento");
            System.out.println("9. Editar Evento"); // Nueva opción
        }
        System.out.println("0. Volver al Menú Principal");
        System.out.print("Seleccione una opción: ");
        int opcion = leerOpcion();

        try {
            switch(opcion) {
                case 1: listarEventos(); break;
                case 2: verDetalleEvento(); break;
                case 3: if (usuarioLogueado instanceof Organizador) crearNuevoEvento(); else System.out.println("Opción solo para Organizadores."); break;
                case 4: if (usuarioLogueado instanceof Organizador) operarSobreEventoOrganizador("publicar"); else System.out.println("Opción solo para Organizadores."); break;
                case 5: if (usuarioLogueado instanceof Organizador) operarSobreEventoOrganizador("cancelar"); else System.out.println("Opción solo para Organizadores."); break;
                case 6: if (usuarioLogueado instanceof Organizador) verMisEventos(); else System.out.println("Opción solo para Organizadores."); break;
                case 7: if (usuarioLogueado instanceof Organizador) operarSobreEventoOrganizador("iniciar"); else System.out.println("Opción solo para Organizadores."); break;
                case 8: if (usuarioLogueado instanceof Organizador) operarSobreEventoOrganizador("finalizar"); else System.out.println("Opción solo para Organizadores."); break;
                case 9: if (usuarioLogueado instanceof Organizador) editarEventoOrganizador(); else System.out.println("Opción solo para Organizadores."); break; // Llamada al nuevo método
                case 0: break;
                default: System.out.println("Opción no válida.");
            }
        } catch (AplicacionException e) {
            System.err.println("Error de Evento: " + e.getMessage());
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
        // Usar buscarEventoPorIdConException para que lance la excepción si no se encuentra
        Evento e = eventoController.buscarEventoPorIdConException(idEvento);
        // Si llega aquí, el evento fue encontrado.
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
        String nombreLugar = leerString("Nombre del lugar: ");
        String dirLugar = leerString("Dirección del lugar: ");
        Lugar lugar = new Lugar(nombreLugar, dirLugar);
        int capacidad = leerInt("Capacidad total del evento: ");

        List<TipoEntrada> tipos = new ArrayList<>();
        String addTipoEntrada;
        TipoEntradaFactory genFactory = new EntradaGeneralFactory();
        do {
            System.out.println("Añadiendo tipo de entrada...");
            String nombreTipo = leerString("  Nombre del tipo (ej. General): ");
            double precioTipo = leerDouble("  Precio: ");
            int cantidadTipo = leerInt("  Cantidad total: ");
            int limiteUsrTipo = leerInt("  Límite por usuario: ");
            tipos.add(genFactory.crearTipoEntrada(nombreTipo, "Entrada " + nombreTipo, precioTipo, cantidadTipo, limiteUsrTipo));
            addTipoEntrada = leerString("¿Añadir otro tipo de entrada? (s/n): ");
        } while ("s".equalsIgnoreCase(addTipoEntrada));

        List<modelo.evento.Imagen> imagenes = new ArrayList<>();
        String addImagenStr;
        do {
            addImagenStr = leerString("¿Añadir imagen? (s/n): ");
            if ("s".equalsIgnoreCase(addImagenStr)) {
                String urlImg = leerString("  URL de la imagen: ");
                String descImg = leerString("  Descripción de la imagen (opcional): ");
                if (!urlImg.trim().isEmpty()) {
                    imagenes.add(new modelo.evento.Imagen(urlImg, descImg));
                } else {
                    System.out.println("URL de imagen no puede ser vacía. Imagen no añadida.");
                }
            }
        } while ("s".equalsIgnoreCase(addImagenStr));

        List<modelo.evento.Video> videos = new ArrayList<>();
        String addVideoStr;
        do {
            addVideoStr = leerString("¿Añadir video promocional? (s/n): ");
            if ("s".equalsIgnoreCase(addVideoStr)) {
                String urlVideo = leerString("  URL del video: ");
                String tituloVideo = leerString("  Título del video (opcional): ");
                if(!urlVideo.trim().isEmpty()){
                    videos.add(new modelo.evento.Video(urlVideo, tituloVideo));
                } else {
                     System.out.println("URL de video no puede ser vacía. Video no añadido.");
                }
            }
        } while ("s".equalsIgnoreCase(addVideoStr));


        Evento nuevo = eventoController.crearEvento(org, id, nombre, desc, cat, fecha, hora, lugar, capacidad, imagenes, videos, tipos);
        System.out.println("Evento '" + nuevo.getNombre() + "' creado. Estado actual: " + nuevo.getEstadoActual());
    }

    private static void operarSobreEventoOrganizador(String operacion){
        Organizador org = (Organizador) usuarioLogueado;
        String idEvento = leerString("Ingrese ID del evento a " + operacion + ": ");

        switch(operacion.toLowerCase()){
            case "publicar":
                eventoController.publicarEvento(idEvento, org);
                System.out.println("Evento publicado o solicitud enviada.");
                break;
            case "cancelar":
                eventoController.cancelarEvento(idEvento, org);
                System.out.println("Evento cancelado o solicitud enviada.");
                break;
            case "iniciar":
                eventoController.iniciarEvento(idEvento, org);
                System.out.println("Evento iniciado o solicitud enviada.");
                break;
            case "finalizar":
                eventoController.finalizarEvento(idEvento, org);
                System.out.println("Evento finalizado o solicitud enviada.");
                break;
            default:
                System.out.println("Operación desconocida sobre evento.");
                return;
        }
    }

    private static void editarEventoOrganizador() {
        Organizador org = (Organizador) usuarioLogueado;
        System.out.println("\n--- Editar Evento ---");
        String idEvento = leerString("Ingrese ID del evento a editar: ");

        Evento eventoActual;
        try {
            eventoActual = eventoController.buscarEventoPorIdConException(idEvento);
            System.out.println("Editando evento: " + eventoActual.getNombre() + " (ID: " + eventoActual.getId() + ")");
            System.out.println("Deje el campo en blanco y presione Enter si no desea modificar un valor específico.");
        } catch (EntidadNoEncontradaException e) {
            // La excepción será capturada por el manejador en manejarMenuEventos()
            // por lo que relanzarla o imprimir aquí es redundante si la estructura de captura ya existe.
            // System.err.println(e.getMessage());
            throw e; // Relanzar para que sea capturada por el try-catch de manejarMenuEventos
        }

        EventoDTO dto = new EventoDTO();
        String input;

        System.out.print("Nuevo nombre (actual: " + eventoActual.getNombre() + "): ");
        input = scanner.nextLine().trim();
        if (!input.isEmpty()) dto.setNombre(input);

        System.out.print("Nueva descripción (actual: " + eventoActual.getDescripcion() + "): ");
        input = scanner.nextLine().trim();
        // Si se quiere permitir explícitamente borrar la descripción, se necesitaría una lógica diferente.
        // Por ejemplo, si el usuario ingresa un carácter especial como "-" o similar.
        // Con la lógica actual, si input es "", no se actualiza. Si es " ", se actualiza a " ".
        if (!input.isEmpty() || (input.isEmpty() && !eventoActual.getDescripcion().isEmpty())) { // Actualizar si es nuevo o si se quiere borrar uno existente
            dto.setDescripcion(input);
        }


        System.out.print("Nueva categoría (actual: " + eventoActual.getCategoria() + "): ");
        input = scanner.nextLine().trim();
        if (!input.isEmpty()) dto.setCategoria(input);

        System.out.print("Nueva fecha (dd/MM/yyyy) (actual: " + new SimpleDateFormat("dd/MM/yyyy").format(eventoActual.getFecha()) + "): ");
        input = scanner.nextLine().trim();
        if (!input.isEmpty()) {
            try {
                dto.setFecha(new SimpleDateFormat("dd/MM/yyyy").parse(input));
            } catch (ParseException e) {
                System.out.println("Formato de fecha inválido. Este campo no será actualizado.");
            }
        }

        System.out.print("Nueva hora (HH:mm) (actual: " + eventoActual.getHora() + "): ");
        input = scanner.nextLine().trim();
        if (!input.isEmpty()) dto.setHora(input);

        System.out.print("Nueva capacidad (actual: " + eventoActual.getCapacidad() + "): ");
        input = scanner.nextLine().trim();
        if (!input.isEmpty()) {
            try {
                dto.setCapacidad(Integer.parseInt(input));
            } catch (NumberFormatException e) {
                System.out.println("Formato de capacidad inválido. Este campo no será actualizado.");
            }
        }

        if (!dto.tieneDatosParaActualizar()) {
            System.out.println("No se proporcionaron datos nuevos. No se realizaron cambios.");
            return;
        }

        eventoController.editarEvento(idEvento, dto, org);
        System.out.println("Evento actualizado con éxito.");
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
        System.out.println("\n--- Compras y Entradas ---");
        if (usuarioLogueado instanceof Asistente) {
            System.out.println("1. Comprar Entradas para un Evento");
            System.out.println("2. Ver Mi Historial de Compras");
            System.out.println("3. Cancelar una Compra (solicitar reembolso)");
            System.out.println("4. Deshacer Última Operación de Compra/Cancelación");
            System.out.println("5. Rehacer Última Operación Deshecha");
        } else if (usuarioLogueado == null) {
            System.out.println("Debe iniciar sesión como Asistente para gestionar compras.");
            return; // Salir del menú si no es asistente o no está logueado
        } else {
             System.out.println("Los Organizadores no gestionan compras de esta forma.");
             return; // Salir
        }
        System.out.println("0. Volver al Menú Principal");
        System.out.print("Seleccione una opción: ");
        int opcion = leerOpcion();

        try {
            if (usuarioLogueado instanceof Asistente) {
                Asistente asistente = (Asistente) usuarioLogueado;
                switch(opcion) {
                    case 1: comprarEntradasAsistente(asistente); break;
                    case 2: verHistorialComprasAsistente(asistente); break;
                    case 3: cancelarCompraAsistente(asistente); break;
                    case 4:
                        if (asistente.deshacerUltimaOperacion()) System.out.println("Última operación deshecha.");
                        else System.out.println("No se pudo deshacer o no hay nada que deshacer.");
                        break;
                    case 5:
                        if (asistente.rehacerUltimaOperacionDeshecha()) System.out.println("Última operación rehecha.");
                        else System.out.println("No se pudo rehacer o no hay nada que rehacer.");
                        break;
                    case 0: break;
                    default: System.out.println("Opción no válida.");
                }
            }
        } catch (AplicacionException e) {
            System.err.println("Error de Compra: " + e.getMessage());
        }
    }

    private static void comprarEntradasAsistente(Asistente asistente) {
        System.out.println("\n--- Comprar Entradas ---");
        listarEventos();
        String idEvento = leerString("Ingrese ID del evento para comprar entradas: ");

        Evento eventoSeleccionado = eventoController.buscarEventoPorIdConException(idEvento);
        // Si no lanza excepción, el evento existe.

        System.out.println("Ha seleccionado: " + eventoSeleccionado.getNombre());
        if (eventoSeleccionado.getTiposEntrada() == null || eventoSeleccionado.getTiposEntrada().isEmpty()) {
            // Esto podría ser una OperacionInvalidaException desde el controller si se intenta comprar
            // pero por ahora lo dejamos como una validación en la vista.
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
        String tipoPagoStr = leerString("Método de pago (ej. TARJETA_CREDITO, PAYPAL): ");
        String titularPago = leerString("Titular del método de pago: ");
        MetodoPago metodoPago = new MetodoPago(tipoPagoStr, titularPago);

        if(tipoPagoStr.toUpperCase().contains("TARJETA")) {
            String numT = leerString("Número Tarjeta (simulado): ");
            String venT = leerString("Vencimiento (MM/YY): ");
            String cvvT = leerString("CVV (simulado): ");
            metodoPago = new MetodoPago(tipoPagoStr, numT, titularPago, venT, cvvT);
        }

        String codDesc = leerString("Código de descuento (opcional, presione Enter si no tiene): ");
        if(codDesc.trim().isEmpty()) codDesc = null;

        // La llamada a procesarNuevaCompra ahora puede lanzar excepciones.
        compraController.procesarNuevaCompra(asistente, eventoSeleccionado, tipoSeleccionado, cantidad, metodoPago, codDesc);
        System.out.println("Solicitud de compra procesada. Verifique su historial o notificaciones para el estado final.");
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
        // Esta búsqueda podría mejorarse para usar un método en CompraController que lance excepción
        Optional<Compra> optCompra = asistente.getHistorialCompras().stream()
                                        .filter(c -> c.getId().equals(idCompra))
                                        .findFirst();
        if (optCompra.isEmpty()) {
            System.out.println("Compra con ID '" + idCompra + "' no encontrada en su historial.");
            return;
        }
        Compra compraACancelar = optCompra.get();
        String motivo = leerString("Motivo de la cancelación/reembolso: ");

        // Asumir que solicitarCancelacionCompra en CompraController ahora maneja excepciones
        compraController.solicitarCancelacionCompra(asistente, compraACancelar, motivo);
        System.out.println("Solicitud de cancelación/reembolso procesada.");
    }
}
