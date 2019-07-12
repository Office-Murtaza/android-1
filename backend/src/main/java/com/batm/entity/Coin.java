package com.batm.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "w_coins")
public class Coin extends AbstractAuditingEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1712969322089989538L;

	@Id
	@Column(name = "coin_id")
	private String id;

	private String coinName;
	
	@Column(name = "order_index")
	private Integer orderIndex;

	public Coin(String coinCode, String coinName, Integer index) {
		super();
		this.id = coinCode;
		this.coinName = coinName;
		this.orderIndex = index;
	}
	
	

}
