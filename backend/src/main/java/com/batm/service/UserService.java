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
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class UserService {

    public static final int TIER_BASIC_VERIFICATION = 1;
    public static final int TIER_VIP_VERIFICATION = 2;

    public static final int REVIEW_STATUS_PENDING = 1;
    public static final int REVIEW_STATUS_REJECTED = 2;
    public static final int REVIEW_STATUS_VERIFIED = 3;

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

        // default status if no requests found
        VerificationStatus verificationStatus = VerificationStatus.NOT_VERIFIED;
        // default message
        String verificationMessage = null;

        User user = userRep.getOne(userId);

        List<IdentityKycReview> identityKycReviews = identityKycReviewRep.findAllByIdentityOrderByIdDesc(user.getIdentity());

        if (!CollectionUtils.isEmpty(identityKycReviews)) {
            IdentityKycReview currentIdentityKycReview = identityKycReviews.get(0);

            // identify status and message
            verificationStatus = VerificationStatus.getByValue(currentIdentityKycReview.getReviewStatus());
            verificationMessage = currentIdentityKycReview.getRejectedMessage();
        }

        verificationStateDTO.setStatus(verificationStatus);
        verificationStateDTO.setMessage(verificationMessage);

        // find and set latest limits by identity
        verificationStateDTO.setDailyLimit(user.getIdentity().getLimitCashPerDay().stream()
                .sorted(Comparator.comparingLong(Limit::getId).reversed()).findFirst().get().getAmount());
        verificationStateDTO.setTxLimit(user.getIdentity().getLimitCashPerTransaction().stream()
                .sorted(Comparator.comparingLong(Limit::getId).reversed()).findFirst().get().getAmount());

        return verificationStateDTO;
    }

    @Transactional
    public void submitVerification(Long userId, UserVerificationDTO verificationData) throws IOException {
        // TODO add validations for filename, idnumber, ssn etc
        User user = userRep.getOne(userId);
        String preparedFileName;
        String preparedFilePath;
        IdentityKycReview identityKycReview;

        if (verificationData.getTierId() == TIER_BASIC_VERIFICATION) {
            //prepare file path
            preparedFileName = verificationData.getIdNumber() + "_" + verificationData.getFile().getOriginalFilename();
            preparedFilePath = idScanPath + File.separator + preparedFileName;

            // prepare personal info for VERIFIED
            identityKycReview = IdentityKycReview
                    .builder()
                    .identity(user.getIdentity())
                    .tierId(TIER_BASIC_VERIFICATION)
                    .reviewStatus(VerificationStatus.VERIFICATION_PENDING.getValue())
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
        } else if (verificationData.getTierId() == TIER_VIP_VERIFICATION) {
            //prepare file path
            preparedFileName = verificationData.getSsn() + "_" + verificationData.getFile().getOriginalFilename();
            preparedFilePath = idSelfiePath + File.separator + preparedFileName;

            // prepare personal info for VIP_VERIFIED
            identityKycReview = IdentityKycReview
                    .builder()
                    .identity(user.getIdentity())
                    .tierId(TIER_VIP_VERIFICATION)
                    .reviewStatus(VerificationStatus.VIP_VERIFICATION_PENDING.getValue())
                    .ssn(verificationData.getSsn())
                    .build();

        } else {
            // if wrong tier don't do anything
            return;
        }

        Path preparedPath = Paths.get(preparedFilePath);
        uploadFile(verificationData.getFile(), preparedPath);

        String mimeType = null;

        try {
            mimeType = Files.probeContentType(preparedPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

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
            if (review.getTierId() == TIER_BASIC_VERIFICATION) {

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

                // add new limits per Tier
                addDailyLimit(review, Constant.VERIFIED_DAILY_LIMIT);
                addTransactionLimit(review, Constant.VERIFIED_TX_LIMIT);

                // save updated status of review
                review.setReviewStatus(VerificationStatus.VERIFIED.getValue());
                identityKycReviewRep.save(review);
            } else if (review.getTierId() == TIER_VIP_VERIFICATION) {

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

                // add new limits per Tier
                addDailyLimit(review, Constant.VIP_VERIFIED_DAILY_LIMIT);
                addTransactionLimit(review, Constant.VIP_VERIFIED_TX_LIMIT);

                // save updated status of review
                review.setReviewStatus(VerificationStatus.VIP_VERIFIED.getValue());
                identityKycReviewRep.save(review);
            } else {
                // if wrong tier do nothing
                return;
            }
        }
    }

    private void addTransactionLimit(IdentityKycReview review, BigDecimal newTxLimit) {
        Limit txLimit = new Limit();
        txLimit.setAmount(newTxLimit);
        txLimit.setCurrency("USD");
        Limit txLimitSaved = limitRep.save(txLimit);

        review.getIdentity().getLimitCashPerTransaction().add(txLimitSaved);

        identityRep.save(review.getIdentity());
    }

    private void addDailyLimit(IdentityKycReview review, BigDecimal newDailyLimit) {
        Limit dailyLimit = new Limit();
        dailyLimit.setAmount(newDailyLimit);
        dailyLimit.setCurrency("USD");
        Limit dailyLimitSaved = limitRep.save(dailyLimit);

        review.getIdentity().getLimitCashPerDay().add(dailyLimitSaved);
    }

    private void uploadFile(MultipartFile file, Path outPath) throws IOException {
        Files.write(outPath, file.getBytes());
    }
}