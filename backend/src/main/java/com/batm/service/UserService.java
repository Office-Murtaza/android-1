package com.batm.service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import com.batm.dto.GiftAddressDTO;
import com.batm.entity.*;
import com.batm.repository.*;
import com.batm.dto.PhoneDTO;
import com.batm.util.Constant;
import com.batm.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;

@Service
public class UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRep userRep;

    @Autowired
    private UnlinkRep unlinkRep;

    @Autowired
    private PhoneChangeRep phoneChangeRep;

    @Autowired
    private MessageService messageService;

    @Autowired
    private CodeVerifyRep codeValidatorRepository;

    @Autowired
    private IdentityRep identityRep;

    @Autowired
    private UserCoinRep userCoinRep;

    @Autowired
    private LimitRep limitRep;

    @Autowired
    private IdentityPieceRep identityPieceRep;

    @Autowired
    private IdentityPieceCellPhoneRep identityPieceCellPhoneRep;

    @Transactional
    public User register(String phone, String password) {
        User user = new User();
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("ROLE_USER");
        User savedUser = userRep.save(user);

        Limit dailyLimit = new Limit();
        dailyLimit.setAmount(Constant.DAILY_LIMIT);
        dailyLimit.setCurrency("USD");
        Limit savedDailyLimit = limitRep.save(dailyLimit);

        Limit trxLimit = new Limit();
        trxLimit.setAmount(Constant.TX_LIMIT);
        trxLimit.setCurrency("USD");
        Limit savedTrxLimit = limitRep.save(trxLimit);

        Date date = new Date();
        Identity identity = new Identity();
        identity.setPublicId(Util.generatePublicId());
        identity.setState(0);
        identity.setUser(savedUser);
        identity.setCreated(date);
        identity.setLastUpdatedAt(date);
        identity.setRegistered(date);
        identity.setLimitCashPerDay(Arrays.asList(savedDailyLimit));
        identity.setLimitCashPerTransaction(Arrays.asList(savedTrxLimit));
        Identity savedIdentity = identityRep.save(identity);

        user.setIdentity(savedIdentity);

        IdentityPiece ip = new IdentityPiece();
        ip.setIdentity(savedIdentity);
        ip.setPieceType(4);
        ip.setRegistration(true);
        ip.setCreated(date);
        IdentityPiece ipSaved = identityPieceRep.save(ip);

        IdentityPieceCellPhone ipCellPhone = new IdentityPieceCellPhone();
        ipCellPhone.setIdentity(savedIdentity);
        ipCellPhone.setIdentityPiece(ipSaved);
        ipCellPhone.setCreated(date);
        ipCellPhone.setPhoneNumber(Util.formatPhone(user.getPhone()));
        identityPieceCellPhoneRep.save(ipCellPhone);

        return user;
    }

    public User findById(Long userId) {
        return userRep.getOne(userId);
    }

    public void updatePassword(String encodedPassword, Long userId) {
        userRep.updatePassword(encodedPassword, userId);
    }

    public void updatePhone(String phone, Long userId) {
        userRep.updatePhone(phone, userId);
    }

    public Boolean isPhoneExist(String phone, Long userId) {
        User user = userRep.isPhoneExist(phone, userId);

        return user != null ? true : false;
    }

    public Unlink unlinkUser(Long userId) {
        User user = userRep.getOne(userId);

        if (user != null) {
            Unlink unlink = unlinkRep.findByUserId(userId);

            if (unlink == null) {
                unlink = new Unlink();
                unlink.setUser(user);
            }

            unlinkRep.save(unlink);

            return unlink;
        }

        return null;
    }

    public PhoneChange updatePhone(PhoneDTO phoneRequest, Long userId) {
        User user = userRep.getOne(userId);
        PhoneChange phoneChange = user.getPhoneChange();

        if (phoneChange == null || phoneChange.getId() == null) {
            phoneChange = new PhoneChange();
        }

        phoneChange.setUser(user);
        phoneChange.setPhone(phoneRequest.getPhone());
        phoneChange.setStatus(0);

        phoneChangeRep.save(phoneChange);
        messageService.sendVerificationCode(user);

        return phoneChange;
    }

    @Transactional
    public PhoneChange getUpdatePhone(Long userId) {
        return phoneChangeRep.findByUserId(userId);
    }

    public PhoneChange save(PhoneChange phoneChange) {
        return phoneChangeRep.save(phoneChange);
    }

    public List<UserCoin> save(List<UserCoin> list) {
        return userCoinRep.saveAll(list);
    }

    public CodeVerify getCodeByUserId(Long userId) {
        return codeValidatorRepository.findByUserId(userId);
    }

    public List<UserCoin> getUserCoins(Long userId) {
        return userCoinRep.findByUserId(userId);
    }

    public void save(CodeVerify codeVerify) {
        codeValidatorRepository.save(codeVerify);
    }

    public Optional<User> findByPhone(String phone) {
        return userRep.findOneByPhone(phone);
    }

    public GiftAddressDTO getUserGiftAddress(CoinService.CoinEnum coinId, String phone) {
        Optional<User> user = findByPhone(phone);

        if (user.isPresent()) {
            String address = user.get().getUserCoins().stream()
                    .filter(k -> k.getCoin().getCode().equalsIgnoreCase(coinId.name()))
                    .findFirst().get().getAddress();

            return new GiftAddressDTO(address);
        } else {
            return new GiftAddressDTO(coinId.getWalletAddress());
        }
    }
}