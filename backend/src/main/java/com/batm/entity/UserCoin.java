package com.batm.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "w_user_coin_map")
public class UserCoin extends AbstractAuditingEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1712969322089989538L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne
	@JoinColumn(name = "coin_id", nullable = false)
	private Coin coin;

	@Column(name = "coin_id", insertable = false, updatable = false)
	private String coinId;

	private String publicKey;

	public UserCoin() {
		super();
	}
	
	public UserCoin(User user, Coin coin, String publicKey) {
		super();
		this.user = user;
		this.coin = coin;
		if(this.coin != null) {
			this.coinId = this.coin.getId();
		}
		this.publicKey = publicKey;
	}
	
	public UserCoin(String coinId) {
		super();
		this.coinId = coinId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((coinId == null) ? 0 : coinId.hashCode());
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
		UserCoin other = (UserCoin) obj;
		if (coinId == null) {
			if (other.coinId != null)
				return false;
		} else if (!coinId.equals(other.coinId))
			return false;
		return true;
	}

}
