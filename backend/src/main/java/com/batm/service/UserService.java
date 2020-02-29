package com.batm.service;

import com.batm.dto.GiftAddressDTO;
import com.batm.dto.PhoneDTO;
import com.batm.dto.UserVerificationDTO;
import com.batm.dto.VerificationStateDTO;
import com.batm.entity.*;
import com.batm.model.VerificationStatus;
import com.batm.repository.*;
import com.batm.util.Constant;
import com.batm.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class UserService {

    public static final int TIER_VERIFIED = 1;
    public static final int TIER_VIP_VERIFIED = 2;
    public static final int REVIEW_STATUS_REJECTED = 2;
    public static final int REVIEW_STATUS_ACCEPTED = 3;
    public static final int REVIEW_STATUS_PENDING = 1;
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

    @Autowired
    private IdentityKycReviewRep identityKycReviewRep;

    @Autowired
    private IdentityPiecePersonalInfoRep identityPiecePersonalInfoRep;

    @Autowired
    private IdentityPieceDocumentRep identityPieceDocumentRep;

    @Autowired
    private IdentityPieceSelfieRep identityPieceSelfieRep;

    @Value("${upload.path.id_scan}")
    private String idScanPath;
    @Value("${upload.path.id_selfie}")
    private String idSelfiePath;

    @Transactional
    public User register(String phone, String password) {
        User user = new User();
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("ROLE_USER");
        User savedUser = userRep.save(user);

        Date date = new Date();
        String formattedPhone = Util.formatPhone(user.getPhone());

        List<IdentityPieceCellPhone> pieceCellPhones = identityPieceCellPhoneRep.findByPhoneNumber(formattedPhone);

        if (pieceCellPhones.isEmpty()) {
            user.setIdentity(createNewIdentity(savedUser, date, formattedPhone));
        } else {
            user.setIdentity(selectFromExistingIdentities(savedUser, pieceCellPhones));
        }

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

    public UserCoin getUserCoin(Long userId, String coinCode) {
        return userCoinRep.findByUserIdAndCoinCode(userId, coinCode);
    }

    public void save(CodeVerify codeVerify) {
        codeValidatorRepository.save(codeVerify);
    }

    public Optional<User> findByPhone(String phone) {
        return userRep.findOneByPhone(phone);
    }

    public GiftAddressDTO getUserGiftAddress(CoinService.CoinEnum coinCode, String phone) {
        Optional<User> user = findByPhone(phone);

        if (user.isPresent()) {
            String address = user.get().getUserCoins().stream()
                    .filter(k -> k.getCoin().getCode().equalsIgnoreCase(coinCode.name()))
                    .findFirst().get().getAddress();

            return new GiftAddressDTO(address);
        } else {
            return new GiftAddressDTO(coinCode.getWalletAddress());
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
        dailyLimit.setAmount(Constant.DAILY_LIMIT);
        dailyLimit.setCurrency("USD");
        Limit savedDailyLimit = limitRep.save(dailyLimit);

        Limit trxLimit = new Limit();
        trxLimit.setAmount(Constant.TX_LIMIT);
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

    public VerificationStateDTO getVerificationState(Long userId) {
        VerificationStateDTO verificationStateDTO = new VerificationStateDTO();
        VerificationStatus verificationStatus = VerificationStatus.NOT_VERIFIED;
        String verificationMessage = "OK";
        User user = userRep.getOne(userId);
        List<IdentityKycReview> identityKycReviews = identityKycReviewRep.findAllByIdentityOrderByTierIdAsc(user.getIdentity());

        if (!CollectionUtils.isEmpty(identityKycReviews)) {
            IdentityKycReview currentIdentityKycReview = identityKycReviews.get(0);
            ///identify status
            switch (currentIdentityKycReview.getTierId()) {
                case TIER_VERIFIED:
                    switch (currentIdentityKycReview.getReviewStatus()) {
                        case REVIEW_STATUS_PENDING:
                            verificationStatus = VerificationStatus.PENDING;
                            break;
                        case REVIEW_STATUS_REJECTED:
                            verificationStatus = VerificationStatus.REJECTED;
                            verificationMessage = currentIdentityKycReview.getRejectedMessage();
                            break;
                        case REVIEW_STATUS_ACCEPTED:
                            verificationStatus = VerificationStatus.ACCEPTED;
                            break;
                    }
                    break;
                case TIER_VIP_VERIFIED:
                    switch (currentIdentityKycReview.getReviewStatus()) {
                        case REVIEW_STATUS_PENDING:
                            verificationStatus = VerificationStatus.VIP_PENDING;
                            break;
                        case REVIEW_STATUS_REJECTED:
                            verificationStatus = VerificationStatus.VIP_REJECTED;
                            verificationMessage = currentIdentityKycReview.getRejectedMessage();
                            break;
                        case REVIEW_STATUS_ACCEPTED:
                            verificationStatus = VerificationStatus.VIP_ACCEPTED;
                            break;
                    }
                    break;
            }
        }

        verificationStateDTO.setStatus(verificationStatus);
        verificationStateDTO.setMessage(verificationMessage);

        //find latest limits
        verificationStateDTO.setDailyLimit(user.getIdentity().getLimitCashPerDay().stream()
                .max(Comparator.comparing(Limit::getAmount)).get().getAmount());
        verificationStateDTO.setTxLimit(user.getIdentity().getLimitCashPerTransaction().stream()
                .max(Comparator.comparing(Limit::getAmount)).get().getAmount());

        return verificationStateDTO;
    }

    @Transactional
    public void submitVerification(Long userId, UserVerificationDTO verificationData) throws IOException {
        // TODO add validations for filename, idnumber, ssn etc
        User user = userRep.getOne(userId);
        String preparedFileName;
        IdentityKycReview identityKycReview;

        if (verificationData.getTierId() == TIER_VERIFIED) {
            //prepare file path
            preparedFileName = idScanPath + File.separator
                    + verificationData.getIdNumber() + "_"
                    + verificationData.getFile().getOriginalFilename();

            // prepare personal info for VERIFIED
            identityKycReview = IdentityKycReview
                    .builder()
                    .identity(user.getIdentity())
                    .tierId(TIER_VERIFIED)
                    .reviewStatus(REVIEW_STATUS_PENDING)
                    .idCardNumber(verificationData.getIdNumber())
                    .address(verificationData.getAddress())
                    .country(verificationData.getCountry())
                    .province(verificationData.getProvince())
                    .city(verificationData.getCity())
                    .zip(verificationData.getZipCode())
                    .firstName(verificationData.getFirstName())
                    .lastName(verificationData.getLastName())
                    .build();
            identityKycReviewRep.save(identityKycReview);
        } else if (verificationData.getTierId() == TIER_VIP_VERIFIED) {
            //prepare file path
            preparedFileName = idSelfiePath + File.separator
                    + verificationData.getIdNumber() + "_"
                    + verificationData.getFile().getOriginalFilename();

            // prepare personal info for VIP_VERIFIED
            identityKycReview = IdentityKycReview
                    .builder()
                    .identity(user.getIdentity())
                    .tierId(TIER_VIP_VERIFIED)
                    .reviewStatus(REVIEW_STATUS_PENDING)
                    .ssn(verificationData.getSsn())
                    .build();

        } else {
            return;
        }

        Path preparedPath = Paths.get(preparedFileName);
        uploadFile(verificationData.getFile(), preparedPath);
        String mimeType = Files.probeContentType(preparedPath);

        // add uploaded file info
        identityKycReview.setFileName(preparedFileName);
        identityKycReview.setFileMimeType(mimeType);

        IdentityKycReview identityKycReviewSaved = identityKycReviewRep.save(identityKycReview);

        //TODO mov out to new API when we get dashboard for verification of users
        acceptVerificationData(identityKycReviewSaved.getId()); // hack to auto-accept
    }

    //TODO later(when we get admin dashboard for review) to change to accept by ID
    @Transactional
    public void acceptVerificationData(Long identityKycReviewId) {
        Optional<IdentityKycReview> identityKycReview = identityKycReviewRep.findById(identityKycReviewId);
        if (identityKycReview.isPresent()) {
            IdentityKycReview review = identityKycReview.get();
            if (review.getTierId() == TIER_VERIFIED) {

                IdentityPiece identityPiece = new IdentityPiece();
                identityPiece.setIdentity(review.getIdentity());
                identityPiece.setPieceType(IdentityPiece.TYPE_ID_SCAN);
                identityPiece.setRegistration(true);
                identityPiece.setCreated(new Date());
                IdentityPiece identityPieceSaved = identityPieceRep.save(identityPiece);

                IdentityPieceDocument identityPieceDocument = IdentityPieceDocument
                        .builder()
                        .fileName(review.getFileName())
                        .mimeType(review.getFileMimeType())
                        .identity(review.getIdentity())
                        .identityPiece(identityPieceSaved)
                        .created(new Date())
                        .build();
                identityPieceDocumentRep.save(identityPieceDocument);

                IdentityPiecePersonalInfo identityPiecePersonalInfo = IdentityPiecePersonalInfo
                        .builder()
                        .firstName(review.getFirstName())
                        .lastName(review.getLastName())
                        .identity(review.getIdentity())
                        .identityPiece(identityPiece)
                        .address(review.getAddress())
                        .country(review.getCountry())
                        .province(review.getProvince())
                        .city(review.getCity())
                        .zip(review.getZip())
                        .idCardNumber(review.getIdCardNumber())
                        .created(new Date())
                        .build();
                identityPiecePersonalInfoRep.save(identityPiecePersonalInfo);
            } else if (review.getTierId() == TIER_VIP_VERIFIED) {

                IdentityPiece identityPiece = new IdentityPiece();
                identityPiece.setIdentity(review.getIdentity());
                identityPiece.setPieceType(IdentityPiece.TYPE_SELFIE);
                identityPiece.setRegistration(true);
                identityPiece.setCreated(new Date());
                IdentityPiece identityPieceSaved = identityPieceRep.save(identityPiece);

                IdentityPieceSelfie identityPieceSelfie = IdentityPieceSelfie
                        .builder()
                        .fileName(review.getFileName())
                        .mimeType(review.getFileMimeType())
                        .identity(review.getIdentity())
                        .identityPiece(identityPieceSaved)
                        .created(new Date())
                        .build();
                identityPieceSelfieRep.save(identityPieceSelfie);

                IdentityPiecePersonalInfo identityPiecePersonalInfo = IdentityPiecePersonalInfo
                        .builder()
                        .identity(review.getIdentity())
                        .identityPiece(identityPiece)
                        .ssn(review.getSsn())
                        .created(new Date())
                        .build();
                identityPiecePersonalInfoRep.save(identityPiecePersonalInfo);
            }

            // updating new limits based on verification tier
            BigDecimal newTxLimit = review.getTierId() == TIER_VERIFIED
                    ? Constant.VERIFIED_TX_LIMIT
                    : review.getTierId() == TIER_VIP_VERIFIED
                    ? Constant.VIP_VERIFIED_TX_LIMIT : Constant.TX_LIMIT;
            BigDecimal newDailyLimit = review.getTierId() == TIER_VERIFIED
                    ? Constant.VERIFIED_DAILY_LIMIT
                    : review.getTierId() == TIER_VIP_VERIFIED
                    ? Constant.VIP_VERIFIED_DAILY_LIMIT : Constant.DAILY_LIMIT;

            Limit dailyLimit = new Limit();
            dailyLimit.setAmount(newDailyLimit);
            dailyLimit.setCurrency("USD");
            Limit dailyLimitSaved = limitRep.save(dailyLimit);

            List<Limit> dailyLimits = review.getIdentity().getLimitCashPerDay();
            dailyLimits.add(dailyLimitSaved);

            Limit txLimit = new Limit();
            txLimit.setAmount(newTxLimit);
            txLimit.setCurrency("USD");
            Limit txLimitSaved = limitRep.save(txLimit);

            List<Limit> txLimits = review.getIdentity().getLimitCashPerDay();
            txLimits.add(txLimitSaved);

            identityRep.save(review.getIdentity());

            // update status of review
            review.setReviewStatus(REVIEW_STATUS_ACCEPTED);
            identityKycReviewRep.save(review);
        }
    }

    private void uploadFile(MultipartFile file, Path outPath) throws IOException {
        Files.write(outPath, file.getBytes());
    }
}