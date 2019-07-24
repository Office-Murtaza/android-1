package com.batm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.batm.entity.CodeVerification;
import com.batm.repository.CodeVerificationRepository;

@Service
public class CodeVerificationService {

    @Autowired
    private CodeVerificationRepository codeValidatorRepository;

    public CodeVerification getCodeByUserId(Long userId) {
        return this.codeValidatorRepository.findByUserUserId(userId);
    }

    public void save(CodeVerification codeVerification) {
        this.codeValidatorRepository.save(codeVerification);
    }
}