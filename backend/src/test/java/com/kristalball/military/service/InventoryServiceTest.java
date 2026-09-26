package com.kristalball.military.service;

import com.kristalball.military.dto.AssignmentRequest;
import com.kristalball.military.dto.ExpenditureRequest;
import com.kristalball.military.dto.PurchaseRequest;
import com.kristalball.military.dto.TransferRequest;
import com.kristalball.military.entity.*;
import com.kristalball.military.exception.BusinessException;
import com.kristalball.military.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

        @Mock
        private BaseRepository baseRepository;
        @Mock
        private AssetRepository assetRepository;
        @Mock
        private InventoryRepository inventoryRepository;
        @Mock
        private PurchaseRepository purchaseRepository;
        @Mock
        private TransferRepository transferRepository;
        @Mock
        private AssignmentRepository assignmentRepository;
        @Mock
        private ExpenditureRepository expenditureRepository;
        @Mock
        private UserRepository userRepository;
        @Mock
        private AuditService auditService;
        @Mock
        private BaseAccessService baseAccessService;
        @Mock
        private OpeningBalanceRepository openingBalanceRepository;

        private InventoryService inventoryService;
        private Base northBase;
        private Base southBase;
        private Asset asset;
        private User administrator;
        private User recipient;
        private Inventory northInventory;
        private Inventory southInventory;

        @BeforeEach
        void setUp() {
                inventoryService = new InventoryService(baseRepository, assetRepository, inventoryRepository,
                                purchaseRepository, transferRepository, assignmentRepository, expenditureRepository,
                                userRepository, auditService, baseAccessService, openingBalanceRepository);

                Role adminRole = Role.builder().name(RoleName.ADMIN).build();
                northBase = Base.builder().id(1L).name("North").active(true).build();
                southBase = Base.builder().id(2L).name("South").active(true).build();
                EquipmentType type = EquipmentType.builder().id(1L).name("Vehicle").build();
                asset = Asset.builder().id(1L).name("Truck").equipmentType(type).active(true).build();
                administrator = User.builder().id(1L).firstName("Test").lastName("Admin").email("admin@test.local")
                                .role(adminRole).enabled(true).build();
                recipient = User.builder().id(2L).firstName("Field").lastName("User").email("field@test.local")
                                .role(Role.builder().name(RoleName.BASE_COMMANDER).build()).assignedBase(northBase)
                                .enabled(true)
                                .build();
                northInventory = inventory(northBase, 5);
                southInventory = inventory(southBase, 0);

                when(baseRepository.findById(1L)).thenReturn(Optional.of(northBase));
                when(baseRepository.findById(2L)).thenReturn(Optional.of(southBase));
                when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));
                when(inventoryRepository.findByBaseAndAsset(northBase, asset)).thenReturn(Optional.of(northInventory));
        }

        @Test
        void purchaseTransferAssignmentAndExpenditureUpdateStockAndAudit() {
                LocalDate today = LocalDate.now();
                when(userRepository.findById(2L)).thenReturn(Optional.of(recipient));
                when(inventoryRepository.findByBaseAndAsset(southBase, asset)).thenReturn(Optional.of(southInventory));
                when(purchaseRepository.save(any(Purchase.class))).thenAnswer(invocation -> invocation.getArgument(0));
                when(transferRepository.save(any(Transfer.class))).thenAnswer(invocation -> invocation.getArgument(0));
                when(assignmentRepository.save(any(Assignment.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));
                when(expenditureRepository.save(any(Expenditure.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));
                inventoryService.createPurchase(new PurchaseRequest(1L, 1L, 3, today, "PO-1", "Vendor",
                                new BigDecimal("2.50"), null), administrator);
                inventoryService.createTransfer(new TransferRequest(1L, 2L, 1L, 2, today, "TR-1", null), administrator);
                inventoryService.createAssignment(new AssignmentRequest(1L, 1L, 2L, 1, today, "ACTIVE", null),
                                administrator);
                inventoryService.createExpenditure(new ExpenditureRequest(1L, 1L, 2, today, "Damage", "EX-1", null),
                                administrator);

                assertEquals(4, northInventory.getTotalQuantity());
                assertEquals(3, northInventory.getAvailableQuantity());
                assertEquals(1, northInventory.getAssignedQuantity());
                assertEquals(2, northInventory.getExpendedQuantity());
                assertEquals(2, southInventory.getTotalQuantity());
                assertEquals(2, southInventory.getAvailableQuantity());
                verify(purchaseRepository).save(any(Purchase.class));
                verify(transferRepository).save(any(Transfer.class));
                verify(assignmentRepository).save(any(Assignment.class));
                verify(expenditureRepository).save(any(Expenditure.class));
                verify(auditService, org.mockito.Mockito.times(4)).logAction(any(), any(), any(), any(), any(), any());
        }

        @Test
        void transferRejectsInsufficientStockWithoutWritingTransaction() {
                assertThrows(BusinessException.class, () -> inventoryService.createTransfer(
                                new TransferRequest(1L, 2L, 1L, 6, LocalDate.now(), null, null), administrator));

                assertEquals(5, northInventory.getAvailableQuantity());
                verify(transferRepository, never()).save(any(Transfer.class));
        }

        private Inventory inventory(Base base, int quantity) {
                return Inventory.builder().base(base).asset(asset).totalQuantity(quantity).availableQuantity(quantity)
                                .assignedQuantity(0).expendedQuantity(0).lastUpdated(LocalDateTime.now()).build();
        }
}