package com.kristalball.military.service;

import com.kristalball.military.dto.*;
import com.kristalball.military.entity.*;
import com.kristalball.military.exception.BusinessException;
import com.kristalball.military.exception.ResourceNotFoundException;
import com.kristalball.military.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

@Service
public class InventoryService {

        private final BaseRepository baseRepository;
        private final AssetRepository assetRepository;
        private final InventoryRepository inventoryRepository;
        private final PurchaseRepository purchaseRepository;
        private final TransferRepository transferRepository;
        private final AssignmentRepository assignmentRepository;
        private final ExpenditureRepository expenditureRepository;
        private final UserRepository userRepository;
        private final AuditService auditService;
        private final BaseAccessService baseAccessService;
        private final OpeningBalanceRepository openingBalanceRepository;

        public InventoryService(
                        BaseRepository baseRepository,
                        AssetRepository assetRepository,
                        InventoryRepository inventoryRepository,
                        PurchaseRepository purchaseRepository,
                        TransferRepository transferRepository,
                        AssignmentRepository assignmentRepository,
                        ExpenditureRepository expenditureRepository,
                        UserRepository userRepository,
                        AuditService auditService,
                        BaseAccessService baseAccessService,
                        OpeningBalanceRepository openingBalanceRepository) {
                this.baseRepository = baseRepository;
                this.assetRepository = assetRepository;
                this.inventoryRepository = inventoryRepository;
                this.purchaseRepository = purchaseRepository;
                this.transferRepository = transferRepository;
                this.assignmentRepository = assignmentRepository;
                this.expenditureRepository = expenditureRepository;
                this.userRepository = userRepository;
                this.auditService = auditService;
                this.baseAccessService = baseAccessService;
                this.openingBalanceRepository = openingBalanceRepository;
        }

        @Transactional
        public OpeningBalanceResponse createOpeningBalance(OpeningBalanceRequest request, User user) {
                if (request.quantity() == null || request.quantity() <= 0) {
                        throw new BusinessException("Quantity must be greater than zero");
                }
                if (request.effectiveDate().isAfter(LocalDate.now())) {
                        throw new BusinessException("Opening balance date cannot be in the future");
                }
                Base base = baseRepository.findById(request.baseId())
                                .orElseThrow(() -> new ResourceNotFoundException("Base not found"));
                Asset asset = assetRepository.findById(request.assetId())
                                .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));
                if (openingBalanceRepository.existsByBaseAndAsset(base, asset)) {
                        throw new BusinessException("An opening balance already exists for this base and asset");
                }
                Inventory inventory = inventoryRepository.findByBaseAndAsset(base, asset)
                                .orElseGet(() -> Inventory.builder().base(base).asset(asset).totalQuantity(0)
                                                .availableQuantity(0).assignedQuantity(0).expendedQuantity(0)
                                                .lastUpdated(LocalDateTime.now()).build());
                if (inventory.getTotalQuantity() != 0 || inventory.getAssignedQuantity() != 0
                                || inventory.getExpendedQuantity() != 0) {
                        throw new BusinessException("Opening balance can only be recorded before stock activity");
                }

                OpeningBalance openingBalance = openingBalanceRepository.save(new OpeningBalance(
                                base, asset, user, request.quantity(), request.effectiveDate()));
                inventory.setTotalQuantity(request.quantity());
                inventory.setAvailableQuantity(request.quantity());
                inventory.setLastUpdated(LocalDateTime.now());
                inventoryRepository.save(inventory);
                auditService.logAction(user, "CREATE_OPENING_BALANCE", "OPENING_BALANCE", openingBalance.getId(),
                                "Recorded opening balance for " + asset.getName() + " at " + base.getName()
                                                + " quantity " + request.quantity(),
                                null);
                return toOpeningBalanceResponse(openingBalance);
        }

        @Transactional(readOnly = true)
        public List<OpeningBalanceResponse> getOpeningBalances() {
                return openingBalanceRepository.findAll().stream().map(this::toOpeningBalanceResponse).toList();
        }

        @Transactional
        public PurchaseResponse createPurchase(PurchaseRequest request, User user) {
                baseAccessService.enforceBaseAccess(user, request.baseId());
                validateNotFuture(request.purchaseDate(), "Purchase date");
                if (request.quantity() == null || request.quantity() <= 0) {
                        throw new BusinessException("Quantity must be greater than zero");
                }
                Base base = baseRepository.findById(request.baseId())
                                .orElseThrow(() -> new ResourceNotFoundException("Base not found"));
                Asset asset = assetRepository.findById(request.assetId())
                                .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));
                if (request.unitCost() == null || request.unitCost().signum() < 0) {
                        throw new BusinessException("Unit cost must be zero or greater");
                }

                Purchase purchase = Purchase.builder()
                                .base(base)
                                .asset(asset)
                                .createdBy(user)
                                .quantity(request.quantity())
                                .purchaseDate(request.purchaseDate())
                                .referenceNumber(request.referenceNumber())
                                .vendor(request.vendor())
                                .unitCost(request.unitCost())
                                .totalCost(request.unitCost().multiply(BigDecimal.valueOf(request.quantity())))
                                .notes(request.notes())
                                .build();
                purchase = purchaseRepository.save(purchase);

                Inventory inventory = getOrCreateInventory(base, asset);
                inventory.setTotalQuantity(inventory.getTotalQuantity() + request.quantity());
                inventory.setAvailableQuantity(inventory.getAvailableQuantity() + request.quantity());
                inventory.setLastUpdated(LocalDateTime.now());
                inventoryRepository.save(inventory);

                auditService.logAction(user, "CREATE_PURCHASE", "PURCHASE", purchase.getId(),
                                "Created purchase for " + asset.getName() + " at " + base.getName() + " quantity "
                                                + request.quantity(),
                                null);

                return toPurchaseResponse(purchase);
        }

        @Transactional
        public TransferResponse createTransfer(TransferRequest request, User user) {
                baseAccessService.enforceBaseAccess(user, request.sourceBaseId());
                validateNotFuture(request.transferDate(), "Transfer date");
                if (request.sourceBaseId() != null && request.sourceBaseId().equals(request.destinationBaseId())) {
                        throw new BusinessException("Source and destination bases cannot be the same");
                }
                if (request.quantity() == null || request.quantity() <= 0) {
                        throw new BusinessException("Quantity must be greater than zero");
                }

                Base sourceBase = baseRepository.findById(request.sourceBaseId())
                                .orElseThrow(() -> new ResourceNotFoundException("Source base not found"));
                Base destinationBase = baseRepository.findById(request.destinationBaseId())
                                .orElseThrow(() -> new ResourceNotFoundException("Destination base not found"));
                Asset asset = assetRepository.findById(request.assetId())
                                .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));

                Inventory sourceInventory = inventoryRepository.findByBaseAndAsset(sourceBase, asset)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Inventory not found for source base and asset"));
                if (sourceInventory.getAvailableQuantity() < request.quantity()) {
                        throw new BusinessException("Insufficient available inventory at source base");
                }

                Inventory destinationInventory = inventoryRepository.findByBaseAndAsset(destinationBase, asset)
                                .orElseGet(() -> inventoryRepository.save(Inventory.builder()
                                                .base(destinationBase)
                                                .asset(asset)
                                                .totalQuantity(0)
                                                .availableQuantity(0)
                                                .assignedQuantity(0)
                                                .expendedQuantity(0)
                                                .lastUpdated(LocalDateTime.now())
                                                .build()));

                sourceInventory.setTotalQuantity(sourceInventory.getTotalQuantity() - request.quantity());
                sourceInventory.setAvailableQuantity(sourceInventory.getAvailableQuantity() - request.quantity());
                sourceInventory.setLastUpdated(LocalDateTime.now());

                destinationInventory.setTotalQuantity(destinationInventory.getTotalQuantity() + request.quantity());
                destinationInventory
                                .setAvailableQuantity(destinationInventory.getAvailableQuantity() + request.quantity());
                destinationInventory.setLastUpdated(LocalDateTime.now());

                inventoryRepository.save(sourceInventory);
                inventoryRepository.save(destinationInventory);

                Transfer transfer = Transfer.builder()
                                .sourceBase(sourceBase)
                                .destinationBase(destinationBase)
                                .asset(asset)
                                .createdBy(user)
                                .quantity(request.quantity())
                                .transferDate(request.transferDate())
                                .referenceNumber(request.referenceNumber())
                                .notes(request.notes())
                                .build();
                transfer = transferRepository.save(transfer);

                auditService.logAction(user, "CREATE_TRANSFER", "TRANSFER", transfer.getId(),
                                "Transferred " + request.quantity() + " of " + asset.getName() + " from "
                                                + sourceBase.getName()
                                                + " to " + destinationBase.getName(),
                                null);
                return toTransferResponse(transfer);
        }

        @Transactional
        public AssignmentResponse createAssignment(AssignmentRequest request, User user) {
                baseAccessService.enforceBaseAccess(user, request.baseId());
                validateNotFuture(request.assignmentDate(), "Assignment date");
                if (request.quantity() == null || request.quantity() <= 0) {
                        throw new BusinessException("Quantity must be greater than zero");
                }
                Base base = baseRepository.findById(request.baseId())
                                .orElseThrow(() -> new ResourceNotFoundException("Base not found"));
                Asset asset = assetRepository.findById(request.assetId())
                                .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));
                User assignedTo = userRepository.findById(request.assignedToUserId())
                                .orElseThrow(() -> new ResourceNotFoundException("Assigned user not found"));
                if (user.getRole().getName() != RoleName.ADMIN
                                && (assignedTo.getAssignedBase() == null
                                                || !assignedTo.getAssignedBase().getId().equals(base.getId()))) {
                        throw new org.springframework.security.access.AccessDeniedException(
                                        "Recipient must be assigned to the selected base");
                }

                Inventory inventory = inventoryRepository.findByBaseAndAsset(base, asset)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Inventory not found for base and asset"));
                if (inventory.getAvailableQuantity() < request.quantity()) {
                        throw new BusinessException("Insufficient available inventory to assign");
                }

                inventory.setAvailableQuantity(inventory.getAvailableQuantity() - request.quantity());
                inventory.setAssignedQuantity(inventory.getAssignedQuantity() + request.quantity());
                inventory.setLastUpdated(LocalDateTime.now());
                inventoryRepository.save(inventory);

                Assignment assignment = Assignment.builder()
                                .base(base)
                                .asset(asset)
                                .assignedTo(assignedTo)
                                .createdBy(user)
                                .quantity(request.quantity())
                                .assignmentDate(request.assignmentDate())
                                .status(request.status())
                                .notes(request.notes())
                                .build();
                assignment = assignmentRepository.save(assignment);

                auditService.logAction(user, "CREATE_ASSIGNMENT", "ASSIGNMENT", assignment.getId(),
                                "Assigned " + request.quantity() + " of " + asset.getName() + " to "
                                                + assignedTo.getEmail(),
                                null);
                return toAssignmentResponse(assignment);
        }

        @Transactional
        public ExpenditureResponse createExpenditure(ExpenditureRequest request, User user) {
                baseAccessService.enforceBaseAccess(user, request.baseId());
                validateNotFuture(request.expenditureDate(), "Expenditure date");
                if (request.quantity() == null || request.quantity() <= 0) {
                        throw new BusinessException("Quantity must be greater than zero");
                }
                Base base = baseRepository.findById(request.baseId())
                                .orElseThrow(() -> new ResourceNotFoundException("Base not found"));
                Asset asset = assetRepository.findById(request.assetId())
                                .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));

                Inventory inventory = inventoryRepository.findByBaseAndAsset(base, asset)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Inventory not found for base and asset"));
                if (inventory.getAvailableQuantity() < request.quantity()) {
                        throw new BusinessException("Insufficient available inventory to expend");
                }

                inventory.setAvailableQuantity(inventory.getAvailableQuantity() - request.quantity());
                inventory.setExpendedQuantity(inventory.getExpendedQuantity() + request.quantity());
                inventory.setTotalQuantity(inventory.getTotalQuantity() - request.quantity());
                inventory.setLastUpdated(LocalDateTime.now());
                inventoryRepository.save(inventory);

                Expenditure expenditure = Expenditure.builder()
                                .base(base)
                                .asset(asset)
                                .createdBy(user)
                                .quantity(request.quantity())
                                .expenditureDate(request.expenditureDate())
                                .reason(request.reason())
                                .reference(request.reference())
                                .notes(request.notes())
                                .build();
                expenditure = expenditureRepository.save(expenditure);

                auditService.logAction(user, "CREATE_EXPENDITURE", "EXPENDITURE", expenditure.getId(),
                                "Expended " + request.quantity() + " of " + asset.getName() + " for "
                                                + request.reason(),
                                null);
                return toExpenditureResponse(expenditure);
        }

        public List<InventoryResponse> getInventory(Long baseId, Long equipmentTypeId) {
                Long scopedBaseId = baseAccessService.resolveBaseId(baseAccessService.currentUser(), baseId);
                return inventoryRepository.findAll().stream()
                                .filter(item -> scopedBaseId == null || item.getBase().getId().equals(scopedBaseId))
                                .filter(item -> equipmentTypeId == null
                                                || item.getAsset().getEquipmentType().getId().equals(equipmentTypeId))
                                .map(this::toInventoryResponse)
                                .toList();
        }

        private Inventory getOrCreateInventory(Base base, Asset asset) {
                return inventoryRepository.findByBaseAndAsset(base, asset)
                                .orElseGet(() -> inventoryRepository.save(Inventory.builder()
                                                .base(base)
                                                .asset(asset)
                                                .totalQuantity(0)
                                                .availableQuantity(0)
                                                .assignedQuantity(0)
                                                .expendedQuantity(0)
                                                .lastUpdated(LocalDateTime.now())
                                                .build()));
        }

        private void validateNotFuture(LocalDate date, String fieldName) {
                if (date != null && date.isAfter(LocalDate.now())) {
                        throw new BusinessException(fieldName + " cannot be in the future");
                }
        }

        private InventoryResponse toInventoryResponse(Inventory inventory) {
                return new InventoryResponse(
                                inventory.getId(),
                                inventory.getBase().getId(),
                                inventory.getBase().getName(),
                                inventory.getAsset().getId(),
                                inventory.getAsset().getName(),
                                inventory.getAsset().getEquipmentType().getName(),
                                inventory.getTotalQuantity(),
                                inventory.getAvailableQuantity(),
                                inventory.getAssignedQuantity(),
                                inventory.getExpendedQuantity(),
                                inventory.getLastUpdated());
        }

        private PurchaseResponse toPurchaseResponse(Purchase purchase) {
                return new PurchaseResponse(
                                purchase.getId(),
                                purchase.getBase().getId(),
                                purchase.getBase().getName(),
                                purchase.getAsset().getId(),
                                purchase.getAsset().getName(),
                                purchase.getQuantity(),
                                purchase.getPurchaseDate(),
                                purchase.getReferenceNumber(),
                                purchase.getVendor(),
                                purchase.getUnitCost(),
                                purchase.getTotalCost(),
                                purchase.getNotes(),
                                purchase.getCreatedBy().getFirstName() + " " + purchase.getCreatedBy().getLastName(),
                                purchase.getCreatedAt());
        }

        private TransferResponse toTransferResponse(Transfer transfer) {
                return new TransferResponse(
                                transfer.getId(),
                                transfer.getSourceBase().getId(),
                                transfer.getSourceBase().getName(),
                                transfer.getDestinationBase().getId(),
                                transfer.getDestinationBase().getName(),
                                transfer.getAsset().getId(),
                                transfer.getAsset().getName(),
                                transfer.getQuantity(),
                                transfer.getTransferDate(),
                                transfer.getReferenceNumber(),
                                transfer.getNotes(),
                                transfer.getCreatedBy().getFirstName() + " " + transfer.getCreatedBy().getLastName(),
                                transfer.getCreatedAt());
        }

        private AssignmentResponse toAssignmentResponse(Assignment assignment) {
                return new AssignmentResponse(
                                assignment.getId(),
                                assignment.getBase().getId(),
                                assignment.getBase().getName(),
                                assignment.getAsset().getId(),
                                assignment.getAsset().getName(),
                                assignment.getAssignedTo().getId(),
                                assignment.getAssignedTo().getFirstName() + " "
                                                + assignment.getAssignedTo().getLastName(),
                                assignment.getQuantity(),
                                assignment.getAssignmentDate(),
                                assignment.getStatus(),
                                assignment.getNotes(),
                                assignment.getCreatedBy().getFirstName() + " "
                                                + assignment.getCreatedBy().getLastName(),
                                assignment.getCreatedAt());
        }

        private ExpenditureResponse toExpenditureResponse(Expenditure expenditure) {
                return new ExpenditureResponse(
                                expenditure.getId(),
                                expenditure.getBase().getId(),
                                expenditure.getBase().getName(),
                                expenditure.getAsset().getId(),
                                expenditure.getAsset().getName(),
                                expenditure.getQuantity(),
                                expenditure.getExpenditureDate(),
                                expenditure.getReason(),
                                expenditure.getReference(),
                                expenditure.getNotes(),
                                expenditure.getCreatedBy().getFirstName() + " "
                                                + expenditure.getCreatedBy().getLastName(),
                                expenditure.getCreatedAt());
        }

        private OpeningBalanceResponse toOpeningBalanceResponse(OpeningBalance openingBalance) {
                return new OpeningBalanceResponse(openingBalance.getId(), openingBalance.getBase().getId(),
                                openingBalance.getBase().getName(), openingBalance.getAsset().getId(),
                                openingBalance.getAsset().getName(), openingBalance.getQuantity(),
                                openingBalance.getEffectiveDate(), openingBalance.getCreatedBy().getFirstName() + " "
                                                + openingBalance.getCreatedBy().getLastName(),
                                openingBalance.getCreatedAt());
        }
}
