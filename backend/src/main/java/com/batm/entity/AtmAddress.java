package com.batm.entity;

import java.math.BigDecimal;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "w_atm_address")
public class AtmAddress extends AbstractAuditingEntity{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "address_id")
	private Long id;
	
	@Column(name = "location_name")
	private String locationName;
	
	private String address;
	
	private BigDecimal latitude;
	
	private BigDecimal longitude;
	
	@JsonManagedReference
	@OneToMany(mappedBy="atmAddress")
    private Set<OpenHour> openHours;
	
	
}
