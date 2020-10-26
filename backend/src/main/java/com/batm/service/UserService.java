package com.batm.service;

import com.batm.dto.*;
import com.batm.entity.*;
import com.batm.model.KycStatus;
import com.batm.repository.*;
import com.batm.util.Util;
import liquibase.util.file.FilenameUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    private static final int TIER_1 = 1;
    private static final int TIER_2 = 2;

    private static final String SELFIE_PREFIX = "selfie_";
    private static final String ID_CARD_PREFIX = "id_card_";

    private static final BigDecimal DAILY_LIMIT = BigDecimal.valueOf(900);
    private static final BigDecimal TX_LIMIT = BigDecimal.valueOf(900);
    private static final BigDecimal VERIFIED_DAILY_LIMIT = BigDecimal.valueOf(10_000);
    private static final BigDecimal VERIFIED_TX_LIMIT = BigDecimal.valueOf(3_000);
    private static final BigDecimal VIP_VERIFIED_DAILY_LIMIT = BigDecimal.valueOf(20_000);
    private static final BigDecimal VIP_VERIFIED_TX_LIMIT = BigDecimal.valueOf(10_000);

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRep userRep;

    @Autowired
    private UnlinkRep unlinkRep;

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

    @Autowired
    private CoinService coinService;

    @Value("${document.upload.path}")
    private String documentUploadPath;

    @Transactional
    public User register(String phone, String password, Integer platform, List<CoinDTO> coins) {
        User user = new User();
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("ROLE_USER");
        user.setPlatform(platform);

        User savedUser = userRep.save(user);

        Date date = new Date();
        String formattedPhone = Util.formatPhone(user.getPhone());

        List<IdentityPieceCellPhone> pieceCellPhones = identityPieceCellPhoneRep.findByPhoneNumber(formattedPhone);

        if (pieceCellPhones.isEmpty()) {
            user.setIdentity(createNewIdentity(savedUser, date, formattedPhone));
        } else {
            user.setIdentity(selectFromExistingIdentities(savedUser, pieceCellPhones));
        }

        coinService.addUserCoins(user, coins);

        return user;
    }

    public User findById(Long userId) {
        return userRep.getOne(userId);
    }

    public Identity findByUserId(Long userId) {
        return findById(userId).getIdentity();
    }

    public void updatePassword(Long userId, String encodedPassword) {
        User user = userRep.findById(userId).get();
        user.setPassword(encodedPassword);
        userRep.save(user);
    }

    public void updatePhone(Long userId, String phone) {
        User user = userRep.findById(userId).get();
        user.setPhone(phone);
        userRep.save(user);

        Identity identity = findByUserId(userId);
        IdentityPiece identityPiece = identityPieceRep.findFirstByIdentityAndPieceTypeOrderByIdDesc(identity, IdentityPiece.TYPE_CELLPHONE);
        IdentityPieceCellPhone identityPieceCellPhone = identityPieceCellPhoneRep.findByIdentityAndIdentityPiece(identity, identityPiece);

        identityPieceCellPhone.setPhoneNumber(Util.formatPhone(phone));
        identityPieceCellPhoneRep.save(identityPieceCellPhone);
    }

    public Boolean isPhoneExist(Long userId, String phone) {
        User user = userRep.findOneByPhone(phone).get();

        if (user != null && !user.getId().equals(userId)) {
            return true;
        }

        List<IdentityPieceCellPhone> identityPieceCellPhones = identityPieceCellPhoneRep.findByPhoneNumber(Util.formatPhone(phone));

        for (IdentityPieceCellPhone ipcp : identityPieceCellPhones) {
            if (!ipcp.getIdentity().getId().equals(user.getIdentity().getId())) {
                if (ipcp.getPhoneNumber().equalsIgnoreCase(Util.formatPhone(phone))) {
                    return true;
                }
            }
        }

        return false;
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

    public GiftAddressDTO getCoinAddressByPhone(CoinService.CoinEnum coinCode, String phone) {
        Optional<User> userOpt = findByPhone(phone);

        if (userOpt.isPresent()) {
            String address = userOpt.get().getUserCoins().stream()
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

    public KycDetailsDTO getKycDetails(Long userId) {
        KycDetailsDTO dto = new KycDetailsDTO();
        dto.setStatus(KycStatus.NOT_VERIFIED);

        User user = userRep.getOne(userId);
        IdentityKycReview ikr = identityKycReviewRep.findFirstByIdentityOrderByIdDesc(user.getIdentity());

        if (ikr != null) {
            dto.setStatus(KycStatus.valueOf(ikr.getReviewStatus()));
            dto.setMessage(ikr.getRejectedMessage());
        }

        if (dto.getStatus() == KycStatus.NOT_VERIFIED) {
            dto.setMessage("To increase your limits to 3000$ per transaction and 10000$ per day, please verify your account");
        } else if (dto.getStatus() == KycStatus.VERIFIED) {
            dto.setMessage("To increase your limits to 10000$ per transaction and 20000$ per day, please VIP verify your account");
        }

        dto.setDailyLimit(getLastLimit(user.getIdentity().getLimitCashPerDay()));
        dto.setTxLimit(getLastLimit(user.getIdentity().getLimitCashPerTransaction()));

        return dto;
    }

    @Transactional
    public boolean submitKyc(Long userId, SubmitKycDTO dto) {
        try {
            User user = userRep.getOne(userId);
            IdentityKycReview ikr = new IdentityKycReview();

            if (dto.getTierId() == TIER_1) {
                String fileExtension = FilenameUtils.getExtension(dto.getFile().getOriginalFilename());
                String newFileName = ID_CARD_PREFIX + RandomStringUtils.randomAlphanumeric(20).toLowerCase() + "." + fileExtension;
                String newFilePath = documentUploadPath + File.separator + newFileName;
                Path path = Paths.get(newFilePath);

                Util.uploadFile(dto.getFile(), path);

                ikr.setIdentity(user.getIdentity());
                ikr.setTierId(TIER_1);
                ikr.setReviewStatus(KycStatus.VERIFICATION_PENDING.getValue());
                ikr.setIdCardNumber(dto.getIdNumber());
                ikr.setAddress(dto.getAddress());
                ikr.setCountry(dto.getCountry());
                ikr.setProvince(dto.getProvince());
                ikr.setCity(dto.getCity());
                ikr.setZip(dto.getZipCode());
                ikr.setFirstName(dto.getFirstName());
                ikr.setLastName(dto.getLastName());
                ikr.setIdCardFileName(newFileName);
                ikr.setIdCardFileMimeType(dto.getFile().getContentType());
            } else if (dto.getTierId() == TIER_2) {
                ikr = identityKycReviewRep.findFirstByIdentityOrderByIdDesc(user.getIdentity());

                String fileExtension = FilenameUtils.getExtension(dto.getFile().getOriginalFilename());
                String newFileName = SELFIE_PREFIX + RandomStringUtils.randomAlphanumeric(20).toLowerCase() + "." + fileExtension;
                String newFilePath = documentUploadPath + File.separator + newFileName;
                Path path = Paths.get(newFilePath);

                Util.uploadFile(dto.getFile(), path);

                ikr.setTierId(TIER_2);
                ikr.setReviewStatus(KycStatus.VIP_VERIFICATION_PENDING.getValue());
                ikr.setSsn(dto.getSsn());
                ikr.setSsnFileName(newFileName);
                ikr.setSsnFileMimeType(dto.getFile().getContentType());
            } else {
                return false;
            }

            IdentityKycReview ikrSaved = identityKycReviewRep.save(ikr);

            confirmKyc(ikrSaved.getId());

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Transactional
    public void confirmKyc(Long id) {
        Optional<IdentityKycReview> identityKycReview = identityKycReviewRep.findById(id);

        if (identityKycReview.isPresent()) {
            IdentityKycReview review = identityKycReview.get();

            if (review.getTierId() == TIER_1) {
                IdentityPiece scanIdentityPiece = identityPieceRep.findFirstByIdentityAndPieceTypeOrderByIdDesc(review.getIdentity(), IdentityPiece.TYPE_ID_SCAN);

                if (scanIdentityPiece != null) {
                    IdentityPieceDocument identityPieceDocument = identityPieceDocumentRep.findFirstByIdentityPieceOrderByIdDesc(scanIdentityPiece).get();
                    identityPieceDocument.setFileName(review.getIdCardFileName());
                    identityPieceDocument.setMimeType(review.getIdCardFileMimeType());
                    identityPieceDocument.setCreated(new Date());
                    identityPieceDocumentRep.save(identityPieceDocument);

                    scanIdentityPiece.setCreated(new Date());
                    identityPieceRep.save(scanIdentityPiece);
                } else {
                    scanIdentityPiece = new IdentityPiece();
                    scanIdentityPiece.setIdentity(review.getIdentity());
                    scanIdentityPiece.setPieceType(IdentityPiece.TYPE_ID_SCAN);
                    scanIdentityPiece.setRegistration(true);
                    scanIdentityPiece.setCreated(new Date());
                    IdentityPiece identityPieceSaved = identityPieceRep.save(scanIdentityPiece);

                    IdentityPieceDocument identityPieceDocument = IdentityPieceDocument.builder()
                            .fileName(review.getIdCardFileName())
                            .mimeType(review.getIdCardFileMimeType())
                            .identity(review.getIdentity())
                            .identityPiece(identityPieceSaved)
                            .created(new Date())
                            .build();

                    identityPieceDocumentRep.save(identityPieceDocument);
                }

                IdentityPiece personalInfoIdentityPiece = identityPieceRep.findFirstByIdentityAndPieceTypeOrderByIdDesc(review.getIdentity(), IdentityPiece.TYPE_PERSONAL_INFORMATION);

                if (personalInfoIdentityPiece != null) {
                    IdentityPiecePersonalInfo identityPiecePersonalInfo = identityPiecePersonalInfoRep.findFirstByIdentityPieceOrderByIdDesc(personalInfoIdentityPiece).get();
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
                } else {
                    personalInfoIdentityPiece = new IdentityPiece();
                    personalInfoIdentityPiece.setIdentity(review.getIdentity());
                    personalInfoIdentityPiece.setPieceType(IdentityPiece.TYPE_PERSONAL_INFORMATION);
                    personalInfoIdentityPiece.setRegistration(true);
                    personalInfoIdentityPiece.setCreated(new Date());
                    IdentityPiece personalInfoIdentityPieceSaved = identityPieceRep.save(personalInfoIdentityPiece);

                    IdentityPiecePersonalInfo identityPiecePersonalInfo = IdentityPiecePersonalInfo.builder()
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

                addDailyLimit(review, VERIFIED_DAILY_LIMIT);
                addTransactionLimit(review, VERIFIED_TX_LIMIT);

                review.setReviewStatus(KycStatus.VERIFIED.getValue());
                identityKycReviewRep.save(review);
            } else if (review.getTierId() == TIER_2) {
                IdentityPiece identityPiece = identityPieceRep.findFirstByIdentityAndPieceTypeOrderByIdDesc(review.getIdentity(), IdentityPiece.TYPE_SELFIE);

                if (identityPiece != null) {
                    IdentityPieceSelfie identityPieceSelfie = identityPieceSelfieRep.findFirstByIdentityPieceOrderByIdDesc(identityPiece).get();
                    identityPieceSelfie.setFileName(review.getSsnFileName());
                    identityPieceSelfie.setMimeType(review.getSsnFileMimeType());
                    identityPieceSelfie.setCreated(new Date());
                    identityPieceSelfieRep.save(identityPieceSelfie);

                    identityPiece.setCreated(new Date());
                    identityPieceRep.save(identityPiece);
                } else {
                    identityPiece = new IdentityPiece();
                    identityPiece.setIdentity(review.getIdentity());
                    identityPiece.setPieceType(IdentityPiece.TYPE_SELFIE);
                    identityPiece.setRegistration(true);
                    identityPiece.setCreated(new Date());
                    IdentityPiece identityPieceSaved = identityPieceRep.save(identityPiece);

                    IdentityPieceSelfie identityPieceSelfie = IdentityPieceSelfie.builder()
                            .fileName(review.getSsnFileName())
                            .mimeType(review.getSsnFileMimeType())
                            .identity(review.getIdentity())
                            .identityPiece(identityPieceSaved)
                            .created(new Date())
                            .build();

                    identityPieceSelfieRep.save(identityPieceSelfie);
                }

                IdentityPiece personalInfoIdentityPiece = identityPieceRep.findFirstByIdentityAndPieceTypeOrderByIdDesc(review.getIdentity(), IdentityPiece.TYPE_PERSONAL_INFORMATION);

                if (personalInfoIdentityPiece != null) {
                    IdentityPiecePersonalInfo identityPiecePersonalInfo = identityPiecePersonalInfoRep.findFirstByIdentityPieceOrderByIdDesc(personalInfoIdentityPiece).get();
                    identityPiecePersonalInfo.setSsn(review.getSsn());
                    identityPiecePersonalInfo.setCreated(new Date());
                    identityPiecePersonalInfoRep.save(identityPiecePersonalInfo);

                    personalInfoIdentityPiece.setCreated(new Date());
                    identityPieceRep.save(personalInfoIdentityPiece);
                }

                addDailyLimit(review, VIP_VERIFIED_DAILY_LIMIT);
                addTransactionLimit(review, VIP_VERIFIED_TX_LIMIT);

                review.setReviewStatus(KycStatus.VIP_VERIFIED.getValue());
                identityKycReviewRep.save(review);
            }
        }
    }

    @Transactional
    public boolean deleteKyc(Long userId) {
        User user = userRep.getOne(userId);
        identityKycReviewRep.deleteByIdentity(user.getIdentity());

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

    public void save(User user) {
        userRep.save(user);
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