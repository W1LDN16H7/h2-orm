package h2.orm.example;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Order entity with status field for demonstrating field-based queries
 */
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", unique = true, nullable = false)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private User customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "processed_date")
    private LocalDateTime processedDate;

    // Constructors
    public Order() {
        this.createdDate = LocalDateTime.now();
    }

    public Order(String orderNumber, User customer, Status status, Double amount) {
        this();
        this.orderNumber = orderNumber;
        this.customer = customer;
        this.status = status;
        this.amount = amount;

        if (status == Status.PROCESSED) {
            this.processedDate = LocalDateTime.now();
        }
    }

    // Status enum
    public enum Status {
        NEW,
        PROCESSED,
        SHIPPED,
        DELIVERED,
        CANCELLED,
        PENDING
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public User getCustomer() {
        return customer;
    }

    public void setCustomer(User customer) {
        this.customer = customer;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
        if (status == Status.PROCESSED && this.processedDate == null) {
            this.processedDate = LocalDateTime.now();
        }
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getProcessedDate() {
        return processedDate;
    }

    public void setProcessedDate(LocalDateTime processedDate) {
        this.processedDate = processedDate;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", orderNumber='" + orderNumber + '\'' +
                ", status=" + status +
                ", amount=" + amount +
                ", createdDate=" + createdDate +
                '}';
    }
}
