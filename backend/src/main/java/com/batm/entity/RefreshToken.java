package com.batm.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "w_refresh_token")
public class RefreshToken extends AbstractAuditingEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9027143155135946959L;

	@Id
	private Long id;

	private String token;

	@MapsId
	@OneToOne
	@JoinColumn(name = "user_id")
	private User user;

	public RefreshToken(String token, User user) {
		super();
		this.token = token;
		this.user = user;
	}

}