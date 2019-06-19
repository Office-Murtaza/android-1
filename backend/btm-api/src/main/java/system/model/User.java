package system.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name="users")
public class User {
    /*
    user_id(pk, autogenerate),
    phone(phone),
    phone_confirmed(phone default 0),
    create_date(date),
    update_date(date)
    */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", unique = true, nullable = false)
    private long userId;

    @Column(name = "phone")
    private long phone;

    @Column(name = "phone_confirmed")
    private long phoneConfirmed;

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

    public long getPhone() {
        return phone;
    }

    public void setPhone(long phone) {
        this.phone = phone;
    }

    public long getPhoneConfirmed() {
        return phoneConfirmed;
    }

    public void setPhoneConfirmed(long phoneConfirmed) {
        this.phoneConfirmed = phoneConfirmed;
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
