package system.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name="code_verifications")
@PrimaryKeyJoinColumn(name = "user_id")
public class CodeVerification {
    /*
    code_verifications(code_id, user_id, phone, code, code_confirmed, create_date, update_date)
    */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "code_id", unique = true, nullable = false)
    private long userId;

    @OneToOne
    @PrimaryKeyJoinColumn
    private User user;

    @Column(name = "phone")
    private long phone;

    @Column(name = "code")
    private long code;

    @Column(name = "code_confirmed")
    private long codeConfirmed;

    @Column(name = "create_date")
    private Date createDate;

    @Column(name = "update_date")
    private Date updateDate;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public long getPhone() {
        return phone;
    }

    public void setPhone(long phone) {
        this.phone = phone;
    }

    public long getCode() {
        return code;
    }

    public void setCode(long code) {
        this.code = code;
    }

    public long getCodeConfirmed() {
        return codeConfirmed;
    }

    public void setCodeConfirmed(long codeConfirmed) {
        this.codeConfirmed = codeConfirmed;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
}
