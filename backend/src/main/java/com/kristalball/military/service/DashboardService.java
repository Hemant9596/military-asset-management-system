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
        List<PurchaseRepository.DashboardDailyTotal> purchases = purchaseRepository.findDashboardDailyTotals(
                baseId, equipmentTypeId, from);
        List<TransferRepository.DashboardTransferTotal> transfers = transferRepository.findDashboardDailyTotals(
                baseId, equipmentTypeId, from);
        Long assignedQuantity = assignmentRepository.sumDashboardQuantities(baseId, equipmentTypeId, from, to);
        List<ExpenditureRepository.DashboardDailyTotal> expenditures = expenditureRepository
                .findDashboardDailyTotals(baseId, equipmentTypeId, from);
        List<InventoryRepository.DashboardInventoryTotal> inventory = inventoryRepository
                .findDashboardTotals(baseId, equipmentTypeId);
        List<OpeningBalanceRepository.DashboardDailyTotal> openingBalances = openingBalanceRepository
                .findDashboardDailyTotals(baseId, equipmentTypeId, from);

        int purchaseTotal = purchases.stream()
                .filter(p -> matchesDate(p.getEventDate(), from, to))
                .mapToInt(p -> p.getQuantity().intValue())
                .sum();
        int transferIn = 0;
        int transferOut = 0;
        int assignedTotal = assignedQuantity == null ? 0 : assignedQuantity.intValue();
        int expendedTotal = expenditures.stream()
                .filter(e -> matchesDate(e.getEventDate(), from, to))
                .mapToInt(e -> e.getQuantity().intValue())
                .sum();
        int currentBalance = inventory.stream()
                .mapToInt(i -> i.getTotalQuantity().intValue())
                .sum();
        LocalDate periodStart = from == null ? LocalDate.MIN : from;
        int movementSinceStart = purchases.stream()
                .mapToInt(p -> p.getQuantity().intValue())
                .sum();
        for (TransferRepository.DashboardTransferTotal transfer : transfers) {
            int quantity = transfer.getQuantity().intValue();
            if (matchesDate(transfer.getEventDate(), from, to)) {
                if (baseId == null || transfer.getDestinationBaseId().equals(baseId)) {
                    transferIn += quantity;
                }
                if (baseId == null || transfer.getSourceBaseId().equals(baseId)) {
                    transferOut += quantity;
                }
            }
            if (!transfer.getEventDate().isBefore(periodStart)) {
                if (baseId == null || transfer.getDestinationBaseId().equals(baseId)) {
                    movementSinceStart += quantity;
                }
                if (baseId == null || transfer.getSourceBaseId().equals(baseId)) {
                    movementSinceStart -= quantity;
                }
            }
        }
        movementSinceStart -= expenditures.stream()
                .mapToInt(e -> e.getQuantity().intValue())
                .sum();
        LocalDate balanceDate = from == null ? LocalDate.MIN : from;
        int openingBalanceMovement = openingBalances.stream()
                .filter(b -> b.getEventDate().isAfter(balanceDate)
                        && inPeriod(b.getEventDate(), from, to))
                .mapToInt(b -> b.getQuantity().intValue())
                .sum();
        int openingBalance = currentBalance - movementSinceStart
                - openingBalances.stream().filter(b -> b.getEventDate().isAfter(balanceDate))
                        .mapToInt(b -> b.getQuantity().intValue()).sum();
        int netMovement = openingBalanceMovement + purchaseTotal + transferIn - transferOut - expendedTotal;
        int closingBalance = openingBalance + netMovement;

        List<Map<String, Object>> movementTrend = buildTrend(baseId, from, to,
                purchases, transfers, expenditures, openingBalances);
        List<Map<String, Object>> inventoryByType = buildInventoryByType(inventory);
        List<Map<String, Object>> transfersByBase = buildTransfersByBase(from, to, transfers);

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

    private List<Map<String, Object>> buildTrend(Long baseId, LocalDate from, LocalDate to,
            List<PurchaseRepository.DashboardDailyTotal> purchases,
            List<TransferRepository.DashboardTransferTotal> transfers,
            List<ExpenditureRepository.DashboardDailyTotal> expenditures,
            List<OpeningBalanceRepository.DashboardDailyTotal> openingBalances) {
        List<Map<String, Object>> results = new ArrayList<>();
        LocalDate start = from != null ? from : LocalDate.now().minusDays(30);
        LocalDate end = to != null ? to : LocalDate.now();

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(7)) {
            int value = 0;
            for (PurchaseRepository.DashboardDailyTotal purchase : purchases) {
                if (!purchase.getEventDate().isBefore(date)
                        && purchase.getEventDate().isBefore(date.plusDays(7))) {
                    value += purchase.getQuantity().intValue();
                }
            }
            for (TransferRepository.DashboardTransferTotal transfer : transfers) {
                if (baseId != null
                        && !transfer.getEventDate().isBefore(date)
                        && transfer.getEventDate().isBefore(date.plusDays(7))) {
                    if (transfer.getDestinationBaseId().equals(baseId)) {
                        value += transfer.getQuantity().intValue();
                    }
                    if (transfer.getSourceBaseId().equals(baseId)) {
                        value -= transfer.getQuantity().intValue();
                    }
                }
            }
            for (ExpenditureRepository.DashboardDailyTotal expenditure : expenditures) {
                if (!expenditure.getEventDate().isBefore(date)
                        && expenditure.getEventDate().isBefore(date.plusDays(7))) {
                    value -= expenditure.getQuantity().intValue();
                }
            }
            for (OpeningBalanceRepository.DashboardDailyTotal openingBalance : openingBalances) {
                if (!openingBalance.getEventDate().isBefore(date)
                        && openingBalance.getEventDate().isBefore(date.plusDays(7))) {
                    value += openingBalance.getQuantity().intValue();
                }
            }
            Map<String, Object> row = new HashMap<>();
            row.put("label", date.toString());
            row.put("value", value);
            results.add(row);
        }
        return results;
    }

    private List<Map<String, Object>> buildInventoryByType(
            List<InventoryRepository.DashboardInventoryTotal> inventory) {
        return inventory.stream()
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", entry.getEquipmentTypeName());
                    map.put("quantity", entry.getAvailableQuantity().intValue());
                    return map;
                })
                .toList();
    }

    private List<Map<String, Object>> buildTransfersByBase(LocalDate from, LocalDate to,
            List<TransferRepository.DashboardTransferTotal> transfers) {
        Map<String, Integer> totals = new HashMap<>();
        for (TransferRepository.DashboardTransferTotal transfer : transfers) {
            if (!matchesDate(transfer.getEventDate(), from, to)) {
                continue;
            }
            String label = transfer.getSourceBaseName() + "->" + transfer.getDestinationBaseName();
            totals.merge(label, transfer.getQuantity().intValue(), Integer::sum);
        }
        return totals.entrySet().stream().map(entry -> {
            Map<String, Object> map = new HashMap<>();
            map.put("name", entry.getKey());
            map.put("value", entry.getValue());
            return map;
        }).toList();
    }
}
