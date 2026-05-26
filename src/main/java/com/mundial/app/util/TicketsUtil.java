
package com.mundial.app.util;


import com.mundial.app.model.TicketModel;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author jdmm0
 */
public class TicketsUtil {

    private static final List<TicketModel> lista = new ArrayList<>();

    //agregar un ticket al carrito de venta
    public static void agregar(TicketModel ticket) {
        //evitar duplicados por si dan click 2 veces al mismo ticket
        if (!contiene(ticket.getId())) {
            lista.add(ticket);
        }
    }

    //quitar un ticket del carrito
    public static void quitar(int ticketId) {
        lista.removeIf(t -> t.getId() == ticketId);
    }

    //vaciar el carrito completo
    public static void limpiar() {
        lista.clear();
    }

    //verificar si ya esta en el carrito
    public static boolean contiene(int ticketId) {
        return lista.stream().anyMatch(t -> t.getId() == ticketId);
    }

    //traer la lista completa para mostrar
    public static List<TicketModel> getLista() {
        return new ArrayList<>(lista);
    }

    //cuantos tickets hay en el carrito
    public static int cantidad() {
        return lista.size();
    }
}
