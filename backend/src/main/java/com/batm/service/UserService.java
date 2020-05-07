package com.batm.service;

import com.batm.dto.*;
import com.batm.entity.*;
import com.batm.model.VerificationStatus;
import com.batm.repository.*;
import com.batm.util.Constant;
import com.batm.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    public static final int TIER_BASIC_VERIFICATION = 1;
    public static final int TIER_VIP_VERIFICATION = 2;

    public static final String SELFIE_FILE_PREFIX = "Selfie_";
    public static final String ID_CARD_FILE_PREFIX = "ID_card_";

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

    @Value("${document.upload.path}")
    private String documentUploadPath;

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

        Optional<IdentityKycReview> identityKycReview = identityKycReviewRep.findFirstByIdentityOrderByIdDesc(user.getIdentity());

        if (identityKycReview.isPresent()) {
            IdentityKycReview currentIdentityKycReview = identityKycReview.get();

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
    public void submitVerification(Long userId, UserVerificationDTO verificationData) throws Exception {
        // TODO add validations for filename, idnumber, ssn etc
        User user = userRep.getOne(userId);
        String preparedFileName;
        String preparedFilePath;
        String mimeType = null;
        IdentityKycReview identityKycReview;

        if (verificationData.getTierId() == TIER_BASIC_VERIFICATION) {
            //validate
            Optional<IdentityKycReview> basicVerification = identityKycReviewRep.findFirstByIdentityOrderByIdDesc(user.getIdentity());
            if (basicVerification.isPresent()) {
                identityKycReview = basicVerification.get(); // in order to update previous records
                if (identityKycReview.getTierId() != null && identityKycReview.getTierId().equals(TIER_VIP_VERIFICATION)) {
                    throw new IllegalStateException("Failed to make verification request. Can not downgrade Verification tier");
                }
            } else {
                identityKycReview = new IdentityKycReview();
            }

            //prepare file path
            preparedFileName = ID_CARD_FILE_PREFIX + UUID.randomUUID().toString()
                    + Util.getExtensionByStringHandling(verificationData.getFile().getOriginalFilename());
            preparedFilePath = documentUploadPath + File.separator + preparedFileName;
            Path preparedPath = Paths.get(preparedFilePath);

            uploadFile(verificationData.getFile(), preparedPath);
            try { // to get mime-type from saved file
                mimeType = Files.probeContentType(preparedPath);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // prepare personal info for VERIFIED
            identityKycReview.setIdentity(user.getIdentity());
            identityKycReview.setTierId(TIER_BASIC_VERIFICATION);
            identityKycReview.setReviewStatus(VerificationStatus.VERIFICATION_PENDING.getValue());
            identityKycReview.setIdCardNumber(verificationData.getIdNumber());
            identityKycReview.setAddress(verificationData.getAddress());
            identityKycReview.setCountry(verificationData.getCountry());
            identityKycReview.setProvince(verificationData.getProvince());
            identityKycReview.setCity(verificationData.getCity());
            identityKycReview.setZip(verificationData.getZipCode());
            identityKycReview.setFirstName(verificationData.getFirstName());
            identityKycReview.setLastName(verificationData.getLastName());
            identityKycReview.setIdCardFileName(preparedFileName);
            identityKycReview.setIdCardFileMimeType(mimeType);

        } else if (verificationData.getTierId() == TIER_VIP_VERIFICATION) {
            //validate
            Optional<IdentityKycReview> basicVerification = identityKycReviewRep.findFirstByIdentityOrderByIdDesc(user.getIdentity());
            if (!basicVerification.isPresent()) {
                throw new IllegalStateException("Failed to make verification request. Can not skip basic Verification");
            } else {
                identityKycReview = basicVerification.get();
            }

            //prepare file path
            preparedFileName = SELFIE_FILE_PREFIX + UUID.randomUUID().toString()
                    + Util.getExtensionByStringHandling(verificationData.getFile().getOriginalFilename());
            preparedFilePath = documentUploadPath + File.separator + preparedFileName;
            Path preparedPath = Paths.get(preparedFilePath);

            uploadFile(verificationData.getFile(), preparedPath);
            try { // to get mime-type from saved file
                mimeType = Files.probeContentType(preparedPath);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // prepare personal info for VIP_VERIFIED
            identityKycReview.setTierId(TIER_VIP_VERIFICATION);
            identityKycReview.setReviewStatus(VerificationStatus.VIP_VERIFICATION_PENDING.getValue());
            identityKycReview.setSsn(verificationData.getSsn());
            identityKycReview.setSsnFileName(preparedFileName);
            identityKycReview.setSsnFileMimeType(mimeType);
        } else {
            // if wrong tier don't do anything
            return;
        }

        IdentityKycReview identityKycReviewSaved = identityKycReviewRep.save(identityKycReview);

        //TODO move out to new API when we get dashboard for verification of users
        acceptVerificationData(identityKycReviewSaved.getId()); // hack to auto-accept
    }

    //TODO later(when we get admin dashboard for review) to change to accept by ID
    @Transactional
    public void acceptVerificationData(Long identityKycReviewId) {
        Optional<IdentityKycReview> identityKycReview = identityKycReviewRep.findById(identityKycReviewId);
        if (identityKycReview.isPresent()) {
            IdentityKycReview review = identityKycReview.get();
            if (review.getTierId() == TIER_BASIC_VERIFICATION) {

                Optional<IdentityPiece> _scanIdentityPiece = identityPieceRep
                        .findFirstByIdentityAndPieceTypeOrderByIdDesc(review.getIdentity(), IdentityPiece.TYPE_ID_SCAN);

                if (_scanIdentityPiece.isPresent()) { // update
                    IdentityPiece scanIdentityPiece = _scanIdentityPiece.get();

                    IdentityPieceDocument identityPieceDocument = identityPieceDocumentRep
                            .findFirstByIdentityPieceOrderByIdDesc(scanIdentityPiece).get();
                    identityPieceDocument.setFileName(review.getIdCardFileName());
                    identityPieceDocument.setMimeType(review.getIdCardFileMimeType());
                    identityPieceDocument.setCreated(new Date());
                    identityPieceDocumentRep.save(identityPieceDocument);

                    scanIdentityPiece.setCreated(new Date());
                    identityPieceRep.save(scanIdentityPiece);
                } else {                                // crete new
                    IdentityPiece scanIdentityPiece = new IdentityPiece();
                    scanIdentityPiece.setIdentity(review.getIdentity());
                    scanIdentityPiece.setPieceType(IdentityPiece.TYPE_ID_SCAN);
                    scanIdentityPiece.setRegistration(true);
                    scanIdentityPiece.setCreated(new Date());
                    IdentityPiece identityPieceSaved = identityPieceRep.save(scanIdentityPiece);

                    IdentityPieceDocument identityPieceDocument = IdentityPieceDocument
                            .builder()
                            .fileName(review.getIdCardFileName())
                            .mimeType(review.getIdCardFileMimeType())
                            .identity(review.getIdentity())
                            .identityPiece(identityPieceSaved)
                            .created(new Date())
                            .build();
                    identityPieceDocumentRep.save(identityPieceDocument);
                }

                Optional<IdentityPiece> _personalInfoIdentityPiece = identityPieceRep
                        .findFirstByIdentityAndPieceTypeOrderByIdDesc(review.getIdentity(), IdentityPiece.TYPE_PERSONAL_INFORMATION);
                if (_personalInfoIdentityPiece.isPresent()) { // update
                    IdentityPiece personalInfoIdentityPiece = _personalInfoIdentityPiece.get();

                    IdentityPiecePersonalInfo identityPiecePersonalInfo = identityPiecePersonalInfoRep
                            .findFirstByIdentityPieceOrderByIdDesc(personalInfoIdentityPiece).get();
                    identityPiecePersonalInfo.setFirstName(review.getFirstName());
                    identityPiecePersonalInfo.setLastName(review.getLastName());
                    identityPiecePersonalInfo.setLastName(review.getLastName());
                    identityPiecePersonalInfo.setAddress(review.getAddress());
                    identityPiecePersonalInfo.setCountry(review.getCountry());
                    identityPiecePersonalInfo.setProvince(review.getProvince());
                    identityPiecePersonalInfo.setCity(review.getCity());
                    identityPiecePersonalInfo.setZip(review.getZip());
                    identityPiecePersonalInfo.setIdCardNumber(review.getIdCardNumber());
                    identityPiecePersonalInfo.setCreated(new Date());
                    identityPiecePersonalInfoRep.save(identityPiecePersonalInfo);

                    personalInfoIdentityPiece.setCreated(new Date());
                    identityPieceRep.save(personalInfoIdentityPiece);
                } else {                                    // create new
                    IdentityPiece personalInfoIdentityPiece = new IdentityPiece();
                    personalInfoIdentityPiece.setIdentity(review.getIdentity());
                    personalInfoIdentityPiece.setPieceType(IdentityPiece.TYPE_PERSONAL_INFORMATION);
                    personalInfoIdentityPiece.setRegistration(true);
                    personalInfoIdentityPiece.setCreated(new Date());
                    IdentityPiece personalInfoIdentityPieceSaved = identityPieceRep.save(personalInfoIdentityPiece);

                    IdentityPiecePersonalInfo identityPiecePersonalInfo = IdentityPiecePersonalInfo
                            .builder()
                            .firstName(review.getFirstName())
                            .lastName(review.getLastName())
                            .identity(review.getIdentity())
                            .identityPiece(personalInfoIdentityPieceSaved)
                            .address(review.getAddress())
                            .country(review.getCountry())
                            .province(review.getProvince())
                            .city(review.getCity())
                            .zip(review.getZip())
                            .idCardNumber(review.getIdCardNumber())
                            .created(new Date())
                            .build();
                    identityPiecePersonalInfoRep.save(identityPiecePersonalInfo);
                }

                // add new limits per Tier
                addDailyLimit(review, Constant.VERIFIED_DAILY_LIMIT);
                addTransactionLimit(review, Constant.VERIFIED_TX_LIMIT);

                // save updated status of review
                review.setReviewStatus(VerificationStatus.VERIFIED.getValue());
                identityKycReviewRep.save(review);
            } else if (review.getTierId() == TIER_VIP_VERIFICATION) {
                Optional<IdentityPiece> _selfieIdentityPiece = identityPieceRep
                        .findFirstByIdentityAndPieceTypeOrderByIdDesc(review.getIdentity(), IdentityPiece.TYPE_SELFIE);
                if (_selfieIdentityPiece.isPresent()) { // update
                    IdentityPiece selfieIdentityPiece = _selfieIdentityPiece.get();

                    IdentityPieceSelfie identityPieceSelfie = identityPieceSelfieRep
                            .findFirstByIdentityPieceOrderByIdDesc(selfieIdentityPiece).get();
                    identityPieceSelfie.setFileName(review.getSsnFileName());
                    identityPieceSelfie.setMimeType(review.getSsnFileMimeType());
                    identityPieceSelfie.setCreated(new Date());
                    identityPieceSelfieRep.save(identityPieceSelfie);

                    selfieIdentityPiece.setCreated(new Date());
                    identityPieceRep.save(selfieIdentityPiece);
                } else {

                    IdentityPiece identityPiece = new IdentityPiece();
                    identityPiece.setIdentity(review.getIdentity());
                    identityPiece.setPieceType(IdentityPiece.TYPE_SELFIE);
                    identityPiece.setRegistration(true);
                    identityPiece.setCreated(new Date());
                    IdentityPiece identityPieceSaved = identityPieceRep.save(identityPiece);

                    IdentityPieceSelfie identityPieceSelfie = IdentityPieceSelfie
                            .builder()
                            .fileName(review.getSsnFileName())
                            .mimeType(review.getSsnFileMimeType())
                            .identity(review.getIdentity())
                            .identityPiece(identityPieceSaved)
                            .created(new Date())
                            .build();
                    identityPieceSelfieRep.save(identityPieceSelfie);
                }

                Optional<IdentityPiece> _personalInfoIdentityPiece = identityPieceRep
                        .findFirstByIdentityAndPieceTypeOrderByIdDesc(review.getIdentity(), IdentityPiece.TYPE_PERSONAL_INFORMATION);
                if (_personalInfoIdentityPiece.isPresent()) { // update
                    IdentityPiece personalInfoIdentityPiece = _personalInfoIdentityPiece.get();

                    IdentityPiecePersonalInfo identityPiecePersonalInfo = identityPiecePersonalInfoRep
                            .findFirstByIdentityPieceOrderByIdDesc(personalInfoIdentityPiece).get();
                    identityPiecePersonalInfo.setSsn(review.getSsn());
                    identityPiecePersonalInfo.setCreated(new Date());
                    identityPiecePersonalInfoRep.save(identityPiecePersonalInfo);

                    personalInfoIdentityPiece.setCreated(new Date());
                    identityPieceRep.save(personalInfoIdentityPiece);
                } else {                                    // create new
                    throw new IllegalStateException("Failed to make verification request. Can not skip basic Verification");
                }

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

    public boolean updateLocation(Long userId, LocationDTO dto) {
        User user = findById(userId);
        user.setLatitude(dto.getLatitude());
        user.setLongitude(dto.getLongitude());
        
        userRep.save(user);
        
        return true;
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

    @Transactional
    public Boolean resetVerificationsForUser(Long userId) {
        User user = userRep.getOne(userId);
        identityKycReviewRep.deleteByIdentity(user.getIdentity());
        List<IdentityPiece> identityPieces = identityPieceRep.findAllByIdentityAndPieceTypeIn(user.getIdentity(), new int[]{IdentityPiece.TYPE_ID_SCAN, IdentityPiece.TYPE_SELFIE, IdentityPiece.TYPE_PERSONAL_INFORMATION});
        identityPiecePersonalInfoRep.deleteAllByIdentityPieceIn(identityPieces);
        identityPieceDocumentRep.deleteAllByIdentityPieceIn(identityPieces);
        identityPieceSelfieRep.deleteAllByIdentityPieceIn(identityPieces);
        identityPieceRep.deleteAll(identityPieces);

        //revert prices
        List<Limit> limitsPerTx = user.getIdentity().getLimitCashPerTransaction().
                stream().sorted(Comparator.comparingLong(Limit::getId))
                .skip(1).collect(Collectors.toList());
        List<Limit> limitsPerDay = user.getIdentity().getLimitCashPerDay().stream().sorted(Comparator.comparingLong(Limit::getId)).skip(1).collect(Collectors.toList());
        user.getIdentity().getLimitCashPerTransaction().removeAll(limitsPerTx);
        user.getIdentity().getLimitCashPerDay().removeAll(limitsPerDay);
        identityRep.save(user.getIdentity());
        limitRep.deleteAll(limitsPerTx);
        limitRep.deleteAll(limitsPerDay);

        return true;
    }
}