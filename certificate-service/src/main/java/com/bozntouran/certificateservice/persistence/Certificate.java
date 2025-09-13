package com.bozntouran.certificateservice.persistence;


import com.bozntouran.api.core.certificate.CertificateField;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@Table(name = "certificate")//, indexes = { @Index(name = "certificate_unique_idx", unique = true, columnList = "id,companyId") })
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long companyId;

    @NotNull
    @NotBlank
    private String name;

    @Version
    private int version;

    private double price;

    @Enumerated(EnumType.STRING)
    private CertificateField field;

    private String description;

    @UpdateTimestamp
    private Date updateDate;

    @Column(unique = true, nullable = false)
    private String publicId;

}
