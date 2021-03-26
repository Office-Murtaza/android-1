package com.belco.server.service;

import com.belco.server.dto.AuthenticationDTO;
import com.belco.server.dto.LocationDTO;
import com.belco.server.dto.VerificationDTO;
import com.belco.server.dto.VerificationDetailsDTO;
import com.belco.server.entity.*;
import com.belco.server.model.Response;
import com.belco.server.model.VerificationStatus;
import com.belco.server.model.VerificationTier;
import com.belco.server.repository.*;
import com.belco.server.util.Util;
import liquibase.util.file.FilenameUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private static final String SELFIE_PREFIX = "selfie_";
    private static final String ID_CARD_PREFIX = "id_card_";

    private static final BigDecimal DAILY_LIMIT = BigDecimal.valueOf(900);
    private static final BigDecimal TX_LIMIT = BigDecimal.valueOf(900);
    private static final BigDecimal VERIFIED_DAILY_LIMIT = BigDecimal.valueOf(10_000);
    private static final BigDecimal VERIFIED_TX_LIMIT = BigDecimal.valueOf(3_000);
    private static final BigDecimal VIP_VERIFIED_DAILY_LIMIT = BigDecimal.valueOf(20_000);
    private static final BigDecimal VIP_VERIFIED_TX_LIMIT = BigDecimal.valueOf(10_000);

    @Lazy
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRep userRep;

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

    @Autowired
    private VerificationReviewRep verificationReviewRep;

    @Autowired
    private IdentityPiecePersonalInfoRep identityPiecePersonalInfoRep;

    @Autowired
    private IdentityPieceDocumentRep identityPieceDocumentRep;

    @Autowired
    private IdentityPieceSelfieRep identityPieceSelfieRep;

    @Autowired
    private ReferralRep referralRep;

    @Lazy
    @Autowired
    private CoinService coinService;

    @Value("${document.upload.path}")
    private String documentUploadPath;

    @Transactional
    public User register(AuthenticationDTO dto) {
        User user = new User();
        user.setPhone(dto.getPhone());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole("ROLE_USER");
        user.setPlatform(dto.getPlatform());
        user.setDeviceModel(dto.getDeviceModel());
        user.setDeviceOS(dto.getDeviceOS());
        user.setAppVersion(dto.getAppVersion());
        user.setLatitude(dto.getLatitude());
        user.setLongitude(dto.getLongitude());
        user.setTimezone(dto.getTimezone());
        user.setNotificationsToken(dto.getNotificationsToken());
        user.setByReferralCode(dto.getByReferralCode());
        User savedUser = userRep.save(user);

        addUserReferral(savedUser);

        String formattedPhone = Util.formatPhone(user.getPhone());
        List<IdentityPieceCellPhone> pieceCellPhones = identityPieceCellPhoneRep.findByPhoneNumber(formattedPhone);

        if (pieceCellPhones.isEmpty()) {
            user.setIdentity(createNewIdentity(savedUser, new Date(), formattedPhone));
        } else {
            user.setIdentity(selectFromExistingIdentities(savedUser, pieceCellPhones));
        }

        coinService.addUserCoins(user, dto.getCoins());

        return user;
    }

    public User findById(Long userId) {
        return userRep.getOne(userId);
    }

    public Identity findByUserId(Long userId) {
        return findById(userId).getIdentity();
    }

    public boolean updatePassword(Long userId, String encodedPassword) {
        User user = userRep.findById(userId).get();
        user.setPassword(encodedPassword);

        return userRep.save(user) != null;
    }

    public boolean updatePhone(Long userId, String phone) {
        User user = userRep.findById(userId).get();
        user.setPhone(phone);
        userRep.save(user);

        Identity identity = findByUserId(userId);
        IdentityPiece identityPiece = identityPieceRep.findFirstByIdentityAndPieceTypeOrderByIdDesc(identity, IdentityPiece.TYPE_CELLPHONE);
        IdentityPieceCellPhone identityPieceCellPhone = identityPieceCellPhoneRep.findByIdentityAndIdentityPiece(identity, identityPiece);

        identityPieceCellPhone.setPhoneNumber(Util.formatPhone(phone));

        return identityPieceCellPhoneRep.save(identityPieceCellPhone) != null;
    }

    public Boolean isPhoneExist(Long userId, String phone) {
        Optional<User> userOpt = userRep.findOneByPhone(phone);

        if (userOpt.isPresent() && !userOpt.get().getId().equals(userId)) {
            return true;
        }

        List<IdentityPieceCellPhone> identityPieceCellPhones = identityPieceCellPhoneRep.findByPhoneNumber(Util.formatPhone(phone));

        for (IdentityPieceCellPhone ipcp : identityPieceCellPhones) {
            if (!ipcp.getIdentity().getId().equals(userOpt.get().getIdentity().getId())) {
                if (ipcp.getPhoneNumber().equalsIgnoreCase(Util.formatPhone(phone))) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean unlinkUser(Long userId) {
        User user = findById(userId);

        if (user != null) {
            user.setNotificationsToken(null);
            return save(user) != null;

        }

        return false;
    }

    public List<UserCoin> save(List<UserCoin> list) {
        return userCoinRep.saveAll(list);
    }

    public List<UserCoin> getUserCoins(Long userId) {
        return userCoinRep.findByUserId(userId);
    }

    public UserCoin getUserCoin(Long userId, String coinCode) {
        return userCoinRep.findByUserIdAndCoinCode(userId, coinCode);
    }

    public Optional<User> findByPhone(String phone) {
        return userRep.findOneByPhone(phone);
    }

    public String getTransferAddress(CoinService.CoinEnum coinCode, String phone) {
        Optional<User> userOpt = findByPhone(phone);

        if (userOpt.isPresent()) {
            String address = userOpt.get().getUserCoins().stream()
                    .filter(k -> k.getCoin().getCode().equalsIgnoreCase(coinCode.name()))
                    .findFirst().get().getAddress();

            return address;
        } else {
            return coinCode.getWalletAddress();
        }
    }

    private Identity selectFromExistingIdentities(User savedUser, List<IdentityPieceCellPhone> pieceCellPhones) {
        pieceCellPhones.sort(Comparator.comparing(IdentityPieceCellPhone::getId).reversed());

        Optional<IdentityPieceCellPhone> identityPieceCellPhone = pieceCellPhones.stream().filter(e -> e.getIdentity().getState() == Identity.STATE_REGISTERED).findFirst();

        if (!identityPieceCellPhone.isPresent()) {
            identityPieceCellPhone = pieceCellPhones.stream().findFirst();
        }

        Identity identity = identityPieceCellPhone.get().getIdentity();
        identity.setUser(savedUser);

        return identityRep.save(identity);
    }

    private Identity createNewIdentity(User savedUser, Date date, String formattedPhone) {
        Limit dailyLimit = new Limit();
        dailyLimit.setAmount(DAILY_LIMIT);
        dailyLimit.setCurrency("USD");
        Limit savedDailyLimit = limitRep.save(dailyLimit);

        Limit trxLimit = new Limit();
        trxLimit.setAmount(TX_LIMIT);
        trxLimit.setCurrency("USD");
        Limit savedTrxLimit = limitRep.save(trxLimit);

        Identity identity = new Identity();
        identity.setPublicId(Util.generatePublicId());
        identity.setState(Identity.STATE_REGISTERED);
        identity.setVipbuydiscount(BigDecimal.ZERO);
        identity.setVipselldiscount(BigDecimal.ZERO);
        identity.setUser(savedUser);
        identity.setCreated(date);
        identity.setLastUpdatedAt(date);
        identity.setRegistered(date);
        identity.setLimitCashPerDay(Arrays.asList(savedDailyLimit));
        identity.setLimitCashPerTransaction(Arrays.asList(savedTrxLimit));
        Identity savedIdentity = identityRep.save(identity);

        IdentityPiece ip = new IdentityPiece();
        ip.setIdentity(savedIdentity);
        ip.setPieceType(IdentityPiece.TYPE_CELLPHONE);
        ip.setRegistration(true);
        ip.setCreated(date);
        IdentityPiece ipSaved = identityPieceRep.save(ip);

        IdentityPieceCellPhone ipCellPhone = new IdentityPieceCellPhone();
        ipCellPhone.setIdentity(savedIdentity);
        ipCellPhone.setIdentityPiece(ipSaved);
        ipCellPhone.setCreated(date);
        ipCellPhone.setPhoneNumber(formattedPhone);
        identityPieceCellPhoneRep.save(ipCellPhone);

        return savedIdentity;
    }

    public VerificationDetailsDTO getVerificationDetails(Long userId) {
        VerificationDetailsDTO dto = new VerificationDetailsDTO();
        dto.setStatus(VerificationStatus.NOT_VERIFIED);

        User user = userRep.getOne(userId);
        VerificationReview verificationReview = verificationReviewRep.findFirstByIdentityOrderByIdDesc(user.getIdentity());

        if (verificationReview != null) {
            dto.setStatus(VerificationStatus.valueOf(verificationReview.getStatus()));
            dto.setMessage(verificationReview.getMessage());
        }

        if (dto.getStatus() == VerificationStatus.NOT_VERIFIED) {
            dto.setMessage("To increase your limits to 3000$ per transaction and 10000$ per day, please verify your account");
        } else if (dto.getStatus() == VerificationStatus.VERIFIED) {
            dto.setMessage("To increase your limits to 10000$ per transaction and 20000$ per day, please VIP verify your account");
        }

        dto.setDailyLimit(getLastLimit(user.getIdentity().getLimitCashPerDay()));
        dto.setTxLimit(getLastLimit(user.getIdentity().getLimitCashPerTransaction()));

        return dto;
    }

    @Transactional
    public Response submitVerification(Long userId, VerificationDTO dto) {
        try {
            User user = userRep.getOne(userId);
            VerificationReview verificationReview = new VerificationReview();
            String fileExtension = FilenameUtils.getExtension(dto.getFile().getOriginalFilename());
            String newFileName = RandomStringUtils.randomAlphanumeric(20).toLowerCase() + "." + fileExtension;

            if (dto.getVerificationTier() == VerificationTier.VERIFICATION) {
                String newFilePath = documentUploadPath + File.separator + ID_CARD_PREFIX + newFileName;
                Path path = Paths.get(newFilePath);
                Util.uploadFile(dto.getFile(), path);

                verificationReview.setTier(dto.getVerificationTier().getValue());
                verificationReview.setIdentity(user.getIdentity());
                verificationReview.setStatus(VerificationStatus.VERIFICATION_PENDING.getValue());
                verificationReview.setAddress(dto.getAddress());
                verificationReview.setCountry(dto.getCountry());
                verificationReview.setProvince(dto.getProvince());
                verificationReview.setCity(dto.getCity());
                verificationReview.setZipCode(dto.getZipCode());
                verificationReview.setFirstName(dto.getFirstName());
                verificationReview.setLastName(dto.getLastName());
                verificationReview.setIdCardNumber(dto.getIdNumber());
                verificationReview.setIdCardNumberFilename(newFileName);
                verificationReview.setIdCardNumberMimetype(dto.getFile().getContentType());
            } else if (dto.getVerificationTier() == VerificationTier.VIP_VERIFICATION) {
                verificationReview = verificationReviewRep.findFirstByIdentityOrderByIdDesc(user.getIdentity());
                String newFilePath = documentUploadPath + File.separator + SELFIE_PREFIX + newFileName;
                Path path = Paths.get(newFilePath);
                Util.uploadFile(dto.getFile(), path);

                verificationReview.setTier(dto.getVerificationTier().getValue());
                verificationReview.setStatus(VerificationStatus.VIP_VERIFICATION_PENDING.getValue());
                verificationReview.setSsn(dto.getSsn());
                verificationReview.setSsnFilename(newFileName);
                verificationReview.setSsnMimetype(dto.getFile().getContentType());
            }

            verificationReview = verificationReviewRep.save(verificationReview);
            user.setStatus(verificationReview.getStatus());
            save(user);

            return Response.ok(verificationReview != null);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError();
        }
    }

    @Transactional
    public Response updateVerification(Long userId, VerificationDTO dto) {
        VerificationReview verificationReview = verificationReviewRep.findById(dto.getId()).get();

        if (dto.getStatus() == VerificationStatus.VERIFICATION_REJECTED || dto.getStatus() == VerificationStatus.VIP_VERIFICATION_REJECTED) {
            verificationReview.setStatus(dto.getStatus().getValue());
            verificationReview.setMessage(dto.getMessage());
            verificationReviewRep.save(verificationReview);
        } else if (verificationReview.getVerificationTier() == VerificationTier.VERIFICATION) {
            IdentityPiece idScan = identityPieceRep.findFirstByIdentityAndPieceTypeOrderByIdDesc(verificationReview.getIdentity(), IdentityPiece.TYPE_ID_SCAN);

            if (idScan != null) {
                IdentityPieceDocument identityPieceDocument = identityPieceDocumentRep.findFirstByIdentityPieceOrderByIdDesc(idScan).get();
                identityPieceDocument.setFileName(verificationReview.getIdCardNumberFilename());
                identityPieceDocument.setMimeType(verificationReview.getIdCardNumberMimetype());
                identityPieceDocument.setCreated(new Date());
                identityPieceDocumentRep.save(identityPieceDocument);

                idScan.setCreated(new Date());
                identityPieceRep.save(idScan);
            } else {
                idScan = new IdentityPiece();
                idScan.setIdentity(verificationReview.getIdentity());
                idScan.setPieceType(IdentityPiece.TYPE_ID_SCAN);
                idScan.setRegistration(true);
                idScan.setCreated(new Date());
                idScan = identityPieceRep.save(idScan);

                IdentityPieceDocument identityPieceDocument = new IdentityPieceDocument();
                identityPieceDocument.setFileName(verificationReview.getIdCardNumberFilename());
                identityPieceDocument.setMimeType(verificationReview.getIdCardNumberMimetype());
                identityPieceDocument.setIdentity(verificationReview.getIdentity());
                identityPieceDocument.setIdentityPiece(idScan);
                identityPieceDocument.setCreated(new Date());
                identityPieceDocumentRep.save(identityPieceDocument);
            }

            IdentityPiece personalInfo = identityPieceRep.findFirstByIdentityAndPieceTypeOrderByIdDesc(verificationReview.getIdentity(), IdentityPiece.TYPE_PERSONAL_INFORMATION);

            if (personalInfo != null) {
                IdentityPiecePersonalInfo identityPiecePersonalInfo = identityPiecePersonalInfoRep.findFirstByIdentityPieceOrderByIdDesc(personalInfo).get();
                identityPiecePersonalInfo.setFirstName(verificationReview.getFirstName());
                identityPiecePersonalInfo.setLastName(verificationReview.getLastName());
                identityPiecePersonalInfo.setLastName(verificationReview.getLastName());
                identityPiecePersonalInfo.setAddress(verificationReview.getAddress());
                identityPiecePersonalInfo.setCountry(verificationReview.getCountry());
                identityPiecePersonalInfo.setProvince(verificationReview.getProvince());
                identityPiecePersonalInfo.setCity(verificationReview.getCity());
                identityPiecePersonalInfo.setZip(verificationReview.getZipCode());
                identityPiecePersonalInfo.setIdCardNumber(verificationReview.getIdCardNumber());
                identityPiecePersonalInfo.setCreated(new Date());
                identityPiecePersonalInfoRep.save(identityPiecePersonalInfo);

                personalInfo.setCreated(new Date());
                identityPieceRep.save(personalInfo);
            } else {
                personalInfo = new IdentityPiece();
                personalInfo.setIdentity(verificationReview.getIdentity());
                personalInfo.setPieceType(IdentityPiece.TYPE_PERSONAL_INFORMATION);
                personalInfo.setRegistration(true);
                personalInfo.setCreated(new Date());
                IdentityPiece personalInfoIdentityPieceSaved = identityPieceRep.save(personalInfo);

                IdentityPiecePersonalInfo identityPiecePersonalInfo = new IdentityPiecePersonalInfo();
                identityPiecePersonalInfo.setFirstName(verificationReview.getFirstName());
                identityPiecePersonalInfo.setLastName(verificationReview.getLastName());
                identityPiecePersonalInfo.setIdentity(verificationReview.getIdentity());
                identityPiecePersonalInfo.setIdentityPiece(personalInfoIdentityPieceSaved);
                identityPiecePersonalInfo.setAddress(verificationReview.getAddress());
                identityPiecePersonalInfo.setCountry(verificationReview.getCountry());
                identityPiecePersonalInfo.setProvince(verificationReview.getProvince());
                identityPiecePersonalInfo.setCity(verificationReview.getCity());
                identityPiecePersonalInfo.setZip(verificationReview.getZipCode());
                identityPiecePersonalInfo.setIdCardNumber(verificationReview.getIdCardNumber());
                identityPiecePersonalInfo.setCreated(new Date());

                identityPiecePersonalInfoRep.save(identityPiecePersonalInfo);
            }

            addDailyLimit(verificationReview, VERIFIED_DAILY_LIMIT);
            addTransactionLimit(verificationReview, VERIFIED_TX_LIMIT);
            verificationReview.setStatus(VerificationStatus.VERIFIED.getValue());
            verificationReviewRep.save(verificationReview);
        } else if (verificationReview.getVerificationTier() == VerificationTier.VIP_VERIFICATION) {
            IdentityPiece selfie = identityPieceRep.findFirstByIdentityAndPieceTypeOrderByIdDesc(verificationReview.getIdentity(), IdentityPiece.TYPE_SELFIE);

            if (selfie != null) {
                IdentityPieceSelfie identityPieceSelfie = identityPieceSelfieRep.findFirstByIdentityPieceOrderByIdDesc(selfie).get();
                identityPieceSelfie.setFileName(verificationReview.getSsnFilename());
                identityPieceSelfie.setMimeType(verificationReview.getSsnMimetype());
                identityPieceSelfie.setCreated(new Date());
                identityPieceSelfieRep.save(identityPieceSelfie);

                selfie.setCreated(new Date());
                identityPieceRep.save(selfie);
            } else {
                selfie = new IdentityPiece();
                selfie.setIdentity(verificationReview.getIdentity());
                selfie.setPieceType(IdentityPiece.TYPE_SELFIE);
                selfie.setRegistration(true);
                selfie.setCreated(new Date());
                selfie = identityPieceRep.save(selfie);

                IdentityPieceSelfie identityPieceSelfie = new IdentityPieceSelfie();
                identityPieceSelfie.setFileName(verificationReview.getSsnFilename());
                identityPieceSelfie.setMimeType(verificationReview.getSsnMimetype());
                identityPieceSelfie.setIdentity(verificationReview.getIdentity());
                identityPieceSelfie.setIdentityPiece(selfie);
                identityPieceSelfie.setCreated(new Date());
                identityPieceSelfieRep.save(identityPieceSelfie);
            }

            IdentityPiece personalInfo = identityPieceRep.findFirstByIdentityAndPieceTypeOrderByIdDesc(verificationReview.getIdentity(), IdentityPiece.TYPE_PERSONAL_INFORMATION);

            if (personalInfo != null) {
                IdentityPiecePersonalInfo identityPiecePersonalInfo = identityPiecePersonalInfoRep.findFirstByIdentityPieceOrderByIdDesc(personalInfo).get();
                identityPiecePersonalInfo.setSsn(verificationReview.getSsn());
                identityPiecePersonalInfo.setCreated(new Date());
                identityPiecePersonalInfoRep.save(identityPiecePersonalInfo);

                personalInfo.setCreated(new Date());
                identityPieceRep.save(personalInfo);
            }

            addDailyLimit(verificationReview, VIP_VERIFIED_DAILY_LIMIT);
            addTransactionLimit(verificationReview, VIP_VERIFIED_TX_LIMIT);

            verificationReview.setStatus(VerificationStatus.VIP_VERIFIED.getValue());
            verificationReviewRep.save(verificationReview);
        }

        User user = findById(userId);
        user.setStatus(verificationReview.getStatus());

        return Response.ok(save(user) != null);
    }

    @Transactional
    public boolean deleteVerification(Long userId) {
        User user = userRep.getOne(userId);
        verificationReviewRep.deleteByIdentity(user.getIdentity());

        List<IdentityPiece> identityPieces = identityPieceRep.findAllByIdentityAndPieceTypeIn(user.getIdentity(), new int[]{IdentityPiece.TYPE_ID_SCAN, IdentityPiece.TYPE_SELFIE, IdentityPiece.TYPE_PERSONAL_INFORMATION});
        identityPiecePersonalInfoRep.deleteAllByIdentityPieceIn(identityPieces);
        identityPieceDocumentRep.deleteAllByIdentityPieceIn(identityPieces);
        identityPieceSelfieRep.deleteAllByIdentityPieceIn(identityPieces);
        identityPieceRep.deleteAll(identityPieces);

        List<Limit> txLimits = user.getIdentity().getLimitCashPerTransaction().stream().sorted(Comparator.comparingLong(Limit::getId)).skip(1).collect(Collectors.toList());
        List<Limit> dailyLimits = user.getIdentity().getLimitCashPerDay().stream().sorted(Comparator.comparingLong(Limit::getId)).skip(1).collect(Collectors.toList());

        user.getIdentity().getLimitCashPerTransaction().removeAll(txLimits);
        user.getIdentity().getLimitCashPerDay().removeAll(dailyLimits);

        identityRep.save(user.getIdentity());
        limitRep.deleteAll(txLimits);
        limitRep.deleteAll(dailyLimits);

        return true;
    }

    public boolean updateLocation(Long userId, LocationDTO dto) {
        User user = findById(userId);
        user.setLatitude(dto.getLatitude());
        user.setLongitude(dto.getLongitude());

        userRep.save(user);

        return true;
    }

    public void addUserReferral(User user) {
        referralRep.save(new Referral(RandomStringUtils.randomAlphanumeric(10).toUpperCase(), 0, BigDecimal.ZERO, user));
    }

    public User save(User user) {
        return userRep.save(user);
    }

    private void addTransactionLimit(VerificationReview review, BigDecimal newTxLimit) {
        Limit txLimit = new Limit();
        txLimit.setAmount(newTxLimit);
        txLimit.setCurrency("USD");
        Limit txLimitSaved = limitRep.save(txLimit);

        review.getIdentity().getLimitCashPerTransaction().add(txLimitSaved);

        identityRep.save(review.getIdentity());
    }

    private void addDailyLimit(VerificationReview review, BigDecimal newDailyLimit) {
        Limit dailyLimit = new Limit();
        dailyLimit.setAmount(newDailyLimit);
        dailyLimit.setCurrency("USD");
        Limit dailyLimitSaved = limitRep.save(dailyLimit);

        review.getIdentity().getLimitCashPerDay().add(dailyLimitSaved);
    }

    private BigDecimal getLastLimit(List<Limit> limits) {
        return limits.get(limits.size() - 1).getAmount().stripTrailingZeros();
    }

    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        Optional<User> userByPhone = userRep.findOneByPhone(phone);

        if (userByPhone.isPresent()) {
            return build(userByPhone.get());
        }

        throw new UsernameNotFoundException("User not found");
    }

    private org.springframework.security.core.userdetails.User build(User user) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority(user.getRole()));

        return new org.springframework.security.core.userdetails.User(user.getPhone(), user.getPassword(),
                authorities);
    }
}