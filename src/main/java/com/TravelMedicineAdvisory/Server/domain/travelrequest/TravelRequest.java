package com.TravelMedicineAdvisory.Server.domain.travelrequest;

import com.TravelMedicineAdvisory.Server.core.base.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import com.TravelMedicineAdvisory.Server.domain.company.Company;
import com.TravelMedicineAdvisory.Server.domain.employee.Employee;
import java.time.LocalDateTime;

@Entity
@Table(name = "travel_requests")
@SQLDelete(sql = "UPDATE travel_requests SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class TravelRequest extends BaseEntity {

    private String destination;
    private String dates;
    private String status;
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    // Getters and Setters omitted for brevity but they exist implicitly
}
