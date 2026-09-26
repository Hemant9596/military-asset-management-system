package com.kristalball.military.service;

import com.kristalball.military.dto.DashboardSummary;
import com.kristalball.military.entity.*;
import com.kristalball.military.repository.*;
import com.kristalball.military.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final PurchaseRepository purchaseRepository;
    private final TransferRepository transferRepository;
    private final AssignmentRepository assignmentRepository;
    private final ExpenditureRepository expenditureRepository;
    private final InventoryRepository inventoryRepository;
    private final BaseRepository baseRepository;
    private final EquipmentTypeRepository equipmentTypeRepository;
    private final BaseAccessService baseAccessService;
    private final OpeningBalanceRepository openingBalanceRepository;

    public DashboardService(
            PurchaseRepository purchaseRepository,
            TransferRepository transferRepository,
            AssignmentRepository assignmentRepository,
            ExpenditureRepository expenditureRepository,
            InventoryRepository inventoryRepository,
            BaseRepository baseRepository,
            EquipmentTypeRepository equipmentTypeRepository,
            BaseAccessService baseAccessService,
            OpeningBalanceRepository openingBalanceRepository) {
        this.purchaseRepository = purchaseRepository;
        this.transferRepository = transferRepository;
        this.assignmentRepository = assignmentRepository;
        this.expenditureRepository = expenditureRepository;
        this.inventoryRepository = inventoryRepository;
        this.baseRepository = baseRepository;
        this.equipmentTypeRepository = equipmentTypeRepository;
        this.baseAccessService = baseAccessService;
        this.openingBalanceRepository = openingBalanceRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummary getDashboard(Long requestedBaseId, Long equipmentTypeId, LocalDate from, LocalDate to) {
        Long baseId = baseAccessService.resolveBaseId(baseAccessService.currentUser(), requestedBaseId);
        if (from != null && to != null && from.isAfter(to)) {
            throw new BusinessException("Start date must be on or before end date");
        }
        List<Purchase> purchases = purchaseRepository.findAll().stream()
                .filter(p -> matchesBaseAndType(p.getBase(), p.getAsset().getEquipmentType(), baseId, equipmentTypeId))
                .filter(p -> matchesDate(p.getPurchaseDate(), from, to))
                .toList();

        List<Transfer> transfers = transferRepository.findAll().stream()
                .filter(t -> matchesBaseAndType(t.getSourceBase(), t.getAsset().getEquipmentType(), baseId,
                        equipmentTypeId)
                        || matchesBaseAndType(t.getDestinationBase(), t.getAsset().getEquipmentType(), baseId,
                                equipmentTypeId))
                .filter(t -> matchesDate(t.getTransferDate(), from, to))
                .toList();

        List<Assignment> assignments = assignmentRepository.findAll().stream()
                .filter(a -> matchesBaseAndType(a.getBase(), a.getAsset().getEquipmentType(), baseId, equipmentTypeId))
                .filter(a -> matchesDate(a.getAssignmentDate(), from, to))
                .toList();

        List<Expenditure> expenditures = expenditureRepository.findAll().stream()
                .filter(e -> matchesBaseAndType(e.getBase(), e.getAsset().getEquipmentType(), baseId, equipmentTypeId))
                .filter(e -> matchesDate(e.getExpenditureDate(), from, to))
                .toList();

        int purchaseTotal = purchases.stream().mapToInt(Purchase::getQuantity).sum();
        int transferIn = transfers.stream()
                .filter(t -> baseId == null || t.getDestinationBase().getId().equals(baseId))
                .mapToInt(Transfer::getQuantity)
                .sum();
        int transferOut = transfers.stream()
                .filter(t -> baseId == null || t.getSourceBase().getId().equals(baseId))
                .mapToInt(Transfer::getQuantity)
                .sum();
        int assignedTotal = assignments.stream().mapToInt(Assignment::getQuantity).sum();
        int expendedTotal = expenditures.stream().mapToInt(Expenditure::getQuantity).sum();
        int currentBalance = inventoryRepository.findAll().stream()
                .filter(i -> matchesBase(i.getBase(), baseId))
                .filter(i -> matchesEquipment(i.getAsset().getEquipmentType(), equipmentTypeId))
                .mapToInt(Inventory::getTotalQuantity)
                .sum();
        LocalDate periodStart = from == null ? LocalDate.MIN : from;
        int movementSinceStart = purchaseRepository.findAll().stream()
                .filter(p -> matchesBaseAndType(p.getBase(), p.getAsset().getEquipmentType(), baseId, equipmentTypeId))
                .filter(p -> !p.getPurchaseDate().isBefore(periodStart))
                .mapToInt(Purchase::getQuantity).sum();
        for (Transfer transfer : transferRepository.findAll()) {
            if (matchesEquipment(transfer.getAsset().getEquipmentType(), equipmentTypeId)
                    && !transfer.getTransferDate().isBefore(periodStart)) {
                if (baseId == null || transfer.getDestinationBase().getId().equals(baseId)) {
                    movementSinceStart += transfer.getQuantity();
                }
                if (baseId == null || transfer.getSourceBase().getId().equals(baseId)) {
                    movementSinceStart -= transfer.getQuantity();
                }
            }
        }
        movementSinceStart -= expenditureRepository.findAll().stream()
                .filter(e -> matchesBaseAndType(e.getBase(), e.getAsset().getEquipmentType(), baseId, equipmentTypeId))
                .filter(e -> !e.getExpenditureDate().isBefore(periodStart))
                .mapToInt(Expenditure::getQuantity).sum();
        LocalDate balanceDate = from == null ? LocalDate.MIN : from;
        List<OpeningBalance> scopedOpeningBalances = openingBalanceRepository.findAll().stream()
                .filter(b -> (baseId == null || b.getBase().getId().equals(baseId))
                        && (equipmentTypeId == null
                                || b.getAsset().getEquipmentType().getId().equals(equipmentTypeId)))
                .toList();
        int openingBalanceMovement = scopedOpeningBalances.stream()
                .filter(b -> b.getEffectiveDate().isAfter(balanceDate)
                        && inPeriod(b.getEffectiveDate(), from, to))
                .mapToInt(OpeningBalance::getQuantity).sum();
        int openingBalance = currentBalance - movementSinceStart
                - scopedOpeningBalances.stream().filter(b -> b.getEffectiveDate().isAfter(balanceDate))
                        .mapToInt(OpeningBalance::getQuantity).sum();
        int netMovement = openingBalanceMovement + purchaseTotal + transferIn - transferOut - expendedTotal;
        int closingBalance = openingBalance + netMovement;

        List<Map<String, Object>> movementTrend = buildTrend(baseId, equipmentTypeId, from, to);
        List<Map<String, Object>> inventoryByType = buildInventoryByType(baseId, equipmentTypeId);
        List<Map<String, Object>> transfersByBase = buildTransfersByBase(baseId, equipmentTypeId, from, to);

        return new DashboardSummary(openingBalance, purchaseTotal, transferIn, transferOut, netMovement,
                assignedTotal, expendedTotal, closingBalance, movementTrend, inventoryByType, transfersByBase);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getMovementDetails(Long baseId, Long equipmentTypeId, LocalDate from, LocalDate to) {
        DashboardSummary summary = getDashboard(baseId, equipmentTypeId, from, to);
        Long scopedBaseId = baseAccessService.resolveBaseId(baseAccessService.currentUser(), baseId);
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("openingBalance", summary.openingBalance());
        details.put("openingBalances", openingBalanceRepository.findAll().stream()
                .filter(b -> (scopedBaseId == null || b.getBase().getId().equals(scopedBaseId))
                        && (equipmentTypeId == null
                                || b.getAsset().getEquipmentType().getId().equals(equipmentTypeId)))
                .filter(b -> matchesDate(b.getEffectiveDate(), from, to))
                .map(this::openingBalanceDetail).toList());
        details.put("purchases", purchaseRepository.findAll().stream()
                .filter(p -> matchesBaseAndType(p.getBase(), p.getAsset().getEquipmentType(), scopedBaseId,
                        equipmentTypeId))
                .filter(p -> matchesDate(p.getPurchaseDate(), from, to)).map(this::purchaseDetail).toList());
        details.put("transfers", transferRepository.findAll().stream()
                .filter(t -> matchesBaseAndType(t.getSourceBase(), t.getAsset().getEquipmentType(), scopedBaseId,
                        equipmentTypeId)
                        || matchesBaseAndType(t.getDestinationBase(), t.getAsset().getEquipmentType(), scopedBaseId,
                                equipmentTypeId))
                .filter(t -> matchesDate(t.getTransferDate(), from, to)).map(this::transferDetail).toList());
        details.put("expenditures", expenditureRepository.findAll().stream()
                .filter(e -> matchesBaseAndType(e.getBase(), e.getAsset().getEquipmentType(), scopedBaseId,
                        equipmentTypeId))
                .filter(e -> matchesDate(e.getExpenditureDate(), from, to)).map(this::expenditureDetail).toList());
        details.put("transferIn", summary.transferIn());
        details.put("transferOut", summary.transferOut());
        details.put("netMovement", summary.netMovement());
        details.put("closingBalance", summary.closingBalance());
        return details;
    }

    private Map<String, Object> purchaseDetail(Purchase purchase) {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("date", purchase.getPurchaseDate());
        detail.put("base", purchase.getBase().getName());
        detail.put("asset", purchase.getAsset().getName());
        detail.put("quantity", purchase.getQuantity());
        detail.put("reference", purchase.getReferenceNumber());
        detail.put("vendor", purchase.getVendor());
        return detail;
    }

    private Map<String, Object> transferDetail(Transfer transfer) {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("date", transfer.getTransferDate());
        detail.put("source", transfer.getSourceBase().getName());
        detail.put("destination", transfer.getDestinationBase().getName());
        detail.put("asset", transfer.getAsset().getName());
        detail.put("quantity", transfer.getQuantity());
        detail.put("reference", transfer.getReferenceNumber());
        return detail;
    }

    private Map<String, Object> expenditureDetail(Expenditure expenditure) {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("date", expenditure.getExpenditureDate());
        detail.put("base", expenditure.getBase().getName());
        detail.put("asset", expenditure.getAsset().getName());
        detail.put("quantity", expenditure.getQuantity());
        detail.put("reason", expenditure.getReason());
        detail.put("reference", expenditure.getReference());
        return detail;
    }

    private Map<String, Object> openingBalanceDetail(OpeningBalance openingBalance) {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("effectiveDate", openingBalance.getEffectiveDate());
        detail.put("base", openingBalance.getBase().getName());
        detail.put("asset", openingBalance.getAsset().getName());
        detail.put("quantity", openingBalance.getQuantity());
        return detail;
    }

    private boolean matchesBaseAndType(Base base, EquipmentType equipmentType, Long baseId, Long equipmentTypeId) {
        return matchesBase(base, baseId) && matchesEquipment(equipmentType, equipmentTypeId);
    }

    private boolean matchesBase(Base base, Long baseId) {
        return baseId == null || base.getId().equals(baseId);
    }

    private boolean matchesEquipment(EquipmentType equipmentType, Long equipmentTypeId) {
        return equipmentTypeId == null || equipmentType.getId().equals(equipmentTypeId);
    }

    private boolean matchesDate(LocalDate date, LocalDate from, LocalDate to) {
        if (date == null)
            return true;
        if (from != null && date.isBefore(from))
            return false;
        if (to != null && date.isAfter(to))
            return false;
        return true;
    }

    private boolean inPeriod(LocalDate date, LocalDate from, LocalDate to) {
        return !date.isBefore(from) && !date.isAfter(to);
    }

    private List<Map<String, Object>> buildTrend(Long baseId, Long equipmentTypeId, LocalDate from, LocalDate to) {
        List<Map<String, Object>> results = new ArrayList<>();
        LocalDate start = from != null ? from : LocalDate.now().minusDays(30);
        LocalDate end = to != null ? to : LocalDate.now();

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(7)) {
            int value = 0;
            for (Purchase purchase : purchaseRepository.findAll()) {
                if (matchesBaseAndType(purchase.getBase(), purchase.getAsset().getEquipmentType(), baseId,
                        equipmentTypeId)
                        && !purchase.getPurchaseDate().isBefore(date)
                        && purchase.getPurchaseDate().isBefore(date.plusDays(7))) {
                    value += purchase.getQuantity();
                }
            }
            for (Transfer transfer : transferRepository.findAll()) {
                if (matchesEquipment(transfer.getAsset().getEquipmentType(), equipmentTypeId)
                        && !transfer.getTransferDate().isBefore(date)
                        && transfer.getTransferDate().isBefore(date.plusDays(7))) {
                    if (baseId != null && transfer.getDestinationBase().getId().equals(baseId)) {
                        value += transfer.getQuantity();
                    }
                    if (baseId != null && transfer.getSourceBase().getId().equals(baseId)) {
                        value -= transfer.getQuantity();
                    }
                }
            }
            for (Expenditure expenditure : expenditureRepository.findAll()) {
                if (matchesBaseAndType(expenditure.getBase(), expenditure.getAsset().getEquipmentType(), baseId,
                        equipmentTypeId)
                        && !expenditure.getExpenditureDate().isBefore(date)
                        && expenditure.getExpenditureDate().isBefore(date.plusDays(7))) {
                    value -= expenditure.getQuantity();
                }
            }
            for (OpeningBalance openingBalance : openingBalanceRepository.findAll()) {
                if (matchesBaseAndType(openingBalance.getBase(), openingBalance.getAsset().getEquipmentType(), baseId,
                        equipmentTypeId)
                        && !openingBalance.getEffectiveDate().isBefore(date)
                        && openingBalance.getEffectiveDate().isBefore(date.plusDays(7))) {
                    value += openingBalance.getQuantity();
                }
            }
            Map<String, Object> row = new HashMap<>();
            row.put("label", date.toString());
            row.put("value", value);
            results.add(row);
        }
        return results;
    }

    private List<Map<String, Object>> buildInventoryByType(Long baseId, Long equipmentTypeId) {
        return inventoryRepository.findAll().stream()
                .filter(i -> matchesBase(i.getBase(), baseId))
                .filter(i -> matchesEquipment(i.getAsset().getEquipmentType(), equipmentTypeId))
                .collect(Collectors.groupingBy(i -> i.getAsset().getEquipmentType().getName(),
                        Collectors.summingInt(Inventory::getAvailableQuantity)))
                .entrySet().stream()
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", entry.getKey());
                    map.put("quantity", entry.getValue());
                    return map;
                })
                .toList();
    }

    private List<Map<String, Object>> buildTransfersByBase(Long baseId, Long equipmentTypeId, LocalDate from,
            LocalDate to) {
        Map<String, Integer> totals = new HashMap<>();
        for (Transfer transfer : transferRepository.findAll()) {
            if (!matchesBaseAndType(transfer.getSourceBase(), transfer.getAsset().getEquipmentType(), baseId,
                    equipmentTypeId) &&
                    !matchesBaseAndType(transfer.getDestinationBase(), transfer.getAsset().getEquipmentType(), baseId,
                            equipmentTypeId)) {
                continue;
            }
            if (from != null && transfer.getTransferDate().isBefore(from))
                continue;
            if (to != null && transfer.getTransferDate().isAfter(to))
                continue;
            String label = transfer.getSourceBase().getName() + "->" + transfer.getDestinationBase().getName();
            totals.merge(label, transfer.getQuantity(), Integer::sum);
        }
        return totals.entrySet().stream().map(entry -> {
            Map<String, Object> map = new HashMap<>();
            map.put("name", entry.getKey());
            map.put("value", entry.getValue());
            return map;
        }).toList();
    }
}
