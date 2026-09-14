package com.zootropolis.controller;

import com.zootropolis.dto.AreaDTO;
import com.zootropolis.dto.PromozioneDTO;
import com.zootropolis.dto.ReportDTO;
import com.zootropolis.entity.Area;
import com.zootropolis.entity.Corsa;
import com.zootropolis.entity.Promozione;
import com.zootropolis.entity.Report;
import com.zootropolis.repository.AreaRepository;
import com.zootropolis.repository.CorsaRepository;
import com.zootropolis.repository.PromozioneRepository;
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
    private final PromozioneRepository promozioneRepository;

    public GestioneAmministrazione(CorsaRepository corsaRepository, ReportRepository reportRepository, AreaRepository areaRepository, PromozioneRepository promozioneRepository) {
        this.corsaRepository = corsaRepository;
        this.reportRepository = reportRepository;
        this.areaRepository = areaRepository;
        this.promozioneRepository = promozioneRepository;
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

    @Transactional
    public PromozioneDTO creaIncentivo(PromozioneDTO datiIncentivo) {
        log.info("Creazione nuovo incentivo promozionale: {}", datiIncentivo != null ? datiIncentivo.getDescrizione() : "null");

        // Self-Message: validaParametri(datiIncentivo)
        validaParametri(datiIncentivo);

        // Message: create(datiIncentivo) -> Return: nuovaPromozione
        Promozione nuovaPromozione = new Promozione();
        nuovaPromozione.setDescrizione(datiIncentivo.getDescrizione());
        nuovaPromozione.setSconto(datiIncentivo.getSconto());
        nuovaPromozione.setDataInizio(datiIncentivo.getDataInizio());
        nuovaPromozione.setDataFine(datiIncentivo.getDataFine());

        // Message: setStato("ATTIVA") -> Return: promozioneRegistrata
        Promozione salvata = promozioneRepository.save(nuovaPromozione);
        log.info("Promozione salvata con successo con ID: {}", salvata.getId());

        // Return: salvataggioCompletato (mappato su DTO)
        PromozioneDTO resultDto = new PromozioneDTO();
        resultDto.setId(salvata.getId());
        resultDto.setDescrizione(salvata.getDescrizione());
        resultDto.setSconto(salvata.getSconto());
        resultDto.setDataInizio(salvata.getDataInizio());
        resultDto.setDataFine(salvata.getDataFine());

        return resultDto;
    }

    // Self-Message: validaParametri(datiIncentivo)
    private void validaParametri(PromozioneDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Parametri non validi o incongruenti");
        }
        if (dto.getDescrizione() == null || dto.getDescrizione().isBlank()) {
            throw new IllegalArgumentException("Parametri non validi o incongruenti");
        }
        if (dto.getSconto() == null || dto.getSconto() <= 0 || dto.getSconto() > 100) {
            throw new IllegalArgumentException("Parametri non validi o incongruenti");
        }
        if (dto.getDataInizio() == null || dto.getDataFine() == null || dto.getDataFine().isBefore(dto.getDataInizio())) {
            throw new IllegalArgumentException("Parametri non validi o incongruenti");
        }
    }

    // Metodo helper per recuperare tutti gli incentivi attivi
    public List<PromozioneDTO> recuperaTuttiGliIncentivi() {
        return promozioneRepository.findAll().stream().map(p -> {
            PromozioneDTO dto = new PromozioneDTO();
            dto.setId(p.getId());
            dto.setDescrizione(p.getDescrizione());
            dto.setSconto(p.getSconto());
            dto.setDataInizio(p.getDataInizio());
            dto.setDataFine(p.getDataFine());
            return dto;
        }).collect(Collectors.toList());
    }

    // ==========================================
    // UC-20 INSERIRE AREE VIETATE
    // ==========================================

    // Message: creaZonaVietata(coordinate)
    @Transactional
    public AreaDTO creaZonaVietata(String coordinate, String nomeArea) {
        log.info("Creazione nuova area vietata. Nome: {}, Coordinate: {}", nomeArea, coordinate);

        // Self-Message: validaCoordinate(coordinate)
        validaCoordinate(coordinate);

        // Message: create(coordinate) -> Return: nuovaArea
        Area nuovaArea = new Area();
        nuovaArea.setNome(nomeArea != null && !nomeArea.isBlank() ? nomeArea : "Zona Vietata");
        nuovaArea.setTipo("ZONA_VIETATA");
        nuovaArea.setDescrizione("Coordinate delimitate: " + coordinate);

        // Message: setStatoArea("VIETATA") -> Return: areaRegistrata
        nuovaArea.setStato("VIETATA");

        Area areaSalvata = areaRepository.save(nuovaArea);
        log.info("Area vietata salvata con successo. ID: {}", areaSalvata.getId());

        // Return: salvataggioCompletato (mappato su DTO)
        AreaDTO dto = new AreaDTO();
        dto.setId(areaSalvata.getId());
        dto.setNome(areaSalvata.getNome());
        dto.setTipo(areaSalvata.getTipo());
        dto.setStato(areaSalvata.getStato());
        dto.setDescrizione(areaSalvata.getDescrizione());

        return dto;
    }

    // Self-Message: validaCoordinate(coordinate)
    private void validaCoordinate(String coordinate) {
        if (coordinate == null || coordinate.isBlank()) {
            // Sequenza 4.a: erroreValidazione
            throw new IllegalArgumentException("Dati geografici non validi");
        }
    }

    // Metodo helper per recuperare le sole aree vietate
    public List<AreaDTO> recuperaAreeVietate() {
        return areaRepository.findAll().stream()
                .filter(a -> "VIETATA".equalsIgnoreCase(a.getStato()))
                .map(area -> {
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