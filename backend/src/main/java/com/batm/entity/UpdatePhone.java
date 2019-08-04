package com.batm.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Entity
@Table(name = "w_update_phone")
public class UpdatePhone extends AbstractAuditingEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9027143155135946959L;

	@Id
	private Long id;

	@MapsId
	@OneToOne
	@JoinColumn(name = "user_id")
	private User user;
	
	private String phone;
	
	private Integer status = 0;

	public UpdatePhone() {
		super();
	}
	
}
