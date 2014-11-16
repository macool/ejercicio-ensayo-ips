package javaapplication1;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

class IpNoValida extends RuntimeException {
    public IpNoValida(String message) {
        super(message);
    }

    public IpNoValida(String message, Exception original) {
        super(message, original);
    }
}

class IpYaAsignada extends IpNoValida {
    public IpYaAsignada(String message) {
        super(message);
    }
}

class Ip {
    public String direccion;

    public Ip(String direccionIp) throws IpNoValida {
        this.direccion = direccionIp;
        try {
            validarDireccion();
        } catch(ArrayIndexOutOfBoundsException exception) {
            throw new IpNoValida("No es una IP válida", exception);
        }
    }

    @Override
    public String toString () {
        // Mostramos sólo la dirección de la IP
        return this.direccion;
    }

    /*
     * Valida que sea una dirección IP lo que estamos ingresando
     * Arroja excepciones en caso de no serlo
     */
    private void validarDireccion() throws IpNoValida {
        String[] octetos = octetos();
        if (octetos[0].equals("127")) {
            throw new IpNoValida("El primer número de ID de red no puede ser 127");
        }
        if (this.isBroadcast()) {
            throw new IpNoValida("Los números de ID del host no pueden ser todos 255");
        }
        if (octetos[1].equals("0") &&
            octetos[2].equals("0") &&
            octetos[3].equals("0")) {
            throw new IpNoValida("Los números de ID del host no pueden ser todos 0");
        }
        for (int i = 0; i < 4; i++) {
            if (octetos[i].isEmpty() || Integer.parseInt(octetos[i]) > 255) {
                throw new IpNoValida("No parece ser una IP válida");
            }
        }
    }

    /*
     * Nos permite saber si la IP es broadcast (si sus números de host son todos
     * 255)
     * Esto cambia dependiendo del tipo de IP
     */
    private boolean isBroadcast() {
        String[] octetos = octetos();
        if (this.isClase("A")) {
            // Los 3 últimos octetos definen el ID
            return octetos[1].equals("255") &&
                   octetos[2].equals("255") &&
                   octetos[3].equals("255");
        } else if (this.isClase("B")) {
            // Los 2 últimos octetos definen el ID
            return octetos[2].equals("255") && octetos[3].equals("255");
        } else if (this.isClase("C")) {
            // El último octeto define el ID
            return octetos[3].equals("255");
        }
        return false;
    }

    /*
     * Nos permite preguntar a una IP si es una IP de clase A, B o C
     */
    public boolean isClase(String className) {
        boolean isClase = false;
        int idRed = this.idRed();
        if (idRed >= 0 && idRed <= 127) {
            isClase = className.equals("A");
        }
        else if (idRed >= 128 && idRed <= 191) {
            isClase = className.equals("B");
        }
        else if (idRed >= 192 && idRed <= 223) {
            isClase = className.equals("C");
        }
        return isClase;
    }

    /*
     * Retorna un arreglo con los octetos de la IP
     */
    private String[] octetos() throws IpNoValida {
        String[] octetos = this.direccion.split("\\.");
        if (octetos.length != 4) {
            throw new IpNoValida("No parece ser una IP válida");
        }
        return octetos;
    }

    /*
     * Retorna el primer octeto de la IP
     */
    public int idRed() {
        return Integer.parseInt(octetos()[0]);
    }

    /*
     * Agrega la IP a un grupo de redes
     * Arroja IpYaAsignada si esa IP ya existe en el grupo de redes
     */
    public void agregarA(ArrayList red) throws IpYaAsignada {
        /*
         * Este `contains` llama a `equals` cuando compara las IPs
         */
        if (red.contains(this)) {
            throw new IpYaAsignada("Esa IP ya fue asignada");
        } else {
            red.add(this);
        }
    }

    /*
     * Nos permite comparar si esta IP es igual a otra, basadas en sus
     * direcciones
     */
    @Override
    public boolean equals(Object otherIp) {
        return this.toString().equals(otherIp.toString());
    }
}

public class JavaApplication1 {
    /*
     * Grupos de redes donde guardaremos las IPs
     */
    private final ArrayList<Ip> redA = new ArrayList<>();
    private final ArrayList<Ip> redB = new ArrayList<>();
    private final ArrayList<Ip> redC = new ArrayList<>();

    public static void main(String[] args) {
        JavaApplication1 app = new JavaApplication1();
        app.ejecutar();
    }

    /**
     * Lee ips desde la línea de comandos hasta que el usuario escriba 'salir'
     */
    public void ejecutar() {
        Scanner entrada = new Scanner(System.in);
        String instruccion;

        do {
            System.out.println("Por favor ingrese una dirección IP o 'salir'");
            instruccion = entrada.next();

            if (!instruccion.equals("salir")) {
                procesar(instruccion);
            }
        } while(!instruccion.equals("salir"));

        mostrar_resultados();
    }

    /**
     * Procesa la cadena de texto ingresada por el usuario
     * Agrega la IP al grupo de redes que pertenece
     * Muestra errores en pantalla si no es una IP
     * @param ip La cadena de texto que puede o no ser una IP
     */
    private void procesar(String ip) {
        try {
            Ip direccionIp = new Ip(ip);

            if (direccionIp.isClase("A")) {
                direccionIp.agregarA(redA);
            } else if (direccionIp.isClase("B")) {
                direccionIp.agregarA(redB);
            } else if (direccionIp.isClase("C")) {
                direccionIp.agregarA(redC);
            } else {
                System.out.println("No parece ser una dirección IP válida");
            }
        }
        catch (IpNoValida exception) {
            System.out.println(exception.getMessage());
        }
    }

    /*
     * Lista los resultados de los grupos de redes
     */
    private void mostrar_resultados() {
        System.out.println("\n== Resultados ==");
        System.out.println("Red A");
        resultados_para(redA);
        System.out.println("Red B");
        resultados_para(redB);
        System.out.println("Red C");
        resultados_para(redC);
    }

    /*
     * Lista las IPs que pertenecen a un grupo de red
     * Se encarga de ordenar las IPs dentro del grupo antes de mostrarlas
     */
    private void resultados_para(ArrayList red) {
        Collections.sort(red, new Comparator<Ip>() {
            @Override
            public int compare(Ip ip1, Ip ip2) {
                return ip1.toString().compareTo(ip2.toString());
            }
        });
        for (Object ip : red) {
            System.out.println("- " + ip.toString());
        }
    }
}
