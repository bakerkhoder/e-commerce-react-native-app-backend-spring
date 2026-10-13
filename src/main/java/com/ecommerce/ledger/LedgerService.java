package com.ecommerce.ledger;

import com.ecommerce.catalog.model.Product;
import com.ecommerce.catalog.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
public class LedgerService {

    private final LedgerRepository repository;
    private final ProductService productService;

    public LedgerService(LedgerRepository repository, ProductService productService) {
        this.repository = repository;
        this.productService = productService;
    }

    @Transactional
    public LedgerEntry record(Long userId, boolean isAdmin, LedgerRequest req) {
        Product product = productService.getById(req.productId());
        if (!isAdmin && !Objects.equals(product.getSellerId(), userId)) {
            throw new IllegalStateException("You can only record entries for your own products");
        }

        LocalDate date = req.entryDate() == null ? LocalDate.now() : req.entryDate();
        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("The date cannot be in the future");
        }

        LedgerEntry e = new LedgerEntry();
        e.setType(req.type());
        e.setSellerId(product.getSellerId());
        e.setProductId(product.getId());
        e.setProductName(product.getName());
        e.setQuantity(req.quantity());
        e.setUnitAmount(req.unitAmount());
        e.setEntryDate(date);
        e.setNotes(req.notes());

        if (req.type() == LedgerType.PURCHASE) {
            productService.applyPurchase(product.getId(), req.quantity(), req.unitAmount(), req.addToStock());
        } else {
            e.setUnitCost(product.getCostPrice());
            productService.reduceStock(product.getId(), req.quantity()); // a sale uses up stock
        }

        return repository.save(e);
    }

    /** {@code sellerScope == null} means "everything", which is what an admin sees. */
    @Transactional(readOnly = true)
    public List<LedgerEntry> since(Long sellerScope, LocalDate from) {
        return sellerScope == null
                ? repository.findByEntryDateGreaterThanEqual(from)
                : repository.findBySellerIdAndEntryDateGreaterThanEqual(sellerScope, from);
    }
}
