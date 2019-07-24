package com.batm.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "w_open_hour")
public class OpenHour extends AbstractAuditingEntity{

	//address_id, days, hours
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	
	@JsonBackReference
	@ManyToOne
	@JoinColumn(name="address_id", nullable=false)
	private AtmAddress atmAddress;
	
	@Column(name = "days")
	private String days;
	
	@Column(name = "hours")
	private String hours;
	
}
