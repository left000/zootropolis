package com.zootropolis.controller;

import com.zootropolis.dto.AreaDTO;
import com.zootropolis.dto.ReportDTO;
import com.zootropolis.entity.Area;
import com.zootropolis.entity.Corsa;
import com.zootropolis.entity.Report;
import com.zootropolis.repository.AreaRepository;
import com.zootropolis.repository.CorsaRepository;
import com.zootropolis.repository.ReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GestioneAmministrazione {

    private static final Logger log = LoggerFactory.getLogger(GestioneAmministrazione.class);
    private final CorsaRepository corsaRepository;
    private final ReportRepository reportRepository;
    private final AreaRepository areaRepository;

    public GestioneAmministrazione(CorsaRepository corsaRepository, ReportRepository reportRepository, AreaRepository areaRepository) {
        this.corsaRepository = corsaRepository;
        this.reportRepository = reportRepository;
        this.areaRepository = areaRepository;
    }

    // ==========================================
    // UC-17 RICEVERE REPORT
    // ==========================================

    // Sequenza 1.a: report automatico
    public boolean configuraReportPeriodico(String frequenza) {
        log.info("Impostazione report automatico con frequenza: {}", frequenza);
        salvaPreferenzaEAvviaTimer(frequenza);
        return true;
    }

    private void salvaPreferenzaEAvviaTimer(String frequenza) {
        log.info("Preferenza salvata e timer avviato per frequenza: {}", frequenza);
    }

    // Sequenza Principale / 2.a: manuale
    @Transactional
    public ReportDTO generaReport(LocalDate dataInizio, LocalDate dataFine) {
        log.info("Generazione report per periodo: {} - {}", dataInizio, dataFine);

        LocalDateTime start = dataInizio.atStartOfDay();
        LocalDateTime end = dataFine.atTime(LocalTime.MAX);

        // Self-Message: recuperaDatiUtilizzo(dataInizio, dataFine)
        List<Corsa> corsePeriodo = corsaRepository.findByOraInizioBetween(start, end);

        // Sequenza 2.a: eccezioneNessunDato
        if (corsePeriodo.isEmpty()) {
            throw new IllegalStateException("Nessun dato nel periodo selezionato");
        }

        // Calcolo dati aggregati usando getImporto()
        int totaleCorse = corsePeriodo.size();
        double totaleIncassi = corsePeriodo.stream()
                .mapToDouble(c -> c.getImporto() != null ? c.getImporto() : 0.0)
                .sum();

        String datiAggregati = String.format("Periodo: %s / %s | Totale Corse: %d | Incasso Totale: %.2f €",
                dataInizio, dataFine, totaleCorse, totaleIncassi);

        // Salvataggio Entity Report
        Report report = new Report();
        report.setTipo(false); // false = Manuale, true = Automatico
        report.setData(LocalDate.now());
        report.setContenuto(datiAggregati);

        Report reportSalvato = reportRepository.save(report);

        // Mappatura su ReportDTO
        ReportDTO dto = new ReportDTO();
        dto.setId(reportSalvato.getId());
        dto.setTipo(reportSalvato.getTipo());
        dto.setData(reportSalvato.getData());
        dto.setContenuto(reportSalvato.getContenuto());

        return dto;
    }

    // ==========================================
    // UC-18 INSERIRE LAVORI URBANI
    // ==========================================

    // Message: inserisciLavoriUrbani(coordinate, date)
    @Transactional
    public AreaDTO inserisciLavoriUrbani(String coordinate, String date, String nomeArea) {
        log.info("Inserimento lavori urbani per coordinate: {}, date: {}", coordinate, date);

        // Self-Message: validaDati(coordinate, date)
        validaDatiLavori(coordinate, date);

        // Message: create(coordinate, date) -> Return: nuovaArea
        Area nuovaArea = new Area();
        nuovaArea.setNome(nomeArea != null && !nomeArea.isBlank() ? nomeArea : "Cantiere Lavori Urbani");
        nuovaArea.setTipo("LAVORI_URBANI");
        nuovaArea.setDescrizione("Coordinate: " + coordinate + " | Periodo: " + date);

        // Message: setStatoArea("VIETATA") -> Return: areaRegistrata
        nuovaArea.setStato("VIETATA");

        Area areaSalvata = areaRepository.save(nuovaArea);
        log.info("Area per lavori urbani registrata con successo. ID: {}", areaSalvata.getId());

        // Return: aggiornamentoCompletato (mappato su DTO)
        AreaDTO dto = new AreaDTO();
        dto.setId(areaSalvata.getId());
        dto.setNome(areaSalvata.getNome());
        dto.setTipo(areaSalvata.getTipo());
        dto.setStato(areaSalvata.getStato());
        dto.setDescrizione(areaSalvata.getDescrizione());

        return dto;
    }

    // Self-Message: validaDati(coordinate, date)
    private void validaDatiLavori(String coordinate, String date) {
        if (coordinate == null || coordinate.isBlank() || date == null || date.isBlank()) {
            // Sequenza 4.a: erroreValidazione
            throw new IllegalArgumentException("Dati non validi: coordinate e date obbligatorie.");
        }
    }

    // Recupera la lista di tutte le aree per la visualizzazione nel pannello
    public List<AreaDTO> recuperaTutteLeAree() {
        return areaRepository.findAll().stream().map(area -> {
            AreaDTO dto = new AreaDTO();
            dto.setId(area.getId());
            dto.setNome(area.getNome());
            dto.setTipo(area.getTipo());
            dto.setStato(area.getStato());
            dto.setDescrizione(area.getDescrizione());
            return dto;
        }).collect(Collectors.toList());
    }
}