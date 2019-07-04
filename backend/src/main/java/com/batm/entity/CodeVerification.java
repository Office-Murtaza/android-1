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
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "w_code_verification")
public class CodeVerification extends AbstractAuditingEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1712969322089989538L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "code_id")
	private Long codeId;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	private String code = "0";

	private String codeStatus;

	public CodeVerification(User user, String code, String codeStatus) {
		super();
		this.user = user;
		this.code = code;
		this.codeStatus = codeStatus;
	}

}
