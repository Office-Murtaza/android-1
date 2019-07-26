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
	
	public Coin(String coinCode) {
		super();
		this.id = coinCode;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Coin other = (Coin) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	

}
