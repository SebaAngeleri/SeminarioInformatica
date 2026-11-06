package com.vigilplus.sigma.reporte;

import com.vigilplus.sigma.modelo.Alerta;
import com.vigilplus.sigma.modelo.Severidad;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ResumenAlertasTest {

    private static Alerta alerta(Severidad s, String complejo) {
        return new Alerta(1, LocalDateTime.now(), "T", s, "PENDIENTE", complejo, "P");
    }

    @Test
    public void up28_cuentaPorSeveridadYOrdenaElRanking() {
        List<Alerta> alertas = new ArrayList<>();
        alertas.add(alerta(Severidad.MEDIA, "Torre Libertador"));
        alertas.add(alerta(Severidad.ALTA, "Complejo Los Aromos"));
        alertas.add(alerta(Severidad.ALTA, "Complejo Los Aromos"));
        alertas.add(alerta(Severidad.MEDIA, "Edificio Catalinas"));
        alertas.add(alerta(Severidad.MEDIA, "Complejo Los Aromos"));

        ResumenAlertas r = new ResumenAlertas(alertas);
        assertEquals(5, r.total());
        assertEquals(2, r.cantidad(Severidad.ALTA));
        assertEquals(3, r.cantidad(Severidad.MEDIA));
        assertEquals(0, r.cantidad(Severidad.BAJA));

        ConteoComplejo[] ranking = r.ranking();
        assertEquals(3, ranking.length);
        assertEquals("Complejo Los Aromos", ranking[0].getComplejo());
        assertEquals(3, ranking[0].getCantidad());
        assertEquals("Edificio Catalinas", ranking[1].getComplejo());   // empate: orden alfabetico
    }

    @Test
    public void up29_sinAlertas_resumenVacio() {
        ResumenAlertas r = new ResumenAlertas(List.of());
        assertEquals(0, r.total());
        assertEquals(0, r.ranking().length);
    }
}
